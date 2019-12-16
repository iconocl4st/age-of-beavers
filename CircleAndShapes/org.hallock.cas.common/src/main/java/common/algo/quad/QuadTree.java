package common.algo.quad;

import common.util.json.*;
import org.bson.json.JsonReader;

import java.awt.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class QuadTree<T extends Enum> implements Jsonable {
    QuadTreeNode<T> root;
    final T[] values;
    private boolean[] allTypesPresent;
    DataSerializer<T> serializer;
    DataSerializer<QuadTreeNode<T>> nodeSerializer = QuadTreeNode.nodeSerializer();

    public QuadTree(int w, int h, T[] values, T defaultValue, DataSerializer<T> serializer) {
        this.values = values;
        allTypesPresent = new boolean[values.length];
        root = LeafNode.create(this, 0, 0, w, h, defaultValue, allTypesPresent);
        this.serializer = serializer;
    }

    public void setType(Point location, Dimension size, T type) {
        root = root.setType(location, size, type, allTypesPresent);
    }

    public Set<LeafNode<T>> getNeighbors(QuadTreeNode<T> empty) {
        return root.collectNeighbors(new HashSet<>(), empty);
    }

    public NodeTypeCounts size() {
        return root.count(new NodeTypeCounts<>(values.length));
    }

    public Iterator<MarkedRectangle<T>> leaves() {
        return new MarkedRectangleIterator<>(this);
    }

    public Iterator<MarkedRectangle<T>> leaves(double x1, double y1, double x2, double y2) {
        return new MarkedRectangleIterator<>(
                this,
                (int) Math.floor(x1),
                (int) Math.floor(y1),
                (int) Math.ceil(x2 - x1),
                (int) Math.ceil(y2 - y1)
        );
    }

    QuadTreeNode getNode(int x, int y) {
        return root.getNode(x, y);
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("root", root, nodeSerializer, options);
        writer.writeEndDocument();
    }


    public void updateAll(JsonReaderWrapperSpec reader, ReadOptions options) throws IOException {
        reader.readBeginDocument();
        options.tree = this;
        root = reader.read("root", nodeSerializer, options);
        reader.readEndDocument();
    }

//    static final QuadTreeNode<T> NO_NODE = new FillerNode<>(0, 0, 0, 0);
}
