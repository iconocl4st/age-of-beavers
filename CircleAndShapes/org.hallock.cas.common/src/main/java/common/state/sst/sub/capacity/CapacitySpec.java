package common.state.sst.sub.capacity;

import common.state.spec.ResourceType;
import common.state.sst.sub.Load;
import common.util.json.*;

import java.io.IOException;

public abstract class CapacitySpec implements Jsonable {

    private final CapacitySpecType type;

    protected CapacitySpec(CapacitySpecType type) {
        this.type = type;
    }

    public abstract boolean hasRoomFor(Load load, ResourceType resource);

    public abstract int amountPossibleToAccept(Load load, ResourceType resource);

    public abstract void writeInnards(JsonWriterWrapperSpec wrapper, WriteOptions options) throws IOException;

    public String getDisplayString() { return toString(); }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("type", type.ordinal());
        writeInnards(writer, options);
        writer.writeEndDocument();
    }

    // todo
//    String getDisplayString();

    public enum CapacitySpecType {
        Capable,
        CarryLimit,
        Combination,
        Inacapable,
        Prioritized,
        Simple
    }


    public static final DataSerializer<CapacitySpec> Serializer = new DataSerializer.JsonableSerializer<CapacitySpec>() {
        @Override
        public CapacitySpec parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            CapacitySpec ret = null;
            reader.readBeginDocument();
            switch (reader.b(CapacitySpecType.values(), reader.readInt32("type"))) {
                case Simple: ret = SimpleCapacitySpec.finishReading(reader, spec); break;
                case Capable: ret = CapableCapacitySpec.finishReading(reader, spec); break;
                case CarryLimit: ret = CarryLimitCapacitySpec.finishReading(reader, spec); break;
                case Combination: ret = CombinationCapacitySpec.finishReading(reader, spec); break;
                case Inacapable: ret = InCapableCapacitySpec.finishReading(reader, spec); break;
                case Prioritized: ret = PrioritizedCapacitySpec.finishReading(reader, spec); break;
            }
            reader.readEndDocument();
            return ret;
        }
    };
}