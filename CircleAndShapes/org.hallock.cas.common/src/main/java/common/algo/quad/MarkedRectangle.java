package common.algo.quad;

public final class MarkedRectangle<T> {
    public final int x;
    public final int y;
    public final int w;
    public final int h;
    public final T type;

    MarkedRectangle(int x, int y, int w, int h, T type) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.type = type;
    }
}
