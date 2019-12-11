package client.event;

import common.event.AiEventType;
import common.state.EntityId;

import java.util.HashMap;
import java.util.Set;

public interface EventManagerListener {

    void showDebugView(EventsDebugView view);

    class EventsDebugView {
        public final HashMap<EntityId, Set<AiEventListener>> listenersByEntity = new HashMap<>();
        public final HashMap<AiEventType, Set<AiEventListener>> listenersByType = new HashMap<>();
    }
}
