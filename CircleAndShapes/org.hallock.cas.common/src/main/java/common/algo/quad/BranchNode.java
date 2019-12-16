package common.algo.quad;

import common.util.DPoint;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;

import java.awt.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

class BranchNode<T extends Enum> extends QuadTreeNode<T> {

    QuadTreeNode<T> upperRight;
    QuadTreeNode<T> upperLeft;
    QuadTreeNode<T> lowerLeft;
    QuadTreeNode<T> lowerRight;

    final boolean[][] presentTypes;

    BranchNode(QuadTree<T> tree, int x, int y, int w, int h, QuadTreeNode<T> upperRight, QuadTreeNode<T> upperLeft, QuadTreeNode<T> lowerLeft, QuadTreeNode<T> lowerRight, boolean[][] present) {
        super(tree, x, y, w, h);
        this.upperRight = upperRight;
        this.upperLeft = upperLeft;
        this.lowerLeft = lowerLeft;
        this.lowerRight = lowerRight;
        this.presentTypes = present;
        if (w == 0 || h == 0) throw new IllegalStateException();
    }


    NodeType nodeType() {
        return NodeType.Branch;
    }

    @Override
    public QuadTreeNode<T> setType(Point location, Dimension size, T type, boolean[] totalPresentTypes) {
        if (upperRight.intersects(location, size)) upperRight = upperRight.setType(location, size, type, presentTypes[0]);
        if (upperLeft.intersects(location, size)) upperLeft = upperLeft.setType(location, size, type, presentTypes[1]);
        if (lowerLeft.intersects(location, size)) lowerLeft = lowerLeft.setType(location, size, type, presentTypes[2]);
        if (lowerRight.intersects(location, size)) lowerRight = lowerRight.setType(location, size, type, presentTypes[3]);
        T nt = orTypes(tree.values, totalPresentTypes);
        if (nt != null)
            return LeafNode.create(tree, x, y, w, h, nt, totalPresentTypes);
        return this;
    }

    @Override
    Set<LeafNode<T>> collectNeighbors(HashSet<LeafNode<T>> emptyNodes, QuadTreeNode<T> node) {
        if (upperRight.intersects(node) || upperRight.neighbors(node)) upperRight.collectNeighbors(emptyNodes, node);
        if (upperLeft.intersects(node) || upperLeft.neighbors(node)) upperLeft.collectNeighbors(emptyNodes, node);
        if (lowerLeft.intersects(node) || lowerLeft.neighbors(node)) lowerLeft.collectNeighbors(emptyNodes, node);
        if (lowerRight.intersects(node) || lowerRight.neighbors(node)) lowerRight.collectNeighbors(emptyNodes, node);
        return emptyNodes;
    }

    @Override
    QuadTreeNode<T> getNodeIn(Subdivision subdivision) {
        switch (subdivision) {
            case UpperRight: return upperRight;
            case UpperLeft: return upperLeft;
            case LowerLeft: return lowerLeft;
            case LowerRight: return lowerRight;
            default: throw new IllegalStateException(subdivision.name());
        }
    }

    @Override
    QuadTreeNode<T> getNode(int x, int y) {
        if (upperRight.contains(x, y)) return upperRight.getNode(x, y);
        if (upperLeft.contains(x, y)) return upperLeft.getNode(x, y);
        if (lowerLeft.contains(x, y)) return lowerLeft.getNode(x, y);
        if (lowerRight.contains(x, y)) return lowerRight.getNode(x, y);
        throw new IllegalStateException();
    }

