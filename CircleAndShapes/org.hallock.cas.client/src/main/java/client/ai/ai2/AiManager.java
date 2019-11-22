package client.ai.ai2;

import client.state.ClientGameState;
import common.state.EntityId;
import common.state.EntityReader;

import java.util.HashMap;

public class AiManager {

    private final ClientGameState clientGameState;
    private final HashMap<EntityId, AiStack> stacks = new HashMap<>();

    // Not that efficient...
    private final Object stacksSync = new Object();

    public AiManager(ClientGameState clientGameState) {
        this.clientGameState = clientGameState;
    }

    public boolean isControlling(EntityReader entity) {
        return stacks.containsKey(entity.entityId);
    }

    private AiContext createAiContext() {
        AiContext context = new AiContext();
        context.clientGameState = clientGameState;
        context.locator = new AiLocator(clientGameState);
        context.requester = clientGameState.actionRequester;
        context.controlling = null;
        context.stack = null;
        return context;
    }

    public void set(EntityReader reader, AiTask task) {
        AiContext aiContext = createAiContext();
        aiContext.controlling = reader;
        AiStack stack = new AiStack(this);

        remove(aiContext, reader);

        if (task == null) {
            return;
        }
        stacks.put(reader.entityId, stack);
        stack.push(aiContext, task);
    }

    private void remove(AiContext aiContext, EntityReader reader) {
        // Complicated, because I don't want to lock stacks and the aiStack at the same time.
        AiStack aiStack;
        synchronized (stacksSync) {
            aiStack = stacks.remove(reader.entityId);
        }
        if (aiStack == null)
            return;

        aiStack.removeAllListeners(aiContext);
        remove(aiStack);
    }

    void remove(AiStack stack) {
        synchronized (stacksSync) {
            stacks.entrySet().removeIf(e -> e.getValue() == stack);
        }
    }

    public AiTask get(EntityReader entity) {
        AiStack aiStack = stacks.get(entity.entityId);
        if (aiStack == null) return null;
        return aiStack.getLast();
    }

    public String getDisplayString(EntityReader entity) {
        AiStack aiTask = stacks.get(entity.entityId);
        if (aiTask == null) return "No ai stack";
        return aiTask.getDisplayString();
    }
}
