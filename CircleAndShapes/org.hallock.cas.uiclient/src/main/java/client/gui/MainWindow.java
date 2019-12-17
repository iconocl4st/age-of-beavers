package client.gui;

import javax.swing.*;
import java.awt.*;

public class MainWindow {
    private final UiManager uiManager;

    public JPanel panel1;
    private JPanel gamePanel;
    private JPanel buildingPlacer;
    private JPanel lowerPanel;
    private JToolBar toolbar;
    private JLabel logConsole;
    private JSplitPane mainSplitPane;
    private JSplitPane upperSplitPane;

    MainWindow(UiManager uiManager) {
        this.uiManager = uiManager;
    }

    void show(String string) {
        logConsole.setText(string);
    }

    void updateSplitPaneDividers() {
        int h1 = mainSplitPane.getHeight();
        mainSplitPane.setDividerLocation(8 * h1 / 10);

        int h2 = upperSplitPane.getWidth();
        upperSplitPane.setDividerLocation(9 * h2 / 10);
    }

    void addBottom() {
        lowerPanel.setLayout(new GridLayout(1, 0));
        lowerPanel.add(uiManager.minimap);
        lowerPanel.add(uiManager.selectedUnitsBrowser);
        lowerPanel.add(uiManager.unitActions);
        lowerPanel.setPreferredSize(new Dimension(1, 1));
        lowerPanel.setMinimumSize(new Dimension(1, 1));
    }

    private void createUIComponents() {
        gamePanel = new JPanel();
        gamePanel.setLayout(new GridLayout(0, 1));
        buildingPlacer = uiManager.demandsView;
    }

    void setGamePanel(Component component) {
        gamePanel.removeAll();
        gamePanel.add(component);
        gamePanel.setPreferredSize(new Dimension(1, 1));
        gamePanel.setMinimumSize(new Dimension(1, 1));
        gamePanel.revalidate();
        gamePanel.repaint();
    }
}
