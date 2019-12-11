package common.factory;

import common.util.DPoint;
import common.util.json.*;

import java.io.IOException;
import java.util.LinkedList;

public class Path<T extends Jsonable> implements Jsonable {
    public static final Path<? extends Jsonable> FAILED = new Path<>();

    public final LinkedList<DPoint> points;
    public final boolean successful;
    public final T debug;
    public final DataSerializer<T> serializer;
    public final SearchDestination destination;

    private Path(LinkedList<DPoint> path, boolean success, T debug, DataSerializer<T> serializer, SearchDestination destination) {
        this.points = path;
        this.successful = success;
        this.debug = debug;
        this.serializer = serializer;
        this.destination = destination;
    }

    public Path() {
        this.successful = false;
        debug = null;
        serializer = null;
        points = new LinkedList<>();
        destination = null;
    }

    public Path(LinkedList<DPoint> path, T debug, DataSerializer<T> serializer, SearchDestination destination) {
        this.successful = true;
        this.debug = debug;
        this.serializer = serializer;
        this.points = path;
        this.destination = destination;
    }

    public String toString() {
        if (!successful) return "No such points.";
        StringBuilder builder = new StringBuilder();
        for (DPoint p : points)
            builder.append("->").append(p);
        return builder.toString();
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("points", points, DPoint.Serializer, options);
        writer.write("debug", debug, serializer, options);
        writer.write("successful", successful);
        writer.write("destination", destination, SearchDestination.Serializer, options);
        writer.writeEndDocument();
    }

    public static <G extends Jsonable> DataSerializer<Path<G>> createSerializer(DataSerializer<G> serializer) {
        return new DataSerializer.JsonableSerializer<Path<G>>() {
            @Override
            public Path parse(JsonReaderWrapperSpec reader, ReadOptions opts) throws IOException {
                reader.readBeginDocument();
                LinkedList<DPoint> points = (LinkedList<DPoint>) reader.read("points", new LinkedList<>(), DPoint.Serializer, opts);
                G t = reader.read("debug", serializer, opts);
                Boolean successful = reader.readBoolean("successful");
                SearchDestination destination = reader.read("destination", SearchDestination.Serializer, opts);
                reader.readEndDocument();

                return new Path<>(points, successful, t, serializer, destination);
            }
        };
    }
}
