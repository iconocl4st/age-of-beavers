package common.algo.quad;

import common.util.json.*;

import java.awt.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

abstract class QuadTreeNode<T extends Enum> implements Jsonable {
    protected int x;
    protected int y;
    protected int w;
    protected int h;
    protected final QuadTree<T> tree;

    QuadTreeNode(QuadTree<T> tree, int x, int y, int w, int h) {
        this.tree = tree;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        if (w < 0 || h < 0) {
            throw new IllegalStateException();
        }
    }

    public boolean equals(Object other) {
        if (!(other instanceof QuadTreeNode)) {
            return false;
        }
        QuadTreeNode<T> n = (QuadTreeNode<T>) other;
        return x == n.x && y == n.y && w == n.w && h == n.h;
    }

    public int hashCode() {
        return Integer.hashCode(x) * Integer.hashCode(y) * Integer.hashCode(w) * Integer.hashCode(h);
    }

    boolean contains(int px, int py) {
        return x <= px && px < x + w && y <= py && py < y + h;
    }

    boolean intersects(Point location, Dimension size) {
        return intersects(location.x, location.y, size.width, size.height);
    }

    boolean intersects(QuadTreeNode node) {
        return intersects(node.x, node.y, node.w, node.h);
    }

    boolean intersects(int ox, int oy, int ow, int oh) {
        return x + w > ox && x < ox + ow && y + h > oy && y < oy + oh;
    }

    boolean neighbors(QuadTreeNode<T> node) {
        return
            ((x == node.x + node.w || x + w == node.x) && y + h > node.y && y < node.y + node.h) ||
            ((y == node.y + node.h || y + h == node.y) && x + w > node.x && x < node.x + node.w)
        ;
    }

    abstract QuadTreeNode<T> setType(Point location, Dimension size, T type, boolean[] typesPresent);

    abstract NodeTypeCounts<T> count(NodeTypeCounts<T> counts);

    abstract Set<LeafNode<T>> collectNeighbors(HashSet<LeafNode<T>> emptyNodes, QuadTreeNode<T> empty);

    abstract QuadTreeNode<T> getNodeIn(Subdivision subdivision);

    abstract NodeType nodeType();

    abstract QuadTreeNode<T> getNode(int x, int y);

    abstract void locateNearest(NodeFilter nodeFilter, NearestTracker tracker);

    abstract void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException;

    Point getProjection(int x, int y) {
        return new Point(Math.max(this.x, Math.min(this.x + w - 1, x)), Math.max(this.y, Math.min(this.y + h - 1, y)));
    }

    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("node-type", nodeType().ordinal());
        writer.write("x", x);
        writer.write("y", y);
        writer.write("w", w);
        writer.write("h", h);
        writeInnards(writer, options);
        writer.writeEndDocument();
    }

    static <T extends Enum> DataSerializer<QuadTreeNode<T>> nodeSerializer() {
        return new DataSerializer.JsonableSerializer<QuadTreeNode<T>>() {
            @Override
            public QuadTreeNode<T> parse(JsonReaderWrapperSpec reader, ReadOptions opts) throws IOException {
                reader.readBeginDocument();

                NodeType b = reader.b(NodeType.values(), reader.readInt32("node-type"));
                int x = reader.readInt32("x");
                int y = reader.readInt32("y");
                int w = reader.readInt32("w");
                int h = reader.readInt32("h");
                QuadTreeNode<T> ret;
                switch (b) {
                    case Branch:
                        ret = BranchNode.finishParsing(reader, opts, x, y, w, h, (QuadTree<T>) opts.tree);
                        break;
                    case Filler:
                        ret = FillerNode.finishParsing(reader, opts, x, y, w, h, (QuadTree<T>) opts.tree);
                        break;
                    case Leaf:
                        ret = LeafNode.finishParsing(reader, opts, x, y, w, h, (QuadTree<T>) opts.tree);
                        break;
                    default:
                        throw new IllegalStateException();
                }
                reader.readEndDocument();
                return ret;
            }
        };
    }
}
