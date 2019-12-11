package common.algo.quad;

import java.util.Iterator;
import java.util.LinkedList;

class MarkedRectangleIterator implements Iterator<MarkedRectangle> {
    private final QuadTree quadTree;
    private final LinkedList<Integer> stack = new LinkedList<>();
    private final RootFinder rf;
    private MarkedRectangle next;

    MarkedRectangleIterator(QuadTree quadTree, RootFinder rf) {
        this.quadTree = quadTree;
        stack.addLast(-1);
        this.rf = rf;
        setNext();
    }

    private QuadTreeNode get() {
        QuadTreeNode node = quadTree.root;
        for (Integer i : stack) {
            node = node.getNodeIn(Subdivision.values()[i]);
        }
        return node;
    }

    private void setNext() {
        while (true) {
            while (true) {
                if (stack.isEmpty()) {
                    next = null;
                    return;
                }
                if (stack.getLast() != 3)
                    break;
                stack.removeLast();
            }
            stack.addLast(stack.removeLast() + 1);
            QuadTreeNode current = get();
            while (current instanceof BranchNode) {
                stack.addLast(0);
                current = get();
            }
            if (current instanceof LeafNode) {
                next = ((LeafNode) current).toRectangle(rf == null ? -1 : rf.getRoot(current.x, current.y));
                return;
            }
        }
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public MarkedRectangle next() {
        MarkedRectangle ret = next;
        if (ret == null) throw new IllegalStateException();
        setNext();
        return ret;
    }
}
