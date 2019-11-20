package common.app;

import common.msg.Message;
import common.msg.NoExceptionsConnectionWriter;

import java.awt.geom.Rectangle2D;
import java.util.List;

public interface PlayerAiInterface {
    void handleMessage(Message message);
    void writeActions(NoExceptionsConnectionWriter requester);
}
