package common.algo.quad;

import common.algo.OneDUnionFind;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

class LeafNode extends QuadTreeNode {
    final QuadNodeType type;

    LeafNode(int x, int y, int w, int h, QuadNodeType type) {
        super(x, y, w, h);
        this.type = type;
    }

    MarkedRectangle toRectangle(int root) {
        return new MarkedRectangle(x, y, w, h, type, root);
    }

    @Override
    QuadTreeNode setType(Point location, Dimension size, QuadNodeType type, boolean[] presentTypes) {
        if ((w == 1 && h == 1) || (location.x == x && location.y == y && size.width == w && size.height == h)) {
            if (this.type.equals(type))
                return this;
            presentTypes[this.type.ordinal()] = false;
            return create(x, y, w, h, type, presentTypes);
        }
        return split().setType(location, size, type, presentTypes);
    }

    @Override
    NodeTypeCounts count(NodeTypeCounts counts) {
        ++counts.byType[type.ordinal()];
        return counts;
    }

    @Override
    final Set<LeafNode> collectNeighbors(HashSet<LeafNode> emptyNodes, QuadTreeNode empty) {
        if (neighbors(empty))
            emptyNodes.add(this);
        return emptyNodes;
    }

    @Override
    QuadTreeNode getNodeIn(Subdivision subdivision) {
        return this;
    }

    void locateNearest(NodeFilter nodeFilter, NearestTracker t) {
        if (nodeFilter.include(this))
            t.consider(getProjection(t.x, t.y));
    }

    @Override
    void assignConnectivity(OneDUnionFind unionFind, NodeIndexer indexer, QuadTreeNode upper, QuadTreeNode lower, QuadTreeNode left, QuadTreeNode right) {
        if (!isConnectable())
            return;
        // Isn't this too many?

        if (upper.isConnectable()) {
            unionFind.connect(indexer.getIndex(this), indexer.getIndex(upper));
        } else
            upper.assignConnectivity(unionFind, indexer, QuadTree.NO_NODE, this, QuadTree.NO_NODE, QuadTree.NO_NODE);

        if (lower.isConnectable())
            unionFind.connect(indexer.getIndex(this), indexer.getIndex(lower));
        else
            lower.assignConnectivity(unionFind, indexer, this, QuadTree.NO_NODE, QuadTree.NO_NODE, QuadTree.NO_NODE);

        if (left.isConnectable()) {
            unionFind.connect(indexer.getIndex(this), indexer.getIndex(left));
        } else
            left.assignConnectivity(unionFind, indexer, QuadTree.NO_NODE, QuadTree.NO_NODE, QuadTree.NO_NODE, this);

        if (right.isConnectable())
            unionFind.connect(indexer.getIndex(this), indexer.getIndex(right));
        else
            right.assignConnectivity(unionFind, indexer, QuadTree.NO_NODE, QuadTree.NO_NODE, this, QuadTree.NO_NODE);
    }

    @Override
    boolean isConnectable() {
        return type.equals(QuadNodeType.Empty);
    }

    @Override
    QuadTreeNode getNode(int x, int y) {
        if (!contains(x, y)) throw new IllegalStateException();
        return this;
    }

    private QuadTreeNode split() {
        boolean[][] present = new boolean[4][QuadNodeType.values().length];
        int cx = x + w / 2;
        int cy = y + h / 2;
        QuadTreeNode upperRight = create(cx, cy, x + w - cx, y + h - cy, type, present[0]);
        QuadTreeNode upperLeft = create(x, cy, cx - x, y + h - cy, type, present[1]);
        QuadTreeNode lowerLeft = create(x, y, cx - x, cy - y, type, present[2]);
        QuadTreeNode lowerRight = create(cx, y, x + w - cx, cy - y, type, present[3]);
        return new BranchNode(x, y, w, h,  upperRight, upperLeft, lowerLeft, lowerRight, present);
    }

    static QuadTreeNode create(int x, int y, int w, int h, QuadNodeType type, boolean[] present) {
        if (w == 0 || h == 0) {
            return new FillerNode(x, y, w, h);
        }
        present[type.ordinal()] = true;
        return new LeafNode(x, y, w, h, type);
    }
}
