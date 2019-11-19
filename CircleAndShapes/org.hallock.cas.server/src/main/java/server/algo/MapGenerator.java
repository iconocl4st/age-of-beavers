package server.algo;

import common.state.EntityId;
import common.state.Player;
import common.state.spec.GameSpec;
import common.state.spec.GenerationSpec;
import common.state.sst.GameState;
import common.util.DPoint;
import common.util.EvolutionSpec;
import server.state.ServerGameState;
import server.state.ServerStateManipulator;
import server.util.IdGenerator;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

public class MapGenerator {

    private static final double MIN_DISTANCE_TO_PLAYER = 20.0;

    private final Random random;
    private final IdGenerator idGenerator;
    private final GameSpec spec;
    private final ServerGameState gameState;
    private final ServerStateManipulator ssm;
    private final Point[] playerLocations;

    public MapGenerator(Point[] playerLocations, ServerGameState gameState, Random random, IdGenerator idGenerator,  ServerStateManipulator ssm) {
        this.playerLocations = playerLocations;
        this.spec = gameState.state.gameSpec;
        this.random = random;
        this.idGenerator = idGenerator;
        this.gameState = gameState;
        this.ssm = ssm;
    }

    private double distanceToPlayer(int i, int j) {
        double min = Double.MAX_VALUE;
        for (Point p : playerLocations)  {
            int dx = i - p.x;
            int dy = j - p.y;
            double d = Math.sqrt(dx*dx+dy*dy);
            if (d  <  min)
                min = d;
        }
        return min;
    }

    private GameState.OccupancyView getOccupancy() {
        return gameState.state.getOccupancyForAny();
    }

