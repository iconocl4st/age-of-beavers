package common.event;

import common.state.EntityId;

public class AlarmEvent extends AiEvent {
    // TODO: There should be no need for this class.
    // I am adding this for the transport ai, which appears to be stuck.
    // I am not sure why it is needed for the transport ai...

    // Fixed a couple of bugs, maybe this is no longer needed.
    // check the transport ai...
    public AlarmEvent() {
        super(EntityId.NONE, AiEventType.Bell);
    }
}
