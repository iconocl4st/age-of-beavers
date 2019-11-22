package client.event.supply;

import client.ai.ai2.AiContext;
import client.ai.ai2.OneTripTransport;
import client.ai.ai2.TransportAi;
import client.event.AiEventListener;
import client.state.ClientGameState;
import common.AiAttemptResult;
import common.event.AiEvent;
import common.event.AiEventType;
import common.event.BuildingPlacementChanged;
import common.event.DemandsChanged;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.spec.GameSpec;
import common.state.spec.ResourceType;
import common.state.sst.sub.Load;
import common.state.sst.sub.capacity.Prioritization;
import common.state.sst.sub.capacity.PrioritizedCapacitySpec;
import common.util.ConcurrentModificationDebugingSet;

import java.util.*;

public class SupplyAndDemandManager implements AiEventListener {
    private final Object sync = new Object();
    private final HashMap<ResourceType, Set<EntityId>> currentlyExceeding = new HashMap<>();
    private final HashMap<ResourceType, /*Tree*/Set<TransportRequest>> excess = new HashMap<>();
    private final HashMap<ResourceType, Set<EntityId>> currentlyDemanding = new HashMap<>();
    private final HashMap<ResourceType, /*Tree*/Set<TransportRequest>> demands = new HashMap<>();

    private final ClientGameState context;
    private final GameSpec gameSpec;

    public SupplyAndDemandManager(ClientGameState context, GameSpec gameSpec) {
        this.context = context;
        this.gameSpec = gameSpec;
        initialize(gameSpec);
    }


    public void stopServicing(TransportAi transportAi, Transport transport) {
        remove(transport.getRequester());
        update(transport.getRequester());
    }

    public Transport commitToNextTransportationRequest(TransportAi transportAi, Runnable onTransport, Runnable onNone) {
        synchronized (sync) {
            for (TransportRequest request : getExceeding()) {
                if (request.servicer != null)
                    continue;
                onTransport.run();
                request.servicer = transportAi;
                context.executor.submit(() -> context.eventManager.notifyListeners(new DemandsChanged(request.requester.entityId)));
                return new ExceedingTransport(request);
            }

            for (TransportRequest request : getDemands()) {
                if (request.servicer != null)
                    continue;
                onTransport.run();
                request.servicer = transportAi;
                context.executor.submit(() -> context.eventManager.notifyListeners(new DemandsChanged(request.requester.entityId)));
                return new DemandTransport(request);
            }

            onNone.run();
        }
        return null;
    }

    public void initialize(GameSpec gameSpec) {
        synchronized (sync) {
            for (ResourceType resourceType : gameSpec.resourceTypes) {
                currentlyExceeding.put(resourceType, new ConcurrentModificationDebugingSet<>(new HashSet<>()));
                excess.put(resourceType, new ConcurrentModificationDebugingSet<>(new TreeSet<>()));
                currentlyDemanding.put(resourceType, new ConcurrentModificationDebugingSet<>(new HashSet<>()));
                demands.put(resourceType, new ConcurrentModificationDebugingSet<>(new TreeSet<>()));
            }
        }
    }

    private Set<TransportRequest> getAll(Map<ResourceType, Set<TransportRequest>> map) {
        synchronized (sync) {
            if (gameSpec == null) {
                return Collections.emptySet();
            }
            Set<TransportRequest> ret = new TreeSet<>();
            for (ResourceType resourceType : gameSpec.resourceTypes) {
                ret.addAll(map.get(resourceType));
            }
            return ret;
        }
    }

    public Set<TransportRequest> getExceeding() {
        return getAll(excess);
    }

    public Set<TransportRequest> getDemands() {
        return getAll(demands);
    }

