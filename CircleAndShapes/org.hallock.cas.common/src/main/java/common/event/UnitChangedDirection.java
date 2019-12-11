package common.event;

import common.msg.Message;

public class UnitChangedDirection extends AiEvent {
    public final Message.DirectedLocationChange msg;

    public UnitChangedDirection(Message.DirectedLocationChange msg) {
        super(msg.entity, AiEventType.UnitChangedDirection);
        this.msg = msg;
    }
}
