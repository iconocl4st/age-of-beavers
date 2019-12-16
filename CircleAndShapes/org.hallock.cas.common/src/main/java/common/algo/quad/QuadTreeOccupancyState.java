package common.algo.quad;

import common.util.json.*;

import java.io.IOException;

public enum QuadTreeOccupancyState {
    Occupied,
    Empty,



    //    UnitOccupied,  I wish...
    // cosntruction zones are only visible if they have resources
    // otherwise you can build on them, and walk  on them
    ConstructionOccupied,




    ;

    public static DataSerializer<QuadTreeOccupancyState> Serializer = new DataSerializer<QuadTreeOccupancyState>() {
        @Override
        public void write(QuadTreeOccupancyState value, JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write(value.ordinal());
        }

        @Override
        public QuadTreeOccupancyState parse(JsonReaderWrapperSpec reader, ReadOptions opts) throws IOException {
            return reader.b(QuadTreeOccupancyState.values(), reader.readInt32());
        }
    };
}