    private boolean updateExceeding(EntityReader entity, ResourceType resource, int currentlyCarring, Prioritization prioritization) {
        if (currentlyCarring <= prioritization.desiredMaximum) {
            return removeFromExceeding(entity, resource);
        }
        if (currentlyExceeding.get(resource).contains(entity.entityId))
            return false;
        currentlyExceeding.get(resource).add(entity.entityId);
        excess.get(resource).add(
                new TransportRequest(
                        entity,
                        resource,
                        prioritization.priority,
                        context.gameState.currentTime
                )
        );
        return true;
    }

    private boolean updateDemanding(EntityReader entity, ResourceType resource, int currentlyCarrying, Prioritization prioritization, int possibleToAccept) {
        if (currentlyCarrying >= prioritization.desiredAmount || possibleToAccept <= 0) {
            return removeFromDemanding(entity, resource);
        }
        if (currentlyDemanding.get(resource).contains(entity.entityId))
            return false;
        currentlyDemanding.get(resource).add(entity.entityId);
        demands.get(resource).add(
                new TransportRequest(
                        entity,
                        resource,
                        prioritization.priority,
                        context.gameState.currentTime
                )
        );
        return true;
    }

    private boolean update(EntityReader reader, PrioritizedCapacitySpec capacity, Load carrying, ResourceType resource) {
        int currentlyCarring = carrying.quantities.getOrDefault(resource, 0);
        Prioritization prioritization = capacity.getPrioritization(resource);
        int amountPossibleToAccept = capacity.amountPossibleToAccept(carrying, resource);
        boolean hasUpdate = false;
        hasUpdate |= updateExceeding(reader, resource, currentlyCarring, prioritization);
        hasUpdate |= updateDemanding(reader, resource, currentlyCarring, prioritization, amountPossibleToAccept);
        return hasUpdate;
    }

    public void update(EntityReader reader) {
        final Object entitySync = reader.getSync();
        PrioritizedCapacitySpec capacity;
        Load carrying;
        synchronized (entitySync) {
            if (reader.noLongerExists() || !reader.getType().containsClass("storage") || !context.entityTracker.isTracking(reader)) {
                remove(reader);
                return;
            }
            capacity = reader.getCapacity();
            carrying = reader.getCarrying();
        }
        boolean hasUpdate = false;
        synchronized (sync) {
            for (ResourceType resourceType : gameSpec.resourceTypes) {
                hasUpdate |= update(reader, capacity, carrying, resourceType);
            }
        }
        if (hasUpdate) {
            context.eventManager.notifyListeners(new DemandsChanged(reader.entityId));
        }
    }

    private boolean removeFromExceeding(EntityReader entity, ResourceType resource) {
        if (!currentlyExceeding.get(resource).remove(entity.entityId)) return false;
        excess.get(resource).removeIf(t -> t.requester.entityId.equals(entity.entityId));
        return true;
    }
    private boolean removeFromDemanding(EntityReader entity, ResourceType resource) {
        if (!currentlyDemanding.get(resource).remove(entity.entityId)) return false;
        demands.get(resource).removeIf(t -> t.requester.entityId.equals(entity.entityId));
        return true;
    }
    public void remove(EntityReader unitId) {
        boolean hasUpdate = false;
        synchronized (sync) {
            for (ResourceType resourceType: gameSpec.resourceTypes) {
                hasUpdate |= removeFromExceeding(unitId, resourceType);
                hasUpdate |= removeFromDemanding(unitId, resourceType);
            }
        }
        if (hasUpdate) {
            context.eventManager.notifyListeners(new DemandsChanged(unitId.entityId));
        }
    }

    @Override
    public void receiveEvent(AiContext aiContext, AiEvent event) {
        if (!event.type.equals(AiEventType.BuildingPlacementChanged))
            return;
        BuildingPlacementChanged buildingPlacementChanged = (BuildingPlacementChanged) event;
        if (buildingPlacementChanged.constructionZone == null) {
            return;
        }
        EntityReader reader = new EntityReader(context.gameState, buildingPlacementChanged.constructionZone);
        context.entityTracker.track(reader);
        update(reader);
    }
}
