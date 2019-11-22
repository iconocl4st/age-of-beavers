package app.ui;

import app.DebugSnapshot;
import client.state.ClientGameState;
import common.state.EntityReader;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

public final class DebugPanel extends JPanel implements Highlighter {

    private JPanel trackedPanel;
    private ArrayList<EntityViewer> viewers = new ArrayList<>();
    private JPanel informationPanel;
    private JLabel popLabel;
    private JLabel numShiftable;
    private JLabel desiredNumTransportWagons;
    private JLabel desiresMoreDropoffWagons;
    private JLabel storageSpace;
    private EntityReader highlight;

    private DebugPanel() {}

    public void show(ClientGameState state, DebugSnapshot snapshot) {
        int index = 0;
        for (Map.Entry<EntityReader, DebugSnapshot.EntityInformation> entry : snapshot.information.entrySet()) {
            EntityViewer viewer;
            if (viewers.size() <= index) {
                viewer = EntityViewer.createEntityViewer(this);
                trackedPanel.add(viewer);
                viewers.add(viewer);
            } else {
                viewer = viewers.get(index);
            }
            viewer.show(state, entry.getKey(), entry.getValue(), highlight);
            ++index;
        }
        while (viewers.size() > index)
            trackedPanel.remove(viewers.remove(index));

        popLabel.setText("Population: " + snapshot.population);

        numShiftable.setText("Shiftable: " + snapshot.numShiftable);
        desiredNumTransportWagons.setText("Num des. trans.: " + snapshot.desiredNumTransportWagons);
        desiresMoreDropoffWagons.setText("Desires more drop offs: " + snapshot.desiresMoreDropoffWagons);
        storageSpace.setText("storage space: " + snapshot.storageSpace);
    }


    public static DebugPanel createDebugPanel() {
        DebugPanel panel = new DebugPanel();
        panel.setLayout(new GridLayout(0, 1));

        JSplitPane jSplitPane = new JSplitPane();
        jSplitPane.setDividerLocation(20);
        jSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

        panel.informationPanel = new JPanel();
        panel.informationPanel.setBorder(new TitledBorder("Other information"));
        panel.informationPanel.setLayout(new GridLayout(0, 5));
        panel.informationPanel.add(panel.popLabel = new JLabel());
        panel.informationPanel.add(panel.numShiftable = new JLabel());
        panel.informationPanel.add(panel.desiredNumTransportWagons = new JLabel());
        panel.informationPanel.add(panel.desiresMoreDropoffWagons = new JLabel());
        panel.informationPanel.add(panel.storageSpace = new JLabel());

        // player number...
//        public final Map<ResourceType, Integer> desiredResources = new TreeMap<>(ResourceType.COMPARATOR);
//        public final Map<ResourceType, Integer> collectedResources = new TreeMap<>(ResourceType.COMPARATOR);
//        public final Map<ResourceType, Integer> desiredAllocations = new TreeMap<>(ResourceType.COMPARATOR);
        // show the demands

        jSplitPane.setTopComponent(panel.informationPanel);

        panel.trackedPanel = new JPanel();
        panel.trackedPanel.setLayout(new GridLayout(0, 1));
        panel.trackedPanel.setBorder(new TitledBorder("Tracked Entities"));

        JScrollPane jScrollPane = new JScrollPane(panel.trackedPanel);
        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jSplitPane.setBottomComponent(jScrollPane);
        panel.add(jSplitPane);

        panel.setPreferredSize(new Dimension(1, 1));
        panel.setMinimumSize(new Dimension(1, 1));
        return panel;
    }




    public static DebugPanel showDebugFrame() {
        DebugPanel debugPanel = createDebugPanel();

        JFrame frame = new JFrame();
        frame.setTitle("Debug");
        frame.setContentPane(debugPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(50, 50, 500, 500);
        frame.setVisible(true);

        return debugPanel;
    }

    @Override
    public void setHighlight(EntityReader reader) {
        highlight = reader;
        // reshow, guess we  have to wait...
    }
}
