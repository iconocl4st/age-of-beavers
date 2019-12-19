package common.state.sst.sub;

import common.event.GrowthStage;
import common.util.json.*;

import java.io.IOException;

public class GrowthInfo implements Jsonable {
    public GrowthStage currentState;
    public double progress;
    public int tendedCount;

    public GrowthInfo(GrowthStage b, int tendedCount, double growthProgress) {
        this.currentState = b;
        this.tendedCount = tendedCount;
        this.progress = growthProgress;
    }

    public String toString() {
        return currentState.name() + ", " + String.format("%.2f", progress) + ", " + tendedCount;
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("growth-state", currentState.ordinal());
        writer.write("tended-count", tendedCount);
        writer.write("current-progress", progress);
        writer.writeEndDocument();
    }

    public static DataSerializer<GrowthInfo> Serializer = new DataSerializer.JsonableSerializer<GrowthInfo>() {
        @Override
        public GrowthInfo parse(JsonReaderWrapperSpec reader, ReadOptions opts) throws IOException {
            reader.readBeginDocument();
            GrowthStage b = reader.b(GrowthStage.values(), reader.readInt32("growth-state"));
            int tendedCount = reader.readInt32("tended-count");
            Double aDouble = reader.readDouble("current-progress");
            reader.readEndDocument();
            return new GrowthInfo(b, tendedCount, aDouble);
        }
    };
}
