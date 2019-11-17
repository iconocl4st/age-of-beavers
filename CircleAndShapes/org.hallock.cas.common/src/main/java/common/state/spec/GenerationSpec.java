package common.state.spec;

import common.util.json.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class GenerationSpec implements Jsonable {
    public List<ResourceGen> resources = new LinkedList<>();
    public List<UnitGen> gaia = new LinkedList<>();
    public List<UnitGen> perPlayerUnits = new LinkedList<>();

    public static final class ResourceGen implements Jsonable {
        public EntitySpec type;
        public int numberOfPatches;
        public int patchSize;

        @Override
        public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.writeBeginDocument();
            writer.write("type", type, EntitySpec.Serializer, options);
            writer.write("num-patches", numberOfPatches);
            writer.write("patch-size", patchSize);
            writer.writeEndDocument();
        }

        public static DataSerializer<ResourceGen> Serializer = new DataSerializer.JsonableSerializer<ResourceGen>() {
            @Override
            public ResourceGen parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
                reader.readBeginDocument();
                ResourceGen r = new ResourceGen();
                r.type = reader.read("type", EntitySpec.Serializer, spec);
                r.numberOfPatches = reader.readInt32("num-patches");
                r.patchSize = reader.readInt32("patch-size");
                reader.readEndDocument();
                return r;
            }
        };

    }
    public static final class UnitGen implements Jsonable {
        public EntitySpec type;
        public int number;

        @Override
        public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.writeBeginDocument();
            writer.write("type", type,  EntitySpec.Serializer, options);
            writer.write("number", number);
            writer.writeEndDocument();
        }

        public static DataSerializer<UnitGen> Serializer = new DataSerializer.JsonableSerializer<UnitGen>() {
            @Override
            public UnitGen parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
                reader.readBeginDocument();
                UnitGen r = new UnitGen();
                r.type = reader.read("type", EntitySpec.Serializer, spec);
                r.number = reader.readInt32("number");
                reader.readEndDocument();
                return r;
            }
        };
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("resources", resources, ResourceGen.Serializer, options);
        writer.write("gaia", gaia, UnitGen.Serializer, options);
        writer.write("perPlayerUnits", perPlayerUnits, UnitGen.Serializer, options);
        writer.writeEndDocument();
    }

    public static DataSerializer<GenerationSpec> Serializer = new DataSerializer.JsonableSerializer<GenerationSpec>() {
        @Override
        public GenerationSpec parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            GenerationSpec r = new GenerationSpec();
            reader.read("resources", spec, r.resources, ResourceGen.Serializer);
            reader.read("gaia", spec, r.gaia, UnitGen.Serializer);
            reader.read("perPlayerUnits", spec, r.perPlayerUnits, UnitGen.Serializer);
            reader.readEndDocument();
            return r;
        }
    };
}