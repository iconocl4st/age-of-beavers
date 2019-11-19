package app;

import common.msg.Message;
import common.msg.NoExceptionsConnectionWriter;

import java.util.LinkedList;
import java.util.List;

public class QueueConnectionWriter implements NoExceptionsConnectionWriter {

    private final LinkedList<Message> messages = new LinkedList<>();

    @Override
    public boolean send(Message message) {
        synchronized (messages) {
            messages.addLast(message);
        }
        return true;
    }

    void writeTo(NoExceptionsConnectionWriter requester) {
        List<Message> toWrite;
        synchronized (messages) {
            toWrite = (List<Message>)  messages.clone();
            messages.clear();
        }
        for (Message msg : toWrite) {
            requester.send(msg);
        }
    }

    public void exportActions() {
        // TODO: make a big array...
    }
}
