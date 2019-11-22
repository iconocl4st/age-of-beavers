package common.event;

import common.state.EntityId;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;

public class ResourcesChanged extends NetworkAiEvent {
    public ResourcesChanged(EntityId entity) {
        super(entity, AiEventType.ResourceChange);
    }

    public static common.event.ResourcesChanged finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec, EntityId entityId) {
        return new common.event.ResourcesChanged(entityId);
    }

    @Override
    protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) {}
}
