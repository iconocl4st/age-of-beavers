package common.util.json;

import java.io.IOException;

public final class EmptyJsonable implements Jsonable {
    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.writeEndDocument();
    }
}
