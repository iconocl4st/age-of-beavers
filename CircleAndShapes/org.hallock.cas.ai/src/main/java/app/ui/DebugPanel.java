package app.ui;

import javax.swing.*;
import java.util.Map;

public class DebugPanel {
    public static void showDebugFrame(Map<String, JPanel> debugPanels) {
        JTabbedPane tabbedPane = new JTabbedPane();
        for (Map.Entry<String, JPanel> entry : debugPanels.entrySet())
            tabbedPane.addTab(entry.getKey(), entry.getValue());

        JFrame frame = new JFrame();
        frame.setTitle("Debug");
        frame.setContentPane(tabbedPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(50, 50, 500, 500);
        frame.setVisible(true);
    }
}
