package common.event;

import common.state.EntityId;
import common.state.EntityReader;

public class TargetWithinRange extends AiEvent {
    public final EntityReader listener;
    public final EntityReader target;
    public final double range;

    public TargetWithinRange(EntityReader listener, EntityReader target, double range) {
        super(listener.entityId, AiEventType.TargetWithinRange);
        this.listener = listener;
        this.target = target;
        this.range = range;
    }
}
