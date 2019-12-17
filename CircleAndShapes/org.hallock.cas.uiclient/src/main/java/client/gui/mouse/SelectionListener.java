package client.gui.mouse;

import client.app.UiClientContext;
import client.gui.game.gl.GlListeners;
import common.state.EntityReader;
import common.state.spec.EntitySpec;
import common.util.DPoint;

import java.awt.geom.Rectangle2D;
import java.util.Set;

public class SelectionListener implements GlListeners.GameMousePressListener, GlListeners.RectangleHandler {
    private final UiClientContext context;
    private final ScreenLocator screenLocator;

    public SelectionListener(UiClientContext context, ScreenLocator screenLocator) {
        this.context = context;
        this.screenLocator = screenLocator;
    }

    @Override
    public void run(double xBegin, double yBegin, double xEnd, double yEnd) {
        context.selectionManager.select(xBegin, yBegin, xEnd, yEnd);
    }

    @Override
    public void mousePressed(double x, double y, GlListeners.PressInfo info) {
        if (info.isMiddleButton || info.isRightButton)
            return;

        DPoint destination = new DPoint(x, y);
        Set<EntityReader> entities = context.clientGameState.gameState.locationManager.getEntities(
                destination,
                entity -> !entity.isHidden()
        );
        if (entities.isEmpty()) {
            context.selectionManager.select(entities);
            return;
        }

        if (info.clickCount <= 1) {
            context.selectionManager.select(entities);
            return;
        }

        if (entities.size() != 1) {
            return;
        }

        EntitySpec type = entities.iterator().next().getType();

        Rectangle2D screenLocation = screenLocator.getScreenLocation();
        context.selectionManager.select(
            screenLocation.getX(),
            screenLocation.getY(),
            screenLocation.getX() + screenLocation.getWidth(),
            screenLocation.getY() + screenLocation.getHeight(),
            type
        );
    }

    @Override
    public void mouseReleased(double x, double y, GlListeners.PressInfo info) {}

    public interface ScreenLocator {
        Rectangle2D getScreenLocation();
    }
}
