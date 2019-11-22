package common.event;

import common.state.EntityId;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class TargetKilled extends NetworkAiEvent {
    public final List<EntityId> droppedUnits;

    public TargetKilled(EntityId entity, List<EntityId> droppedUnits) {
        super(entity, AiEventType.TargetKilled);
        this.droppedUnits = droppedUnits;
    }

    public static common.event.TargetKilled finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec, EntityId entityId) throws IOException {
        return new common.event.TargetKilled(entityId, (List<EntityId>) reader.read("dropped-units", new LinkedList<>(), EntityId.Serializer, spec));
    }

    @Override
    protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.write("dropped-units", droppedUnits, EntityId.Serializer, options);
    }
}
