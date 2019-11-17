package common.msg;

import common.util.json.JsonReaderWrapperSpec;

import java.io.IOException;
import java.io.ObjectInputStream;

public class ConnectionReader {
    private final ObjectInputStream inputStream;
    private final JsonReaderWrapperSpec jsonInput;

    public ConnectionReader(ObjectInputStream inputStream) {
        this.inputStream = inputStream;
        this.jsonInput =  null;
    }
    public ConnectionReader(JsonReaderWrapperSpec inputStream) {
        this.inputStream = null;
        this.jsonInput =  inputStream;
    }

    public Message nextMessage() throws IOException, ClassNotFoundException {
        return (Message) this.inputStream.readObject();
    }
}