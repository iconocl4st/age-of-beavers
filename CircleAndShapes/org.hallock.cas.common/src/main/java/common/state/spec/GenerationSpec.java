package common.state.spec;

import common.util.Immutable;
import common.util.json.*;

import java.io.IOException;

public class GenerationSpec /* implements Jsonable */ {
    public final Immutable.ImmutableList<ResourceGen> resources;
    public final Immutable.ImmutableList<UnitGen> gaia;
    public final Immutable.ImmutableList<ResourceGen> perPlayerResources;
    public final Immutable.ImmutableList<UnitGen> perPlayerUnits;

    public GenerationSpec(
            Immutable.ImmutableList<ResourceGen> resources,
            Immutable.ImmutableList<UnitGen> gaia,
            Immutable.ImmutableList<ResourceGen> perPlayerResources,
            Immutable.ImmutableList<UnitGen> perPlayerUnits
    ) {
        this.resources = resources;
        this.gaia = gaia;
        this.perPlayerResources = perPlayerResources;
        this.perPlayerUnits = perPlayerUnits;
    }

    public static final class ResourceGen /* implements Jsonable */ {
        public final EntitySpec type;
        public final int numberOfPatches;
        public final int patchSize;

        public ResourceGen(EntitySpec type, int numberOfPatches, int patchSize) {
            this.type = type;
            this.numberOfPatches = numberOfPatches;
            this.patchSize = patchSize;
        }

//        @Override
//        public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
//            writer.writeBeginDocument();
//            writer.write("type", type, EntitySpec.Serializer, options);
//            writer.write("num-patches", numberOfPatches);
//            writer.write("patch-size", patchSize);
//            writer.writeEndDocument();
//        }
//
//        public static DataSerializer<ResourceGen> Serializer = new DataSerializer.JsonableSerializer<ResourceGen>() {
//            @Override
//            public ResourceGen parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
//                reader.readBeginDocument();
//                ResourceGen r = new ResourceGen(type, numberOfPatches, patchSize);
//                r.type = reader.read("type", EntitySpec.Serializer, spec);
//                r.numberOfPatches = reader.readInt32("num-patches");
//                r.patchSize = reader.readInt32("patch-size");
//                reader.readEndDocument();
//                return r;
//            }
//        };
    }

    public static final class UnitGen /* implements Jsonable */{
        public final EntitySpec type;
        public final int number;

        public UnitGen(EntitySpec spec, int number) {
            type = spec;
            this.number = number;
        }

//        @Override
//        public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
//            writer.writeBeginDocument();
//            writer.write("type", type,  EntitySpec.Serializer, options);
//            writer.write("number", number);
//            writer.writeEndDocument();
//        }
//
//        public static DataSerializer<UnitGen> Serializer = new DataSerializer.JsonableSerializer<UnitGen>() {
//            @Override
//            public UnitGen parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
//                reader.readBeginDocument();
//                UnitGen r = new UnitGen();
//                r.type = reader.read("type", EntitySpec.Serializer, spec);
//                r.number = reader.readInt32("number");
//                reader.readEndDocument();
//                return r;
//            }
//        };
    }
//
//    @Override
//    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
//        writer.writeBeginDocument();
//        writer.write("resources", resources, ResourceGen.Serializer, options);
//        writer.write("gaia", gaia, UnitGen.Serializer, options);
//        writer.write("perPlayerUnits", perPlayerUnits, UnitGen.Serializer, options);
//        writer.writeEndDocument();
//    }
//
//    public static DataSerializer<GenerationSpec> Serializer = new DataSerializer.JsonableSerializer<GenerationSpec>() {
//        @Override
//        public GenerationSpec parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
//            reader.readBeginDocument();
//            GenerationSpec r = new GenerationSpec(resources, gaia, perPlayerResources, perPlayerUnits);
//            reader.read("resources", r.resources, ResourceGen.Serializer, spec);
//            reader.read("gaia", r.gaia, UnitGen.Serializer, spec);
//            reader.read("perPlayerUnits", r.perPlayerUnits, UnitGen.Serializer, spec);
//            reader.readEndDocument();
//            return r;
//        }
//    };
}