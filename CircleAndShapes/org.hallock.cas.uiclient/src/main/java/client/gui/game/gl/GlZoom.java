package client.gui.game.gl;

import client.gui.game.Focuser;
import common.util.DPoint;

public class GlZoom implements Focuser {
    final Object sync = new Object();

    // Assuming it is a square for now...
    public double screenLowerY;
    public double screenUpperY;
    public double screenLowerX;
    public double screenUpperX;

    int screenWidth;
    int screenHeight;

    double aspect;

    double locationX;
    double locationY;
    double locationZ = GlConstants.INITIAL_Z;

    public String toString() {
        return (
            "Location: " + locationX + ", " + locationY + ", " + locationZ + '\n' +
            "Screen size: " + screenWidth + ", " + screenHeight + '\n' +
            "Game Screen Location: " + "[" + screenLowerX + "," + screenLowerY + ":" + screenUpperX + "," + screenUpperY + "]" + '\n'
//            "Game Screen Center: " + screenCenterX + ", " + screenCenterY + '\n'
        );
    }

    @Override
    public void focusOn(DPoint p) {
        if (p == null) return;
        synchronized (sync) {
            locationX = p.x;
            locationY = p.y;
        }
    }
}
