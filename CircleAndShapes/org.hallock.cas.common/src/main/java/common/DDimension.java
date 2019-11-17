package common;

public class DDimension {
    // TODO: use this...
    public final double width;
    public final double height;


    public DDimension(double x, double y) {
        this.width = x;
        this.height = y;
    }

    public String toString() {
        return "[dimension:" + width + "," + height + "]";
    }
}
