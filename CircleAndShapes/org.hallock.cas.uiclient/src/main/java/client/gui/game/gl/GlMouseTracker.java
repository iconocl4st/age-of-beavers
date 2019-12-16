package client.gui.game.gl;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class GlMouseTracker implements MouseMotionListener {

    private final GlPainter painter;

    GlMouseTracker(GlPainter painter) {
        this.painter = painter;
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        painter.setCurrentMousePosition(mouseEvent.getX(), mouseEvent.getY());
    }
}
