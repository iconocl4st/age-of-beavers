package client.ai.ai2;

import common.AiAttemptResult;
import common.Proximity;
import common.event.AiEventType;
import common.factory.SearchDestination;
import common.state.EntityReader;
import common.state.spec.ResourceType;
import common.state.sst.sub.GrowthInfo;
import common.state.sst.sub.Load;
import common.util.DPoint;

import java.util.*;

public class FarmAi extends DefaultAiTask {

    private final Object sync = new Object();

    private final TreeSet<EntityReader> allFarming = new TreeSet<>(EntityReader.COMPARATOR);
    private final TreeSet<EntityReader> toHarvest = new TreeSet<>(EntityReader.COMPARATOR);
    private final TreeSet<EntityReader> toTend = new TreeSet<>(EntityReader.COMPARATOR);
    private final TreeSet<EntityReader> toPlant = new TreeSet<>((e1, e2) -> {
        DPoint l1 = e1.getLocation();
        DPoint l2 = e2.getLocation();
        int cmp = Double.compare(l1.x, l2.x);
        if (cmp != 0) return cmp;
        return Double.compare(l1.y, l2.y);
    });

    private final Map<ResourceType, Integer> seeds = new HashMap<>();
    private final Map<ResourceType, Integer> collecting = new HashMap<>();
    private AiContext aiContextCache;
    private FarmingState currentState;
    private boolean isIdle;

    public FarmAi(EntityReader entity) {
        super(entity);
        currentState = FarmingState.Delivering;
        isIdle = true;
    }

    public void toggleFarming(EntityReader target) {
        synchronized (sync) {
            if (allFarming.contains(target)) {
                remove(target);
            } else {
                add(target);
            }

            if (isIdle)
                requestActions(aiContextCache);
        }
    }

    public void add(EntityReader farm) {
        synchronized (sync) {
            allFarming.add(farm);
            switch (farm.getGrowthInfo().currentState) {
                case Growing:
                    break;
                case NeedsTending:
                    toTend.add(farm);
                    break;
                case Ripe:
                    toHarvest.add(farm);
                    break;
                case ToBePlanted:
                case Expired:
                    toPlant.add(farm);
                    break;
                default:
                    throw new IllegalStateException();
            }

            ResourceType requiredSeed = farm.getType().requiredSeed;
            incrementCount(seeds, requiredSeed);
            for (ResourceType c : requiredSeed.growsInto.carrying.keySet())
                incrementCount(collecting, c);

            aiContextCache.clientGameState.eventManager.listenForEventsFrom(aiContextCache.stack, farm.entityId);
        }
    }

    public void remove(EntityReader farm) {
        synchronized (sync) {
            aiContextCache.clientGameState.eventManager.stopListeningTo(aiContextCache.stack, farm.entityId);

            allFarming.remove(farm);
            toHarvest.remove(farm);
            toTend.remove(farm);
            toPlant.remove(farm);

            ResourceType requiredSeed = farm.getType().requiredSeed;
            decrementCount(seeds, requiredSeed);
            for (ResourceType c : requiredSeed.growsInto.carrying.keySet())
                decrementCount(collecting, c);
        }
    }

    @Override
    protected AiAttemptResult growthChanged(
            AiContext aiContext,
            EntityReader farm,
            GrowthInfo cropInfo
    ) {
        aiContextCache = aiContext;
        synchronized (sync) {
            switch (cropInfo.currentState) {
                case Growing:
                    toHarvest.remove(farm);
                    toTend.remove(farm);
                    toPlant.remove(farm);
                    break;
                case NeedsTending:
                    toHarvest.remove(farm);
                    toTend.add(farm);
                    toPlant.remove(farm);
                    break;
                case ToBePlanted:
                case Expired:
                    toHarvest.remove(farm);
                    toTend.remove(farm);
                    toPlant.add(farm);
                    break;
                case Ripe:
                    toHarvest.add(farm);
                    toTend.remove(farm);
                    toPlant.remove(farm);
                    break;
            }
        }
        // Wait until the current action is done
        if (isIdle)
            return requestActions(aiContext);
        else
            return AiAttemptResult.NothingDone;
    }

    private boolean isFull(Map<ResourceType, Integer> desiredToCollect) {
        Map<ResourceType, Integer> amountOfResourceAbleToAccept = entity.getAmountOfResourceAbleToAccept();
        for (Map.Entry<ResourceType, Integer> entry : desiredToCollect.entrySet()) {
            if (entry.getValue() == null || entry.getValue() == 0)
                continue;
            if (amountOfResourceAbleToAccept.getOrDefault(entry.getKey(), 0) > 0)
                return false;
        }
        return true;
    }