    private UnionFind createUnionFind() {
        int w = gameState.state.gameSpec.width;
        int h = gameState.state.gameSpec.height;
        GameState.OccupancyView occupancy = getOccupancy();
        UnionFind unionFind = new UnionFind(w, h);
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (occupancy.isOccupied(i, j))
                    continue;
                if (i + 1 < w && !occupancy.isOccupied(i+1, j))
                    unionFind.connect(i, j, i + 1, j);
                if (j + 1 < h && !occupancy.isOccupied(i, j+1))
                    unionFind.connect(i, j, i, j + 1);
            }
        }
        return unionFind;
    }

    DPoint getRandomLocation() {
        // need to add a buffer
        return new DPoint((int) (random.nextDouble() * spec.width), (int)(random.nextDouble() * spec.height));
    }

    DPoint getRandomLocation(DPoint location, double maximumDistance) {
        return new DPoint(
                (int) (Math.max(2, Math.min(spec.width - 2 , location.x + (2 * random.nextDouble() - 1) * maximumDistance))),
                (int) (Math.max(2, Math.min(spec.height - 2, location.y + (2 * random.nextDouble() - 1) * maximumDistance)))
        );
    }

    void generateResources(ServerGameState state) {
        GameState.OccupancyView occupancy = getOccupancy();
        for (GenerationSpec.ResourceGen rg : spec.generationSpec.resources) {
            if (rg == null) {
                throw new RuntimeException("Unable to find resource.");
            }
            for (int batch = 0; batch < rg.numberOfPatches; batch++) {

                Point startingPoint;
                do {
                    startingPoint = getRandomLocation().toPoint();
                } while (occupancy.isOccupied(startingPoint.x, startingPoint.y) || distanceToPlayer(startingPoint.x, startingPoint.y) < 1.5 * MIN_DISTANCE_TO_PLAYER);


                generatePatch(occupancy, rg, startingPoint, true);
            }
        }
    }

    private void generatePatch(GameState.OccupancyView occupancy, GenerationSpec.ResourceGen rg, Point startingPoint, boolean avoidPlayers) {
        HashSet<Point> horizon = new HashSet<>();
        horizon.add(startingPoint);

        for (int tile = 0; tile < rg.patchSize && !horizon.isEmpty(); tile++) {
            Point next = pickRandomElement(horizon);
            if (next == null) // ran out of room
                break;
            ssm.createUnit(idGenerator.generateId(), rg.type, new EvolutionSpec(rg.type), new DPoint(next), Player.GAIA);

            for (int dx=-1; dx<2; dx++) {
                for (int dy = -1; dy<2; dy++) {
                    if (dx == 0 && dy == 0)
                        continue;
                    if (next.x + dx < 0 || next.y + dy < 0)
                        continue;
                    if (next.x + dx >= spec.width || next.y + dy >= spec.height)
                        continue;
                    if (occupancy.isOccupied(next.x + dx, next.y +  dy))
                        continue;
                    if (avoidPlayers && distanceToPlayer(next.x + dx, next.y + dy) < MIN_DISTANCE_TO_PLAYER)
                        continue;
                    horizon.add(new Point(next.x + dx, next.y + dy));
                }
            }
        }
    }

    private Point pickRandomElement(HashSet<Point> horizon) {
        ArrayList<Point> points = new ArrayList<>(horizon);
        Collections.shuffle(points, random);
        if (points.isEmpty()) {
            return null;
        }
        Point next = points.get(0);
        horizon.remove(next);
        return next;
    }

    private DPoint getFreeLocation(Dimension size, DPoint close, double maxDistance, UnionFind unionFind) {
        Point p2 = close.toPoint();
        for (int i = 0; i < 1000; i++) {
            DPoint randomLocation = getRandomLocation(close, maxDistance);
            if (!gameState.state.hasSpaceFor(randomLocation, size))
                continue;
            Point p1 = randomLocation.toPoint();
            if (unionFind != null && !unionFind.areConnected(p1.x, p1.y, p2.x, p2.y))
                continue;
            return randomLocation;
        }
        throw new RuntimeException("Could not find a spot");
    }


    private DPoint getFreeLocation(Dimension size) {
        for (int i = 0; i < 1000; i++) {
            DPoint randomLocation = getRandomLocation();
            if (gameState.state.hasSpaceFor(randomLocation, size)) {
                return randomLocation;
            }
        }
        throw new RuntimeException("Could not find a spot");
    }

    private void generatePlayers(int numPlayers) {
        GameState.OccupancyView occupancy = getOccupancy();
        for (int p = 0; p < numPlayers; p++) {
            DPoint playerLocation = new DPoint(playerLocations[p]);
            for (GenerationSpec.ResourceGen rGen : spec.generationSpec.perPlayerResources) {
                for (int i = 0; i < rGen.numberOfPatches; i++) {
                    generatePatch(
                            occupancy,
                            rGen,
                            getFreeLocation(rGen.type.size, playerLocation, MIN_DISTANCE_TO_PLAYER, null).toPoint(),
                            false
                    );
                }
            }
        }

        UnionFind unionFind = createUnionFind();
        for (int p = 0; p < numPlayers; p++) {
            DPoint playerLocation = new DPoint(playerLocations[p]);
            Player player = new Player(p + 1);
            for (GenerationSpec.UnitGen uGen : spec.generationSpec.perPlayerUnits) {
                for (int i = 0; i < uGen.number; i++) {
                    ssm.createUnit(
                            idGenerator.generateId(),
                            uGen.type,
                            new EvolutionSpec(uGen.type),
                            getFreeLocation(uGen.type.size, playerLocation, MIN_DISTANCE_TO_PLAYER, unionFind),
                            player
                    );
                }
            }
        }
    }

    private void generateGaia() {
//        UnionFind unionFind = createUnionFind();
        for (GenerationSpec.UnitGen uGen : spec.generationSpec.gaia) {
            for (int i = 0; i < uGen.number; i++) {
                EntityId id = idGenerator.generateId();
                ssm.createUnit(
                        id,
                        uGen.type,
                        new EvolutionSpec(uGen.type),
                        getFreeLocation(uGen.type.size),
                        Player.GAIA
                );
            }
        }
    }

    public static ServerGameState randomlyGenerateMap(ServerGameState gameState, GameSpec spec, int numPlayers, Random random, IdGenerator idGenerator, ServerStateManipulator ssm) {
        Point[] playerLocations = new Point[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            int x = spec.width / 2 + (int) (0.75 * spec.width / 2 * Math.cos(2 * Math.PI * i / (double) numPlayers));
            int y = spec.height / 2 + (int) (0.75 * spec.height / 2 * Math.sin(2 * Math.PI * i / (double) numPlayers));
            playerLocations[i] = new Point(x, y);
        }
        MapGenerator gen = new MapGenerator(playerLocations, gameState, random, idGenerator, ssm);
        gen.generateResources(gameState);
        gen.generatePlayers(numPlayers);
        gen.generateGaia();
        return gameState;
    }
}
