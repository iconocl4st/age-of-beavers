//package client.gui.game;
//
//import client.app.UiClientContext;
//import client.gui.actions.unit_action.*;
//import client.gui.game.gl.GlGameScreen;
//import client.gui.keys.*;
//import client.gui.mouse.*;
//import common.state.spec.EntitySpec;
//import common.state.spec.GameSpec;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ComponentListener;
//import java.awt.image.BufferedImage;
//
//
//public class GameScreen {
//    private final UiClientContext context;
//
//
//    private GlGameScreen glGameScreen;
//
//    GameScreen(UiClientContext context) {
//        this.context = context;
//    }
//
//
//    public void paintComponent(Graphics graphics) {
//        Graphics2D g = (Graphics2D) graphics;
////        renderer.renderGame(zoom, new Renderer.Graphics2DRenderer(context, (Graphics2D) graphics, zoom, this));
//
//        if (selectionListener.isSelecting()) {
//            g.setColor(Color.white);
//            g.draw(selectionListener.rectangleListener.getScreenRectangle());
//        }
//    }
//
//
//    public static GameScreen createGameScreen(final UiClientContext context) {
//        final GameScreen gs = new GameScreen(context);
//
//        gs.glGameScreen = GlGameScreen.createGlGameScreen(context);
//
//        return gs;
//    }
//}
