package client.state;

import client.ai.ai2.AiContext;
import client.event.AiEventListener;
import common.event.AiEvent;
import common.event.AiEventType;
import common.event.BuildingPlacementChanged;
import common.event.ProductionComplete;
import common.state.EntityReader;
import common.state.Player;
import common.state.sst.manager.RevPair;
import common.util.DPoint;

import java.util.*;

public class EntityTracker implements AiEventListener {

    private final ClientGameState clientGameState;
    private final Set<EntityReader> currentlyTracking = new HashSet<>();
    private final HashMap<String, Set<EntityReader>> byClass = new HashMap<>();

    EntityTracker(ClientGameState gameState) {
        this.clientGameState = gameState;
    }

    public void track(EntityReader reader) {
        currentlyTracking.add(reader); // should be synchronized, but we can probably get away without it...
    }

    private Set<EntityReader> getByClass(String clazz) {
        Set<EntityReader> ret = byClass.get(clazz);
        if (ret == null) return Collections.emptySet();
        return ret;
    }

    public void updateTracked() {
        synchronized (currentlyTracking) {
            for (RevPair<Player> pair : clientGameState.gameState.playerManager.getByType(clientGameState.currentPlayer))
                track(new EntityReader(clientGameState.gameState, pair.entityId));
            int preSize = currentlyTracking.size();
            currentlyTracking.removeIf(EntityReader::noLongerExists); // TODO: what if it is only out of sight...
            if (currentlyTracking.size() < preSize) {
                System.out.println("why? 2737243572457");
            }
            byClass.clear();
            for (EntityReader reader : currentlyTracking)
                for (String clazz : reader.getType().classes)
                    byClass.computeIfAbsent(clazz, e -> new HashSet<>()).add(reader);
        }
    }

    public Set<EntityReader> getTrackedByClass(String storage) {
        updateTracked(); //  TODO: do this less frequently
        return new HashSet<>(getByClass(storage));
    }

    public TreeSet<EntityReader> getTracked(String clazz, DPoint location) {
        updateTracked(); // TODO: do this less frequently
        TreeSet<EntityReader> ret = new TreeSet<>(Comparator.comparingDouble(e1 -> location.distanceTo(e1.getCenterLocation())));
        ret.addAll(getByClass(clazz));
        return ret;
    }

    public TreeSet<EntityReader> getTracked(String[] classes, DPoint location) {
        updateTracked(); // TODO: do this less frequently
        TreeSet<EntityReader> ret = new TreeSet<>(Comparator.comparingDouble(e1 -> location.distanceTo(e1.getCenterLocation())));
        for (String clazz : classes){
            ret.addAll(getByClass(clazz));
        }
        return ret;
    }

    public Set<EntityReader> getTracked() {
        synchronized (currentlyTracking) {
            return new HashSet<>(currentlyTracking);
        }
    }

    @Override
    public void receiveEvent(AiContext aiContext, AiEvent event) {
        if (event.type.equals(AiEventType.BuildingPlacementChanged)) {
            BuildingPlacementChanged changeEvent = (BuildingPlacementChanged) event;
            if (changeEvent.newBuildingId != null)
                track(new EntityReader(clientGameState.gameState, changeEvent.newBuildingId));
            if (changeEvent.constructionZone != null) {
                track(new EntityReader(clientGameState.gameState, changeEvent.constructionZone));
            }
        } else if (event.type.equals(AiEventType.ProductionComplete)) {
            ProductionComplete p = (ProductionComplete) event;
            track(new EntityReader(clientGameState.gameState, p.created));
        }
    }

    public boolean isTracking(EntityReader reader) {
        synchronized (currentlyTracking) {
            return currentlyTracking.contains(reader);
        }
    }
}
