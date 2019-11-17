package common.state.spec;

import common.util.json.*;

import java.io.IOException;

public class CarrySpec implements Jsonable {
    public ResourceType type;
    public int startingQuantity;

    public CarrySpec(ResourceType resourceType, int quantity) {
        this.type = resourceType;
        this.startingQuantity = quantity;
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("resource", type, ResourceType.Serializer, options);
        writer.write("initial-quantity", startingQuantity);
        writer.writeEndDocument();
    }

    public static final DataSerializer<CarrySpec> Serializer = new DataSerializer.JsonableSerializer<CarrySpec>() {
        @Override
        public CarrySpec parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            ResourceType r = reader.read("resource", ResourceType.Serializer, spec);
            int quantity = reader.readInt32("initial-quantity");
            reader.readEndDocument();
            return new CarrySpec(r, quantity);
        }
    };
}
