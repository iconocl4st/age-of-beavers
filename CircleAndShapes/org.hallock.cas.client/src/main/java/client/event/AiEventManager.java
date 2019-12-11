package client.event;

import client.ai.ai2.AiContext;
import client.ai.ai2.AiLocator;
import client.state.ClientGameState;
import common.event.AiEvent;
import common.event.AiEventType;
import common.event.AlarmEvent;
import common.state.EntityId;
import common.util.ExecutorServiceWrapper;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class AiEventManager {

    private static final long BELL_FREQUENCY = 10000;

    private final Timer timer;
    private final ClientGameState context;
    private final HashMap<EntityId, Set<AiEventListener>> listenersByEntity = new HashMap<>();
    private final HashMap<AiEventType, Set<AiEventListener>> listenersByType = new HashMap<>();
    private final HashSet<EventManagerListener> listenerListeners = new HashSet<>();
    private final ExecutorServiceWrapper executorService;

    public AiEventManager(ClientGameState context, ExecutorServiceWrapper service) {
        this.context = context;
        this.executorService = service;
        timer = new Timer();
        timer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        notifyListeners(new AlarmEvent());
                    }
                },
                BELL_FREQUENCY,
                BELL_FREQUENCY
        );
    }

    public AiContext createAiContext() {
        AiContext aiContext = new AiContext();
        aiContext.clientGameState = context;
        aiContext.gameState = context.gameState;
        aiContext.requester = context.actionRequester;
        aiContext.locator = new AiLocator(context);
        return aiContext;
    }

    public void notifyListeners(final AiEvent event) {
        notifyListeners(event, null);
    }
    public void notifyListeners(final AiEvent event, Runnable onComplete) {
        AiContext context = createAiContext();
        switch (event.type) {
            case ActionCompleted:
            case UnitChangedDirection:
            case ResourceChange:
            case Bell:
                break;
                // target killed?
                // there are too many demands changed messages...
            default:
                System.out.println("Event Triggered: " + event.type.name());
        }

        HashSet<AiEventListener> listenersToNotify = new HashSet<>();
        synchronized (listenersByEntity) {
            listenersToNotify.addAll(listenersByEntity.getOrDefault(event.entity, EMPTY_LISTENERS));
            listenersToNotify.addAll(listenersByType.getOrDefault(event.type, EMPTY_LISTENERS));
        }

        if (onComplete == null) {
            for (AiEventListener listener : listenersToNotify) {
                executorService.submit(() -> listener.receiveEvent(context, event));
            }
            return;
        }
        if (listenersToNotify.isEmpty()) {
            onComplete.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(listenersToNotify.size());
        for (AiEventListener listener : listenersToNotify) {
            executorService.submit(() -> {
                try {
                    listener.receiveEvent(context, event);
                } finally {
                    latch.countDown();
                    synchronized (latch) {
                        if (latch.getCount() == 0) {
                            onComplete.run();
                        }
                    }
                }
            });
        }
    }

    public void listenToListeners(EventManagerListener listener) {
        synchronized (listenersByEntity) {
            listenerListeners.add(listener);
        }
    }

    public void listenForEventsFrom(AiEventListener listener, EntityId entity) {
        synchronized (listenersByEntity) {
            listenersByEntity.computeIfAbsent(entity, k -> new HashSet<>()).add(listener);
        }
        notifyListenerListeners();
    }

    public void listenForEvents(AiEventListener listener, AiEventType type) {
        synchronized (listenersByEntity) {
            listenersByType.computeIfAbsent(type, k -> new HashSet<>()).add(listener);
        }
        notifyListenerListeners();
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
        notifyListenerListeners();
    }

    public void stopListeningTo(AiEventListener listener, AiEventType type) {
        removeFrom(listener, type, listenersByType);
        notifyListenerListeners();
    }

    private void notifyListenerListeners() {
        synchronized (listenersByEntity) {
            for (EventManagerListener listener : listenerListeners)
                executorService.submit(() -> listener.showDebugView(createDebugView()));
        }
    }

    private EventManagerListener.EventsDebugView createDebugView() {
        EventManagerListener.EventsDebugView ret = new EventManagerListener.EventsDebugView();
        synchronized (listenersByEntity) {
//            public final RangeManager rangeManager = new RangeManager();
            for (Map.Entry<EntityId, Set<AiEventListener>> entry : listenersByEntity.entrySet())
                ret.listenersByEntity.computeIfAbsent(entry.getKey(), e -> new HashSet<>()).addAll(entry.getValue());
            for (Map.Entry<AiEventType, Set<AiEventListener>> entry : listenersByType.entrySet())
                ret.listenersByType.computeIfAbsent(entry.getKey(), e -> new HashSet<>()).addAll(entry.getValue());
        }
        return ret;
    }

    private static final Set<AiEventListener> EMPTY_LISTENERS = Collections.emptySet();
}
