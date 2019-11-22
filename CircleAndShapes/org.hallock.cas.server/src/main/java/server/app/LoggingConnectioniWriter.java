package server.app;

import common.msg.ConnectionWriter;
import common.msg.Message;

import java.io.IOException;

public class LoggingConnectioniWriter implements ConnectionWriter {
    private final ConnectionWriter delegate;
    private long lastMessage;
    private int messageCount;

    public LoggingConnectioniWriter(ConnectionWriter delegate) {
        this.delegate = delegate;
    }

    @Override
    synchronized public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    synchronized public void send(Message message) throws IOException {
        ++messageCount;
        delegate.send(message);
        checkCount();
    }

    private void checkCount() {
        long now = System.currentTimeMillis();
        if (now - lastMessage > 10000) {
            System.out.println("Sent " + (messageCount / 10.0) + " messages per second.");
            lastMessage = now;
            messageCount = 0;
        }
    }
}