    private boolean isEmpty() {
        for (Map.Entry<ResourceType, Integer> e : entity.getCarrying().quantities.entrySet()) {
            if (seeds.keySet().contains(e.getKey()))
                continue;
            if (e.getValue() != null && e.getValue() > 0)
                return false;
        }
        return true;
    }

    private boolean hasSeed(ResourceType seed) {
        return entity.getCarrying().quantities.getOrDefault(seed, 0) > 0;
    }

    @Override
    protected AiAttemptResult requestActions(AiContext aiContext) {
        synchronized (sync) {
            aiContext = aiContext.controlling(entity);
            aiContextCache = aiContext;
            AiAttemptResult result;

            stopListenToStorageResults(aiContext);
            isIdle = false;
            outer:
            while (true) {
                if (currentState.equals(FarmingState.Delivering)) {
                    if (isEmpty()) {
                        currentState = FarmingState.Farming;
                        continue;
                    }
                    result = OneTripTransport.deliverAllResources(aiContext); // , Collections.singleton(seed));
                    if (result.didSomething()) return result;
                    continue;
                }
                for (EntityReader farm : toHarvest) {
                    if (isFull(farm.getCarrying().quantities)) {
                        currentState = FarmingState.Delivering;
                        continue outer;
                    }

                    result = OneTripTransport.pickupAllResources(aiContext, farm);
                    if (result.didSomething()) return result;
                }

                for (EntityReader farm : toTend) {
                    if (Proximity.closeEnoughToInteract(entity, farm)) {
                        aiContext.requester.setUnitActionToGarden(entity, farm);
                        return AiAttemptResult.RequestedAction;
                    }
                    result = aiContext.requester.setUnitActionToMove(aiContext.clientGameState.pathFinder, entity, new SearchDestination(farm));
                    if (result.didSomething()) return result;
                }

                for (EntityReader farm : toPlant) {
                    ResourceType requiredSeed = farm.getType().requiredSeed;
                    if (isFull(Collections.singletonMap(requiredSeed, 1))) {
                        currentState = FarmingState.Delivering;
                        continue outer;
                    }
                    if (!hasSeed(requiredSeed)) {
                        result = OneTripTransport.pickupCollectedResource(aiContext, requiredSeed, toPlant.size());
                        if (result.didSomething()) return result;
                        listenToStorageResults(aiContext);
                        break;
                    }
                    if (Proximity.closeEnoughToInteract(entity, farm)) {
                        aiContext.requester.setUnitActionToPlant(entity, farm);
                        return AiAttemptResult.RequestedAction;
                    }
                    return aiContext.requester.setUnitActionToMove(aiContext.clientGameState.pathFinder, entity, new SearchDestination(farm));

                }
                break;
            }
            isIdle = true;
        }
        return AiAttemptResult.NothingDone;
    }

    private void stopListenToStorageResults(AiContext aiContext) {
        aiContext.clientGameState.eventManager.stopListeningTo(aiContext.stack, AiEventType.ResourceChange);
    }

    private void listenToStorageResults(AiContext aiContext) {
        aiContext.clientGameState.eventManager.listenForEvents(aiContext.stack, AiEventType.ResourceChange);
    }

    @Override
    public String toString() {
        return "Farming " + collect(seeds.keySet());
    }

    private static String collect(Set<ResourceType> resources) {
        StringBuilder builder = new StringBuilder();
        for (ResourceType rt : resources)
            builder.append(rt.name).append(' ');
        return builder.toString();
    }

    private static void incrementCount(Map<ResourceType, Integer> map, ResourceType rt) {
        map.put(rt, map.getOrDefault(rt, 0) + 1);
    }

    private static void decrementCount(Map<ResourceType, Integer> map, ResourceType rt) {
        Integer integer = map.get(rt);
        if (integer == null) {
            throw new IllegalStateException();
        }
        if (integer == 1) {
            map.remove(rt);
        } else {
            map.put(rt, integer - 1);
        }
    }

    @Override
    protected void addExtraListeners(AiContext aiContext) {
        aiContextCache = aiContext.controlling(entity);
        synchronized (sync) {
            for (EntityReader farm : allFarming)
                aiContext.clientGameState.eventManager.listenForEventsFrom(aiContext.stack, farm.entityId);
        }
    }

    @Override
    protected void removeExtraListeners(AiContext aiContext) {
        aiContextCache = aiContext.controlling(entity);
        synchronized (sync) {
            for (EntityReader farm : allFarming)
                aiContext.clientGameState.eventManager.stopListeningTo(aiContext.stack, farm.entityId);
        }
    }

    public Set<EntityReader> getFarming() {
        return new HashSet<>(allFarming);
    }

    private enum FarmingState {
        Delivering,
        Farming,
    }
}
