package client.event;

import client.app.ClientContext;
import client.ai.ActionRequester;
import client.state.RangeManager;
import common.AiEvent;
import common.state.EntityId;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class AiEventManager {

    private final ClientContext context;
    private final HashMap<EntityId, Set<AiEventListener>> listeners = new HashMap<>();
    private final ActionRequester requester;
    // todo
//    public RangeManager rangeManager;

    public AiEventManager(ClientContext context) {
        this.context = context;
        this.requester = new ActionRequester(context);
    }

    public void notifyListeners(final AiEvent event) {
        synchronized (listeners) { // less needed now...
            for (final AiEventListener listener : getListeners(event.entity)) {
                context.executorService.submit(() -> listener.receiveEvent(event, requester));
            }
        }
    }

    private Set<AiEventListener> getListeners(EntityId entity) {
        synchronized (listeners) {
            Set<AiEventListener> eventListeners = listeners.get(entity);
            if (eventListeners == null) {
                return Collections.emptySet();
            }
            return eventListeners;
        }
    }

    public void listenForEventsFrom(AiEventListener listener, EntityId entity) {
        synchronized (listeners) {
            Set<AiEventListener> eventListeners = listeners.get(entity);
            if (eventListeners == null) {
                eventListeners = new HashSet<>();
                listeners.put(entity, eventListeners);
            }
            eventListeners.add(listener);
        }
    }

    public void stopListeningTo(AiEventListener listener, EntityId entity) {
        synchronized (listeners) {
            Set<AiEventListener> eventListeners = listeners.get(entity);
            if (eventListeners == null) {
                return;
            }
            eventListeners.remove(listener);
            if (eventListeners.isEmpty()) {
                listeners.remove(entity);
            }
        }
    }

    public void entityMoved(EntityId unitId) {
        // do something with a range manager...
    }
}
