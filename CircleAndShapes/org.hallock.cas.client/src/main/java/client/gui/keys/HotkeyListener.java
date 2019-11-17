package client.gui.keys;

import client.app.ClientContext;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class HotkeyListener implements KeyListener {

    private final ClientContext context;

    public HotkeyListener(ClientContext context) {
        this.context = context;
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {}

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_Q: context.uiManager.unitActions.runHotKey(0,0); break;
            case KeyEvent.VK_A: context.uiManager.unitActions.runHotKey(1,0); break;
            case KeyEvent.VK_Z: context.uiManager.unitActions.runHotKey(2,0); break;
            case KeyEvent.VK_W: context.uiManager.unitActions.runHotKey(0,1); break;
            case KeyEvent.VK_S: context.uiManager.unitActions.runHotKey(1,1); break;
            case KeyEvent.VK_X: context.uiManager.unitActions.runHotKey(2,1); break;
            case KeyEvent.VK_E: context.uiManager.unitActions.runHotKey(0,2); break;
            case KeyEvent.VK_D: context.uiManager.unitActions.runHotKey(1,2); break;
            case KeyEvent.VK_C: context.uiManager.unitActions.runHotKey(2,2); break;
            case KeyEvent.VK_R: context.uiManager.unitActions.runHotKey(0,3); break;
            case KeyEvent.VK_F: context.uiManager.unitActions.runHotKey(1,3); break;
            case KeyEvent.VK_V: context.uiManager.unitActions.runHotKey(2,3); break;
            case KeyEvent.VK_ESCAPE:
                context.uiManager.unitActions.popAll();
                context.uiManager.gameScreen.clearCurrentCommand();
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {}
}
