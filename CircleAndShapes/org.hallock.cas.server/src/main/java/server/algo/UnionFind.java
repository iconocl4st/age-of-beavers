package server.algo;

public class UnionFind {
    int[] roots;

    int dim;

    public UnionFind(int width, int height) {
        roots = new int[width * height];
        for (int i = 0; i < roots.length; i++)
            roots[i] = -1;
        dim = height;
    }

    int getRoot(int i) {
        int or = roots[i];
        if (or < 0) return i;
        int nr = getRoot(or);
        if (or != nr) roots[i] = nr;
        return nr;
    }
    int getRoot(int i, int j) {
        return getRoot(i * dim + j);
    }
    void connect(int x1, int y1, int x2, int y2) {
        roots[x1 *dim + y1] = getRoot(x2, y2);
    }
    boolean areConnected(int x1, int y1, int x2, int y2) {
        return getRoot(x1, y1) == getRoot(x2, y2);
    }
}
