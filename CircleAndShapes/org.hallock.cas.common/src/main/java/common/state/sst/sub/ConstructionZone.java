package common.state.sst.sub;

import common.state.spec.EntitySpec;
import common.util.DPoint;
import common.util.json.*;

import java.io.IOException;

public final class ConstructionZone implements Jsonable {
    public final EntitySpec constructionSpec;
    public final DPoint location;
    public double progress;

    public ConstructionZone(EntitySpec spec, DPoint location) {
        this.constructionSpec = spec;
        this.progress = 0.0;
        this.location = location;
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("construction-specification", constructionSpec, EntitySpec.Serializer, options);
        writer.write("location", location, DPoint.Serializer, options);
        writer.write("progress", progress);
        writer.writeEndDocument();
    }

    // TODO: have humans claim they will provide a certain amount of the resources.
//        HashMap<EntityId, ResourceAllocation> called;

    //    public static final class ResourceAllocation {
//        ResourceType type;
//        double quantity;
//}



    public static final DataSerializer<ConstructionZone> Serializer = new DataSerializer.JsonableSerializer<ConstructionZone>() {
        @Override
        public ConstructionZone parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();

            ConstructionZone zone = new ConstructionZone(
                    reader.read("construction-specification", EntitySpec.Serializer, spec),
                    reader.read("location", DPoint.Serializer, spec)
            );
            zone.progress = reader.readDouble("progress");
            reader.readEndDocument();
            return zone;
        }
    };
}
