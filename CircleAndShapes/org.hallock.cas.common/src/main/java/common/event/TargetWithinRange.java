package common.event;

import common.state.EntityId;
import common.util.json.*;

import java.io.IOException;

public class TargetWithinRange extends NetworkAiEvent {
    public final EntityId listener;
    public final EntityId target;
    public final double range;

    public TargetWithinRange(EntityId listener, EntityId target, double range) {
        super(listener, AiEventType.TargetWithinRange);
        this.listener = listener;
        this.target = target;
        this.range = range;
    }

    public static TargetWithinRange finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec, EntityId entityId) throws IOException {
        return new TargetWithinRange(
            entityId,
            reader.read("target", EntityId.Serializer, spec),
            reader.readDouble("range")
        );
    }

    @Override
    void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.write("target", target, EntityId.Serializer, options);
        writer.write("range", range);
    }
}
