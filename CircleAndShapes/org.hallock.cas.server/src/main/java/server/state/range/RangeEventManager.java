package server.state.range;

import common.event.TargetWithinRange;
import common.msg.ConnectionWriter;
import common.msg.Message;
import common.state.EntityId;
import common.state.EntityReader;
import common.util.ExecutorServiceWrapper;
import server.state.ServerGameState;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class RangeEventManager {

    private final HashMap<EntityId, Set<ConnectionWriter>> listenersByEntity = new HashMap<>();
    private final ExecutorServiceWrapper executorService;
    private final RangeManager rangeManager = new RangeManager();
    private final ServerGameState serverGameState;

    public RangeEventManager(ServerGameState serverGameState, ExecutorServiceWrapper service) {
        this.serverGameState = serverGameState;
        this.executorService = service;
    }

    private void notifyListeners(TargetWithinRange event) {
        Message.AiEventMessage msg = new Message.AiEventMessage(event);
        synchronized (listenersByEntity) {
            for (final ConnectionWriter listener : listenersByEntity.getOrDefault(event.entity, EMPTY_LISTENERS)) {
                executorService.submit(() -> {
                    try {
                        listener.send(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    public void entityMoved(EntityReader unitId) {
        synchronized (listenersByEntity) {
            for (InRangeEvent listener : rangeManager.entityMoved(unitId)) {
                if (listenersByEntity.containsKey(listener.entity1.entityId)) {
                    notifyListeners(new TargetWithinRange(listener.entity1.entityId, listener.entity2.entityId, listener.range));
                }
                if (listenersByEntity.containsKey(listener.entity2.entityId)) {
                    notifyListeners(new TargetWithinRange(listener.entity2.entityId, listener.entity1.entityId, listener.range));
                }
            }
        }
    }

    public void listenForRangeEventsFrom(ConnectionWriter chase, EntityId entity, EntityId target, double radius) {
        rangeManager.listenTo(new EntityReader(serverGameState.state, entity), new EntityReader(serverGameState.state, target), radius);
        synchronized (listenersByEntity) {
            listenersByEntity.computeIfAbsent(entity, e -> new HashSet<>()).add(chase);
        }
    }

    public void stopListeningToRangeEvents(ConnectionWriter chase, EntityId entityId) {
        rangeManager.remove(new EntityReader(serverGameState.state, entityId));
        synchronized (listenersByEntity) {
            Set<ConnectionWriter> eventListeners = listenersByEntity.get(entityId);
            if (eventListeners == null) {
                return;
            }
            eventListeners.remove(chase);
            if (eventListeners.isEmpty())
                listenersByEntity.remove(entityId);
        }
    }

    private static final Set<ConnectionWriter> EMPTY_LISTENERS = Collections.emptySet();
}
