package server.app;

import common.msg.ConnectionWriter;
import common.msg.Message;
import common.state.Player;

import java.util.HashMap;

public class DefaultBroadCaster implements BroadCaster {
    private final ConnectionWriter[] filters;
    private final HashMap<Player, ConnectionWriter> filtersByPlayer;
    private final OnError onError;

    public DefaultBroadCaster(ConnectionWriter[] filters, HashMap<Player, ConnectionWriter> filtersByPlayer, OnError onError) {
        this.filters = filters;
        this.filtersByPlayer = filtersByPlayer;
        this.onError = onError;
    }

    @Override
    public void broadCast(Message msg) {
        for (ConnectionWriter filter : filters) {
            try {
                filter.send(msg);
            } catch (Throwable e) {
                onError.handleError(filter, e);
            }
        }
    }

    @Override
    public void flush() {
        for (ConnectionWriter writer : filters) {
            try {
                writer.flush();
            } catch (Throwable e) {
                onError.handleError(writer, e);
            }
        }
    }

    @Override
    public void send(Player losPlayer, Message unitRemoved) {
        ConnectionWriter messageFilter = filtersByPlayer.get(losPlayer);
        if (messageFilter == null)
            return;
        try {
            messageFilter.send(unitRemoved);
        } catch (Throwable e) {
            onError.handleError(messageFilter, e);
        }
    }
}
