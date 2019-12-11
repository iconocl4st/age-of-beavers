package common.state.sst.sub;

import common.event.GrowthStageChanged;
import common.util.json.*;

import java.io.IOException;

public class GrowthInfo implements Jsonable {

    GrowthStageChanged.GrowthStage currentState;


    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {

    }

    public static DataSerializer<GrowthInfo> Serializer = new DataSerializer.JsonableSerializer<GrowthInfo>() {
        @Override
        public GrowthInfo parse(JsonReaderWrapperSpec reader, ReadOptions opts) throws IOException {
            return null;
        }
    };
}
