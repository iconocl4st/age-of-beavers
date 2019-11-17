package common.state.sst.sub.capacity;

import common.state.spec.GameSpec;
import common.state.spec.ResourceType;
import common.state.sst.sub.Load;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;

import java.io.IOException;

public class CapableCapacitySpec extends CapacitySpec {

    public CapableCapacitySpec() {
        super(CapacitySpecType.Capable);
    }

    @Override
    public boolean hasRoomFor(Load load, ResourceType resource) {
        return true;
    }

    public int amountPossibleToAccept(Load load, ResourceType resource) {
        return Integer.MAX_VALUE;
    }

    public String toString() {
        return "inf";
    }

    @Override
    public void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {}

    public static CapacitySpec finishReading(JsonReaderWrapperSpec reader, ReadOptions spec) {
        return new CapableCapacitySpec();
    }
}