package server.algo;

import common.state.spec.EntitySpec;
import common.state.spec.GameSpec;
import common.state.EntityId;
import common.state.Player;
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
    private final Random random;
    private final IdGenerator idGenerator;
    private final GameSpec spec;
    private final ServerGameState gameState;
    private final ServerStateManipulator ssm;

    public MapGenerator(ServerGameState gameState, Random random, IdGenerator idGenerator,  ServerStateManipulator ssm) {
        this.spec = gameState.state.gameSpec;
        this.random = random;
        this.idGenerator = idGenerator;
        this.gameState = gameState;
        this.ssm = ssm;
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

    EntitySpec getRandomResource() {
        return spec.naturalResources[random.nextInt(spec.naturalResources.length)];
    }

    void generateResources(ServerGameState state) {
        GameState.OccupancyView occupancy = state.state.getOccupancyForAny();
        for (GenerationSpec.ResourceGen rg : spec.generationSpec.resources) {
            if (rg == null) {
                throw new RuntimeException("Unable to find resource.");
            }
            for (int batch = 0; batch < rg.numberOfPatches; batch++) {
                HashSet<Point> horizon = new HashSet<>();

                Point startingPoint;
                do {
                    startingPoint = getRandomLocation().toPoint();
                } while (occupancy.isOccupied(startingPoint.x, startingPoint.y));
                horizon.add(startingPoint);

                for (int tile = 0; tile < rg.patchSize; tile++) {
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
                            horizon.add(new Point(next.x + dx, next.y + dy));
                        }
                    }
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

    private DPoint getFreeLocation(Dimension size, DPoint close, double maxDistance) {
        for (int i = 0; i < 1000; i++) {
            DPoint randomLocation = getRandomLocation(close, maxDistance);
            if (gameState.state.hasSpaceFor(randomLocation, size)) {
                return randomLocation;
            }
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
        for (int p = 0; p < numPlayers; p++) {
            Player player = new  Player(p + 1);
            DPoint playerLocation = getRandomLocation();
            for (GenerationSpec.UnitGen uGen : spec.generationSpec.perPlayerUnits) {
                for (int i = 0; i < uGen.number; i++) {
                    ssm.createUnit(
                            idGenerator.generateId(),
                            uGen.type,
                            new EvolutionSpec(uGen.type),
                            getFreeLocation(uGen.type.size, playerLocation, 10),
                            uGen.type.name.equals("human") ? player : Player.GAIA
                    );
                }
            }
        }
    }

    private void generateGaia() {
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
        MapGenerator gen = new MapGenerator(gameState, random, idGenerator, ssm);
        gen.generateResources(gameState);
        gen.generatePlayers(numPlayers);
        gen.generateGaia();
        return gameState;
    }
}
