package server.app;

import common.msg.ConnectionWriter;
import common.state.Player;
import common.state.spec.GameSpec;
import server.state.ServerStateManipulator;

public class ServerConnectionContext implements PlayerConnection {
    private final ConnectionWriter writer;
    private GamePlayerMessageHandler messageHandler;
    private Lobby lobby;

    ServerConnectionContext(ConnectionWriter writer) {
        this.writer = writer;
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

    void spectate(boolean spectate) {
        if (lobby == null) {
            throw new RuntimeException("Not in a lobby!");
        }
        this.lobby.setSpectating(spectate, this);
    }

    void launch() {
        if (lobby == null) {
            throw new RuntimeException("Not in a lobby!");
        }
        lobby.launch();
    }

    GameSpec getCurrentGameSpec() {
        if (lobby == null) return null;
        return lobby.spec;
    }

    @Override
    public ConnectionWriter getWriter() {
        return writer;
    }

    @Override
    public void setMessageHandler(GamePlayerMessageHandler handler) {
        this.messageHandler = handler;
    }

    @Override
    public void ticked() {
        /* Currently, messages are already handled. */
    }

    GamePlayerMessageHandler getMessageHandler() {
        return messageHandler;
    }
}
