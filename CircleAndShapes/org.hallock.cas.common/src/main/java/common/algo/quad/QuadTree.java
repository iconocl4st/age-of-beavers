package common.algo.quad;

import common.algo.OneDUnionFind;
import common.factory.PathFinder;
import common.util.ExecutorServiceWrapper;

import java.awt.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class QuadTree {
    private final Object sync = new Object();

    private final ExecutorServiceWrapper executorService;
    private boolean[] dummy = new boolean[QuadNodeType.values().length];
    QuadTreeNode root;
    RootFinder rootFinder;
    PathFinder pathFinder;


    public QuadTree(int w, int h, ExecutorServiceWrapper executorService) {
        root = LeafNode.create(0, 0, w, h, QuadNodeType.Empty, dummy);
        this.executorService = executorService;
    }

    public void setPathFinder(PathFinder pathFinder) {
        this.pathFinder = pathFinder;
    }

    public void setType(Point location, Dimension size, QuadNodeType type) {
        PathFinder pf = pathFinder;
        if (pf == null) {
            root = root.setType(location, size, type, dummy);
            return;
        }
        executorService.submit(() -> {
            synchronized (sync) {
                pf.setRootFinder(null, null);
                root.setType(location, size, type, dummy);
                innerUpdateConnectivity();
                pf.setRootFinder(this, rootFinder);
            }
        });
    }

    public Set<LeafNode> getNeighbors(QuadTreeNode empty) {
        return root.collectNeighbors(new HashSet<>(), empty);
    }

    public NodeTypeCounts size() {
        return root.count(new NodeTypeCounts());
    }

    public Iterator<MarkedRectangle> leaves() {
        return new MarkedRectangleIterator(this, rootFinder);
    }

    QuadTreeNode getNode(int x, int y) {
        return root.getNode(x, y);
    }

    private void innerUpdateConnectivity() {
        int numEmpty = size().byType[QuadNodeType.Empty.ordinal()];
        OneDUnionFind unionFind = new OneDUnionFind(numEmpty);
        NodeIndexer indexer = new NodeIndexer(numEmpty);
        long now = System.currentTimeMillis();
        root.assignConnectivity(unionFind, indexer, NO_NODE, NO_NODE, NO_NODE, NO_NODE);
        long dt = System.currentTimeMillis() - now;
        System.out.println("Updating the connectivity took " + String.valueOf(dt) + "ms");
        rootFinder = (x, y) -> {
            Integer idx1 = indexer.getExistingIndex(getNode(x, y));
            if (idx1 == null) return -1;
            return unionFind.getRoot(idx1);
        };
    }

    public Point nearestConnected(Point b, Point e) {
        // right now assuming this is not null
        int root = rootFinder.getRoot(b.x, b.y);
        if (root < 0) return null;
        NearestTracker nt = new NearestTracker(e.x, e.y);
        this.root.locateNearest(node -> node.type.equals(QuadNodeType.Empty) && rootFinder.getRoot(node.x, node.y) == root, nt);
        return nt.minimum;
    }

    public void updateConnectivitySync() {
        synchronized (sync) {
            PathFinder pf = pathFinder;
            if (pf == null)
                return;
            pf.setRootFinder(null, null);
            innerUpdateConnectivity();
            pf.setRootFinder(this, rootFinder);
        }
    }

    public void updateConnectivity() {
        executorService.submit(() -> {
            synchronized (sync) {
                updateConnectivitySync();
            }
        });
    }

    static final QuadTreeNode NO_NODE = new FillerNode(0, 0, 0, 0);
}
