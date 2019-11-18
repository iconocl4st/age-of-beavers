package common.msg;


import common.util.json.JsonWriterWrapperSpec;
import common.util.json.WriteOptions;

import java.io.IOException;

public class NetworkConnectionWriter implements ConnectionWriter {
    private final Object sync = new Object();
    private final JsonWriterWrapperSpec jsonWriter;
    private final WriteOptions options = new WriteOptions();

    public NetworkConnectionWriter(JsonWriterWrapperSpec outputStream) {
        this.jsonWriter = outputStream;
    }

    @Override
    public void flush() throws IOException {
        synchronized (sync) {
            jsonWriter.flush();
        }
    }

    @Override
    public void send(Message message) throws IOException {
        synchronized (sync) {
            message.writeFromStart(jsonWriter, options);
            jsonWriter.writeBeginDocument();
        }
    }
}
