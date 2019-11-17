package server.app;

import common.msg.Message;
import common.state.Player;
import server.state.ServerGameState;

import java.awt.*;

// todo: broadcasat package
public class SelectiveBroadcaster implements BroadCaster {

    private final BroadCaster delegate;
    private final Player[] players;
    private final ServerGameState gameState;

    public SelectiveBroadcaster(ServerGameState gameState, int numPlayers, BroadCaster delegate) {
        if (gameState == null) {
            throw new NullPointerException("");
        }
        this.gameState = gameState;
        this.delegate = delegate;

        players = new Player[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            players[i] = new Player(i + 1);
        }
    }

    @Override
    public void broadCast(Message msg) {
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
        for (Player player : players) {
            if (location != null && !gameState.state.lineOfSight.isVisible(player, location.x, location.y)) {
                continue;
            }
            delegate.send(player, msg);
        }
    }

    @Override
    public void send(Player losPlayer, Message unitRemoved) {
        if (losPlayer.number < 0) {
            System.out.println("here");
        }
        delegate.send(losPlayer, unitRemoved);
    }
}
