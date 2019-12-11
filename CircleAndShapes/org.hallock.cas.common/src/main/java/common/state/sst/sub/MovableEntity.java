package common.state.sst.sub;

import common.state.EntityReader;
import common.state.EntityId;
import common.state.sst.GameState;
import common.util.DPoint;
import common.util.json.*;

import java.awt.*;
import java.io.IOException;


public final class MovableEntity implements Jsonable {
    public EntityReader entity;
    public Dimension size;
    public DPoint movementBegin;
    public DPoint movementEnd;
    public DPoint currentLocation;
    public double movementSpeed;
    public double movementStartTime;

    public MovableEntity() {}

    public MovableEntity(GameState state, MovableEntity previous) {
        entity = new EntityReader(state, previous.entity.entityId);
        size = previous.size;
        movementBegin = previous.movementBegin;
        movementEnd = previous.movementEnd;
        currentLocation = previous.currentLocation;
        movementSpeed = previous.movementSpeed;
        movementStartTime = previous.movementStartTime;
    }

    public DPoint locationCenter() {
        return new DPoint(currentLocation.x + size.width / 2.0, currentLocation.y + size.height / 2.0);
    }

    public boolean equals(Object other) {
        return other instanceof MovableEntity && ((MovableEntity) other).entity.equals(entity);
    }

    public int hashCode() {
        return entity.hashCode();
    }


    public static MovableEntity createStationary(EntityReader reader, DPoint location) {
        MovableEntity entity = new MovableEntity();
        entity.entity = reader;
        entity.size = reader.getType().size;
        entity.currentLocation = entity.movementBegin = entity.movementEnd = location;
        entity.movementSpeed = 0d;
        entity.movementStartTime = reader.state.currentTime;
        return entity;
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("entity-id", entity.entityId, EntityId.Serializer, options);
        writer.write("size", size, DataSerializer.DimensionSerializer, options);
        writer.write("movement-start", movementBegin, DPoint.Serializer, options);
        writer.write("movement-end", movementEnd, DPoint.Serializer, options);
        writer.write("current-location", currentLocation, DPoint.Serializer, options);
        writer.write("movement-speed", movementSpeed);
        writer.write("movement-start-time", movementStartTime);
        writer.writeEndDocument();
    }

    public static DataSerializer<MovableEntity> Serializer = new DataSerializer.JsonableSerializer<MovableEntity>() {
        @Override
        public MovableEntity parse(JsonReaderWrapperSpec reader, ReadOptions options) throws IOException {
            MovableEntity de = new MovableEntity();

            reader.readBeginDocument();
            de.entity = new EntityReader(options.state, reader.read("entity-id", EntityId.Serializer, options));
            de.size = reader.read("size", DataSerializer.DimensionSerializer, options);
            de.movementBegin = reader.read("movement-start", DPoint.Serializer, options);
            de.movementEnd = reader.read("movement-end", DPoint.Serializer, options);
            de.currentLocation = reader.read("current-location", DPoint.Serializer, options);
            de.movementSpeed = reader.readDouble("movement-speed");
            de.movementStartTime = reader.readDouble("movement-start-time");
            reader.readEndDocument();

            return de;
        }
    };
}
