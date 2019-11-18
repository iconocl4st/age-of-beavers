package common.msg;

import java.io.IOException;

public interface ConnectionWriter {
    void flush() throws IOException;

    void send(Message message) throws IOException;
}
