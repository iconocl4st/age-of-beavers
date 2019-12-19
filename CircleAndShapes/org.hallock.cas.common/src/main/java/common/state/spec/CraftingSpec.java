package common.state.spec;

import common.util.json.*;

import java.io.IOException;
import java.util.Map;

public class CraftingSpec implements Jsonable {

    public final Map<ResourceType, Integer> inputs;
    public final Map<ResourceType, Integer> outputs;
    public final SpecTree.SpecNodeReference reference;

    public CraftingSpec(Map<ResourceType, Integer> inputs, Map<ResourceType, Integer> outputs, SpecTree.SpecNodeReference reference) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.reference = reference;
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("reference", reference, SpecTree.SpecNodeReference.Serializer, options);
        writer.writeEndDocument();
    }

    public static final DataSerializer<CraftingSpec> Serializer = new DataSerializer.JsonableSerializer<CraftingSpec>() {
        @Override
        public CraftingSpec parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            SpecTree.SpecNodeReference reference = reader.read("reference", SpecTree.SpecNodeReference.Serializer, spec);
            reader.readEndDocument();
            return reference.entity.canCraft.get(reference.path).getValue();
        }
    };

}
