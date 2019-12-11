package client.state;

import client.ai.ai2.AiTask;
import client.ai.ai2.QueueAi;
import client.app.UiClientContext;
import client.gui.keys.ContextKeyManager;
import common.state.EntityReader;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ActionQueuer implements ContextKeyManager.ContextKeyListener {

    private final HashMap<EntityReader, LinkedList<AiTask>> queing = new HashMap<>();
    private final UiClientContext context;

    public ActionQueuer(UiClientContext context) {
        this.context = context;
    }

    public void maybeQueue(EntityReader entity, AiTask ai) {
        synchronized (queing) {
            LinkedList<AiTask> ais = queing.get(entity);
            if (ais == null) {
                context.clientGameState.aiManager.set(entity, ai);
            } else {
                ais.addLast(ai);
            }
        }
    }

    private void sendRequests() {
        synchronized (queing) {
            for (Map.Entry<EntityReader, LinkedList<AiTask>> entry : queing.entrySet()) {
                context.clientGameState.aiManager.set(entry.getKey(), new QueueAi(entry.getKey(), entry.getValue()));
            }
            queing.clear();
        }
    }

    @Override
    public void keysChanged(ContextKeyManager manager) {
        synchronized (queing) {
            if (manager.containsKey(KeyEvent.VK_CONTROL)) {
                for (EntityReader entity : context.selectionManager.getSelectedUnits()) {
                    queing.computeIfAbsent(entity, e -> new LinkedList<>());
                }
            } else {
                sendRequests();
            }
        }
    }
}
