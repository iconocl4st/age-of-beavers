package client.gui.game;

import client.algo.HueristicPaintMarkedTiles;
import client.app.UiClientContext;
import client.gui.mouse.BuildingPlacer;
import client.gui.mouse.SelectionListener;
import client.state.ClientGameState;
import common.CommonConstants;
import common.DebugGraphics;
import common.action.Action;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.Player;
import common.state.spec.EntitySpec;
import common.state.spec.GameSpec;
import common.state.sst.GameState;
import common.state.sst.manager.RevPair;
import common.state.sst.sub.ProjectileLaunch;
import common.util.DPoint;
import common.util.GridLocation;
import common.util.Marked;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GamePainter {

    private static final double BAR_WIDTH = 0.1;

    private final SelectionListener selectionListener;
    private final BuildingPlacer buildingPlacer;

    UiClientContext context;
    ClientGameState clientGameState;
    GameState state;
    // TODO: remove
    JPanel panel;
    private int gameWidth;
    private int gameHeight;

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

    void renderGame(Graphics2D graphics, Zoom zoom) {
        Graphics2D g = graphics;

        g.setColor(Colors.BACKGROUND);
        g.fillRect(0, 0, panel.getWidth(), panel.getHeight());

        g.setColor(Colors.GRASS);
        g.fillRect(
                zoom.mapGameToScreenX(0),
                zoom.mapGameToScreenY(gameHeight),
                zoom.mapGameToScreenX(gameWidth) - zoom.mapGameToScreenX(0),
                zoom.mapGameToScreenY(0) - zoom.mapGameToScreenY(gameHeight)
        );

        paintGrid(g, zoom);
        paintVisibility(g, zoom);

        for (EntityId entityId : state.entityManager.allKeys()) {
            paintDisplayable(g, new EntityReader(state, entityId), zoom);
        }

        // todo: encapsulate
        if (selectionListener.isSelecting()) {
            g.setColor(Color.white);
            Rectangle r = selectionListener.rectangleListener.getScreenRectangle();
            g.drawRect(r.x, r.y, r.width, r.height);
        }

        // TODO: These should be one pass through the actions...
        // TODO: dry
        paintPaths(g, zoom);
        paintAction(g, Colors.DEPOSIT, Action.ActionType.Deposit, action -> new EntityReader(state, ((Action.Deposit) action).location), zoom);
        paintAction(g, Colors.COLLECT, Action.ActionType.Collect, action -> new EntityReader(state, ((Action.Collect) action).resourceCarrier), zoom);
        paintAction(g, Colors.ATTACK, Action.ActionType.Attack, action -> new EntityReader(state, ((Action.Attack) action).target), zoom);
        paintAction(g, Colors.BUILD, Action.ActionType.Build, action -> new EntityReader(state, ((Action.Build) action).constructionId), zoom);
        paintBuilding(g, zoom);

        paintProjectiles(g, zoom);
        // todo: paint

        g.setColor(Colors.MAP_BOUNDARY);
        g.drawRect(
                zoom.mapGameToScreenX(0),
                zoom.mapGameToScreenY(gameHeight),
                zoom.mapGameToScreenX(gameWidth) - zoom.mapGameToScreenX(0),
                zoom.mapGameToScreenY(0) -  zoom.mapGameToScreenY(gameHeight)
        );

        if (CommonConstants.PAINT_DEBUG_GRAPHICS) {
            g.setColor(Color.pink);
            for (GridLocation.GraphicalDebugRectangle r : state.locationManager.getDebugRectangles()) {

                int x1 = zoom.mapGameToScreenX(r.rectangle.getX());
                int x2 = zoom.mapGameToScreenX(r.rectangle.getX() + r.rectangle.getWidth());
                int y1 = zoom.mapGameToScreenY(r.rectangle.getY());
                int y2 = zoom.mapGameToScreenY(r.rectangle.getY() + r.rectangle.getHeight());

                g.drawRect(
                        x1,
                        y2,
                        x2 - x1,
                        y1 - y2
                );
                g.drawString(
                        r.description,
                        zoom.mapGameToScreenX(r.rectangle.getX() + r.rectangle.getWidth() / 2),
                        zoom.mapGameToScreenY(r.rectangle.getY() + r.rectangle.getHeight() / 2)
                );
            }

            // should be here...
        }

        synchronized (DebugGraphics.byPlayer) {
            for (Map.Entry<Player, List<DebugGraphics>> entry : DebugGraphics.byPlayer.entrySet()) {
                for (DebugGraphics debug : entry.getValue()) {
                    int cx = zoom.mapGameToScreenX(debug.center.x);
                    int cy = zoom.mapGameToScreenY(debug.center.y);
                    g.setColor(Colors.PLAYER_COLORS[entry.getKey().number]);
                    g.fillOval(cx - 2, cy - 2, 4, 4);

                    g.setColor(new Color((int)(255 * Math.random()), (int)(255 * Math.random()), (int)(255 * Math.random())));
                    for (Point rec : debug.list) {
                        g.drawLine(
                                cx,
                                cy,
                                zoom.mapGameToScreenX(rec.x),
                                zoom.mapGameToScreenY(rec.y)
                        );
                    }
                }
            }
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private void paintProjectiles(Graphics2D g, Zoom zoom) {
        g.setColor(Color.yellow);
        for (EntityId entityId : state.projectileManager.allKeys()) {
            ProjectileLaunch projectileLaunch = state.projectileManager.get(entityId);
            DPoint currentLocation = projectileLaunch.getLocation(state.currentTime);
            if (currentLocation == null) continue;
            int x1 = zoom.mapGameToScreenX(currentLocation.x - projectileLaunch.projectile.radius);
            int y1 = zoom.mapGameToScreenY(currentLocation.y - projectileLaunch.projectile.radius);
            int x2 = zoom.mapGameToScreenX(currentLocation.x + projectileLaunch.projectile.radius);
            int y2 = zoom.mapGameToScreenY(currentLocation.y + projectileLaunch.projectile.radius);
            g.fillOval(x1, y2, x2 - x1, y1 - y2);
        }
    }

    private void paintGrid(Graphics2D g, Zoom zoom) {
        g.setColor(Colors.GRID_LINES);
        for (int i = 0; i < gameWidth; i++) {
            g.drawLine(
                    zoom.mapGameToScreenX(i),
                    zoom.mapGameToScreenY(0),
                    zoom.mapGameToScreenX(i),
                    zoom.mapGameToScreenY(gameHeight)
            );
        }
        for (int i = 0; i < gameHeight; i++) {
            g.drawLine(
                    zoom.mapGameToScreenX(0),
                    zoom.mapGameToScreenY(i),
                    zoom.mapGameToScreenX(gameWidth),
                    zoom.mapGameToScreenY(i)
            );
        }
    }


    private HueristicPaintMarkedTiles.RectangleReceiver createPainter(final Color color, final Graphics2D g, Zoom zoom) {
        return new HueristicPaintMarkedTiles.RectangleReceiver() {
            @Override
            public void markedRectangle(int xb, int yb, int xe, int ye) {}

            @Override
            public void unMarkedRectangle(int xb, int yb, int xe, int ye) {
                int tileX1 = zoom.mapGameToScreenX(xb);
                int tileY1 = zoom.mapGameToScreenY(yb);
                int tileX2 = zoom.mapGameToScreenX(xe);
                int tileY2 = zoom.mapGameToScreenY(ye);
                g.setColor(color);
                g.fillRect(
                        tileX1,
                        tileY2,
                        tileX2 - tileX1,
                        tileY1 - tileY2
                );
                if (!CommonConstants.PAINT_DEBUG_GRAPHICS) return;
                g.setColor(Color.white);
                g.drawRect(
                        tileX1,
                        tileY2,
                        tileX2 - tileX1,
                        tileY1 - tileY2
                );
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
                        return state.lineOfSight.isVisible(null, x, y) || !state.lineOfSight.isExplored(null, x, y);
                    }
                },
                createPainter(Colors.NOT_VISIBLE, g, zoom),
                (int) xs,
                (int) ye,
                (int) xe,
                (int) ys
        );

//        if (gameSpec.visibility.equals(GameSpec.VisibilitySpec.EXPLORED)) return;

        HueristicPaintMarkedTiles.enumerateRectangles(
                state.lineOfSight.createExploredView(),
                createPainter(Colors.UNEXPLORED, g, zoom),
                (int) xs,
                (int) ye,
                (int) xe,
                (int) ys
        );

        if (xe - xs > 10 || ys - ye > 10) {
            return;
        }

        g.setColor(Color.white);
        for (int i = (int) xs; i < 1 + (int) xe; i++) {
            for (int j = (int) ye; j < 1 + (int) ys; j++) {
                int tileX1 = zoom.mapGameToScreenX(i);
                int tileY1 = zoom.mapGameToScreenY(j);
                int tileX2 = zoom.mapGameToScreenX(i + 1);
                int tileY2 = zoom.mapGameToScreenY(j + 1);
//                if (!c.gameState.lineOfSight.isExplored(i, j)) {
//                    g.fillRect(
//                            tileX1,
//                            tileY2,
//                            tileX2 - tileX1,
//                            tileY1 - tileY2
//                    );
//                }

                g.drawString(
                        "[" + i + ", " + j + "] " + context.clientGameState.gameState.lineOfSight.getCount(null, i,  j) + " " + context.clientGameState.gameState.lineOfSight.isExplored(null, tileX1, tileY1),
                        (tileX1 + tileX2) / 2,
                        (tileY1 + tileY2) / 2
                );
            }
        }
    }

    public static boolean any(GameState.OccupancyView occupancy, int x, int y, int w, int h) {
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (occupancy.isOccupied(x + i, y + j))
                    return true;
            }
        }
        return false;
    }

    private void paintBuilding(Graphics2D g, Zoom zoom) {
        if (buildingPlacer.isNotPlacing()) {
            return;
        }
        int x1 = zoom.mapGameToScreenX(buildingPlacer.buildingLocX);
        int y1 = zoom.mapGameToScreenY(buildingPlacer.buildingLocY);
        int x2 = zoom.mapGameToScreenX(buildingPlacer.buildingLocX + buildingPlacer.building.size.width);
        int y2 = zoom.mapGameToScreenY(buildingPlacer.buildingLocY + buildingPlacer.building.size.height);

        if (any(state.getOccupancyView(clientGameState.currentPlayer), buildingPlacer.buildingLocX, buildingPlacer.buildingLocY, buildingPlacer.building.size.width, buildingPlacer.building.size.height)) {
            g.setColor(Colors.CANNOT_PLACE);
        } else {
            g.setColor(Colors.CAN_PLACE);
        }

        g.fillRect(x1, y2, x2 - x1, y1 - y2);
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

            g.setColor(Color.white);
            if (CommonConstants.PAINT_DEBUG_GRAPHICS)
                for (Point p : mv.path.checked) {
                    int x1 = zoom.mapGameToScreenX(p.x + 0.4);
                    int y1 = zoom.mapGameToScreenY(p.y + 0.4);
                    int x2 = zoom.mapGameToScreenX(p.x + 0.6);
                    int y2 = zoom.mapGameToScreenY(p.y + 0.6);
                    g.fillRect(
                            x1,
                            y2,
                            x2 - x1,
                            y1 - y2
                    );
                }
            g.setColor(Color.blue);
            for (DPoint p : path) {
                if (c == null) {
                    c = p;
                    continue;
                }

                g.drawLine(
                        zoom.mapGameToScreenX(c.x + type.size.width / 2.0),
                        zoom.mapGameToScreenY(c.y + type.size.height / 2.0),
                        zoom.mapGameToScreenX(p.x + type.size.width / 2.0),
                        zoom.mapGameToScreenY(p.y + type.size.height / 2.0)
                );

                c = p;
            }
            c = null;
            g.setColor(Color.red);
            if (CommonConstants.PAINT_DEBUG_GRAPHICS)
                for (DPoint p : path) {
                    if (c == null) {
                        c = p;
                        continue;
                    }

                    g.drawLine(
                            zoom.mapGameToScreenX(c.x),
                            zoom.mapGameToScreenY(c.y),
                            zoom.mapGameToScreenX(p.x),
                            zoom.mapGameToScreenY(p.y)
                    );

                    c = p;
                }

            int rSize = 2;
            g.setColor(Color.black);
            if (CommonConstants.PAINT_DEBUG_GRAPHICS)
                for (DPoint p : mv.path.intersections) {
                    g.fillRect(
                            zoom.mapGameToScreenX(p.x) - rSize,
                            zoom.mapGameToScreenY(p.y) - rSize,
                            2 * rSize,
                            2 * rSize
                    );
                }

            Point cp = null;
            g.setColor(Color.yellow);
            if (CommonConstants.PAINT_DEBUG_GRAPHICS)
                for (Point p : mv.path.originalPoints) {
                    if (cp == null) {
                        cp = p;
                        continue;
                    }

                    g.drawLine(
                            zoom.mapGameToScreenX(cp.x + type.size.width / 2.0),
                            zoom.mapGameToScreenY(cp.y + type.size.height / 2.0),
                            zoom.mapGameToScreenX(p.x + type.size.width / 2.0),
                            zoom.mapGameToScreenY(p.y + type.size.height / 2.0)
                    );
                    cp = p;
                }
        }
    }

    private interface EntityGetter { EntityReader getEntity(Action action); }
    private void paintAction(Graphics2D g, Color color, Action.ActionType actionType, EntityGetter getter, Zoom zoom) {
        for (RevPair<Action> entry : state.actionManager.getByType(actionType)) {
            EntityReader target = getter.getEntity(entry.value);
            EntityReader source = new EntityReader(state, entry.entityId);
            DPoint sourceLocation = source.getLocation();
            if (sourceLocation == null) continue;
            DPoint destinationLocation = target.getLocation();
            if (destinationLocation == null) continue;
            EntitySpec sourceType = source.getType();
            if (sourceType == null) continue;
            EntitySpec destinationType = target.getType();
            if (destinationType == null) continue;

            DPoint sourceCenter = source.getCenterLocation();
            DPoint targetCenter = target.getCenterLocation();


            g.setColor(color);
            g.drawLine(
                    zoom.mapGameToScreenX(sourceCenter.x),
                    zoom.mapGameToScreenY(sourceCenter.y),
                    zoom.mapGameToScreenX(targetCenter.x),
                    zoom.mapGameToScreenY(targetCenter.y)
            );

            double progress = entry.value.getProgressIndicator();
            if (progress < 0.0) continue;
            int x1 = zoom.mapGameToScreenX(sourceLocation.x + sourceType.size.width);
            int x2 = zoom.mapGameToScreenX(sourceLocation.x + sourceType.size.width + BAR_WIDTH);
            int y1 = zoom.mapGameToScreenY(sourceLocation.y + 0);
            int y2 = zoom.mapGameToScreenY(sourceLocation.y + progress * sourceType.size.height);
            int y3 = zoom.mapGameToScreenY(sourceLocation.y + sourceType.size.height);
            g.fillRect(x1, y2, x2 - x1, y1 - y2);
            g.setColor(Color.black);
            g.fillRect(x1, y3, x2 - x1, y2 - y3);
        }
    }

    private void paintImage(Graphics2D g, String imagePath, double gx, double gy, double gwidth, double gheight, boolean selected, Zoom zoom, Player player) {
        if (zoom.isOutOfScreen(gx, gy, gwidth, gheight)) {
            return;
        }
        int x = zoom.mapGameToScreenX(gx);
        int y = zoom.mapGameToScreenY(gy);

        int ux = zoom.mapGameToScreenX(gx + gwidth);
        int uy = zoom.mapGameToScreenY(gy + gheight);

        BufferedImage image = context.imageCache.get(imagePath);
        g.drawImage(
                image,
                x, y,
                ux, uy,
                image.getWidth(), image.getHeight(),
                0, 0,
                panel
        );
        if (player != null) {
            g.setColor(Colors.PLAYER_COLORS[player.number]);
            int d = 1;
            g.drawRect(x - d, uy - d, ux - x + 2 * d, y - uy + 2 * d);
        }

        if (selected) {
            g.setColor(Color.yellow);
            int d = 3;
            g.drawRect(x - d, uy - d, ux - x + 2 * d, y - uy + 2 * d);
        }
    }

    private void paintDisplayable(Graphics2D g, EntityReader entity, Zoom zoom) {
        EntitySpec type = entity.getType();
        DPoint location = entity.getLocation();
        Boolean isHidden = entity.isHidden();
        Player player = entity.getOwner();
        if (type == null || location == null || isHidden == null || isHidden) return;
        boolean selected = context.selectionManager.isSelected(entity);
        paintImage(
                g,
                type.image,
                location.x,
                location.y,
                type.size.width,
                type.size.width,
                selected,
                zoom,
                player.equals(Player.GAIA) ? null : player
        );
        Set<Integer> controlGroups = context.selectionManager.getControlGroups(entity.entityId);
        if (!controlGroups.isEmpty()) {
            g.setColor(Color.white);
            g.drawString(
                    controlGroups.stream().map(String::valueOf).collect(Collectors.joining(", ")),
                    zoom.mapGameToScreenX(location.x),
                    zoom.mapGameToScreenY(location.y)
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

            int x1 = zoom.mapGameToScreenX(location.x);
            int x2 = zoom.mapGameToScreenX(location.x + ratio * type.size.width);
            int x3 = zoom.mapGameToScreenX(location.x + type.size.width);
            int y1 = zoom.mapGameToScreenY(location.y + type.size.height);
            int y2 = zoom.mapGameToScreenY(location.y + type.size.height + BAR_WIDTH);
            g.fillRect(x1, y2, x2 - x1, y1 - y2);
            g.setColor(Color.black);
            g.fillRect(x2, y2, x3 - x2, y1 - y2);
        }


        DPoint gatherPoint = entity.getCurrentGatherPoint();
        if (gatherPoint != null && selected) {
            g.setColor(Colors.GATHER_POINT);
            int g1x = zoom.mapGameToScreenX(gatherPoint.x - 0.1);
            int g1y = zoom.mapGameToScreenY(gatherPoint.y - 0.1);
            int g2x = zoom.mapGameToScreenX(gatherPoint.x + 0.1);
            int g2y = zoom.mapGameToScreenY(gatherPoint.y + 0.1);
            int sx = zoom.mapGameToScreenX(location.x + type.size.width / 2);
            int sy = zoom.mapGameToScreenY(location.y + type.size.height / 2);
            g.fillOval(
                    g1x,
                    g2y,
                    g2x - g1x,
                    g1y - g2y

            );
            g.drawLine(sx, sy, (g1x + g2x) / 2, (g1y + g2y) / 2);
        }
    }
}
