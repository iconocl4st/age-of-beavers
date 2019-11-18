package client.ai;

import common.msg.ConnectionWriter;
import common.msg.Message;
import common.msg.NoExceptionsConnectionWriter;
import common.util.ExecutorServiceWrapper;

public class ResponsiveConnectionWriter implements NoExceptionsConnectionWriter {

    private final ConnectionWriter writer;
    private final ExecutorServiceWrapper executorService;

    public ResponsiveConnectionWriter(ConnectionWriter delegate, ExecutorServiceWrapper executorService) {
        this.writer = delegate;
        this.executorService = executorService;
    }

    @Override
    public boolean send(Message message) {
        executorService.submit(() -> {
            try {
                writer.send(message);
                writer.flush();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        return true;
    }
}
