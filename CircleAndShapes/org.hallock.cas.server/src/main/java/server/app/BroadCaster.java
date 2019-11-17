package server.app;

import common.msg.Message;
import common.state.Player;

public interface BroadCaster {
    void broadCast(Message msg);

    void send(Player losPlayer, Message unitRemoved);
}
