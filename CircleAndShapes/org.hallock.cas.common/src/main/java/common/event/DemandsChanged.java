package common.event;

import common.state.EntityId;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;

import java.io.IOException;

public class DemandsChanged extends NetworkAiEvent {
    public DemandsChanged(EntityId entity) {
        super(entity, AiEventType.DemandsChanged);
    }

    static DemandsChanged finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec, EntityId entityId) {
        return new common.event.DemandsChanged(entityId);
    }

    @Override
    protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {}
}
