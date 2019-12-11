package common.algo.quad;

public final class MarkedRectangle {
    public final int x;
    public final int y;
    public final int w;
    public final int h;
    public final QuadNodeType type;
    public final int root;

    MarkedRectangle(int x, int y, int w, int h, QuadNodeType type, int root) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.type = type;
        this.root = root;
    }
}
