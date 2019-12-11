package app.ui;

import client.event.AiEventListener;
import client.event.EventManagerListener;
import common.event.AiEventType;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.Map;
import java.util.Set;

public class EventsDebugPanel extends JPanel implements EventManagerListener {

    private DefaultMutableTreeNode root;

    @Override
    public synchronized void showDebugView(EventsDebugView view) {
        root.removeAllChildren();

        {
            DefaultMutableTreeNode byType = new DefaultMutableTreeNode("By Type");
            root.add(byType);
            for (Map.Entry<AiEventType, Set<AiEventListener>> entry : view.listenersByType.entrySet()) {
                DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(entry.getKey().name());
                byType.add(typeNode);
                for (AiEventListener listener : entry.getValue()) {
                    typeNode.add(new DefaultMutableTreeNode(listener.toString()));
                }
            }
        }

        {
            DefaultMutableTreeNode byEntity = new DefaultMutableTreeNode("By Entity");
            root.add(byEntity);
            for (Map.Entry<common.state.EntityId, Set<AiEventListener>> entry : view.listenersByEntity.entrySet()) {
                DefaultMutableTreeNode entityNode = new DefaultMutableTreeNode(String.valueOf(entry.getKey().id));
                byEntity.add(entityNode);
                for (AiEventListener listener : entry.getValue()) {
                    entityNode.add(new DefaultMutableTreeNode(listener.toString()));
                }
            }
        }
    }

    public static EventsDebugPanel createDebugPanel() {
        EventsDebugPanel panel = new EventsDebugPanel();
        panel.root = new DefaultMutableTreeNode("Listeners");
        JTree tree = new JTree();
        tree.setModel(new DefaultTreeModel(panel.root));
        tree.setEditable(false);
        panel.setLayout(new GridLayout(0, 1));
        panel.add(tree);
        return panel;
    }
}
