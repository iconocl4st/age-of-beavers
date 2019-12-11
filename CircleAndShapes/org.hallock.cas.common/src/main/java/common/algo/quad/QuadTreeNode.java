package common.algo.quad;

import common.algo.OneDUnionFind;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

abstract class QuadTreeNode {
    protected int x;
    protected int y;
    protected int w;
    protected int h;

    QuadTreeNode(int x, int y, int w, int  h) {
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
        QuadTreeNode n = (QuadTreeNode) other;
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

    boolean neighbors(QuadTreeNode node) {
        return
            ((x == node.x + node.w || x + w == node.x) && y + h > node.y && y < node.y + node.h) ||
            ((y == node.y + node.h || y + h == node.y) && x + w > node.x && x < node.x + node.w)
        ;
    }

    abstract QuadTreeNode setType(Point location, Dimension size, QuadNodeType type, boolean[] typesPresent);

    abstract NodeTypeCounts count(NodeTypeCounts counts);

    abstract Set<LeafNode> collectNeighbors(HashSet<LeafNode> emptyNodes, QuadTreeNode empty);

    abstract void assignConnectivity(OneDUnionFind unionFind, NodeIndexer indexer, QuadTreeNode upper, QuadTreeNode lower, QuadTreeNode left, QuadTreeNode right);

    abstract QuadTreeNode getNodeIn(Subdivision subdivision);

    boolean isConnectable() {
        return false;
    }

    abstract QuadTreeNode getNode(int x, int y);

    abstract void locateNearest(NodeFilter nodeFilter, NearestTracker tracker);

    Point getProjection(int x, int y) {
        return new Point(Math.max(this.x, Math.min(this.x + w - 1, x)), Math.max(this.y, Math.min(this.y + h - 1, y)));
    }


//    boolean neighborsAny(Set<QuadTreeNode> nodes) {
//        for (QuadTreeNode o : nodes) {
//            if (neighbors(o)) return true;
//        }
//        return false;
//    }

//    double distanceTo(QuadTreeNode other) {
//        int p1x = Math.min(x + w, Math.max(x, other.x));
//        int p1y = Math.min(y + h, Math.max(y, other.y));
//        int p2x = Math.min(other.x + other.w, Math.max(other.x, x));
//        int p2y = Math.min(other.y + other.h, Math.max(other.y, y));
//        int dx = p2x - p1x;
//        int dy = p2y - p1y;
//        return Math.sqrt(dx*dx + dy*dy);
//    }
//
//    double distanceTo(Set<? extends QuadTreeNode> others) {
//        double min = Double.MAX_VALUE;
//        for (QuadTreeNode o : others)
//            min = Math.min(min, distanceTo(o));
//        return min;
//    }
}
