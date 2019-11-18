package client.gui.mouse;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GrabFocusListener extends MouseAdapter {

    private final JPanel panel;

    public GrabFocusListener(JPanel panel) {
        this.panel = panel;
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        panel.grabFocus();
    }
}
