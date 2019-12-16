package common.state.sst.sub;

import common.util.json.*;

import java.io.IOException;

public enum TerrainType {
    Water,
    Grass,
    Sahara,


    ;

    public static DataSerializer<TerrainType> Serializer = new DataSerializer<TerrainType>() {
        @Override
        public void write(TerrainType value, JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write(value.ordinal());
        }

        @Override
        public TerrainType parse(JsonReaderWrapperSpec reader, ReadOptions opts) throws IOException {
            return reader.b(TerrainType.values(), reader.readInt32());
        }
    };
}
