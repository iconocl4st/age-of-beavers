package app.ui;

import app.DebugSnapshot;
import client.state.ClientGameState;
import common.DebugGraphics;
import common.state.EntityReader;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Set;

final class EntityViewer extends JPanel {
    private EntityReaderLabel id;
    private JLabel type;
    private EntityReaderLabel rider;
    private EntityReaderLabel riding;
    private JLabel assignments;
    private JLabel garrisoners;
    private JLabel garrisoned;
    private JLabel location;
    private JButton focus;
    private JLabel action;
    private JLabel ai;

    private EntityViewer() {}

    private static String valueOf(Set<String> assignments) {
        StringBuilder builder = new StringBuilder();
        for (String s : assignments) builder.append(s).append(", ");
        return builder.toString();
    }
    private static String valueOf2(Set<EntityReader> assignments) {
        StringBuilder builder = new StringBuilder();
        for (EntityReader s : assignments) builder.append(s.entityId.id).append(", ");
        return builder.toString();
    }

    void show(ClientGameState clientGameState, EntityReader reader, DebugSnapshot.EntityInformation information, EntityReader highlight) {
        id.setReader(reader, highlight);
        type.setText("Type: " + reader.getType().name);
        location.setText("Loc: " + reader.getLocation());
        riding.setReader(information.riding, highlight);
        rider.setReader(information.rider, highlight);
        assignments.setText(valueOf(information.assignments));
        garrisoners.setText("Garrisoners: " + valueOf2(information.garrisoners));
        garrisoned.setText("Garrisoned: " + valueOf2(information.garrisoners));
        ai.setText("Ai: " + clientGameState.aiManager.getDisplayString(reader));
        action.setText("Action: " + reader.getCurrentAction());
        for (ActionListener l : focus.getActionListeners()) focus.removeActionListener(l);
        focus.addActionListener(e -> {
            synchronized (DebugGraphics.pleaseFocusSync) {
                DebugGraphics.pleaseFocus = reader;
            }
        });
    }

    private void addLabel(JLabel label) {
        label.setBorder(new BevelBorder(BevelBorder.RAISED));
        label.setPreferredSize(new Dimension(1, 1));
        label.setMinimumSize(new Dimension(1, 1));
        add(label);
    }

    static EntityViewer createEntityViewer(Highlighter highlighter) {
        EntityViewer viewer = new EntityViewer();
        viewer.setLayout(new GridLayout(1, 0));
        viewer.addLabel(viewer.id = new EntityReaderLabel("Id: ", highlighter));
        viewer.addLabel(viewer.type = new JLabel());
        viewer.addLabel(viewer.action = new JLabel());
        viewer.addLabel(viewer.ai = new JLabel());
        viewer.addLabel(viewer.location = new JLabel());
        viewer.addLabel(viewer.rider = new EntityReaderLabel("Rider: ", highlighter));
        viewer.addLabel(viewer.riding = new EntityReaderLabel("Ridden: ", highlighter));
        viewer.addLabel(viewer.assignments = new JLabel());
        viewer.addLabel(viewer.garrisoners = new JLabel());
        viewer.addLabel(viewer.garrisoned = new JLabel());
        viewer.add(viewer.focus = new JButton("Go to"));

        viewer.setPreferredSize(new Dimension(1, 1));
        viewer.setMinimumSize(new Dimension(1, 1));
        return viewer;
    }
}
