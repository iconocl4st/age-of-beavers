package common.util.json;

import java.io.IOException;
import java.io.Serializable;

public interface Jsonable extends Serializable {
    void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException;
}
