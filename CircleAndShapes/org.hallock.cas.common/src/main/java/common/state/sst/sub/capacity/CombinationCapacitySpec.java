package common.state.sst.sub.capacity;

import common.state.spec.ResourceType;
import common.state.sst.sub.Load;
import common.util.json.*;

import java.io.IOException;

public class CombinationCapacitySpec extends CapacitySpec {
    final CapacitySpec spec1;
    final CapacitySpec spec2;

    public CombinationCapacitySpec(CapacitySpec spec1, CapacitySpec spec2) {
        super(CapacitySpecType.Combination);
        this.spec1 = spec1;
        this.spec2 = spec2;
    }

    @Override
    public boolean hasRoomFor(Load load, ResourceType resource) {
        return spec1.hasRoomFor(load, resource) && spec2.hasRoomFor(load, resource);
    }

    @Override
    public int amountPossibleToAccept(Load load, ResourceType resource) {
        return Math.min(
                spec1.amountPossibleToAccept(load, resource),
                spec2.amountPossibleToAccept(load, resource)
        );
    }

    public String toString() {
        return spec1 + " and " + spec2;
    }


    @Override
    public void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.write("spec1", spec1, CapacitySpec.Serializer, options);
        writer.write("spec2", spec2, CapacitySpec.Serializer, options);
    }

    public static CapacitySpec finishReading(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
        CapacitySpec spec1 = reader.read("spec1", CapacitySpec.Serializer, spec);
        CapacitySpec spec2 = reader.read("spec2", CapacitySpec.Serializer, spec);
        return new CombinationCapacitySpec(spec1, spec2);
    }
}