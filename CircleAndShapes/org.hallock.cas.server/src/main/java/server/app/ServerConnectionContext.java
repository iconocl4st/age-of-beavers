package server.app;

import common.msg.ConnectionWriter;
import common.state.spec.GameSpec;
import common.state.sst.GameState;

import java.io.IOException;

public class ServerConnectionContext implements PlayerConnection {
    private final ConnectionWriter writer;
    private GamePlayerMessageHandler messageHandler;
    private Lobby lobby;

    ServerConnectionContext(ConnectionWriter writer) {
        this.writer = new LoggingConnectioniWriter(writer);
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

    void launch() throws IOException {
        if (lobby == null) {
            throw new RuntimeException("Not in a lobby!");
        }
        lobby.launch();
    }

    public GameState getCurrentGameState() {
        if (lobby == null) return null;
        return lobby.getCurrentGameState();
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