    @Override
    void locateNearest(NodeFilter nodeFilter, NearestTracker t) {
        boolean ur, ul, ll, lr;
        ur = ul = ll = lr = false;
        if (upperRight.contains(t.x, t.y)) { ur = true; upperRight.locateNearest(nodeFilter, t); }
        if (upperLeft.contains(t.x, t.y)) { ul = true; upperLeft.locateNearest(nodeFilter, t); }
        if (lowerLeft.contains(t.x, t.y)) { ll = true; lowerLeft.locateNearest(nodeFilter, t); }
        if (lowerRight.contains(t.x, t.y)) { lr = true; lowerRight.locateNearest(nodeFilter, t); }
        if (!ur && DPoint.d(t.x, t.y, upperRight.getProjection(t.x, t.y)) < t.minDist) upperRight.locateNearest(nodeFilter, t);
        if (!ul && DPoint.d(t.x, t.y, upperLeft.getProjection(t.x, t.y)) < t.minDist) upperLeft.locateNearest(nodeFilter, t);
        if (!ll && DPoint.d(t.x, t.y, lowerLeft.getProjection(t.x, t.y)) < t.minDist) lowerLeft.locateNearest(nodeFilter, t);
        if (!lr && DPoint.d(t.x, t.y, lowerRight.getProjection(t.x, t.y)) < t.minDist) lowerRight.locateNearest(nodeFilter, t);
    }

    @Override
    public NodeTypeCounts<T> count(NodeTypeCounts<T> counts) {
        upperRight.count(counts);
        upperLeft.count(counts);
        lowerLeft.count(counts);
        lowerRight.count(counts);
        counts.numBranches++;
        return counts;
    }

    private T orTypes(T[] values, boolean[] totalPresentTypes) {
        int cnt = 0;
        T t = null;
        for (int i = 0; i < totalPresentTypes.length; i++) {
            if (!(totalPresentTypes[i] = presentTypes[0][i] || presentTypes[1][i] || presentTypes[2][i] || presentTypes[3][i]))
                continue;
            ++cnt;
            t = values[i];
        }
        if (cnt != 1) return null;
        return t;
    }


    void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.write("upper-right", upperRight, tree.nodeSerializer, options);
        writer.write("upper-left", upperLeft, tree.nodeSerializer, options);
        writer.write("lower-left", lowerLeft, tree.nodeSerializer, options);
        writer.write("lower-right", lowerRight, tree.nodeSerializer, options);
        writer.writeName("present-types");
        write(writer, presentTypes);
    }


    public static <T extends Enum> BranchNode<T> finishParsing(
            JsonReaderWrapperSpec reader, ReadOptions opts, int x, int y, int w, int h, QuadTree<T> tree) throws IOException {
        QuadTreeNode<T> upperRight = reader.read("upper-right", tree.nodeSerializer, opts);
        QuadTreeNode<T> upperLeft = reader.read("upper-left", tree.nodeSerializer, opts);
        QuadTreeNode<T> lowerLeft = reader.read("lower-left", tree.nodeSerializer, opts);
        QuadTreeNode<T> lowerRight = reader.read("lower-right", tree.nodeSerializer, opts);
        reader.readName("present-types");
        boolean[][] present = read(reader);
        return new BranchNode<>(tree, x, y, w, h, upperRight, upperLeft, lowerLeft, lowerRight, present);
    }


    private static void write(JsonWriterWrapperSpec writer, boolean[][] bs) throws IOException {
        writer.writeBeginDocument();
        writer.write("w", bs.length);
        writer.write("h", bs[0].length);
        writer.writeBeginArray("values");
        for (boolean[] b : bs)
            for (boolean aB : b)
                writer.write(aB);
        writer.writeEndArray();
        writer.writeEndDocument();
    }

    private static boolean[][] read(JsonReaderWrapperSpec reader) throws IOException {
        reader.readBeginDocument();
        int w = reader.readInt32("w");
        int h = reader.readInt32("h");
        boolean[][] ret = new boolean[w][h];
        reader.readBeginArray("values");
        for (int i = 0; i < ret.length; i++)
            for (int j = 0; j < ret[i].length; j++)
                ret[i][j] = reader.readBoolean();
        reader.readEndArray();
        reader.readEndDocument();
        return ret;
    }
}
