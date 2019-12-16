package client.gui;

import client.app.UiClientContext;
import client.gui.game.Colors;
import client.gui.game.GamePainter;
import client.gui.game.SquishZoom;
import client.gui.game.Zoom;
import common.state.EntityReader;
import common.state.Player;
import common.state.spec.EntitySpec;
import common.state.spec.GameSpec;
import common.state.sst.manager.RevPair;
import common.util.DPoint;
import common.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class Minimap extends JPanel {

    UiClientContext context;
    Zoom zoom;
    GameSpec spec;

    private Minimap(UiClientContext context) {
        this.context = context;
        this.zoom = new SquishZoom(this);
    }

    public void setGameSpec(GameSpec spec) {
        zoom.initialize(spec, getWidth(), getHeight());
        this.spec = spec;
    }

    public void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;

        client.gui.game.Renderer renderer = new client.gui.game.Renderer.Graphics2DRenderer(context, g, zoom, this);
        int w = getWidth();
        int h = getHeight();

        g.setColor(Colors.DESERT);
        g.fillRect(0, 0, w, h);

        if (true) return;

//        GamePainter.paintTerrain(context.clientGameState.gameState.textures, renderer);

        for (int i = 1; i < context.clientGameState.gameState.numPlayers + 1; i++) {
            for (RevPair<Player> pair : context.clientGameState.gameState.playerManager.getByType(new Player(i))) {
                g.setColor(Colors.PLAYER_COLORS[i]);
                drawUnit(g, pair);
            }
        }

        for (EntitySpec e : spec.unitSpecs) {
            if (e.minimapColor == null)
                continue;
            drawUnitType(g, e.minimapColor, e);
        }
        g.setColor(Color.white);
//        g.draw(zoom.mapGameToScreen(
//                context.uiManager.gameScreen.zoom.getLocationX(),
//                context.uiManager.gameScreen.zoom.getLocationY(),
//                context.uiManager.gameScreen.zoom.getScreenWidth(),
//                context.uiManager.gameScreen.zoom.getScreenHeight()
//        ));
    }

    private void drawUnitType(Graphics2D g, Color color, EntitySpec type) {
        g.setColor(color);
        for (RevPair<EntitySpec> pair : context.clientGameState.gameState.typeManager.getByType(type))
            drawUnit(g, pair);
    }

    private static final int UNIT_SIZE = 2;
    private void drawUnit(Graphics2D g, RevPair pair) {
        EntityReader entity = new EntityReader(context.clientGameState.gameState, pair.entityId);
        Dimension size = entity.getSize();
        DPoint location = entity.getLocation();
        if (Util.anyAreNull(size, location)) return;
        g.fill(zoom.mapGameToScreen(location.x - UNIT_SIZE, location.y - UNIT_SIZE, size.width + 2 * UNIT_SIZE, size.height + 2 * UNIT_SIZE));
    }

    public static Minimap createMinimap(UiClientContext context) {
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
//                context.uiManager.gameScreen.zoom.recenter(x, y);
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
//                context.uiManager.gameScreen.zoom.recenter(x, y);
            }

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {}
        });
        return minimap;
    }
}
