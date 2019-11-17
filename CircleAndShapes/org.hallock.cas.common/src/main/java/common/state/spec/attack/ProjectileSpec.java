package common.state.spec.attack;

import common.util.json.*;

import java.io.IOException;

public class ProjectileSpec implements Jsonable {
    public final double radius;
    public final double speed;
    public final double projectileRange;
    public final boolean stopsOnFirst;

    public ProjectileSpec(double radius, double speed, double projectileRange, boolean stopsOnFirst) {
        this.radius = radius;
        this.speed = speed;
        this.projectileRange = projectileRange;
        this.stopsOnFirst = stopsOnFirst;
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("radius", radius);
        writer.write("speed", speed);
        writer.write("range", projectileRange);
        writer.write("stops-on-first-hit", stopsOnFirst);
        writer.writeEndDocument();
    }

    public static final DataSerializer<ProjectileSpec> Serializer = new DataSerializer.JsonableSerializer<ProjectileSpec>() {
        @Override
        public ProjectileSpec parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            ProjectileSpec pspec = new ProjectileSpec(
                    reader.readDouble("radius"),
                    reader.readDouble("speed"),
                    reader.readDouble("range"),
                    reader.readBoolean("stops-on-first-hit")
            );
            reader.readEndDocument();
            return pspec;
        }
    };
}
