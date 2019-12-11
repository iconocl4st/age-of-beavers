package common.algo.quad;

import common.algo.OneDUnionFind;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

class FillerNode extends QuadTreeNode {

    FillerNode(int x, int y, int w, int h) {
        super(x, y, w, h);
        if (w < 0 || h < 0 || (w != 0 && h != 0)) throw new IllegalStateException();
    }

    boolean intersects(int ox, int oy, int ow, int oh) {
        return false;
    }

    boolean contains(int px, int py) { return false; }

    boolean neighbors(QuadTreeNode node) {
        return false;
    }

    @Override
    QuadTreeNode setType(Point location, Dimension size, QuadNodeType type, boolean[] typesPresent) {
        return this;
    }

    @Override
    public NodeTypeCounts count(NodeTypeCounts counts) {
        counts.numDontExist++;
        return counts;
    }

    @Override
    Set<LeafNode> collectNeighbors(HashSet<LeafNode> emptyNodes, QuadTreeNode empty) {
        return emptyNodes;
    }

    @Override
    void assignConnectivity(OneDUnionFind unionFind, NodeIndexer indexer, QuadTreeNode upper, QuadTreeNode lower, QuadTreeNode left, QuadTreeNode right) {}

    @Override
    QuadTreeNode getNodeIn(Subdivision subdivision) {
        return this;
    }

    @Override
    QuadTreeNode getNode(int x, int y) {
        throw new IllegalStateException();
    }

    @Override
    void locateNearest(NodeFilter nodeFilter, NearestTracker tracker) {}

    Point getProjection(int x, int y) {
        return null;
    }
}
