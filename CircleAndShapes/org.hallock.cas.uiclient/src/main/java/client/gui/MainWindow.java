package client.gui;

import javax.swing.*;
import java.awt.*;

import java.util.Timer;
import java.util.TimerTask;

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

    public MainWindow(UiManager uiManager) {
        this.uiManager = uiManager;
    }

    public void show(String string) {
        logConsole.setText(string);
    }

    public void updateSplitPaneDividers() {
        int h1 = mainSplitPane.getHeight();
        mainSplitPane.setDividerLocation(8 * h1 / 10);

        int h2 = upperSplitPane.getWidth();
        upperSplitPane.setDividerLocation(8 * h2 / 10);
    }

    public void addBottom() {
        lowerPanel.setLayout(new GridLayout(1, 0));
        lowerPanel.add(uiManager.minimap);
        lowerPanel.add(uiManager.selectedUnitsBrowser);
        lowerPanel.add(uiManager.unitActions);
        lowerPanel.setPreferredSize(new Dimension(1, 1));
        lowerPanel.setMinimumSize(new Dimension(1, 1));
    }

    private void createUIComponents() {
        gamePanel = uiManager.gameScreen;
        buildingPlacer = uiManager.buildingSelector;
    }
}
