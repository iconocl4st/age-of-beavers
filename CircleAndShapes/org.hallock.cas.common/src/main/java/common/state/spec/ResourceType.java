package common.state.spec;

import common.util.json.*;

import java.io.IOException;
import java.io.Serializable;

public class ResourceType implements Serializable {
    public final int weight;
    public final String name;

    public ResourceType(String name, int weight) {
        this.weight = weight;
        this.name = name;
    }

    public boolean equals(Object other) {
        if (!(other instanceof ResourceType)) return false;
        ResourceType rt = (ResourceType) other;
        return rt.name.equals(this.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return name;
    }

    public static final DataSerializer<ResourceType> EntireSerializer = new DataSerializer<ResourceType>(){
        @Override
        public void write(ResourceType value, JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.writeBeginDocument();
            writer.write("name", value.name);
            writer.write("weight", value.weight);
            writer.writeEndDocument();
        }

        @Override
        public ResourceType parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            ResourceType rt = new ResourceType(
                reader.readString("name"),
                reader.readInt32("weight")
            );
            reader.readEndDocument();
            return rt;
        }
    };

    public static final DataSerializer<ResourceType> Serializer = new DataSerializer<ResourceType>() {
        @Override
        public void write(ResourceType value, JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.writeBeginDocument();
            writer.write("name", value.name);
            writer.writeEndDocument();
        }

        @Override
        public ResourceType parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            ResourceType rt = spec.spec.getResourceType(reader.readString("name"));
            reader.readEndDocument();
            return rt;
        }
    };
}
