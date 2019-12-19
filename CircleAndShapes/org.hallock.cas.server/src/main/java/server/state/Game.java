package server.state;

import common.state.EntityId;
import common.state.EntityReader;
import common.state.spec.GameSpec;
import common.state.sst.sub.GrowthInfo;
import server.app.Lobby;
import server.app.ServerContext;
import server.engine.Simulator;
import server.util.IdGenerator;

import java.util.Map;

public class Game {
    ServerContext context;
    public IdGenerator idGenerator;
    public Lobby lobby;
    public ServerGameState serverState;
    Simulator simulator;

    public boolean tick(ServerStateManipulator ssm, TimeInfo info) {

        // todo create deer as needed...

        ssm.updateGameTime(info);

        for (EntityId entity : serverState.state.entityManager.allKeys()) {
            simulator.simulateUnitActions(entity, ssm, info);
        }

        for (EntityId entityId : serverState.state.projectileManager.allKeys()) {
            simulator.processProjectile(ssm, entityId, serverState.state.projectileManager.get(entityId), info.prevTime, info.currentTime);
        }

        for (Map.Entry<EntityId, GrowthInfo> entry : serverState.state.cropInfo.allEntries()) {
            simulator.updateGrowth(ssm, new EntityReader(serverState.state, entry.getKey()), entry.getValue(), info.timeDelta);
        }

        lobby.ticked();

        if (isFinished()) {
            System.out.println("Sending Zoom over");
            lobby.gameOver();
            return true;
        }
        return false;
    }

    boolean isFinished() {
        return false;
    }

    public static Game createGame(ServerContext context, Lobby lobby, GameSpec spec, int numPlayers) {
        Game game = new Game();
        game.lobby = lobby;
        game.context = context;
        game.idGenerator = new IdGenerator(lobby.random);
        game.serverState = ServerGameState.createServerGameState(spec, numPlayers);
        game.simulator = new Simulator(context, game.serverState, game.idGenerator, lobby.random);
        return game;
    }
}
