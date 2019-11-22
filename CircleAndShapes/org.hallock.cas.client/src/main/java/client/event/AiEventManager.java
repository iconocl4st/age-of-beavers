package client.event;

import client.ai.ai2.AiContext;
import client.ai.ai2.AiLocator;
import client.state.ClientGameState;
import common.event.AiEvent;
import common.event.AiEventType;
import common.event.TargetWithinRange;
import common.state.EntityId;
import common.state.EntityReader;
import common.util.ExecutorServiceWrapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class AiEventManager {

    private final ClientGameState context;
    private final HashMap<EntityId, Set<AiEventListener>> listenersByEntity = new HashMap<>();
    private final HashMap<AiEventType, Set<AiEventListener>> listenersByType = new HashMap<>();
    private final ExecutorServiceWrapper executorService;
    private final RangeManager rangeManager = new RangeManager();

    public AiEventManager(ClientGameState context, ExecutorServiceWrapper service) {
        this.context = context;
        this.executorService = service;
    }

    public AiContext createAiContext() {
        AiContext aiContext = new AiContext();
        aiContext.clientGameState = context;
        aiContext.gameState = context.gameState;
        aiContext.requester = context.actionRequester;
        aiContext.locator = new AiLocator(context);
        return aiContext;
    }

    private static final Set<AiEventListener> EMPTY_LISTENERS = Collections.emptySet();

    public void notifyListeners(final AiEvent event) {
        AiContext context = createAiContext();
        synchronized (listenersByEntity) {
            for (final AiEventListener listener : listenersByEntity.getOrDefault(event.entity, EMPTY_LISTENERS)) {
                executorService.submit(() -> listener.receiveEvent(context, event));
            }
            for (final AiEventListener listener : listenersByType.getOrDefault(event.type, EMPTY_LISTENERS)) {
                executorService.submit(() -> listener.receiveEvent(context, event));
            }
        }
    }

    public void listenForEventsFrom(AiEventListener listener, EntityId entity) {
        synchronized (listenersByEntity) {
            listenersByEntity.computeIfAbsent(entity, k -> new HashSet<>()).add(listener);
        }
    }

    public void listenForEvents(AiEventListener listener, AiEventType type) {
        synchronized (listenersByEntity) {
            listenersByType.computeIfAbsent(type, k -> new HashSet<>()).add(listener);
        }
    }

    private <T> void removeFrom(AiEventListener listener, T t, HashMap<T, Set<AiEventListener>> map) {
        synchronized (listenersByEntity) {
            Set<AiEventListener> eventListeners = map.get(t);
            if (eventListeners == null) {
                return;
            }
            eventListeners.remove(listener);
            if (eventListeners.isEmpty()) {
                map.remove(t);
            }
        }
    }

    public void stopListeningTo(AiEventListener listener, EntityId entity) {
        removeFrom(listener, entity, listenersByEntity);
    }
    public void stopListeningTo(AiEventListener listener, AiEventType type) {
        removeFrom(listener, type, listenersByType);
    }

    public void entityMoved(EntityReader unitId) {
        synchronized (listenersByEntity) {
            for (RangeManager.InRangeEvent listener : rangeManager.entityMoved(unitId)) {
                if (listenersByEntity.containsKey(listener.entity1.entityId)) {
                    notifyListeners(new TargetWithinRange(listener.entity1, listener.entity2, listener.range));
                }
                if (listenersByEntity.containsKey(listener.entity2.entityId)) {
                    notifyListeners(new TargetWithinRange(listener.entity2, listener.entity1, listener.range));
                }
            }
        }
    }

    public void listenForRangeEventsFrom(AiEventListener chase, EntityReader entity, EntityReader target, double radius) {
        rangeManager.listenTo(entity, target, radius);
        listenForEventsFrom(chase, entity.entityId);

    }
    public void stopListeningToRangeEvents(AiEventListener chase, EntityReader entity) {
        // target for good measure
        rangeManager.remove(entity);
        stopListeningTo(chase, entity.entityId);
    }
}
