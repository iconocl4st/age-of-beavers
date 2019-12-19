package common.event;

import common.state.EntityId;
import common.util.json.*;

import java.io.IOException;

public abstract class NetworkAiEvent extends AiEvent implements Jsonable {
    NetworkAiEvent(EntityId entity, AiEventType type) {
        super(entity, type);
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("event-type", type.ordinal());
        writer.write("entity", entity, EntityId.Serializer, options);
        writeInnards(writer, options);
        writer.writeEndDocument();
    }

    abstract void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException;

    public static final DataSerializer<NetworkAiEvent> Serializer = new DataSerializer.JsonableSerializer<NetworkAiEvent>() {
        @Override
        public NetworkAiEvent parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            AiEventType type = reader.b(AiEventType.values(), reader.readInt32("event-type"));
            EntityId entityId = reader.read("entity", EntityId.Serializer, spec);

            NetworkAiEvent event = null;
            switch (type) {
                case ActionCompleted: event = ActionCompleted.finishParsing(reader, spec, entityId); break;
                case GarrisonChange: event = GarrisonedChanged.finishParsing(reader, spec, entityId); break;
                case ResourceChange: event = ResourcesChanged.finishParsing(reader, spec, entityId); break;
                case TargetKilled: event = TargetKilled.finishParsing(reader, spec, entityId); break;
                case DemandsChanged: event = DemandsChanged.finishParsing(reader, spec, entityId); break;
                case BuildingPlacementChanged: event = BuildingPlacementChanged.finishParsing(reader, spec, entityId); break;
                case TargetWithinRange: event = TargetWithinRange.finishParsing(reader, spec, entityId); break;
                case ProductionComplete: event = ProductionComplete.finishParsing(reader, spec, entityId); break;
            }
            reader.readEndDocument();
            return event;
        }
    };
}
