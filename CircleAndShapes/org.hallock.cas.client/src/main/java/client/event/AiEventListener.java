package client.event;

import client.ai.ActionRequester;
import common.AiEvent;

public interface AiEventListener {
    void receiveEvent(AiEvent event, ActionRequester ar);
}
