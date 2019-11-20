package client.ai;

import common.action.Action;
import common.algo.ConnectedSet;
import common.state.spec.GameSpec;
import common.state.EntityId;
import common.state.Player;
import common.state.sst.GameState;
import common.algo.AStar;
import common.util.DPoint;
import common.util.query.GridLocationQuerier;
import server.state.ServerStateManipulator;

import java.awt.*;
import java.util.Random;

public class RandomlyWaitAndMove {

    private final Random random;
    private final GameSpec gameSpec;
    private final double maxWait;
    private final int maxSize;
    private final EntityId deer;
    private final GameState gameState;

    public RandomlyWaitAndMove(GameState gameState, EntityId deer, Random random, GameSpec gameSpec, double maxWait, int maxSize) {
        this.deer = deer;
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

        DPoint currentLocation = gameState.locationManager.getLocation(deer);
        if (currentLocation == null) {
            return false;
        }
        Point randomConnectedPoint = ConnectedSet.getRandomConnectedPoint(
                random,
                currentLocation.toPoint(),
                gameState.getOccupancyView(Player.GAIA),
                maxSize
        );
        if (randomConnectedPoint == null) {
            return false;
        }
        AStar.PathSearch path = GridLocationQuerier.findPath(gameState, currentLocation, new DPoint(randomConnectedPoint), Player.GAIA);
        if (path == null) {
            return false;
        }
        manipulator.setUnitAction(deer, new Action.MoveSeq(path.path));
        return true;
    }
}
