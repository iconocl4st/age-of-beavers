package client.event;

import client.ai.ai2.AiContext;
import common.event.AiEvent;

public interface AiEventListener {
    void receiveEvent(AiContext aiContext, AiEvent event);
}
