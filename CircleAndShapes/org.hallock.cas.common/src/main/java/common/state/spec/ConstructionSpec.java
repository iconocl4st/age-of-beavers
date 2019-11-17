package common.state.spec;

import common.util.json.DataSerializer;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.WriteOptions;

import java.io.IOException;

public class ConstructionSpec extends EntitySpec {

    public final EntitySpec resultingStructure;

    public ConstructionSpec(EntitySpec resultingStructure, String name, String image) {
        super(name, image);
        this.resultingStructure = resultingStructure;
    }


//    @Override
//    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
//        writer.writeBeginDocument();
//        writer.write("resulting-structure", resultingStructure, EntitySpec.Serializer, options);
//        writer.writeEndDocument();
//    }

//    public static final DataSerializer<ConstructionSpec> Serializer = new DataSerializer.JsonableSerializer<ConstructionSpec>() {
//        @Override
//        public ConstructionSpec parse(JsonReaderWrapperSpec reader, GameSpec spec) throws IOException {
//            reader.readBeginDocument();
//            EntitySpec resultingStructure = reader.read("resulting-structure", spec, EntitySpec.Serializer).createConstructionSpec(spec);
//            reader.readEndDocument();
//            return (ConstructionSpec) resultingStructure;
//        }
//    };
}
