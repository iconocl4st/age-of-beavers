package client.state;

import client.ai.Ai;
import client.ai.QueueAi;
import client.app.ClientContext;
import client.gui.keys.ContextKeyManager;
import common.state.EntityId;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ActionQueuer implements ContextKeyManager.ContextKeyListener {

    private final HashMap<EntityId, LinkedList<Ai>> queing = new HashMap<>();
    private final ClientContext context;

    public ActionQueuer(ClientContext context) {
        this.context = context;
    }

    public void maybeQueue(EntityId entityId, Ai ai) {
        synchronized (queing) {
            LinkedList<Ai> ais = queing.get(entityId);
            if (ais == null) {
                context.aiManager.startAi(entityId, ai);
            } else {
                ais.addLast(ai);
            }
        }
    }

    private void sendRequests() {
        synchronized (queing) {
            for (Map.Entry<EntityId, LinkedList<Ai>> entry : queing.entrySet()) {
                context.aiManager.startAi(entry.getKey(), new QueueAi(context, entry.getKey(), entry.getValue()));
            }
            queing.clear();
        }
    }

    @Override
    public void keysChanged(ContextKeyManager manager) {
        synchronized (queing) {
            if (manager.containsKey(KeyEvent.VK_CONTROL)) {
                for (EntityId entityId : context.selectionManager.getSelectedUnits()) {
                    queing.computeIfAbsent(entityId, e -> new LinkedList<>());
                }
            } else {
                sendRequests();
            }
        }
    }
}
