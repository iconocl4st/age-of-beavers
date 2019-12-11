package common.algo;

public class UnionFind2d extends OneDUnionFind {
    private int dim;

    public UnionFind2d(int width, int height) {
        super(width * height);
        dim = height;
    }

    private int getIndex(int i, int j) {
        return i * dim + j;
    }

    public int getRoot(int i, int j) {
        return getRoot(getIndex(i, j));
    }

    public void connect(int x1, int y1, int x2, int y2) {
        connect(getIndex(x1, y1), getIndex(x2, y2));
    }

    public boolean areConnected(int x1, int y1, int x2, int y2) {
        return areConnected(getIndex(x1, y1), getIndex(x2, y2));
    }
}
