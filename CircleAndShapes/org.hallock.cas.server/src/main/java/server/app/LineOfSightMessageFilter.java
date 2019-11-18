package server.app;

import common.msg.ConnectionWriter;
import common.msg.Message;
import common.state.Player;
import server.state.ServerGameState;

import java.awt.*;
import java.io.IOException;

public class LineOfSightMessageFilter implements ConnectionWriter {
    private final ConnectionWriter delegate;
    private final Player player;
    private final ServerGameState gameState;

    LineOfSightMessageFilter(ServerGameState gameState, ConnectionWriter delegate, Player player) {
        if (gameState == null) {
            throw new NullPointerException();
        }
        this.gameState = gameState;
        this.delegate = delegate;
        this.player = player;
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void send(Message msg) throws IOException {
        Point location = null;
        switch (msg.getMessageType()) {
            case UNIT_REMOVED:
                location = gameState.state.locationManager.getLocation(((Message.UnitRemoved) msg).unitId).toPoint();
                break;
            case UNIT_UPDATED:
                location = gameState.state.locationManager.getLocation(((Message.UnitUpdated) msg).unitId).toPoint();
                break;
            case OCCUPANCY_UPDATED:
                location = ((Message.OccupancyChanged) msg).location;
            case AI_EVENT:  // to do
                break;
        }
        if (location != null && !gameState.state.lineOfSight.isVisible(player, location.x, location.y)) {
            return;
        }
        delegate.send(msg);
    }
}
