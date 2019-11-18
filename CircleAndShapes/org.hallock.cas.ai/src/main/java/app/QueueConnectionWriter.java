package app;

import common.msg.Message;
import common.msg.NoExceptionsConnectionWriter;

import java.util.LinkedList;

public class QueueConnectionWriter implements NoExceptionsConnectionWriter {

    private final LinkedList<Message> messages = new LinkedList<>();

    @Override
    public boolean send(Message message) {
        messages.addLast(message);
        return true;
    }

    void writeTo(NoExceptionsConnectionWriter requester) {
        for (Message msg : messages) {
            requester.send(msg);
        }
    }

    public void exportActions() {
        // TODO: make a big array...
    }
}
