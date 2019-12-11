package common.state.sst.manager;

import common.util.json.*;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;

public class Textures {

    public enum TileType {
        Water,
        Grass,
        Sahara,
    }

    public final HashMap<Point, TileTexture> textures = new HashMap<>();

    public Textures() {}

    public void updateAll(JsonReaderWrapperSpec reader, ReadOptions options) throws IOException {
        textures.clear();
        reader.readBeginDocument();
        reader.read("textures", textures, DataSerializer.PointSerializer, TileTexture.Serializer, options);
        reader.readEndDocument();
    }

    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("textures", textures, DataSerializer.PointSerializer, TileTexture.Serializer, options);
        writer.writeEndDocument();
    }

    public static class TileTexture implements Jsonable {
        public final int x;
        public final int y;
        public final TileType type;

        public TileTexture(int x, int y, TileType type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }

        @Override
        public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.writeBeginDocument();
            writer.write("x", x);
            writer.write("y", y);
            writer.write("type", type.ordinal());
            writer.writeEndDocument();
        }

        public static final DataSerializer<TileTexture> Serializer = new DataSerializer.JsonableSerializer<TileTexture>() {
            @Override
            public TileTexture parse(JsonReaderWrapperSpec reader, ReadOptions opts) throws IOException {
                reader.readBeginDocument();
                TileTexture texture = new TileTexture(
                    reader.readInt32("x"),
                    reader.readInt32("y"),
                    reader.b(TileType.values(), reader.readInt32("type"))
                );
                reader.readEndDocument();
                return texture;
            }
        };
    }
}
