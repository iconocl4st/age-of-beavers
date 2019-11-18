package server.app;

import common.msg.ConnectionWriter;

public interface PlayerConnection {
    ConnectionWriter getWriter();
    void setMessageHandler(GamePlayerMessageHandler handler);
    void ticked();
}
