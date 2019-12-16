package common.state.sst.manager;

import common.algo.quad.MarkedRectangle;
import common.algo.quad.QuadTree;
import common.state.sst.sub.TerrainType;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;

import java.awt.*;
import java.io.IOException;
import java.util.Iterator;

public class Terrains {

    private final QuadTree<TerrainType> tree;

    public Terrains(int w, int h) {
        tree = new QuadTree<>(w, h, TerrainType.values(), TerrainType.Sahara, TerrainType.Serializer);
    }

    public void set(int x, int y, TerrainType type) {
        tree.setType(new Point(x, y), new Dimension(1, 1), type);
    }

    public void set(int x, int y, int w, int h, TerrainType type) {
        tree.setType(new Point(x, y), new Dimension(w, h), type);
    }

    public Iterator<MarkedRectangle<TerrainType>> get(double x1, double y1, double x2, double y2) {
        return tree.leaves(x1, y1,  x2,  y2);
    }

    public void updateAll(JsonReaderWrapperSpec reader, ReadOptions options) throws IOException {
        reader.readBeginDocument();
        reader.readName("tree");
        tree.updateAll(reader, options);
        reader.readEndDocument();
    }

    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.writeName("tree");
        tree.writeTo(writer, options);
        writer.writeEndDocument();
    }
//
//    public static class TileTexture implements Jsonable {
//        public final int x;
//        public final int y;
//        public final TerrainType type;
//
//        public TileTexture(int x, int y, TerrainType type) {
//            this.x = x;
//            this.y = y;
//            this.type = type;
//        }
//
//        @Override
//        public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
//            writer.writeBeginDocument();
//            writer.write("x", x);
//            writer.write("y", y);
//            writer.write("type", type.ordinal());
//            writer.writeEndDocument();
//        }
//
//        public static final DataSerializer<TileTexture> Serializer = new DataSerializer.JsonableSerializer<TileTexture>() {
//            @Override
//            public TileTexture parse(JsonReaderWrapperSpec reader, ReadOptions opts) throws IOException {
//                reader.readBeginDocument();
//                TileTexture texture = new TileTexture(
//                    reader.readInt32("x"),
//                    reader.readInt32("y"),
//                    reader.b(TerrainType.values(), reader.readInt32("type"))
//                );
//                reader.readEndDocument();
//                return texture;
//            }
//        };
//    }
}
