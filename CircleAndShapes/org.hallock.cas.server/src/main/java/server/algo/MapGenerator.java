package server.algo;

import common.algo.UnionFind2d;
import common.state.EntityId;
import common.state.Occupancy;
import common.state.Player;
import common.state.edit.P;
import common.state.spec.GameSpec;
import common.state.spec.GenerationSpec;
import common.state.sst.OccupancyView;
import common.state.sst.sub.TerrainType;
import common.util.DPoint;
import common.util.EvolutionSpec;
import common.util.Profiler;
import common.util.Util;
import server.state.ServerGameState;
import server.state.ServerStateManipulator;
import server.util.IdGenerator;

import java.awt.*;
import java.util.*;

public class MapGenerator {

    private static final double MIN_DISTANCE_TO_PLAYER = 20.0;

    private final Random random;
    private final IdGenerator idGenerator;
    private final GameSpec spec;
    private final ServerGameState gameState;
    private final ServerStateManipulator ssm;
    private final Point[] playerLocations;
    private final Profiler profiler;

    MapGenerator(Point[] playerLocations, ServerGameState gameState, Random random, IdGenerator idGenerator,  ServerStateManipulator ssm, Profiler profiler) {
        this.playerLocations = playerLocations;
        this.spec = gameState.state.gameSpec;
        this.random = random;
        this.idGenerator = idGenerator;
        this.gameState = gameState;
        this.ssm = ssm;
        this.profiler = profiler;
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

    private UnionFind2d createUnionFind(TwoDMapper mapper) {
        int w = gameState.state.gameSpec.width;
        int h = gameState.state.gameSpec.height;
        OccupancyView occupancy = Occupancy.createGenerationOccupancy(gameState.state);
        UnionFind2d unionFind = new UnionFind2d(mapper.maxX() - mapper.minX(), mapper.maxY() - mapper.minY());
        for (int i = mapper.minX(); i < mapper.maxX(); i++) {
            for (int j = mapper.minY(); j < mapper.maxY(); j++) {
                if (occupancy.isOccupied(i, j))
                    continue;
                if (i + 1 < w && !occupancy.isOccupied(i+1, j))
                    unionFind.connect(mapper.mapX(i), mapper.mapY(j), mapper.mapX(i + 1), mapper.mapY(j));
                if (j + 1 < h && !occupancy.isOccupied(i, j+1))
                    unionFind.connect(mapper.mapX(i), mapper.mapY(j), mapper.mapX(i), mapper.mapY(j + 1));
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

    void generateResources() {
        OccupancyView occupancy = Occupancy.createGenerationOccupancy(gameState.state);

        TreeSet<Point> farEnoughAway = new TreeSet<>((p1, p2) -> {
            int compare = Integer.compare(p1.x, p2.x);
            if (compare != 0) return compare;
            return Integer.compare(p1.y, p2.y);
        });
        try (P ignore = profiler.time("creating possible points")) {
            for (int i = 0; i < spec.width; i++) {
                for (int j = 0; j < spec.height; j++) {
                    if (occupancy.isOccupied(i, j))
                        continue;
                    if (distanceToPlayer(i, j) < 1.5 * MIN_DISTANCE_TO_PLAYER)
                        continue;
                    farEnoughAway.add(new Point(i, j));
                }
            }
        }
        for (GenerationSpec.ResourceGen rg : spec.generationSpec.resources) {
            if (rg == null) {
                throw new RuntimeException("Unable to find resource.");
            }
            try (P ignore = profiler.time(rg.type.name)) {
                for (int batch = 0; batch < rg.numberOfPatches; batch++) {
                    Point startingPoint = null;
                    while (startingPoint == null) {
                        startingPoint = farEnoughAway.ceiling(new Point(random.nextInt(spec.width), random.nextInt(spec.height)));
                    }

                    if (occupancy.isOccupied(startingPoint.x, startingPoint.y) || distanceToPlayer(startingPoint.x, startingPoint.y) < 1.5 * MIN_DISTANCE_TO_PLAYER) {
                        throw new RuntimeException("Should not be able to happen");
                    }

                    farEnoughAway.remove(startingPoint);

                    try (P p = profiler.time("patch")) {
                        generatePatch(occupancy, rg, startingPoint, true, farEnoughAway);
                    }
                }
            }
        }
    }



    private void generatePatch(OccupancyView occupancy, GenerationSpec.ResourceGen rg, Point startingPoint, boolean avoidPlayers, Set<Point> farEnoughAway) {
        HashSet<Point> horizon = new HashSet<>();
        HashSet<Point> grass = new HashSet<>();
        horizon.add(startingPoint);

        for (int tile = 0; tile < rg.patchSize && !horizon.isEmpty(); tile++) {
            Point next;
            try (P ignore = profiler.time("getting next");) {
                next = pickRandomElement(horizon);
            }
            if (next == null) // ran out of room
                break;

            if (farEnoughAway != null)
                farEnoughAway.remove(next);

            try (P ignore = profiler.time("Getting random element");) {
                ssm.createUnit(idGenerator.generateId(), rg.type, new EvolutionSpec(rg.type), new DPoint(next), Player.GAIA);
            }

            int R;
            switch (rg.type.name) {
                case "tree":
                    R = 20;
                    break;
                case "berry":
                    R = 2;
                    break;
                default:
                    R = -1;
            }
            // Currently the most expensive...
            try (P ignore = profiler.time("adding terrain");) {
                if (R > 0) {
                    gameState.state.textures.set(next.x, next.y, TerrainType.Grass);
                    for (int i = -R; i <= R; i++) {
                        for (int j = -R; j <= R; j++) {
                            if (next.x + i >= spec.width || next.x + i < 0 || next.y + j >= spec.height || next.y + j < 0)
                                continue;
                            if (Math.random() < 0.5 && new DPoint(next).distanceTo(new DPoint(next.x + i, next.y + j)) < R)
                                grass.add(new Point(next.x + i, next.y + j));
                        }
                    }
                }
            }
            try (P ignore = profiler.time("adding to horizon");) {
                for (int dx = -1; dx < 2; dx++) {
                    for (int dy = -1; dy < 2; dy++) {
                        if (dx == 0 && dy == 0)
                            continue;
                        if (next.x + dx < 0 || next.y + dy < 0)
                            continue;
                        if (next.x + dx >= spec.width || next.y + dy >= spec.height)
                            continue;
                        if (occupancy.isOccupied(next.x + dx, next.y + dy))
                            continue;
                        if (avoidPlayers && distanceToPlayer(next.x + dx, next.y + dy) < MIN_DISTANCE_TO_PLAYER)
                            continue;
                        horizon.add(new Point(next.x + dx, next.y + dy));
                    }
                }
            }
        }
        try (P ignore = profiler.time("setting terrain");) {
            for (Point p : grass) {
                                gameState.state.textures.set(
                                        p.x,
                                        p.y,
                                        TerrainType.Grass
                                );
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

    private DPoint getFreeLocation(Util.SpiralIterator iterator, Dimension size, DPoint close, double maxDistance, UnionFind2d unionFind, TwoDMapper mapper) {
        Point p2 = close.toPoint();
        OccupancyView generationOccupancy = Occupancy.createGenerationOccupancy(gameState.state);
        while (iterator.getRadius() < maxDistance) {
            Point p1 = Util.getSpaceForBuilding(iterator, size, generationOccupancy, spec.width, 4);
            if (!unionFind.areConnected(mapper.mapX(p1.x), mapper.mapY(p1.y), mapper.mapX(p2.x), mapper.mapY(p2.y)))
                continue;
            return new DPoint(p1);
        }
        throw new RuntimeException("Could not find a spot");
    }

    private DPoint getFreeLocation(Dimension size, DPoint close, double minDistance, double maxDistance) {
        Point p2 = close.toPoint();
        OccupancyView generationOccupancy = Occupancy.createGenerationOccupancy(gameState.state);
        for (int i = 0; i < 1000; i++) {
            DPoint randomLocation = getRandomLocation(close, maxDistance);
            if (randomLocation.distanceTo(close) < minDistance)
                continue;
            if (Occupancy.isOccupied(generationOccupancy, randomLocation.toPoint(), size))
                continue;
            return randomLocation;
        }
        throw new RuntimeException("Could not find a spot");
    }


    private DPoint getFreeLocation(Dimension size) {
        for (int i = 0; i < 1000; i++) {
            DPoint randomLocation = getRandomLocation();
            if (!Occupancy.isOccupied(Occupancy.createGenerationOccupancy(gameState.state), randomLocation.toPoint(), size)) {
                return randomLocation;
            }
        }
        throw new RuntimeException("Could not find a spot");
    }

    private void generatePlayers(int numPlayers) {
        try (P ignore = profiler.time("player resources")) {
            OccupancyView occupancy = Occupancy.createGenerationOccupancy(gameState.state);
            for (int p = 0; p < numPlayers; p++) {
                DPoint playerLocation = new DPoint(playerLocations[p]);
                for (GenerationSpec.ResourceGen rGen : spec.generationSpec.perPlayerResources) {
                    for (int i = 0; i < rGen.numberOfPatches; i++) {
                        generatePatch(
                                occupancy,
                                rGen,
                                getFreeLocation(rGen.type.size, playerLocation, 4, MIN_DISTANCE_TO_PLAYER).toPoint(),
                                false,
                                null
                        );
                    }
                }
            }
        }

        try (P ignore = profiler.time("player units")) {
            for (int p = 0; p < numPlayers; p++) {
                DPoint playerLocation = new DPoint(playerLocations[p]);
                TwoDMapper mapper = new TwoDMapper(playerLocation, MIN_DISTANCE_TO_PLAYER, spec.width, spec.height);
                UnionFind2d unionFind = createUnionFind(mapper);
                Util.SpiralIterator spiralIterator = new Util.SpiralIterator(playerLocations[p]);
                Player player = new Player(p + 1);
                for (GenerationSpec.UnitGen uGen : spec.generationSpec.perPlayerUnits) {
                    for (int i = 0; i < uGen.number; i++) {
                        EntityId generatedId = idGenerator.generateId();
                        ssm.createUnit(
                                generatedId,
                                uGen.type,
                                new EvolutionSpec(uGen.type),
                                getFreeLocation(spiralIterator, uGen.type.size, playerLocation, MIN_DISTANCE_TO_PLAYER, unionFind, mapper),
                                player
                        );
                        gameState.startingUnits.get(p).add(generatedId);
                        // TODO: make it explored, at least...
                    }
                }
            }
        }
    }

    private void generateGaia() {
//        UnionFind2d unionFind = createUnionFind();
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

    private static void generateRiver(ServerGameState gameState, Random random) {
        int width = 3;
        int max = gameState.state.gameSpec.width - width;
        int min = width;
        int x = gameState.state.gameSpec.width / 2;// random.nextInt(gameState.state.gameSpec.width);
        int y = 0;
        int pdx = 0;
        while (y < gameState.state.gameSpec.height) {
            for (int i = 0; i < 15; i++) {
                gameState.state.textures.set(x + i + width, y, TerrainType.Grass);
                gameState.state.textures.set(x - i - width, y, TerrainType.Grass);
            }
            for (int i = -width; i < width; i++)
                gameState.state.textures.set(x + i, y, TerrainType.Water);
            switch (pdx) {
                case -1:
                    pdx = random.nextInt(2) - 1;
                    break;
                case +0:
                    pdx = random.nextInt(3) - 1;
                    break;
                case +1:
                    pdx = random.nextInt(2);
                    break;
            }

            x += pdx;
            if (x <= min) x = min + 1;
            if (x >= max) x = max - 1;
            y += 1;
        }
    }

    public static ServerGameState randomlyGenerateMap(ServerGameState gameState, GameSpec spec, int numPlayers, Random random, IdGenerator idGenerator, ServerStateManipulator ssm) {
        Profiler profiler = new Profiler("map generation");
        try (P ignore = profiler.time("generation")) {
            for (int i = 0; i < numPlayers; i++) {
                int x = spec.width / 2 + (int) (0.75 * spec.width / 2 * Math.cos(2 * Math.PI * i / (double) numPlayers));
                int y = spec.height / 2 + (int) (0.75 * spec.height / 2 * Math.sin(2 * Math.PI * i / (double) numPlayers));
                gameState.playerStarts[i] = new Point(x, y);
            }

            MapGenerator gen = new MapGenerator(gameState.playerStarts, gameState, random, idGenerator, ssm, profiler);

            try (P p = profiler.time("resources")) {
                gen.generateResources();
            }
            try (P p = profiler.time("players")) {
                gen.generatePlayers(numPlayers);
            }
            try (P p = profiler.time("gaia")) {
                gen.generateGaia();
            }
            try (P p = profiler.time("river")) {
                generateRiver(gameState, random);
            }
        }

        System.out.println(profiler.report());
        return gameState;
    }

}
