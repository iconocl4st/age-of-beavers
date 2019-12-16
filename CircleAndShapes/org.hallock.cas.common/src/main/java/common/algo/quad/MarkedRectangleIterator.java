package common.algo.quad;

import java.util.Iterator;
import java.util.LinkedList;

class MarkedRectangleIterator<T extends Enum> implements Iterator<MarkedRectangle<T>> {
    private final QuadTree<T> quadTree;
    private final LinkedList<Integer> stack = new LinkedList<>();
    private MarkedRectangle<T> next;

    private int x;
    private int y;
    private int w;
    private int h;

    MarkedRectangleIterator(QuadTree<T> quadTree) {
        this(quadTree, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    MarkedRectangleIterator(QuadTree<T> quadTree, int x, int y, int w, int h) {
        this.quadTree = quadTree;
        stack.addLast(-1);
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        setNext();
    }

    private QuadTreeNode<T> get() {
        QuadTreeNode<T> node = quadTree.root;
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
            QuadTreeNode<T> current = get();
            if (!current.intersects(x, y, w, h)) {
                continue;
            }
            while (current instanceof BranchNode) {
                stack.addLast(0);
                current = get();
            }
            if (current instanceof LeafNode) {
                next = ((LeafNode<T>) current).toRectangle();
                return;
            }
        }
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public MarkedRectangle<T> next() {
        MarkedRectangle<T> ret = next;
        if (ret == null) throw new IllegalStateException();
        setNext();
        return ret;
    }
}
