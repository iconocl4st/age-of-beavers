package server.app;

import common.msg.ConnectionWriter;
import common.state.Player;
import common.state.spec.GameSpec;
import server.state.ServerStateManipulator;

public class ServerConnectionContext {
    final String connectionName;
    public final ConnectionWriter writer;
    private Lobby lobby;

    ServerConnectionContext(String connectionName, ConnectionWriter writer) {
        this.connectionName = connectionName;
        this.writer = writer;
    }

    public Player getPlayer() {
        return lobby.getPlayer(this);
    }

    void join(Lobby lobby) {
        if (lobby == null) {
            throw new RuntimeException("No such lobby");
        }
        if (this.lobby != null) {
            throw new RuntimeException("Already in a lobby");
        }

        this.lobby = lobby;
        this.lobby.join(this);
    }

    void leave() {
        if (lobby == null) {
            throw new RuntimeException("Not in a lobby!");
        }
        this.lobby.leave(this);
        this.lobby = null;
    }

    void launch() {
        if (lobby == null) {
            throw new RuntimeException("Not in a lobby!");
        }
        lobby.launch();
    }

    ServerStateManipulator createStateManipulator(ServerConnectionContext context) {
        return lobby.createStateManipulator(context);
    }

    GameSpec getCurrentGameSpec() {
        if (lobby == null) return null;
        return lobby.spec;
    }
}
