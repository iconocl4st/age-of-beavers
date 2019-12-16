package common.algo.quad;

import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

class FillerNode<T extends Enum> extends QuadTreeNode<T> {

    FillerNode(QuadTree<T> tree, int x, int y, int w, int h) {
        super(tree, x, y, w, h);
        if (w < 0 || h < 0 || (w != 0 && h != 0)) throw new IllegalStateException();
    }

    NodeType nodeType() {
        return NodeType.Filler;
    }

    boolean intersects(int ox, int oy, int ow, int oh) {
        return false;
    }

    boolean contains(int px, int py) { return false; }

    boolean neighbors(QuadTreeNode node) {
        return false;
    }

    @Override
    QuadTreeNode<T> setType(Point location, Dimension size, T type, boolean[] typesPresent) {
        return this;
    }

    @Override
    public NodeTypeCounts<T> count(NodeTypeCounts<T> counts) {
        counts.numDontExist++;
        return counts;
    }

    @Override
    Set<LeafNode<T>> collectNeighbors(HashSet<LeafNode<T>> emptyNodes, QuadTreeNode<T> empty) {
        return emptyNodes;
    }

    @Override
    QuadTreeNode<T> getNodeIn(Subdivision subdivision) {
        return this;
    }

    @Override
    QuadTreeNode<T> getNode(int x, int y) {
        throw new IllegalStateException();
    }

    @Override
    void locateNearest(NodeFilter nodeFilter, NearestTracker tracker) {}

    Point getProjection(int x, int y) {
        return null;
    }


    void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) {}

    public static <T extends Enum> FillerNode<T> finishParsing(JsonReaderWrapperSpec reader, ReadOptions opts, int x, int y, int w, int h, QuadTree<T> tree) {
        return new FillerNode<>(tree, x, y, w, h);
    }
}
