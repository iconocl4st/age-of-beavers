package server.state;

import common.msg.Message;
import common.state.EntityId;
import common.util.json.JsonWrapper;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.WriteOptions;
import server.app.Lobby;
import server.app.ServerConnectionContext;
import server.app.ServerContext;
import server.engine.Simulator;
import server.util.IdGenerator;

import java.io.IOException;

public class Game {
    ServerContext context;
    public IdGenerator idGenerator;
    public Lobby lobby;
    public ServerGameState serverState;
    Simulator simulator;

    public boolean tick(ServerStateManipulator ssm, double prevTime, double currentTime, double timeDelta) {

        // todo create deer as needed...

        ssm.updateGameTime(currentTime);

        for (EntityId entity : serverState.state.entityManager.allKeys()) {
            simulator.simulateUnitActions(entity, ssm, prevTime, timeDelta);
        }

        for (EntityId entityId : serverState.state.projectileManager.allKeys()) {
            simulator.processProjectile(ssm, entityId, serverState.state.projectileManager.get(entityId));
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

    public static Game createGame(ServerContext context, Lobby lobby) {
        Game game = new Game();
        game.lobby = lobby;
        game.context = context;
        game.idGenerator = new IdGenerator(lobby.random);
        game.serverState = ServerGameState.createServerGameState(lobby.getCurrentSpec(), lobby.getNumPlayers());
        game.simulator = new Simulator(context, game.serverState, game.idGenerator, lobby.random);
        return game;
    }
}
