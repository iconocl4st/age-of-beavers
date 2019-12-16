package common.algo.quad;

import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;

import java.awt.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

class LeafNode<T extends Enum> extends QuadTreeNode<T> {
    final T type;

    LeafNode(QuadTree<T> tree, int x, int y, int w, int h, T type) {
        super(tree, x, y, w, h);
        this.type = type;
    }

    NodeType nodeType() {
        return NodeType.Leaf;
    }

    MarkedRectangle<T> toRectangle() {
        return new MarkedRectangle<>(x, y, w, h, type);
    }

    @Override
    QuadTreeNode<T> setType(Point location, Dimension size, T type, boolean[] presentTypes) {
        if ((w == 1 && h == 1) || (location.x == x && location.y == y && size.width == w && size.height == h)) {
            if (this.type.equals(type))
                return this;
            presentTypes[this.type.ordinal()] = false;
            return create(tree, x, y, w, h, type, presentTypes);
        }
        return split(presentTypes.length).setType(location, size, type, presentTypes);
    }

    @Override
    NodeTypeCounts<T> count(NodeTypeCounts<T> counts) {
        ++counts.byType[type.ordinal()];
        return counts;
    }

    @Override
    final Set<LeafNode<T>> collectNeighbors(HashSet<LeafNode<T>> emptyNodes, QuadTreeNode<T> empty) {
        if (neighbors(empty))
            emptyNodes.add(this);
        return emptyNodes;
    }

    @Override
    QuadTreeNode<T> getNodeIn(Subdivision subdivision) {
        return this;
    }

    void locateNearest(NodeFilter nodeFilter, NearestTracker t) {
        if (nodeFilter.include(this))
            t.consider(getProjection(t.x, t.y));
    }

    @Override
    QuadTreeNode<T> getNode(int x, int y) {
        if (!contains(x, y)) throw new IllegalStateException();
        return this;
    }

    private QuadTreeNode<T> split(int numValues) {
        boolean[][] present = new boolean[4][numValues];
        int cx = x + w / 2;
        int cy = y + h / 2;
        QuadTreeNode<T> upperRight = create(tree, cx, cy, x + w - cx, y + h - cy, type, present[0]);
        QuadTreeNode<T> upperLeft = create(tree, x, cy, cx - x, y + h - cy, type, present[1]);
        QuadTreeNode<T> lowerLeft = create(tree, x, y, cx - x, cy - y, type, present[2]);
        QuadTreeNode<T> lowerRight = create(tree, cx, y, x + w - cx, cy - y, type, present[3]);
        return new BranchNode<>(tree, x, y, w, h,  upperRight, upperLeft, lowerLeft, lowerRight, present);
    }

    void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.write("value", type, tree.serializer, options);
    }

    public static <T extends Enum> LeafNode<T> finishParsing(JsonReaderWrapperSpec reader, ReadOptions opts, int x, int y, int w, int h, QuadTree<T> tree) throws IOException {
        return new LeafNode<>(tree, x, y, w,  h, reader.read("value", tree.serializer, opts));
    }

    static <T extends Enum> QuadTreeNode<T> create(QuadTree<T> tree, int x, int y, int w, int h, T type, boolean[] present) {
        if (w == 0 || h == 0)
            return new FillerNode<>(tree, x, y, w, h);
        present[type.ordinal()] = true;
        return new LeafNode<>(tree, x, y, w, h, type);
    }
}
