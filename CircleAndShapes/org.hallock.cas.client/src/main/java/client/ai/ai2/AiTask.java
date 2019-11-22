package client.ai.ai2;

import common.state.EntityReader;

public abstract class AiTask extends AiEventAdapter {
    protected final EntityReader entity;

    public AiTask(EntityReader entity) {
        this.entity = entity;
    }


    final void registerListeners(AiContext aiContext) {
        aiContext.clientGameState.eventManager.listenForEventsFrom(aiContext.stack, entity.entityId);
        addExtraListeners(aiContext);
    }

    final void removeListeners(AiContext aiContext) {
        aiContext.clientGameState.eventManager.stopListeningTo(aiContext.stack, entity.entityId);
        removeExtraListeners(aiContext);
    }

    protected void addExtraListeners(AiContext aiContext) {}

    protected void removeExtraListeners(AiContext aiContext) {}

    @Override
    public abstract String toString();
}
