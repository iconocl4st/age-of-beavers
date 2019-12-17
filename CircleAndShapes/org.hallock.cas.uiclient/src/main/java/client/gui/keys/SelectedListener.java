package client.gui.keys;

import client.app.UiClientContext;
import client.gui.game.Focuser;
import common.state.EntityReader;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collections;

public class SelectedListener implements KeyListener {
    private final UiClientContext context;

    public SelectedListener(UiClientContext context) {
        this.context = context;
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {}

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        // Why the hell isn't this code ran?? the other listeners are called...
         switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_TAB:
                EntityReader cycle = context.uiManager.selectedUnitsBrowser.cycle();
                if (cycle != null) {
                    context.uiManager.unitActions.selectionChanged(Collections.singletonList(cycle));
                    context.uiManager.gameScreen.goToListener.setCurrentSet(Collections.singletonList(cycle));
                }
                break;
            case KeyEvent.VK_DELETE:
                // delete the selected units...
                System.out.println("TODO: Delete this unit...");
             default:
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {}
}
