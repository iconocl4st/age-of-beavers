package client.gui.game;

import client.algo.HueristicPaintMarkedTiles;
import client.app.UiClientContext;
import common.CommonConstants;
import common.DebugGraphics;
import common.action.Action;
import common.algo.AStar;
import common.algo.jmp_pnt.JumpPointSearch;
import common.algo.quad.MarkedRectangle;
import common.algo.quad.OccupiedQuadTree;
import common.algo.quad.QuadTreeOccupancyState;
import common.algo.quad.RootFinder;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.Player;
import common.state.edit.P;
import common.state.spec.EntitySpec;
import common.state.sst.GameState;
import common.state.sst.OccupancyView;
import common.state.sst.manager.RevPair;
import common.state.sst.sub.ProjectileLaunch;
import common.state.sst.sub.TerrainType;
import common.util.DPoint;
import common.util.GridLocation;
import common.util.Profiler;
import common.util.Util;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GamePainter {
    private static final double BAR_WIDTH = 0.1;


    // TODO: Remove all paints that are off screen...

    public static class RenderContext {
        public UiClientContext context;
        public int gameWidth;
        public int gameHeight;
        public double xmin;
        public double xmax;
        public double ymin;
        public double ymax;
        public double currentTime;

        public GameState getState() {
            if (context == null) return null;
            if (context.clientGameState == null) return null;
            return context.clientGameState.gameState;
        }
    }

    public static void s_renderGame(Renderer renderer, RenderContext c, Profiler profiler) {
        renderer.fillEverything(Colors.BACKGROUND, ZLevels.Z_BRACKGROUND);
        renderer.fillRectangle(Colors.DESERT, 0, 0, c.gameWidth, c.gameHeight, ZLevels.Z_GROUND);

        try (P p = profiler.time("terrain")) {
            paintTerrain(c, renderer);
        }

        try (P p = profiler.time("grid")) {
            paintGrid(c, renderer);
        }

        try (P p = profiler.time("displayables")) {
            for (EntityId entityId : c.getState().entityManager.allKeys())
                paintDisplayable(new EntityReader(c.getState(), entityId), c.currentTime, renderer, c.context);
        }
        if (true) return;

        try (P p = profiler.time("quad tree")) {
            paintQuadTree(renderer, c.context.clientGameState.quadTree);
        }

        // TODO: Should combine these with paint displayable
        try (P p = profiler.time("paths")) {
            paintPaths(renderer, c.context);
        }

        try (P p = profiler.time("actions")) {
            paintAction(Colors.DEPOSIT, Action.ActionType.Deposit, action -> new EntityReader(c.getState(), ((Action.Deposit) action).location), c.currentTime, renderer, c.context);
            paintAction(Colors.COLLECT, Action.ActionType.Collect, action -> new EntityReader(c.getState(), ((Action.Collect) action).resourceCarrier), c.currentTime, renderer, c.context);
            paintAction(Colors.ATTACK, Action.ActionType.Attack, action -> new EntityReader(c.getState(), ((Action.Attack) action).target), c.currentTime, renderer, c.context);
            paintAction(Colors.BUILD, Action.ActionType.Build, action -> new EntityReader(c.getState(), ((Action.Build) action).constructionId), c.currentTime, renderer, c.context);
        }

        try (P p = profiler.time("projectiles")) {
            paintProjectiles(renderer, c.getState(), c.currentTime);
        }

        if (CommonConstants.PAINT_GRID_DEBUG) {
            for (GridLocation.GraphicalDebugRectangle gdr : c.getState().locationManager.getDebugRectangles()) {
                renderer.drawRectangle(
                        Color.pink,
                        gdr.rectangle.getX(), gdr.rectangle.getY(), gdr.rectangle.getWidth(), gdr.rectangle.getHeight(),
                        ZLevels.Z_GRID_DEBUG
                );
                renderer.drawGameString(
                        Color.pink,
                        gdr.description,
                        gdr.rectangle.getCenterX(),
                        gdr.rectangle.getCenterY(),
                        ZLevels.Z_GRID_DEBUG
                );
            }
        }

        renderer.drawRectangle(Colors.MAP_BOUNDARY, 0, 0, c.gameWidth, c.gameHeight, ZLevels.Z_MAP_BOUNDARY);

        Util.CyclingIterator<Color> kmeansCycleIterator = new Util.CyclingIterator<>(new Color[]{
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

        synchronized (DebugGraphics.byPlayer) {
            for (Map.Entry<Player, List<DebugGraphics>> entry : DebugGraphics.byPlayer.entrySet()) {
                kmeansCycleIterator.resetIndex();
                for (DebugGraphics debug : entry.getValue()) {
                    renderer.fillCircle(Colors.PLAYER_COLORS[entry.getKey().number], debug.center.x, debug.center.y, 0.2, ZLevels.Z_DEBUG_KMEANS);
                    Color color = kmeansCycleIterator.next();
                    for (DPoint rec : debug.list) {
                        renderer.drawLine(color, debug.center.x, debug.center.y, rec.x, rec.y, ZLevels.Z_DEBUG_KMEANS);
                    }
                }
            }
        }
    }

    void renderGame(Zoom zoom, Renderer renderer) {
//        paintVisibility(renderer, zoom);
//
//
    }

    public static void paintTerrain(RenderContext context, Renderer renderer) {
        Iterator<MarkedRectangle<TerrainType>> terrains = context.getState().textures.get(context.xmin, context.ymin, context.xmax, context.ymax);
        while (terrains.hasNext()) {
            MarkedRectangle<TerrainType> next = terrains.next();
            Color color = null;
            switch (next.type) {
                case Sahara:
                    continue;
                case Grass:
                    color = Colors.GRASS;
                    break;
                case Water:
                    color = Colors.WATER;
                    break;
            }
            if (next.w < 0 || next.h < 0) {
                throw new RuntimeException();
            }
            renderer.fillRectangle(color, next.x, next.y, next.w, next.h, ZLevels.Z_TERRAIN);
            if (CommonConstants.DEBUG_TERRAIN) {
                renderer.drawRectangle(Color.white, next.x, next.y, next.w, next.h, ZLevels.Z_DEBUG_TERRAIN);
            }
        }
    }

    private static void paintQuadTree(Renderer renderer, OccupiedQuadTree quadTree) {
        if (!CommonConstants.PAINT_QUADTREE) return;
        Iterator<MarkedRectangle<QuadTreeOccupancyState>> leafIterator = quadTree.leaves();
        RootFinder rf = quadTree.getRootFinder();
        while (leafIterator.hasNext()) {
            MarkedRectangle<QuadTreeOccupancyState> m = leafIterator.next();
            renderer.drawRectangle(Color.yellow, m.x, m.y, m.w, m.h, ZLevels.Z_QUAD_TREE);
            if (CommonConstants.PAINT_QUADTREE_VERBOSE)
                renderer.drawGameString(Color.yellow, String.valueOf(rf == null ? -1 : rf.getRoot(m.x, m.y)), m.x + m.w / 2d, m.y + m.h / 2d, ZLevels.Z_QUAD_TREE);

            if (m.type.equals(QuadTreeOccupancyState.Occupied)) {
                renderer.drawLine(Color.yellow, m.x, m.y, m.x + m.w, m.y + m.h, ZLevels.Z_QUAD_TREE);
                renderer.drawLine(Color.yellow, m.x, m.y + m.h, m.x + m.w, m.y, ZLevels.Z_QUAD_TREE);
            }
        }
    }

    private static void paintProjectiles(Renderer renderer, GameState state, double currentTime) {
        for (EntityId entityId : state.projectileManager.allKeys()) {
            ProjectileLaunch projectileLaunch = state.projectileManager.get(entityId);
            DPoint currentLocation = projectileLaunch.getLocation(currentTime);
            if (currentLocation == null) continue;
            renderer.fillCircle(Color.yellow, currentLocation.x, currentLocation.y, projectileLaunch.projectile.radius, ZLevels.Z_PROJECTILE);
        }
    }

    private static void paintGrid(RenderContext c, Renderer renderer) {
        if (c.xmax - c.xmin > 200 || c.ymax - c.ymin  > 200)
            return;

        for (int i = (int) Math.max(0, c.xmin); i < (int) Math.min(c.gameWidth, c.xmax); i++)
            renderer.drawLine(Colors.GRID_LINES, i, 0, i, c.gameHeight, ZLevels.Z_GRID);

        for (int i = (int) Math.max(c.ymin, 0); i < (int) Math.min(c.gameHeight, c.ymax); i++)
            renderer.drawLine(Colors.GRID_LINES, 0, i, c.gameWidth, i, ZLevels.Z_GRID);
    }


    private HueristicPaintMarkedTiles.RectangleReceiver createPainter(final Color color, Renderer renderer) {
        return new HueristicPaintMarkedTiles.RectangleReceiver() {
            @Override
            public void markedRectangle(int xb, int yb, int xe, int ye) {}

            @Override
            public void unMarkedRectangle(int xb, int yb, int xe, int ye) {
                renderer.fillRectangleEndPoints(color, xb, yb, xe, ye, ZLevels.Z_VISIBILITY);
                if (!CommonConstants.PAINT_LOS_RECTANGLES) return;
                renderer.drawRectangle(Color.white, xb, yb, xe - xb, ye - yb, ZLevels.Z_VISIBILITY);
            }
        };
    }

//    private void paintVisibility(Renderer renderer, RenderContext context, Zoom zoom) {
////        if (gameSpec.visibility.equals(GameSpec.VisibilitySpec.ALL_VISIBLE)) return;
//
//        double xs = Math.max(0, Math.min(gameWidth, Math.floor(zoom.mapScreenToGameX(0))));
//        double ys = Math.max(0, Math.min(gameHeight, Math.ceil(zoom.mapScreenToGameY(0))));
//        double xe = Math.max(0, Math.min(gameWidth, Math.ceil(zoom.mapScreenToGameX(panel.getWidth()))));
//        double ye = Math.max(0, Math.min(gameHeight, Math.floor(zoom.mapScreenToGameY(panel.getHeight()))));
//
//        HueristicPaintMarkedTiles.enumerateRectangles(
//
//                new Marked() {
//                    @Override
//                    public int getWidth() {
//                        return gameWidth;
//                    }
//
//                    @Override
//                    public int getHeight() {
//                        return gameHeight;
//                    }
//
//                    @Override
//                    public boolean get(int x, int y) {
//                        return clientGameState.lineOfsight.get(x, y) || !clientGameState.exploration.get(x, y);
//                    }
//                },
//                createPainter(Colors.NOT_VISIBLE, renderer),
//                (int) xs,
//                (int) ye,
//                (int) xe,
//                (int) ys
//        );
//        HueristicPaintMarkedTiles.enumerateRectangles(
//                clientGameState.exploration,
//                createPainter(Colors.UNEXPLORED, renderer),
//                (int) xs,
//                (int) ye,
//                (int) xe,
//                (int) ys
//        );
//
//        if (xe - xs > 10 || ys - ye > 10) {
//            return;
//        }
//
//        if (!CommonConstants.PAINT_LOS_RECTANGLES) return;
//
//        for (int i = (int) xs; i < 1 + (int) xe; i++) {
//            for (int j = (int) ye; j < 1 + (int) ys; j++) {
//                renderer.drawGameString(
//                        Color.white,
//                        "[" + i + ", " + j + "] " + clientGameState.lineOfsight.getCount(i,  j)
//                                + " " + clientGameState.exploration.get(i, j)
//                                + " " + clientGameState.gameState.staticOccupancy.get(i,  j)
//                                + " " + clientGameState.gameState.buildingOccupancy.get(i,  j),
//                        i + 0.5, j + 0.5,
//                        ZLevels.Z_VISIBILITY
//                );
//            }
//        }
//    }

    public static boolean any(OccupancyView occupancy, int x, int y, int w, int h) {
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (occupancy.isOccupied(x + i, y + j))
                    return true;
            }
        }
        return false;
    }

    private static void paintPaths(Renderer renderer, UiClientContext context) {
        for (RevPair<Action> entry : context.clientGameState.gameState.actionManager.getByType(Action.ActionType.Move)) {
            EntityReader reader = new EntityReader(context.clientGameState.gameState, entry.entityId);
            EntitySpec type = reader.getType();
            if (type == null) continue;
            if (reader.isHidden()) continue;

            // look at the owner player
            if (!reader.getOwner().equals(context.clientGameState.currentPlayer)) {
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

            if (CommonConstants.PAINT_SEARCH_DEBUG && jpsDebug != null)
                for (Map.Entry<Point, String> e : jpsDebug.closedSet.entrySet()) {
                    Point p = e.getKey();
                    renderer.drawRectangle(Color.yellow, p.x, p.y, 1, 1, ZLevels.Z_PATH_DEBUG);
                    renderer.drawGameString(Color.yellow, e.getValue(), p.x, p.y, ZLevels.Z_PATH_DEBUG);
                }

            if (CommonConstants.PAINT_SEARCH_DEBUG && jpsDebug != null)
                for (Point p : jpsDebug.foundPath)
                    renderer.drawRectangle(Color.blue, p.x, p.y, 1, 1, ZLevels.Z_PATH_DEBUG);



            if (CommonConstants.PAINT_SEARCH_DEBUG && aStarDebug != null)
                for (Point p : aStarDebug.checked)
                    renderer.drawRectangle(Color.white, p.x + 0.4, p.y + 0.4, 0.2, 0.2, ZLevels.Z_PATH_DEBUG);

            for (DPoint p : path) {
                if (c == null) {
                    c = p;
                    continue;
                }
                renderer.drawLine(
                        Color.blue,
                        c.x + type.size.width / 2.0,
                        c.y + type.size.height / 2.0,
                        p.x + type.size.width / 2.0,
                        p.y + type.size.height / 2.0,
                        ZLevels.Z_PATH_DEBUG
                );
                c = p;
            }
            c = null;
            if (CommonConstants.PAINT_SEARCH_DEBUG)
                for (DPoint p : path) {
                    if (c == null) {
                        c = p;
                        continue;
                    }
                    renderer.drawLine(Color.red, c.x, c.y, p.x, p.y, ZLevels.Z_PATH_DEBUG);
                    c = p;
                }

            double rSize = 0.2;
            if (CommonConstants.PAINT_SEARCH_DEBUG && aStarDebug != null)
                for (DPoint p : aStarDebug.intersections) {
                    renderer.fillRectangle(Color.black, p.x - rSize, p.y - rSize, 2 * rSize, 2  * rSize, ZLevels.Z_PATH_DEBUG);
                }

            Point cp = null;
            if (CommonConstants.PAINT_SEARCH_DEBUG && aStarDebug != null)
                for (Point p : aStarDebug.originalPoints) {
                    if (cp == null) {
                        cp = p;
                        continue;
                    }

                    renderer.drawLine(Color.yellow,
                            cp.x + type.size.width / 2.0,
                            cp.y + type.size.height / 2.0,
                            p.x + type.size.width / 2.0,
                            p.y + type.size.height / 2.0,
                            ZLevels.Z_PATH_DEBUG
                    );
                    cp = p;
                }
        }
    }

    private interface EntityGetter { EntityReader getEntity(Action action); }
    private static void paintAction(Color color, Action.ActionType actionType, EntityGetter getter, double currentTime, Renderer renderer, UiClientContext context) {
        for (RevPair<Action> entry : context.clientGameState.gameState.actionManager.getByType(actionType)) {
            EntityReader target = getter.getEntity(entry.value);
            EntityReader source = new EntityReader(context.clientGameState.gameState, entry.entityId);
            DPoint sourceLocation = source.getLocation(currentTime);
            if (sourceLocation == null) continue;
            DPoint destinationLocation = target.getLocation(currentTime);
            if (destinationLocation == null) continue;
            Dimension sourceSize = source.getSize();
            if (sourceSize == null) continue;
            Dimension targetSize = target.getSize();
            if (targetSize == null) continue;
            DPoint sourceCenter = source.getCenterLocation();
            if (sourceCenter == null) return;
            DPoint targetCenter = target.getCenterLocation();
            if (targetCenter == null) return;

            renderer.drawLine(color, sourceCenter.x, sourceCenter.y, targetCenter.x, targetCenter.y, ZLevels.Z_ACTION);

            double progress = entry.value.getProgressIndicator();
            if (progress < 0.0) continue;
            if (false) {
//                double x1 = sourceLocation.x + sourceSize.width;
//                double x2 = sourceLocation.x + sourceSize.width + BAR_WIDTH;
//                double y1 = sourceLocation.y + 0;
//                double y2 = sourceLocation.y + progress * sourceSize.height;
//                double y3 = sourceLocation.y + sourceSize.height;
//                g.fill(zoom.mapGameEndPointsToScreen(x1, y1, x2, y2));
//                g.setColor(Color.black);
//                g.fill(zoom.mapGameEndPointsToScreen(x1, y2, x2, y3));
            } else {
                final double CIRCLE_WIDTH = 0.05;
                double x = sourceLocation.x + sourceSize.width;
                double y = sourceLocation.y + sourceSize.height;
                renderer.drawProgress(progress, color, Color.black, x + 2 * CIRCLE_WIDTH, y + 2 * CIRCLE_WIDTH, CIRCLE_WIDTH, 2 * CIRCLE_WIDTH, ZLevels.Z_ACTION);
            }
        }
    }


    private static void paintDisplayable(EntityReader entity, double currentTime, Renderer renderer, UiClientContext context) {
        EntitySpec type = entity.getType();
        DPoint location = entity.getLocation(currentTime);
        String image = entity.getGraphics();
        boolean isHidden = entity.isHidden();
        Player player = entity.getOwner();
        if (type == null || location == null || isHidden) return;

        renderer.paintImage(image, location.x, location.y, type.size.width, type.size.height, ZLevels.Z_DISPLAYABLE);

        if (player != null && !player.equals(Player.GAIA)) {
            double d = 0.1;
            renderer.drawRectangle(Colors.PLAYER_COLORS[player.number], location.x - d, location.y - d, type.size.width + 2 * d, type.size.height + 2 * d,  ZLevels.Z_PLAYER_COLOR);
        }

        boolean selected = context.selectionManager.isSelected(entity);
        if (selected) {
            double d = 0.2;
            renderer.drawRectangle(Color.yellow, location.x - d, location.y - d, type.size.width + 2 * d, type.size.height + 2 * d,  ZLevels.Z_SELECTED);
        }

        Set<Integer> controlGroups = context.selectionManager.getControlGroups(entity.entityId);
        if (!controlGroups.isEmpty()) {
            renderer.drawGameString(Color.white, controlGroups.stream().map(String::valueOf).collect(Collectors.joining(", ")), location.x, location.y, ZLevels.Z_CONTROL_GROUPS);
        }

        double baseHealth = entity.getBaseHealth();
        double currentHealth = entity.getCurrentHealth();
        if (baseHealth > 0 && baseHealth != currentHealth) {
            double ratio = currentHealth / baseHealth;
            Color color;
            if (ratio > 0.75) {
                color = Color.green;
            } else if (ratio > 0.25) {
                color = Color.yellow;
            } else  {
                color = Color.red;
            }

            double x1 = location.x;
            double x2 = location.x + ratio * type.size.width;
            double x3 = location.x + type.size.width;
            double y1 = location.y + type.size.height;
            double y2 = location.y + type.size.height + BAR_WIDTH;

            renderer.fillRectangleEndPoints(color, x1, y1, x2, y2, ZLevels.Z_HEALTH_BAR);
            renderer.fillRectangleEndPoints(Color.black, x2, y1, x3, y2, ZLevels.Z_HEALTH_BAR);
        }

        DPoint gatherPoint = entity.getCurrentGatherPoint();
        if (gatherPoint != null && selected) {
            renderer.fillCircle(Colors.GATHER_POINT, gatherPoint.x, gatherPoint.y, 0.1, ZLevels.Z_GATHER_POINT);
            renderer.drawLine(Colors.GATHER_POINT, gatherPoint.x, gatherPoint.y, location.x, location.y, ZLevels.Z_GATHER_POINT);
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
