package common.state.edit;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

public class ActuallyUsefulTable extends JPanel {
    private static final int ROW_HEIGHT = 75;

    private String[] headers;
    private ArrayList<Row> rows = new ArrayList<>();
    private double[] percentages;
    private HeaderPanel headerPanel;
    private JPanel listPanel;
    private JScrollPane scrollPane;

    private ActuallyUsefulTable() {}

    private void resetLocations() {
        int w = getWidth();
        int h = getHeight();

        headerPanel.setBounds(0, 0, w, ROW_HEIGHT);
        headerPanel.resetLocations();

        listPanel.setLocation(0, ROW_HEIGHT);

        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            row.panel.setBounds(0, i * ROW_HEIGHT, w, ROW_HEIGHT);

            for (int j = 0; j < row.components.length; j++) {
                int cw = w / row.components.length;
                row.components[j].setBounds(
                        j * cw,
                        0,
                        j == row.components.length - 1 ? (w - j * cw) : cw,
                        ROW_HEIGHT
                );
            }
        }
        Dimension size = new Dimension(w, rows.size() * ROW_HEIGHT);
        listPanel.setPreferredSize(size);
        listPanel.setSize(size);
        listPanel.setMinimumSize(size);
        listPanel.setMaximumSize(size);

        scrollPane.setBounds(0, ROW_HEIGHT, w, h - ROW_HEIGHT);
        revalidate();
        repaint();
    }

    public void addRow(Component[] components) {
        if (components.length != headers.length)
            throw new IllegalArgumentException();

        Row row = new Row();
        row.panel = new JPanel();
        row.panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        row.components = components;

        row.panel.setLayout(null);
        for (Component component : components)
            row.panel.add(component);

        listPanel.add(row.panel);
        rows.add(row);

        resetLocations();
    }

    public void removeRow(Component[] components) {
        for (Row row : rows) {
            if (!row.components.equals(components)) {
                continue;
            }
            rows.remove(row);
            listPanel.remove(row.panel);

            resetLocations();
            return;
        }
    }


    private final class HeaderPanel extends JPanel {
        private static final int BAR_WIDTH = 2;

        JLabel[] headerLabels;

        void resetLocations() {
            int w = getWidth();
            int h = getHeight();

            for (int i = 0; i < headerLabels.length; i++) {
                int right;
                if (i == headerLabels.length - 1) {
                    right = w;
                } else {
                    right = (int) (w * percentages[i]) - BAR_WIDTH;
                }

                int left;
                if (i == 0) {
                    left = 0;
                } else {
                    left = (int) (w * percentages[i - 1]) + BAR_WIDTH;
                }
                headerLabels[i].setBounds(left, 0, right - left, h);
            }
        }
    }

    public static ActuallyUsefulTable createTable(String... headers) {
        ActuallyUsefulTable table = new ActuallyUsefulTable();
        table.headers = headers;
        table.percentages = new double[headers.length - 1];
        for (int i = 0; i < table.percentages.length; i++)
            table.percentages[i] = (i + 1) / (double) (1 + table.percentages.length);

        table.setLayout(null);

        table.headerPanel = table.new HeaderPanel();
        table.headerPanel.setLayout(null);
        table.headerPanel.setBackground(Color.black);
        table.headerPanel.headerLabels = new JLabel[headers.length];
        for (int i = 0; i < table.headerPanel.headerLabels.length; i++) {
            JLabel label = new JLabel(headers[i]);
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setOpaque(true);
            table.headerPanel.headerLabels[i] = label;
            table.headerPanel.add(label);
        }


        table.add(table.headerPanel);
        table.listPanel = new JPanel();
        table.listPanel.setLayout(null);
        table.scrollPane = new JScrollPane(table.listPanel);
        table.add(table.scrollPane);
        table.scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        table.scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        table.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                table.resetLocations();
            }

            @Override
            public void componentMoved(ComponentEvent componentEvent) {
                table.resetLocations();
            }

            @Override
            public void componentShown(ComponentEvent componentEvent) {
                table.resetLocations();
            }

            @Override
            public void componentHidden(ComponentEvent componentEvent) {
                table.resetLocations();
            }
        });
        return table;
    }

    private static final class Row {
        JPanel panel;
        Component[] components;
    }

    public static void main(String[] args) {
        ActuallyUsefulTable table = createTable("foo", "bar", "raboof");
        for (int i = 0; i < 30; i++) {
            Component[] components = new Component[3];
            for (int j = 0; j < components.length; j++) {
                components[j] = new JLabel(String.valueOf(Math.random()));
            }
            table.addRow(components);
        }

        JFrame frame = new JFrame();
        frame.setTitle("testing");
        frame.setBounds(50, 50, 500, 500);
        frame.setContentPane(table);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
