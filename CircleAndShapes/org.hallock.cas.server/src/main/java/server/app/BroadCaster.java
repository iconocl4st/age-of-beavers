package server.app;

import common.msg.ConnectionWriter;
import common.msg.Message;
import common.state.Player;

public interface BroadCaster {
    void broadCast(Message msg);
    void flush();
    void send(Player losPlayer, Message unitRemoved);


    interface OnError {
        void handleError(ConnectionWriter writer, Throwable t);
    }
}
