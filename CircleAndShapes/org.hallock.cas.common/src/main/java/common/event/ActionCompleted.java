package common.event;

import common.state.EntityId;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;

import java.io.IOException;

public class ActionCompleted extends NetworkAiEvent {
    public final ActionCompletedReason reason;

    public ActionCompleted(EntityId entity, ActionCompletedReason reason) {
        super(entity, AiEventType.ActionCompleted);
        this.reason = reason;
    }

    public static common.event.ActionCompleted finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec, EntityId entityId) throws IOException {
        return new common.event.ActionCompleted(entityId, reader.b(ActionCompletedReason.values(), reader.readInt32("reason")));
    }

    @Override
    protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.write("reason", reason.ordinal());
    }

    // Could have had a no longer exists...
    public enum ActionCompletedReason {
        Invalid,
        OverCapacity,
        TooFar,
        Successful,
    }
}
