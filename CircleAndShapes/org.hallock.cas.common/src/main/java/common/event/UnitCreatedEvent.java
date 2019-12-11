package common.event;

import common.state.EntityId;

public class UnitCreatedEvent extends AiEvent {
    public UnitCreatedEvent(EntityId entity) {
        super(entity, AiEventType.UnitCreated);
    }
}
