package client.event.supply;

import client.ai.TransportAi;
import client.state.ClientGameState;
import common.AiEvent;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.spec.GameSpec;
import common.state.spec.ResourceType;
import common.state.sst.sub.Load;
import common.state.sst.sub.capacity.Prioritization;
import common.state.sst.sub.capacity.PrioritizedCapacitySpec;

import java.util.*;

public class SupplyAndDemandManager {

    private final Object sync = new Object();
    private final HashMap<ResourceType, Set<EntityId>> currentlyExceeding = new HashMap<>();
    private final HashMap<ResourceType, TreeSet<TransportRequest>> excess = new HashMap<>();
    private final HashMap<ResourceType, Set<EntityId>> currentlyDemanding = new HashMap<>();
    private final HashMap<ResourceType, TreeSet<TransportRequest>> demands = new HashMap<>();
    private final HashMap<EntityId, TransportAi> transports = new HashMap<>();

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

    public Transport commitToNextTransportationRequest(TransportAi transportAi) {
        synchronized (sync) {
            for (TransportRequest request : getExceeding()) {
                if (request.servicer != null)
                    continue;
                request.servicer = transportAi;
                context.eventManager.notifyListeners(new AiEvent.DemandsChanged(request.requester.entityId));
                return new ExceedingTransport(request);
            }

            for (TransportRequest request : getDemands()) {
                if (request.servicer != null)
                    continue;
                request.servicer = transportAi;
                context.eventManager.notifyListeners(new AiEvent.DemandsChanged(request.requester.entityId));
                return new DemandTransport(request);
            }
        }
        return null;
    }

    public void initialize(GameSpec gameSpec) {
        synchronized (sync) {
            for (ResourceType resourceType : gameSpec.resourceTypes) {
                currentlyExceeding.put(resourceType, new HashSet<>());
                excess.put(resourceType, new TreeSet<>());
                currentlyDemanding.put(resourceType, new HashSet<>());
                demands.put(resourceType, new TreeSet<>());
            }
        }
    }

    private Set<TransportRequest> getAll(HashMap<ResourceType, TreeSet<TransportRequest>> map) {
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
        Object sync = reader.getSync();
        synchronized (sync) {
            if (reader.noLongerExists() || !reader.getType().containsClass("storage") || !reader.getOwner().equals(context.currentPlayer)) {
                remove(reader);
                return;
            }
            PrioritizedCapacitySpec capacity = reader.getCapacity();
            Load carrying = reader.getCarrying();
            boolean hasUpdate = false;
            for (ResourceType resourceType : gameSpec.resourceTypes) {
                hasUpdate |= update(reader, capacity, carrying, resourceType);
            }
            if (hasUpdate) {
                context.eventManager.notifyListeners(new AiEvent.DemandsChanged(reader.entityId));
            }
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
            context.eventManager.notifyListeners(new AiEvent.DemandsChanged(unitId.entityId));
        }
    }
}
