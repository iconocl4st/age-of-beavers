package common.state.spec;

import common.util.json.*;

import java.awt.*;
import java.io.IOException;
import java.util.Comparator;

public class ResourceType {
    public final String name;
    public final int weight;
    public EntitySpec growsInto;
    public final Color minimapColor;
    public int ordinal;

    public ResourceType(String name, int weight, Color minimapColor) {
        this.weight = weight;
        this.name = name;
        this.growsInto = null;
        this.minimapColor = minimapColor;
    }

    public boolean equals(Object other) {
        if (!(other instanceof ResourceType))
            return false;
        ResourceType rt = (ResourceType) other;
        return rt.name.equals(this.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return name;
    }

    public static final Comparator<ResourceType> COMPARATOR = Comparator.comparing(r -> r.name);

    public static String formatWeight(int weight) {
        return String.format("%.2f", weight / (double) 100);
    }

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
            ResourceType rt = spec.spec().getResourceType(reader.readString("name"));
            reader.readEndDocument();
            return rt;
        }
    };
}
