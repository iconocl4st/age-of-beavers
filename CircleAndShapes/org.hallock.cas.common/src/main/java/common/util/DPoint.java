package common.util;

import common.util.json.*;

import java.awt.*;
import java.io.IOException;

public class DPoint implements Jsonable {
    public final double x;
    public final double y;


    public DPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public DPoint() {
        this(0, 0);
    }

    public DPoint(Point next) {
        this(next.x, next.y);
    }

    public double distanceTo(DPoint other) {
        if (other == null) return Double.MAX_VALUE;
        return distanceTo(other.x, other.y);
    }

    public double distanceTo(double x, double y) {
        double dx = this.x - x;
        double dy = this.y - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public String toString() {
        return String.format("[%.2f,%.2f]", x , y);
    }

    public Point toPoint() {
        return new Point((int) x, (int) y);
    }

    public int hashCode() {
        return (x + "," + y).hashCode();
    }

    public boolean equals(Object other) {
        if (!(other instanceof DPoint)) {
            return false;
        }
        DPoint o = (DPoint) other;
        return x == o.x && y == o.y;
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("x", x);
        writer.write("y", y);
        writer.writeEndDocument();
    }

    public static final DataSerializer<DPoint> Serializer = new DataSerializer.JsonableSerializer<DPoint>() {
        @Override
        public DPoint parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            double x = reader.readDouble("x");
            double y = reader.readDouble("y");
            reader.readEndDocument();
            return new DPoint(x, y);
        }
    };
}
