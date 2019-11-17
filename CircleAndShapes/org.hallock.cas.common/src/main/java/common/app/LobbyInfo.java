package common.app;

import common.util.json.*;

import java.io.IOException;

public class LobbyInfo implements Jsonable {
    public final LobbyStatus status;
    public final String name;
    public final int size;

    public LobbyInfo(LobbyStatus status, String name, int length) {
        this.status = status;
        this.name = name;
        this.size = length;
    }

    public String toString() {
        return name + ": " + status.name() + ", " + size;
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("status", status.ordinal());
        writer.write("name", name);
        writer.write("size", size);
        writer.writeEndDocument();
    }

    public enum LobbyStatus {
        Waiting,
        InGame,
    }


    public static final DataSerializer<LobbyInfo> Serializer = new DataSerializer.JsonableSerializer<LobbyInfo>() {
        @Override
        public LobbyInfo parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            LobbyStatus status = reader.b(LobbyStatus.values(), reader.readInt32("status"));
            String name = reader.readString("name");
            int size = reader.readInt32("size");
            reader.readEndDocument();
            return new LobbyInfo(status, name, size);
        }
    };
}
