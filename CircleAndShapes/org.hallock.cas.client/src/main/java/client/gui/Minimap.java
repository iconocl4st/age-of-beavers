package client.gui;

import client.app.ClientContext;
import client.gui.game.Colors;
import client.gui.game.Zoom;
import common.state.EntityReader;
import common.state.Player;
import common.state.spec.EntitySpec;
import common.state.spec.GameSpec;
import common.state.sst.manager.ReversableManagerImpl;
import common.util.DPoint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class Minimap extends JPanel {

    ClientContext context;
    Zoom zoom;
    GameSpec spec;

    private Minimap(ClientContext context) {
        this.context = context;
        this.zoom = new Zoom(this, context);
    }

    public void setGameSpec(GameSpec spec) {
        zoom.initialize(spec);
        this.spec = spec;
    }

    public void paintComponent(Graphics g) {
        int w = getWidth();
        int h = getHeight();

        g.setColor(Colors.GRASS);
        g.fillRect(0, 0, w, h);

        for (int i = 1; i < spec.numPlayers; i++) {
            for (ReversableManagerImpl.Pair<Player> pair : context.gameState.playerManager.getByType(new Player(i))) {
                g.setColor(Colors.PLAYER_COLORS[i]);
                drawUnit(g, pair);
            }
        }

        drawResource(g, Colors.MINIMAP_TREE, "tree");
        drawResource(g, Color.yellow, "gold stone");
        drawResource(g, Color.red, "berry");
        drawUnitType(g, Color.gray, "deer");
        drawUnitType(g, Color.black, "horse");

        g.setColor(Color.white);
        int sx1 = zoom.mapGameToScreenX(context.uiManager.gameScreen.zoom.locationX);
        int sy1 = zoom.mapGameToScreenY(context.uiManager.gameScreen.zoom.locationY);
        int sx2 = zoom.mapGameToScreenX(context.uiManager.gameScreen.zoom.locationX + context.uiManager.gameScreen.zoom.screenWidth);
        int sy2 = zoom.mapGameToScreenY(context.uiManager.gameScreen.zoom.locationY + context.uiManager.gameScreen.zoom.screenHeight);
        g.drawRect(sx1, sy2, sx2 - sx1, sy1 - sy2);
    }

    private void drawUnitType(Graphics g, Color color, String typeName) {
        g.setColor(color);
        for (ReversableManagerImpl.Pair<EntitySpec> pair : context.gameState.typeManager.getByType(spec.getUnitSpec(typeName))) {
            drawUnit(g, pair);
        }
    }

    private void drawResource(Graphics g, Color color, String resourceName) {
        g.setColor(color);
        for (ReversableManagerImpl.Pair<EntitySpec> pair : context.gameState.typeManager.getByType(spec.getNaturalResource(resourceName))) {
            drawUnit(g, pair);
        }
    }

    private static final int UNIT_SIZE = 0;
    private void drawUnit(Graphics g, ReversableManagerImpl.Pair pair) {
        EntityReader entity = new EntityReader(context.gameState, pair.entityId);
        EntitySpec type = entity.getType();
        DPoint location = entity.getLocation();
        int x1 = zoom.mapGameToScreenX(location.x);
        int y1 = zoom.mapGameToScreenY(location.y);
        int x2 = zoom.mapGameToScreenX(location.x + type.size.width);
        int y2 = zoom.mapGameToScreenY(location.y + type.size.height);
        g.fillRect(x1 - UNIT_SIZE, y2 - UNIT_SIZE, x2 - x1 + 2 * UNIT_SIZE, y1 - y2 + 2 * UNIT_SIZE);
    }

    public static Minimap createMinimap(ClientContext context) {
        Minimap minimap = new Minimap(context);
//        GrabFocusListener grabFocusListener = new GrabFocusListener(minimap);
//        minimap.addMouseListener(grabFocusListener);
        minimap.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {}

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                double x = minimap.zoom.mapScreenToGameX(mouseEvent.getX());
                double y = minimap.zoom.mapScreenToGameY(mouseEvent.getY());
                context.uiManager.gameScreen.zoom.recenter(x, y);
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {}

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {}

            @Override
            public void mouseExited(MouseEvent mouseEvent) {}
        });
        minimap.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                double x = minimap.zoom.mapScreenToGameX(mouseEvent.getX());
                double y = minimap.zoom.mapScreenToGameY(mouseEvent.getY());
                context.uiManager.gameScreen.zoom.recenter(x, y);
            }

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {}
        });
        return minimap;
    }
}
