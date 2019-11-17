package common.util.json;

import java.awt.*;
import java.io.IOException;
import java.io.Serializable;

public interface DataSerializer<T> extends Serializable {
    void write(T value, JsonWriterWrapperSpec writer, WriteOptions options) throws IOException;
    T parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException;


    abstract class JsonableSerializer<G extends Jsonable> implements DataSerializer<G> {
        @Override
        public void write(G value, JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            value.writeTo(writer, options);
        }
    }

    DataSerializer<Double> DoubleSerializer = new DataSerializer<Double>() {
        @Override
        public void write(Double value, JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write(value);
        }

        @Override
        public Double parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return reader.readDouble();
        }
    };


    DataSerializer<Boolean> BooleanSerializer = new DataSerializer<Boolean>() {
        @Override
        public void write(Boolean value, JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write(value);
        }

        @Override
        public Boolean parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return reader.readBoolean();
        }
    };

    DataSerializer<Integer> IntegerSerializer = new DataSerializer<Integer>() {
        @Override
        public void write(Integer value, JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write(value);
        }

        @Override
        public Integer parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return reader.readInt32();
        }
    };

    DataSerializer<Point> PointSerializer = new DataSerializer<Point>() {
        @Override
        public void write(Point value, JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            if (value == null) {
                writer.writeNull();
                return;
            }
            writer.writeBeginDocument();
            writer.write("x", value.x);
            writer.write("y", value.y);
            writer.writeEndDocument();
        }

        @Override
        public Point parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            int x = reader.readInt32("x");
            int y = reader.readInt32("y");
            reader.readEndDocument();
            return new Point(x, y);
        }
    };

    DataSerializer<Dimension> DimensionSerializer = new DataSerializer<Dimension>() {
        @Override
        public void write(Dimension value, JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            if (value == null) {
                writer.writeNull();
                return;
            }
            writer.writeBeginDocument();
            writer.write("w", value.width);
            writer.write("h", value.height);
            writer.writeEndDocument();
        }

        @Override
        public Dimension parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            int w = reader.readInt32("w");
            int h = reader.readInt32("h");
            reader.readEndDocument();
            return new Dimension(w, h);
        }
    };

    DataSerializer<String> StringSerializer = new DataSerializer<String>() {
        @Override
        public void write(String value, JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write(value);
        }

        @Override
        public String parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return reader.readString();
        }
    };

    DataSerializer<EmptyJsonable>  EmptyJsonableSerializer = new JsonableSerializer<EmptyJsonable>() {
        @Override
        public EmptyJsonable parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            reader.readEndDocument();
            return new EmptyJsonable();
        }
    };
}
