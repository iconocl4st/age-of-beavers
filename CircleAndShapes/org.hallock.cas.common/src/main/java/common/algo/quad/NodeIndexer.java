package common.algo.quad;

import java.util.HashMap;

public class NodeIndexer {
    int currentIndex;
    private final HashMap<QuadTreeNode, Integer> assigned;
    private final int size;

    public NodeIndexer(int size) {
        this.assigned = new HashMap<>(size);
        this.size = size;
    }

    public int getIndex(QuadTreeNode node) {
        if (currentIndex >= size) throw new IllegalStateException();
        return assigned.computeIfAbsent(node, e -> currentIndex++);
    }

    public Integer getExistingIndex(QuadTreeNode n1) {
        return assigned.get(n1);
    }
}
