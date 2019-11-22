package common.event;

import common.state.EntityId;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;

public class GarrisonedChanged extends NetworkAiEvent {
    public GarrisonedChanged(EntityId entity) {
        super(entity, AiEventType.GarrisonChange);
    }

    public static common.event.GarrisonedChanged finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec, EntityId entityId) {
        return new common.event.GarrisonedChanged(entityId);
    }

    @Override
    protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) {}
}
