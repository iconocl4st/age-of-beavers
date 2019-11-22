package common.event;

import common.state.EntityId;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;

import java.io.IOException;

public class BuildingPlacementChanged extends NetworkAiEvent {
    public final EntityId constructionZone;
    public final EntityId newBuildingId;

    public BuildingPlacementChanged(EntityId c, EntityId b) {
        super(c == null ? b : c, AiEventType.BuildingPlacementChanged);
        constructionZone = c;
        newBuildingId = b;
    }

    public static common.event.BuildingPlacementChanged finishParsing(JsonReaderWrapperSpec reader, ReadOptions opt, EntityId entityId) throws IOException {
        return new common.event.BuildingPlacementChanged(
                reader.read("construction-zone", EntityId.Serializer, opt),
                reader.read("building", EntityId.Serializer, opt)
        );
    }

    @Override
    protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.write("construction-zone", constructionZone, EntityId.Serializer, options);
        writer.write("building", newBuildingId, EntityId.Serializer, options);
    }
}
