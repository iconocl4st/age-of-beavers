package common.util.json;

import java.io.IOException;

public interface Jsonable {
    void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException;
}
