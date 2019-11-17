package common.msg;


import common.util.json.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class ConnectionWriter {
    private final Object sync = new Object();
    private final ObjectOutputStream outputStream;
    private final JsonWriterWrapperSpec jsonWriter;
    private final WriteOptions options = new WriteOptions();

    public ConnectionWriter(ObjectOutputStream outputStream) {
        this.jsonWriter = null;
        this.outputStream = outputStream;
    }

    public ConnectionWriter(JsonWriterWrapperSpec outputStream) {
        this.jsonWriter = outputStream;
        this.outputStream = null;
    }

    public void flush() throws IOException {
        synchronized (sync) {
            if (outputStream == null) {
                jsonWriter.flush();
            } else {
                outputStream.flush();
                outputStream.reset();
            }
        }
    }

    public void send(Message message) throws IOException {
        synchronized (sync) {
            if (outputStream == null) {
                message.writeFromStart(jsonWriter, options);
                jsonWriter.writeBeginDocument();
            } else {
                outputStream.writeObject(message);
                outputStream.flush();
                outputStream.reset();
            }
        }
    }
}
