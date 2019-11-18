package common.app;

import common.msg.Message;
import common.msg.NoExceptionsConnectionWriter;

public interface PlayerAiInterface {
    void handleMessage(Message message);
    void writeActions(NoExceptionsConnectionWriter requester);
}
