package client.ai.ai2;

import common.AiAttemptResult;
import common.event.GrowthStageChanged;
import common.factory.SearchDestination;
import common.state.EntityReader;
import common.state.spec.CarrySpec;
import common.state.spec.ResourceType;
import common.util.DPoint;

import java.awt.*;
import java.util.*;

public class FarmAi extends DefaultAiTask {

    private final TreeSet<EntityReader> toHarvest = new TreeSet<>(EntityReader.COMPARATOR);
    private final TreeSet<EntityReader> toTend = new TreeSet<>(EntityReader.COMPARATOR);
    private final TreeSet<Point> toPlant = new TreeSet<>((p1, p2) -> {
        int cmp = Integer.compare(p1.x, p2.x);
        if (cmp != 0) return cmp;
        return Integer.compare(p1.y, p2.y);
    });

    private final ResourceType seed;
    private final Set<ResourceType> collecting = new HashSet<>(); // Could be passed in...
    private FarmingState currentState;

    protected FarmAi(EntityReader entity, Collection<Point> where, ResourceType seed) {
        super(entity);
        if (seed.growsInto == null)
            throw new IllegalStateException("Cannot farm " + seed.name);
        this.seed = seed;
        this.toPlant.addAll(where);
        for (CarrySpec s : seed.growsInto.carrying)
            collecting.add(s.type);
    }

    protected AiAttemptResult growthChanged(AiContext aiContext, EntityReader plant, int x, int y, GrowthStageChanged.GrowthStage newStage) {
        switch (newStage) {
            case Growing:
                toHarvest.remove(plant);
                toTend.remove(plant);
                toPlant.remove(new Point(x, y));
                break;
            case NeedsTending:
                toHarvest.remove(plant);
                toTend.add(plant);
                toPlant.remove(new Point(x, y));
                break;
            case ToBePlanted:
            case Expired:
                toHarvest.remove(plant);
                toTend.remove(plant);
                toPlant.add(new Point(x, y));
                break;
            case Ripe:
                toHarvest.add(plant);
                toTend.remove(plant);
                toPlant.remove(new Point(x, y));
                break;
        }
        return requestActions(aiContext);
    }

    private boolean isFull() {
        Map<ResourceType, Integer> amountOfResourceAbleToAccept = entity.getAmountOfResourceAbleToAccept();
        for (ResourceType rt : collecting) {
            if (amountOfResourceAbleToAccept.getOrDefault(rt, 0) > 0)
                return false;
        }
        return true;
    }

    private boolean isEmpty() {
        for (Map.Entry<ResourceType, Integer> e : entity.getCarrying().quantities.entrySet()) {
            if (e.getKey().equals(seed))
                continue;
            if (e.getValue() != null && e.getValue() > 0)
                return false;
        }
        return true;
    }

    private boolean hasSeed() {
        Integer amt = entity.getCarrying().quantities.getOrDefault(seed, 0);
        return amt != null && amt > 0;
    }

    @Override
    protected AiAttemptResult requestActions(AiContext aiContext) {
        aiContext = aiContext.controlling(entity);
        AiAttemptResult result;

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
            for (EntityReader p : toHarvest) {
                if (isFull()) {
                    currentState = FarmingState.Delivering;
                    continue outer;
                }

                result = OneTripTransport.pickupAllResources(aiContext, p);
                if (result.didSomething()) return result;
            }

            for (EntityReader plant : toTend) {
                result = aiContext.requester.setUnitActionToMove(aiContext.clientGameState.pathFinder, entity, new SearchDestination(plant));
                if (result.didSomething()) return result;
                aiContext.requester.setUnitActionToGarden(entity, plant);
                return AiAttemptResult.RequestedAction;
            }

            for (Point p : toPlant) {
                if (isFull()) {
                    currentState = FarmingState.Delivering;
                    continue outer;
                }
                if (!hasSeed()) {
                    result = OneTripTransport.pickupCollectedResource(aiContext, seed, toPlant.size());
                    if (result.didSomething()) return result;
                    break;
                }
                result = aiContext.requester.setUnitActionToMove(aiContext.clientGameState.pathFinder, entity, new SearchDestination(new DPoint(p)));
                if (result.didSomething()) return result;
                aiContext.requester.setUnitActionToPlant(entity, seed);

            }
            break;
        }
        return AiAttemptResult.NothingDone;
    }

    @Override
    public String toString() {
        return "Farming " + seed.name;
    }




    private enum FarmingState {
        Delivering,
        Farming,
    }
}
