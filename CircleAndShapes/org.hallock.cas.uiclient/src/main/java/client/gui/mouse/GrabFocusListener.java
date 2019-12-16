package client.gui.mouse;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GrabFocusListener extends MouseAdapter {

    private final Component panel;

    public GrabFocusListener(Component component) {
        this.panel = component;
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        panel.requestFocus();
        panel.requestFocusInWindow();
    }
}
