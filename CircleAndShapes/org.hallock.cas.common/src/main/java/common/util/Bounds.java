package common.util;

import common.state.spec.GameSpec;
import org.omg.PortableInterceptor.Interceptor;

public class Bounds {
    public final int xmin;
    public final int xmax;
    public final int ymin;
    public final int ymax;

    public Bounds(int xmin, int ymin, int xmax, int ymax) {
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;
    }

    public boolean contains(DPoint point) {
        return !outOfBounds(point.x, point.y);
    }

    public boolean outOfBoundsX(double x) {
        return x < xmin || x >= xmax;
    }

    public boolean outOfBoundsY(double y) {
        return y < ymin || y >= ymax;
    }

    public boolean outOfBounds(double x, double y) {
        return outOfBoundsX(x) || outOfBoundsY(y);
    }

    public Bounds intersect(Bounds bnds) {
        return new Bounds(
                Math.max(xmin, bnds.xmin),
                Math.max(ymin, bnds.ymin),
                Math.min(xmax, bnds.xmax),
                Math.min(ymax, bnds.ymax)
        );
    }

    public static Bounds fromDimension(int w, int h) {
        return new Bounds(0, 0, w, h);
    }

    public static Bounds fromGameSpec(GameSpec spec) {
        return fromDimension(spec.width, spec.height);
    }

    public static Bounds fromRadius(int x, int y, int r) {
        return new Bounds(x - r,  y - r, x + r + 1, y + r + 1);
    }

    public static Bounds None = new Bounds(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    public DPoint project(DPoint finalDestination) {
        return new DPoint(
                Math.min(xmax, Math.max(xmin, finalDestination.x)),
                Math.min(ymax, Math.max(ymin, finalDestination.y))
        );
    }
}
