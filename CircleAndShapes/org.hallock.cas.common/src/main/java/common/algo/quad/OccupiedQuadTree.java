package common.algo.quad;

import common.algo.OneDUnionFind;
import common.factory.PathFinder;
import common.util.ExecutorServiceWrapper;
import common.util.json.*;

import java.awt.*;
import java.io.IOException;

public class OccupiedQuadTree extends QuadTree<QuadTreeOccupancyState> {
    private final Object sync = new Object();

    private final ExecutorServiceWrapper executorService;
    private RootFinder rootFinder;
    private PathFinder pathFinder;


    public OccupiedQuadTree(int w, int h, ExecutorServiceWrapper executorService) {
        super(w, h, QuadTreeOccupancyState.values(), QuadTreeOccupancyState.Empty, QuadTreeOccupancyState.Serializer);
        this.executorService = executorService;
    }

    public RootFinder getRootFinder() {
        return rootFinder;
    }

    public void setPathFinder(PathFinder pathFinder) {
        this.pathFinder = pathFinder;
    }

    @Override
    public void setType(Point location, Dimension size, QuadTreeOccupancyState type) {
        PathFinder pf = pathFinder;
        if (pf == null) {
            super.setType(location, size, type);
            return;
        }
        executorService.submit(() -> {
            synchronized (sync) {
                pf.setRootFinder(null, null);
                super.setType(location, size, type);
                innerUpdateConnectivity();
                pf.setRootFinder(this, rootFinder);
            }
        });
    }

    private void innerUpdateConnectivity() {
        int numEmpty = size().byType[QuadTreeOccupancyState.Empty.ordinal()];
        OneDUnionFind unionFind = new OneDUnionFind(numEmpty);
        NodeIndexer indexer = new NodeIndexer(numEmpty);
        long now = System.currentTimeMillis();
        assignConnectivity(root, unionFind, indexer, NO_NODE, NO_NODE, NO_NODE, NO_NODE);
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
        this.root.locateNearest(node -> node.type.equals(QuadTreeOccupancyState.Empty) && rootFinder.getRoot(node.x, node.y) == root, nt);
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















    private boolean isConnectable(QuadTreeNode<QuadTreeOccupancyState> node) {
        switch (node.nodeType()) {
            case Filler:
            case Branch:
                return false;
            case Leaf:
                return ((LeafNode<QuadTreeOccupancyState>) node).type.equals(QuadTreeOccupancyState.Empty);
            default:
                throw new IllegalStateException();
        }
    }

    private void assignConnectivity(
            QuadTreeNode<QuadTreeOccupancyState> node,
            OneDUnionFind unionFind,
            NodeIndexer indexer,
            QuadTreeNode<QuadTreeOccupancyState> upper,
            QuadTreeNode<QuadTreeOccupancyState> lower,
            QuadTreeNode<QuadTreeOccupancyState> left,
            QuadTreeNode<QuadTreeOccupancyState> right
    ) {
        switch (node.nodeType()) {
            case Branch:
                BranchNode<QuadTreeOccupancyState> b = (BranchNode<QuadTreeOccupancyState>) node;
                assignConnectivity(b.upperRight, unionFind, indexer, upper.getNodeIn(Subdivision.LowerRight), b.lowerRight, b.upperLeft, right.getNodeIn(Subdivision.UpperLeft));
                assignConnectivity(b.upperLeft, unionFind, indexer, upper.getNodeIn(Subdivision.LowerLeft), b.lowerLeft, left.getNodeIn(Subdivision.UpperRight), b.upperRight);
                assignConnectivity(b.lowerLeft, unionFind, indexer, b.upperLeft, lower.getNodeIn(Subdivision.UpperLeft), left.getNodeIn(Subdivision.LowerRight), b.lowerRight);
                assignConnectivity(b.lowerRight, unionFind, indexer, b.upperRight, lower.getNodeIn(Subdivision.UpperRight), b.lowerLeft, right.getNodeIn(Subdivision.LowerLeft));
                break;
            case Leaf:
                if (!isConnectable(node))
                    return;
                // Isn't this too many?

                if (isConnectable(upper)) {
                    unionFind.connect(indexer.getIndex(node), indexer.getIndex(upper));
                } else
                    assignConnectivity(upper, unionFind, indexer, NO_NODE, node, NO_NODE, NO_NODE);

                if (isConnectable(lower))
                    unionFind.connect(indexer.getIndex(node), indexer.getIndex(lower));
                else
                    assignConnectivity(lower, unionFind, indexer, node, NO_NODE, NO_NODE, NO_NODE);

                if (isConnectable(left)) {
                    unionFind.connect(indexer.getIndex(node), indexer.getIndex(left));
                } else
                    assignConnectivity(left, unionFind, indexer, NO_NODE, NO_NODE, NO_NODE, node);

                if (isConnectable(right))
                    unionFind.connect(indexer.getIndex(node), indexer.getIndex(right));
                else
                    assignConnectivity(right, unionFind, indexer, NO_NODE, NO_NODE, node, NO_NODE);

                break;
            case Filler:
                break;
        }
    }

    private final QuadTreeNode<QuadTreeOccupancyState> NO_NODE = new FillerNode<>(this, 0, 0, 0, 0);
}
