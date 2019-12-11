package server.app;

import common.app.PlayerAiInterface;
import common.msg.ConnectionWriter;
import common.msg.Message;
import common.msg.NoExceptionsConnectionWriter;

public class AiConnection implements PlayerConnection {

    private final PlayerAiInterface context;
    private NoExceptionsConnectionWriter actionRequester;
    private TickCounter tickCounter =  new TickCounter(5);

    public AiConnection(PlayerAiInterface context) {
        this.context = context;
    }

    @Override
    public ConnectionWriter getWriter() {
        return new ConnectionWriter() {
            @Override
            public void flush() {}

            @Override
            public void send(Message message) {
                context.handleMessage(message);
            }
        };
    }

    @Override
    public void setMessageHandler(GamePlayerMessageHandler handler) {
        actionRequester = handler;
    }

    @Override
    public void ticked() {
        if (tickCounter.ticked()) {
            context.writeActions(actionRequester);
        }
    }
}
