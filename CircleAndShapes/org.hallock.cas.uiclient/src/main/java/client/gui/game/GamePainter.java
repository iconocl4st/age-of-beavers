package client.gui.game;

import client.algo.HueristicPaintMarkedTiles;
import client.app.UiClientContext;
import client.gui.mouse.BuildingPlacer;
import client.gui.mouse.SelectionListener;
import client.state.ClientGameState;
import common.CommonConstants;
import common.DebugGraphics;
import common.action.Action;
import common.algo.AStar;
import common.algo.jmp_pnt.JumpPointSearch;
import common.algo.quad.MarkedRectangle;
import common.algo.quad.QuadNodeType;
import common.algo.quad.QuadTree;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.Player;
import common.state.spec.EntitySpec;
import common.state.spec.GameSpec;
import common.state.sst.GameState;
import common.state.sst.OccupancyView;
import common.state.sst.manager.ManagerImpl;
import common.state.sst.manager.RevPair;
import common.state.sst.manager.Textures;
import common.state.sst.sub.ProjectileLaunch;
import common.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GamePainter {

    private static final double BAR_WIDTH = 0.1;

    private final SelectionListener selectionListener;
    private final BuildingPlacer buildingPlacer;

    private UiClientContext context;
    private ClientGameState clientGameState;
    private GameState state;
    // TODO: remove
    private JPanel panel;
    private int gameWidth;
    private int gameHeight;
    private String fps = "counting";
    private TicksPerSecondTracker tracker = new TicksPerSecondTracker(2);

    // TODO: Remove all paints that are off screen...

    public GamePainter(
            UiClientContext context,
            SelectionListener selectionListener,
            BuildingPlacer placer,
            JPanel panel
    ) {
        this.context = context;

        this.selectionListener = selectionListener;
        this.buildingPlacer = placer;
        this.panel = panel;
    }

    public void initialize(GameSpec spec) {
        this.gameWidth = spec.width;
        this.gameHeight = spec.height;
        this.clientGameState = this.context.clientGameState;
        this.state = clientGameState.gameState;
    }

    private final Util.CyclingIterator<Color> kmeansCycleIterator = new Util.CyclingIterator<>(new Color[]{
            Color.white,
            Color.pink,
            Color.green,
            Color.yellow,
            Color.red,
            Color.blue,
            Color.black,
            Color.cyan,
            Color.gray
    });

    void renderGame(Graphics2D graphics, Zoom zoom) {
        Graphics2D g = graphics;

        double currentTime = clientGameState.gameState.getCurrentGameTime();

        g.setColor(Colors.BACKGROUND);
        g.fillRect(0, 0, panel.getWidth(), panel.getHeight());

        g.setColor(Colors.DESERT);
        g.fill(zoom.mapGameToScreen(0, 0, gameWidth, gameHeight));

        paintTerrain(g, zoom, clientGameState.gameState.textures);

        paintGrid(g, zoom);

        for (EntityId entityId : state.entityManager.allKeys()) {
            paintDisplayable(g, new EntityReader(state, entityId), zoom, currentTime);
        }

        paintQuadTree(zoom, g, clientGameState.quadTree);

        // todo: encapsulate
        if (selectionListener.isSelecting()) {
            g.setColor(Color.white);
            g.draw(selectionListener.rectangleListener.getScreenRectangle());
        }

        // TODO: These should be one pass through the actions...
        // TODO: dry
        paintPaths(g, zoom);
        paintAction(g, Colors.DEPOSIT, Action.ActionType.Deposit, action -> new EntityReader(state, ((Action.Deposit) action).location), zoom, currentTime);
        paintAction(g, Colors.COLLECT, Action.ActionType.Collect, action -> new EntityReader(state, ((Action.Collect) action).resourceCarrier), zoom, currentTime);
        paintAction(g, Colors.ATTACK, Action.ActionType.Attack, action -> new EntityReader(state, ((Action.Attack) action).target), zoom, currentTime);
        paintAction(g, Colors.BUILD, Action.ActionType.Build, action -> new EntityReader(state, ((Action.Build) action).constructionId), zoom, currentTime);
        paintBuilding(g, zoom);

        paintProjectiles(g, zoom);
        // todo: paint

        if (CommonConstants.PAINT_GRID_DEBUG) {
            g.setColor(Color.pink);
            for (GridLocation.GraphicalDebugRectangle gdr : state.locationManager.getDebugRectangles()) {

                g.draw(gdr.rectangle);
                g.drawString(
                        gdr.description,
                        (int) zoom.mapGameToScreenX(gdr.rectangle.getX() + gdr.rectangle.getWidth() / 2),
                        (int) zoom.mapGameToScreenY(gdr.rectangle.getY() + gdr.rectangle.getHeight() / 2)
                );
            }
            // below should be here...
        }

        g.setColor(Colors.MAP_BOUNDARY);
        g.draw(zoom.mapGameToScreen(0, 0, gameWidth, gameHeight));

        synchronized (DebugGraphics.byPlayer) {
            for (Map.Entry<Player, List<DebugGraphics>> entry : DebugGraphics.byPlayer.entrySet()) {
                kmeansCycleIterator.resetIndex();
                for (DebugGraphics debug : entry.getValue()) {
                    g.setColor(Colors.PLAYER_COLORS[entry.getKey().number]);
                    g.fill(zoom.mapGameCircleToScreen(debug.center.x, debug.center.y, 0.2));
                    g.setColor(kmeansCycleIterator.next());
                    for (DPoint rec : debug.list) {
                        g.draw(zoom.mapGameLineToScreen(debug.center.x, debug.center.y, rec.x, rec.y));
                    }
                }
            }
        }

        synchronized (DebugGraphics.pleaseFocusSync) {
            if (DebugGraphics.pleaseFocus != null) {
                zoom.focusOn(Collections.singleton(DebugGraphics.pleaseFocus));
                DebugGraphics.pleaseFocus = null;
            }
        }

        paintVisibility(g, zoom);

        if (CommonConstants.PAINT_FPS) {
            String s = tracker.receiveTick();
            if (s != null) fps = s;
            g.setColor(Color.black);
            g.drawString(String.valueOf(currentTime) + ", fps: " + fps, 20, 20);

            Toolkit.getDefaultToolkit().sync();
        }
    }

    public static void paintTerrain(Graphics2D g, Zoom zoom, Textures textures) {
        for (Textures.TileTexture texture : textures.textures.values()) {
            switch (texture.type) {
                case Grass:
                    g.setColor(Colors.GRASS);
                    break;
                case Water:
                    g.setColor(Colors.WATER);
                    break;
            }
            g.fill(zoom.mapGameToScreen(texture.x,  texture.y, 1, 1));
        }
    }

    private void paintQuadTree(Zoom zoom, Graphics2D g, QuadTree quadTree) {
        if (!CommonConstants.PAINT_QUADTREE) return;
        g.setColor(Color.yellow);
        Iterator<MarkedRectangle> leafIterator = quadTree.leaves();
        while (leafIterator.hasNext()) {
//            Rectangle re = zoom.mapGameToScreen(vertex.location.x,  vertex.location.y, 1, 1);
            MarkedRectangle m = leafIterator.next();
            Rectangle2D r = zoom.mapGameToScreen(m.x, m.y, m.w, m.h);
            g.draw(r);

            if (CommonConstants.PAINT_QUADTREE_VERBOSE)
                g.drawString(String.valueOf(m.root), (int) (r.getX() + r.getWidth() / 2), (int) (r.getY() + r.getHeight() / 2));

            if (m.type.equals(QuadNodeType.Occupied)) {
                g.draw(zoom.mapGameLineToScreen(m.x,  m.y, m.x + m.w, m.y + m.h));
                g.draw(zoom.mapGameLineToScreen(m.x,  m.y + m.h, m.x + m.w, m.y));
            }
        }
    }

    private void paintProjectiles(Graphics2D g, Zoom zoom) {
        g.setColor(Color.yellow);
        for (EntityId entityId : state.projectileManager.allKeys()) {
            ProjectileLaunch projectileLaunch = state.projectileManager.get(entityId);
            DPoint currentLocation = projectileLaunch.getLocation(state.currentTime);
            if (currentLocation == null) continue;
            g.fill(zoom.mapGameCircleToScreen(currentLocation.x, currentLocation.y, projectileLaunch.projectile.radius));
        }
    }

    private void paintGrid(Graphics2D g, Zoom zoom) {
        double beginX = zoom.mapScreenToGameX(0);
        double endX = zoom.mapScreenToGameX(panel.getWidth());

        if (endX - beginX > 200) {
            return;
        }

        g.setColor(Colors.GRID_LINES);
        for (int i = 0; i < gameWidth; i++)
            g.draw(zoom.mapGameLineToScreen(i, 0, i, gameHeight));
        for (int i = 0; i < gameHeight; i++)
            g.draw(zoom.mapGameLineToScreen(0, i, gameWidth, i));
    }


    private HueristicPaintMarkedTiles.RectangleReceiver createPainter(final Color color, final Graphics2D g, Zoom zoom) {
        return new HueristicPaintMarkedTiles.RectangleReceiver() {
            @Override
            public void markedRectangle(int xb, int yb, int xe, int ye) {}

            @Override
            public void unMarkedRectangle(int xb, int yb, int xe, int ye) {
                Rectangle2D r = zoom.mapGameToScreen(xb, yb, xe - xb, ye - yb);
                g.setColor(color);
                g.fill(r);
                if (!CommonConstants.PAINT_LOS_RECTANGLES) return;
                g.setColor(Color.white);
                g.draw(r);
            }
        };
    }

    private void paintVisibility(final Graphics2D g, Zoom zoom) {
//        if (gameSpec.visibility.equals(GameSpec.VisibilitySpec.ALL_VISIBLE)) return;

        double xs = Math.max(0, Math.min(gameWidth, Math.floor(zoom.mapScreenToGameX(0))));
        double ys = Math.max(0, Math.min(gameHeight, Math.ceil(zoom.mapScreenToGameY(0))));
        double xe = Math.max(0, Math.min(gameWidth, Math.ceil(zoom.mapScreenToGameX(panel.getWidth()))));
        double ye = Math.max(0, Math.min(gameHeight, Math.floor(zoom.mapScreenToGameY(panel.getHeight()))));

        HueristicPaintMarkedTiles.enumerateRectangles(

                new Marked() {
                    @Override
                    public int getWidth() {
                        return gameWidth;
                    }

                    @Override
                    public int getHeight() {
                        return gameHeight;
                    }

                    @Override
                    public boolean get(int x, int y) {
                        return clientGameState.lineOfsight.get(x, y) || !clientGameState.exploration.get(x, y);
                    }
                },
                createPainter(Colors.NOT_VISIBLE, g, zoom),
                (int) xs,
                (int) ye,
                (int) xe,
                (int) ys
        );
        HueristicPaintMarkedTiles.enumerateRectangles(
                clientGameState.exploration,
                createPainter(Colors.UNEXPLORED, g, zoom),
                (int) xs,
                (int) ye,
                (int) xe,
                (int) ys
        );

        if (xe - xs > 10 || ys - ye > 10) {
            return;
        }

        if (!CommonConstants.PAINT_LOS_RECTANGLES) return;

        g.setColor(Color.white);
        for (int i = (int) xs; i < 1 + (int) xe; i++) {
            for (int j = (int) ye; j < 1 + (int) ys; j++) {
                int tileX1 = (int) zoom.mapGameToScreenX(i);
                int tileY1 = (int) zoom.mapGameToScreenY(j);
                int tileX2 = (int) zoom.mapGameToScreenX(i + 1);
                int tileY2 = (int) zoom.mapGameToScreenY(j + 1);

                g.drawString(
                        "[" + i + ", " + j + "] " + clientGameState.lineOfsight.getCount(i,  j)
                                + " " + clientGameState.exploration.get(i, j)
                                + " " + clientGameState.gameState.staticOccupancy.get(i,  j)
                                + " " + clientGameState.gameState.buildingOccupancy.get(i,  j),
                        (tileX1 + tileX2) / 2,
                        (tileY1 + tileY2) / 2
                );
            }
        }
    }

    public static boolean any(OccupancyView occupancy, int x, int y, int w, int h) {
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (occupancy.isOccupied(x + i, y + j))
                    return true;
            }
        }
        return false;
    }

    private void paintBuilding(Graphics2D g, Zoom zoom) {
        Rectangle r = buildingPlacer.getBuildingLocation();
        if (r == null) return;
        if (buildingPlacer.canBuild()) {
            g.setColor(Colors.CAN_PLACE);
        } else {
            g.setColor(Colors.CANNOT_PLACE);
        }

        g.fill(zoom.mapGameToScreen(r));
    }

    private void paintPaths(Graphics2D g, Zoom zoom) {
        for (RevPair<Action> entry : state.actionManager.getByType(Action.ActionType.Move)) {
            EntityReader reader = new EntityReader(state, entry.entityId);
            EntitySpec type = reader.getType();
            if (type == null) continue;
            if (reader.isHidden()) continue;

            // look at the owner player
            if (!reader.getOwner().equals(clientGameState.currentPlayer)) {
                continue;
            }

            Action.MoveSeq mv = (Action.MoveSeq) entry.value;
            if (mv == null || mv.path == null) return;
            List<DPoint> path = mv.path.points;
            DPoint c = null;

            AStar.AStarDebug aStarDebug = null;
            if (mv.path.debug instanceof AStar.AStarDebug)
                aStarDebug = (AStar.AStarDebug) mv.path.debug;

            JumpPointSearch.JPSDebug jpsDebug = null;
            if (mv.path.debug instanceof JumpPointSearch.JPSDebug)
                jpsDebug = (JumpPointSearch.JPSDebug) mv.path.debug;

            g.setColor(Color.yellow);
            if (CommonConstants.PAINT_SEARCH_DEBUG && jpsDebug != null)
                for (Map.Entry<Point, String> e : jpsDebug.closedSet.entrySet()) {
                    Point p = e.getKey();

                    Rectangle2D rectangle2D = zoom.mapGameToScreen(p.x, p.y, 1, 1);
                    g.draw(rectangle2D);
                    g.drawString(e.getValue(), (int)(rectangle2D.getX() + 1), (int)(rectangle2D.getY() + 1));
                }

            g.setColor(Color.blue);
            if (CommonConstants.PAINT_SEARCH_DEBUG && jpsDebug != null)
                for (Point p : jpsDebug.foundPath)
                    g.draw(zoom.mapGameToScreen(p.x, p.y, 1, 1));



            g.setColor(Color.white);
            if (CommonConstants.PAINT_SEARCH_DEBUG && aStarDebug != null)
                for (Point p : aStarDebug.checked)
                    g.fill(zoom.mapGameToScreen(p.x + 0.4, p.y + 0.4, 0.2, 0.2));

            g.setColor(Color.blue);
            for (DPoint p : path) {
                if (c == null) {
                    c = p;
                    continue;
                }

                g.draw(zoom.mapGameLineToScreen(
                        c.x + type.size.width / 2.0,
                        c.y + type.size.height / 2.0,
                        p.x + type.size.width / 2.0,
                        p.y + type.size.height / 2.0
                ));

                c = p;
            }
            c = null;
            g.setColor(Color.red);
            if (CommonConstants.PAINT_SEARCH_DEBUG)
                for (DPoint p : path) {
                    if (c == null) {
                        c = p;
                        continue;
                    }
                    g.draw(zoom.mapGameLineToScreen(c.x, c.y, p.x, p.y));
                    c = p;
                }

            int rSize = 2;
            g.setColor(Color.black);
            if (CommonConstants.PAINT_SEARCH_DEBUG && aStarDebug != null)
                for (DPoint p : aStarDebug.intersections) {
                    Point2D point2D = zoom.mapGameToScreen(p.x, p.y);
                    g.fillRect(
                            (int) (point2D.getX() - rSize),
                            (int) (point2D.getY() - rSize),
                            2 * rSize,
                            2 * rSize
                    );
                }

            Point cp = null;
            g.setColor(Color.yellow);
            if (CommonConstants.PAINT_SEARCH_DEBUG && aStarDebug != null)
                for (Point p : aStarDebug.originalPoints) {
                    if (cp == null) {
                        cp = p;
                        continue;
                    }

                    g.draw(zoom.mapGameLineToScreen(
                            cp.x + type.size.width / 2.0,
                            cp.y + type.size.height / 2.0,
                            p.x + type.size.width / 2.0,
                            p.y + type.size.height / 2.0
                    ));
                    cp = p;
                }
        }
    }

    private interface EntityGetter { EntityReader getEntity(Action action); }
    private void paintAction(Graphics2D g, Color color, Action.ActionType actionType, EntityGetter getter, Zoom zoom, double currentTime) {
        for (RevPair<Action> entry : state.actionManager.getByType(actionType)) {
            if (actionType.equals(Action.ActionType.Attack))
                System.out.println("here");
            EntityReader target = getter.getEntity(entry.value);
            EntityReader source = new EntityReader(state, entry.entityId);
            DPoint sourceLocation = source.getLocation(currentTime);
            if (sourceLocation == null) continue;
            DPoint destinationLocation = target.getLocation(currentTime);
            if (destinationLocation == null) continue;
            Dimension sourceSize = source.getSize();
            if (sourceSize == null) continue;
            Dimension targetSize = target.getSize();
            if (targetSize == null) continue;

            DPoint sourceCenter = source.getCenterLocation();
            DPoint targetCenter = target.getCenterLocation();

            if (zoom.isOutOfScreen(sourceCenter) || zoom.isOutOfScreen(targetCenter))
                continue;

            g.setColor(color);
            g.draw(zoom.mapGameLineToScreen(sourceCenter, targetCenter));

            double progress = entry.value.getProgressIndicator();
            if (progress < 0.0) continue;
            if (false) {
                double x1 = sourceLocation.x + sourceSize.width;
                double x2 = sourceLocation.x + sourceSize.width + BAR_WIDTH;
                double y1 = sourceLocation.y + 0;
                double y2 = sourceLocation.y + progress * sourceSize.height;
                double y3 = sourceLocation.y + sourceSize.height;
                g.fill(zoom.mapGameEndPointsToScreen(x1, y1, x2, y2));
                g.setColor(Color.black);
                g.fill(zoom.mapGameEndPointsToScreen(x1, y2, x2, y3));
            } else {
                final double CIRCLE_WIDTH = 0.05;
                double x = sourceLocation.x + sourceSize.width;
                double y = sourceLocation.y + sourceSize.height;
                Rectangle2D outer = zoom.mapGameToScreen(x, y, 4 * CIRCLE_WIDTH, 4 * CIRCLE_WIDTH);
                Rectangle2D inner = zoom.mapGameToScreen(x + CIRCLE_WIDTH, y + CIRCLE_WIDTH, 2 * CIRCLE_WIDTH, 2 * CIRCLE_WIDTH);
                Area outerArea = new Area(new Ellipse2D.Double(outer.getX(), outer.getY(), outer.getWidth(), outer.getHeight()));
                Area innerArea = new Area(new Ellipse2D.Double(inner.getX(), inner.getY(), inner.getWidth(), inner.getHeight()));
                Area progressArea = new Area(new Arc2D.Double(outer.getX(), outer.getY(), outer.getWidth(), outer.getHeight(), 0, progress * 360, Arc2D.PIE));
                outerArea.subtract(innerArea);
                progressArea.subtract(innerArea);
                g.setColor(Color.black);
                g.fill(outerArea);
                g.setColor(color);
                g.fill(progressArea);
            }
        }
    }

    private void paintImage(Graphics2D g, String imagePath, double gx, double gy, double gwidth, double gheight, boolean selected, Zoom zoom, Player player) {
        if (zoom.isOutOfScreen(gx, gy, gwidth, gheight)) {
            return;
        }

        Rectangle r = zoom.mapGameToScreenInts(gx, gy, gwidth, gheight);

        BufferedImage image = context.imageCache.get(imagePath);
        g.drawImage(
                image,
                r.x, r.y,
                r.x + r.width, r.y + r.height,
                0, 0,
                image.getWidth(), image.getHeight(),
                panel
        );
        if (player != null) {
            g.setColor(Colors.PLAYER_COLORS[player.number]);
            int d = 1;
            g.drawRect(r.x - d, r.y - d, r.width + 2 * d, r.height + 2 * d);
        }

        if (selected) {
            g.setColor(Color.yellow);
            int d = 3;
            g.drawRect(r.x - d, r.y - d, r.width + 2 * d, r.height + 2 * d);
        }
    }

    private void paintDisplayable(Graphics2D g, EntityReader entity, Zoom zoom, double currentTime) {
        EntitySpec type = entity.getType();
        DPoint location = entity.getLocation(currentTime);
        String image = entity.getGraphics();
        boolean isHidden = entity.isHidden();
        Player player = entity.getOwner();
        if (type == null || location == null || isHidden) return;
        boolean selected = context.selectionManager.isSelected(entity);
        paintImage(
                g,
                image,
                location.x,
                location.y,
                type.size.width,
                type.size.height,
                selected,
                zoom,
                player.equals(Player.GAIA) ? null : player
        );
        Set<Integer> controlGroups = context.selectionManager.getControlGroups(entity.entityId);
        if (!controlGroups.isEmpty()) {
            g.setColor(Color.white);
            g.drawString(
                    controlGroups.stream().map(String::valueOf).collect(Collectors.joining(", ")),
                    (int) zoom.mapGameToScreenX(location.x),
                    (int) zoom.mapGameToScreenY(location.y)
            );
        }

        double baseHealth = entity.getBaseHealth();
        double currentHealth = entity.getCurrentHealth();
        if (baseHealth > 0 && baseHealth != currentHealth) {
            double ratio = currentHealth / baseHealth;
            if (ratio > 0.75) {
                g.setColor(Color.green);
            } else if (ratio > 0.25) {
                g.setColor(Color.yellow);
            } else  {
                g.setColor(Color.red);
            }

            double x1 = location.x;
            double x2 = location.x + ratio * type.size.width;
            double x3 = location.x + type.size.width;
            double y1 = location.y + type.size.height;
            double y2 = location.y + type.size.height + BAR_WIDTH;
            g.fill(zoom.mapGameEndPointsToScreen(x1, y1, x2, y2));
            g.setColor(Color.black);
            g.fill(zoom.mapGameEndPointsToScreen(x2, y1, x3, y2));
        }


        DPoint gatherPoint = entity.getCurrentGatherPoint();
        if (gatherPoint != null && selected) {
            g.setColor(Colors.GATHER_POINT);
            g.fill(zoom.mapGameCircleToScreen(gatherPoint.x, gatherPoint.y, 0.1));
            g.draw(zoom.mapGameLineToScreen(gatherPoint.x, gatherPoint.y, location.x, location.y));
        }
    }




    private static double getAngle(double dx, double dy) {
        if (Math.abs(dx) > 14-3)
            return 180 * Math.atan(dy / dx) / Math.PI;
        if (dy > 0)
            return 90d;
        else
            return -90d;
    }

    private static void drawProjectile(Graphics2D g, ProjectileLaunch launch, double prevTime, double curTime) {
        g.setColor(Color.yellow);
        double theta = getAngle(launch.directionX, launch.directionY);
        double spread = 15;
        double innerRadius = launch.projectile.speed * (prevTime - launch.launchTime);
        double outerRadius = launch.projectile.speed * (curTime - launch.launchTime);

        Area inner = new Area(new Arc2D.Double(launch.launchLocation.x - innerRadius, launch.launchLocation.y - innerRadius, 2 * innerRadius, 2 * innerRadius, theta - spread, 2 * spread, Arc2D.PIE));
        Area outer = new Area(new Arc2D.Double(launch.launchLocation.x - outerRadius, launch.launchLocation.y - outerRadius, 2 * outerRadius, 2 * outerRadius, theta - spread, 2 * spread, Arc2D.PIE));
        outer.subtract(inner);
        g.fill(outer);
    }
}
