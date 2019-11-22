package common.event;

import common.state.EntityId;

public class InitializeAi extends AiEvent {
    public InitializeAi(EntityId entity) {
        super(entity, AiEventType.Initialize);
    }
}
