package client.gui.game;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class RectangleListener
{

    private int sr1x, sr1y, sr2x, sr2y;
    private double gr1x, gr2x, gr1y, gr2y;


    public void removeSelectingRectangle() {
        sr1x = -1;
        sr1y = -1;
        sr2x = -1;
        sr2y = -1;
        gr1x = -1;
        gr1y = -1;
        gr2x = -1;
        gr2y = -1;
//        redraw();
    }

    public void setSelectingRectangle(
            double gx1, double gy1, double gx2, double gy2,
            int sx1, int sy1, int sx2, int sy2) {
        sr1x = Math.min(sx1, sx2);
        sr2x = Math.max(sx1, sx2);
        sr1y = Math.min(sy1, sy2);
        sr2y = Math.max(sy1, sy2);

        gr1x = Math.min(gx1, gx2);
        gr2x = Math.max(gx1, gx2);
        gr1y = Math.min(gy1, gy2);
        gr2y = Math.max(gy1, gy2);
//        redraw();
    }

    Rectangle getScreenRectangle() {
        return new Rectangle(sr1x, sr1y, sr2x - sr1x, sr2y - sr1y);
    }
    public Rectangle2D getGameRectangle() {
        return new Rectangle2D.Double(gr1x, gr1y, gr2x - gr1x, gr2y - gr1y);
    }

    boolean isSelecting() {
        return (
            sr1x >= 0 &&
            sr1y >= 0 &&
            sr2x >= 0 &&
            sr2y >= 0 &&
            gr1x >= 0 &&
            gr1y >= 0 &&
            gr2x >= 0 &&
            gr2y >= 0
        );
    }
}
