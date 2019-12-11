package common.algo.quad;

import common.algo.OneDUnionFind;
import common.util.DPoint;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

class BranchNode extends QuadTreeNode {

    QuadTreeNode upperRight;
    QuadTreeNode upperLeft;
    QuadTreeNode lowerLeft;
    QuadTreeNode lowerRight;

    final boolean[][] presentTypes;

    BranchNode(int x, int y, int w, int h, QuadTreeNode upperRight, QuadTreeNode upperLeft, QuadTreeNode lowerLeft, QuadTreeNode lowerRight, boolean[][] present) {
        super(x, y, w, h);
        this.upperRight = upperRight;
        this.upperLeft = upperLeft;
        this.lowerLeft = lowerLeft;
        this.lowerRight = lowerRight;
        this.presentTypes = present;
        if (w == 0 || h == 0) throw new IllegalStateException();
    }

    @Override
    public QuadTreeNode setType(Point location, Dimension size, QuadNodeType type, boolean[] totalPresentTypes) {
        if (upperRight.intersects(location, size)) upperRight = upperRight.setType(location, size, type, presentTypes[0]);
        if (upperLeft.intersects(location, size)) upperLeft = upperLeft.setType(location, size, type, presentTypes[1]);
        if (lowerLeft.intersects(location, size)) lowerLeft = lowerLeft.setType(location, size, type, presentTypes[2]);
        if (lowerRight.intersects(location, size)) lowerRight = lowerRight.setType(location, size, type, presentTypes[3]);
        QuadNodeType nt = orTypes(totalPresentTypes);
        if (nt != null)
            return LeafNode.create(x, y, w, h, nt, totalPresentTypes);
        return this;
    }

    @Override
    Set<LeafNode> collectNeighbors(HashSet<LeafNode> emptyNodes, QuadTreeNode node) {
        if (upperRight.intersects(node) || upperRight.neighbors(node)) upperRight.collectNeighbors(emptyNodes, node);
        if (upperLeft.intersects(node) || upperLeft.neighbors(node)) upperLeft.collectNeighbors(emptyNodes, node);
        if (lowerLeft.intersects(node) || lowerLeft.neighbors(node)) lowerLeft.collectNeighbors(emptyNodes, node);
        if (lowerRight.intersects(node) || lowerRight.neighbors(node)) lowerRight.collectNeighbors(emptyNodes, node);
        return emptyNodes;
    }

    @Override
    void assignConnectivity(OneDUnionFind unionFind, NodeIndexer indexer, QuadTreeNode upper, QuadTreeNode lower, QuadTreeNode left, QuadTreeNode right) {
        upperRight.assignConnectivity(unionFind, indexer, upper.getNodeIn(Subdivision.LowerRight), lowerRight, upperLeft, right.getNodeIn(Subdivision.UpperLeft));
        upperLeft.assignConnectivity(unionFind, indexer, upper.getNodeIn(Subdivision.LowerLeft), lowerLeft, left.getNodeIn(Subdivision.UpperRight), upperRight);
        lowerLeft.assignConnectivity(unionFind, indexer, upperLeft, lower.getNodeIn(Subdivision.UpperLeft), left.getNodeIn(Subdivision.LowerRight),  lowerRight);
        lowerRight.assignConnectivity(unionFind, indexer, upperRight, lower.getNodeIn(Subdivision.UpperRight), lowerLeft, right.getNodeIn(Subdivision.LowerLeft));
    }

    @Override
    QuadTreeNode getNodeIn(Subdivision subdivision) {
        switch (subdivision) {
            case UpperRight: return upperRight;
            case UpperLeft: return upperLeft;
            case LowerLeft: return lowerLeft;
            case LowerRight: return lowerRight;
            default: throw new IllegalStateException(subdivision.name());
        }
    }

    @Override
    QuadTreeNode getNode(int x, int y) {
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
    public NodeTypeCounts count(NodeTypeCounts counts) {
        upperRight.count(counts);
        upperLeft.count(counts);
        lowerLeft.count(counts);
        lowerRight.count(counts);
        counts.numBranches++;
        return counts;
    }

    private QuadNodeType orTypes(boolean[] totalPresentTypes) {
        int cnt = 0;
        QuadNodeType t = null;
        for (int i = 0; i < totalPresentTypes.length; i++) {
            if (!(totalPresentTypes[i] = presentTypes[0][i] || presentTypes[1][i] || presentTypes[2][i] || presentTypes[3][i]))
                continue;
            ++cnt;
            t = QuadNodeType.values()[i];
        }
        if (cnt != 1) return null;
        return t;
    }
}
