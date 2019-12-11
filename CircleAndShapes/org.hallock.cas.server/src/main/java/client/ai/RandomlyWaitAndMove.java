package client.ai;

import common.action.Action;
import common.algo.ConnectedSet;
import common.factory.Path;
import common.factory.SearchDestination;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.Occupancy;
import common.state.Player;
import common.state.spec.GameSpec;
import common.util.DPoint;
import server.state.ServerGameState;
import server.state.ServerStateManipulator;

import java.awt.*;
import java.util.Random;

public class RandomlyWaitAndMove {

    private final Random random;
    private final GameSpec gameSpec;
    private final double maxWait;
    private final int maxSize;
    private final EntityReader deer;
    private final ServerGameState gameState;

    public RandomlyWaitAndMove(ServerGameState gameState, EntityId deer, Random random, GameSpec gameSpec, double maxWait, int maxSize) {
        this.deer = new EntityReader(gameState.state, deer);
        this.random = random;
        this.gameSpec = gameSpec;
        this.gameState = gameState;
        this.maxWait = maxWait;
        this.maxSize = maxSize;
    }

    public boolean actionCompleted(ServerStateManipulator manipulator) {
        if (random.nextBoolean()) {
            manipulator.setUnitAction(deer, new Action.Wait(maxWait * random.nextDouble()));
            return true;
        }

        DPoint currentLocation = deer.getLocation();
        if (currentLocation == null) {
            return false;
        }
        Point randomConnectedPoint = ConnectedSet.getRandomConnectedPoint(
                random,
                currentLocation.toPoint(),
                Occupancy.createStaticOccupancy(gameState.state, Player.GAIA),
                maxSize
        );
        if (randomConnectedPoint == null) {
            return false;
        }
        Path path = gameState.pathFinder.findPath(deer, new SearchDestination(new DPoint(randomConnectedPoint)));
        if (path == null) return false;
        manipulator.setUnitAction(deer, new Action.MoveSeq(path));
        return true;
    }
}
