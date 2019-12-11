package common.event;

import common.state.EntityId;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;

import java.io.IOException;

public class ProductionComplete extends NetworkAiEvent {
    public final EntityId created;

    public ProductionComplete(EntityId entity, EntityId createdId) {
        super(entity, AiEventType.ProductionComplete);
        this.created = createdId;
    }

    @Override
    void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.write("created", created, EntityId.Serializer,  options);
    }

    public static ProductionComplete finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec, EntityId entityId) throws IOException {
        return new ProductionComplete(entityId, reader.read("created", EntityId.Serializer, spec));
    }
}
