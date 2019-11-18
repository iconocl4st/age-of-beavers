package client.event;

import client.state.ClientGameState;
import common.AiEvent;
import common.state.EntityId;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class AiEventManager {

    private final ClientGameState context;
    private final HashMap<EntityId, Set<AiEventListener>> listenersByEntity = new HashMap<>();
    private final HashMap<AiEvent.EventType, Set<AiEventListener>> listenersByType = new HashMap<>();
    private final ExecutorService executorService;

    // todo
//    public RangeManager rangeManager;

    public AiEventManager(ClientGameState context, ExecutorService service) {
        this.context = context;
        this.executorService = service;
    }

    private static final Set<AiEventListener> EMPTY_LISTENERS = Collections.emptySet();

    public void notifyListeners(final AiEvent event) {
        synchronized (listenersByEntity) { // less needed now...
            for (final AiEventListener listener : listenersByEntity.getOrDefault(event.entity, EMPTY_LISTENERS)) {
                executorService.submit(() -> listener.receiveEvent(event, context.actionRequester));
            }
            for (final AiEventListener listener : listenersByType.getOrDefault(event.type, EMPTY_LISTENERS)) {
                executorService.submit(() -> listener.receiveEvent(event, context.actionRequester));
            }
        }
    }

    public void listenForEventsFrom(AiEventListener listener, EntityId entity) {
        synchronized (listenersByEntity) {
            listenersByEntity.computeIfAbsent(entity, k -> new HashSet<>()).add(listener);
        }
    }

    public void listenForEvents(AiEventListener listener, AiEvent.EventType type) {
        synchronized (listenersByEntity) {
            listenersByType.computeIfAbsent(type, k -> new HashSet<>()).add(listener);
        }
    }

    public void stopListeningTo(AiEventListener listener, EntityId entity) {
        synchronized (listenersByEntity) {
            Set<AiEventListener> eventListeners = listenersByEntity.get(entity);
            if (eventListeners == null) {
                return;
            }
            eventListeners.remove(listener);
            if (eventListeners.isEmpty()) {
                listenersByEntity.remove(entity);
            }
        }
    }
    public void stopListeningTo(AiEventListener listener, AiEvent.EventType type) {
        synchronized (listenersByEntity) {
            Set<AiEventListener> eventListeners = listenersByType.get(type);
            if (eventListeners == null) {
                return;
            }
            eventListeners.remove(listener);
            if (eventListeners.isEmpty()) {
                listenersByType.remove(type);
            }
        }
    }

    public void entityMoved(EntityId unitId) {
        // do something with a range manager...
    }
}
