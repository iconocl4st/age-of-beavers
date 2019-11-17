package common;

import common.state.EntityId;
import common.util.json.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public abstract class AiEvent implements Jsonable {

    public final EventType type;
    public final EntityId entity;

    private AiEvent(EntityId entity, EventType type) {
        this.entity = entity;
        this.type = type;
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("event-type", type.ordinal());
        writer.write("entity", entity, EntityId.Serializer, options);
        writeInnards(writer, options);
        writer.writeEndDocument();
    }

    protected abstract void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException;


    public static class ActionCompleted extends AiEvent {
        public final ActionCompletedReason reason;

        public ActionCompleted(EntityId entity, ActionCompletedReason reason) {
            super(entity, EventType.ActionCompleted);
            this.reason = reason;
        }

        public static ActionCompleted finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec, EntityId entityId) throws IOException {
            return new ActionCompleted(entityId, reader.b(ActionCompletedReason.values(), reader.readInt32("reason")));
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("reason", reason.ordinal());
        }
    }

    public static class TargetKilled extends AiEvent {
        public final List<EntityId> droppedUnits;

        public TargetKilled(EntityId entity, List<EntityId> droppedUnits) {
            super(entity, EventType.TargetKilled);
            this.droppedUnits = droppedUnits;
        }

        public static TargetKilled finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec, EntityId entityId) throws IOException {
            return new TargetKilled(entityId, (List<EntityId>) reader.read("dropped-units", spec, new LinkedList<>(), EntityId.Serializer));
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("dropped-units", droppedUnits, EntityId.Serializer, options);
        }
    }

//    public static class TargetWithinRange extends AiEvent {
//        public final EntityId listener;
//        public final EntityId target;
//        public final double range;
//
//        public TargetWithinRange(EntityId listener, EntityId target, double range) {
//            super(listener, EventType.TargetWithinRange);
//            this.listener = listener;
//            this.target = target;
//            this.range = range;
//        }
//    }


    public static class ResourcesChanged extends AiEvent {
        public ResourcesChanged(EntityId entity) {
            super(entity, EventType.ResourceChange);
        }

        public static ResourcesChanged finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec, EntityId entityId) {
            return new ResourcesChanged(entityId);
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) {}
    }

    public static class GarrisonedChanged extends AiEvent {
        public GarrisonedChanged(EntityId entity) {
            super(entity, EventType.GarrisonChange);
        }

        public static GarrisonedChanged finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec, EntityId entityId) {
            return new GarrisonedChanged(entityId);
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) {}
    }

    public enum EventType {
        TargetWithinRange,
        ActionCompleted,
        TargetKilled,
        GarrisonChange,
        ResourceChange,
    }



    // Could have had a no longer exists...
    public enum ActionCompletedReason {
        Invalid,
        OverCapacity,
        TooFar,
        Successful,
    }


    public static final DataSerializer<AiEvent> Serializer = new DataSerializer.JsonableSerializer<AiEvent>() {
        @Override
        public AiEvent parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            EventType type = reader.b(EventType.values(), reader.readInt32("event-type"));
            EntityId entityId = reader.read("entity", EntityId.Serializer, spec);

            AiEvent event = null;
            switch (type) {
                case ActionCompleted: event = ActionCompleted.finishParsing(reader, spec, entityId); break;
                case GarrisonChange: event = GarrisonedChanged.finishParsing(reader, spec, entityId); break;
                case ResourceChange: event = ResourcesChanged.finishParsing(reader, spec, entityId); break;
                case TargetKilled: event = TargetKilled.finishParsing(reader, spec, entityId); break;
//                case TargetWithinRange: event = TargetWithinRange.finishParsing(reader, spec, entityId); break;
            }
            reader.readEndDocument();
            return event;
        }
    };
}
