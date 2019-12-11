package common.event;

import common.state.EntityId;

public class UniRemovedEvent extends AiEvent {
    public UniRemovedEvent(EntityId entity) {
        super(entity, AiEventType.UnitRemoved);
    }
}
