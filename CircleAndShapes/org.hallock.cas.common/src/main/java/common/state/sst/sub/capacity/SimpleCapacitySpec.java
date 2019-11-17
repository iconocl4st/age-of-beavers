package common.state.sst.sub.capacity;

import common.state.spec.GameSpec;
import common.state.spec.ResourceType;
import common.state.sst.sub.Load;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;

import java.io.IOException;

public class SimpleCapacitySpec extends CapacitySpec {
    private final int capacity;

    public SimpleCapacitySpec(int capacity) {
        super(CapacitySpecType.Simple);
        this.capacity = capacity;
    }

    @Override
    public boolean hasRoomFor(Load load, ResourceType resource) {
        return load.getWeight() + resource.weight < capacity;
    }

    @Override
    public int amountPossibleToAccept(Load load, ResourceType resource) {
        int carryingWeight = load.quantities.getOrDefault(resource, 0) * resource.weight;
        if (carryingWeight >= capacity) return 0;
        return (capacity - carryingWeight) / resource.weight;
    }

    public String toString() {
        return String.valueOf(capacity);
    }

    @Override
    public void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.write("capacity", capacity);
    }

    public static CapacitySpec finishReading(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
        return new SimpleCapacitySpec(reader.readInt32("capacity"));
    }
}
