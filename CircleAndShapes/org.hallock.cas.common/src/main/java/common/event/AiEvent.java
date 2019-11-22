package common.event;

import common.state.EntityId;

public abstract class AiEvent {

    public final AiEventType type;
    public final EntityId entity;

    protected AiEvent(EntityId entity, AiEventType type) {
        this.entity = entity;
        this.type = type;
    }
}
