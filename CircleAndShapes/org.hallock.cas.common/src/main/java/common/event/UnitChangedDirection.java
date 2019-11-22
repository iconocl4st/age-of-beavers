package common.event;

import common.state.EntityId;
import common.util.DPoint;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;

import java.io.IOException;

public class UnitChangedDirection extends NetworkAiEvent {
    public final DPoint currentLocation;
    public final DPoint endLocation;
    public final double speed;

    public UnitChangedDirection(EntityId entity, DPoint currentLocation, DPoint endLocation, double speed) {
        super(entity, AiEventType.UnitChangedDirection);
        this.endLocation = endLocation;
        this.currentLocation = currentLocation;
        this.speed = speed;
    }

//    public DPoint getDirection() {
//        double dx = endLocation.x - currentLocation.x;
//        double dy = endLocation.y - currentLocation.y;
//        double n = Math.sqrt(dx * dx + dy * dy);
//        return new DPoint(dx / n, dy / n);
//    }

    @Override
    void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.write("location-begin", currentLocation, DPoint.Serializer, options);
        writer.write("location-end", currentLocation, DPoint.Serializer, options);
        writer.write("speed", speed);
    }

    static NetworkAiEvent finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec, EntityId entityId) throws IOException {
        return new UnitChangedDirection(
                entityId,
                reader.read("location-begin", DPoint.Serializer, spec),
                reader.read("location-end", DPoint.Serializer, spec),
                reader.readDouble("speed")
        );
    }
}
