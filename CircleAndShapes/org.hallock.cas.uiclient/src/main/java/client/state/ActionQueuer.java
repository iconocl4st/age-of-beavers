package client.state;

import client.ai.Ai;
import client.ai.QueueAi;
import client.app.UiClientContext;
import client.gui.keys.ContextKeyManager;
import common.state.EntityId;
import common.state.EntityReader;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ActionQueuer implements ContextKeyManager.ContextKeyListener {

    private final HashMap<EntityId, LinkedList<Ai>> queing = new HashMap<>();
    private final UiClientContext context;

    public ActionQueuer(UiClientContext context) {
        this.context = context;
    }

    public void maybeQueue(EntityId entityId, Ai ai) {
        synchronized (queing) {
            LinkedList<Ai> ais = queing.get(entityId);
            if (ais == null) {
                context.clientGameState.aiManager.startAi(entityId, ai);
            } else {
                ais.addLast(ai);
            }
        }
    }

    private void sendRequests() {
        synchronized (queing) {
            for (Map.Entry<EntityId, LinkedList<Ai>> entry : queing.entrySet()) {
                context.clientGameState.aiManager.startAi(entry.getKey(), new QueueAi(context.clientGameState, new EntityReader(context.clientGameState.gameState, entry.getKey()), entry.getValue()));
            }
            queing.clear();
        }
    }

    @Override
    public void keysChanged(ContextKeyManager manager) {
        synchronized (queing) {
            if (manager.containsKey(KeyEvent.VK_CONTROL)) {
                for (EntityReader entity : context.selectionManager.getSelectedUnits()) {
                    queing.computeIfAbsent(entity.entityId, e -> new LinkedList<>());
                }
            } else {
                sendRequests();
            }
        }
    }
}
