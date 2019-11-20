package app;

import client.ai.ActionRequester;
import client.event.AiEventListener;
import common.AiEvent;
import common.state.EntityReader;
import common.state.Player;
import common.state.sst.manager.RevPair;

import java.util.HashSet;
import java.util.Set;

public class EntityTracker implements AiEventListener {

    private final PlayerAiContext context;
    Set<EntityReader> currentlyTracking = new HashSet<>();

    EntityTracker(PlayerAiContext context) {
        this.context = context;
    }

    void add(EntityReader reader) {
        currentlyTracking.add(reader);
    }

    Iterable<EntityReader> getTracked() {
        for (RevPair<Player> pair : context.clientGameState.gameState.playerManager.getByType(context.clientGameState.currentPlayer))
            currentlyTracking.add(new EntityReader(context.clientGameState.gameState, pair.entityId));
        currentlyTracking.removeIf(EntityReader::noLongerExists);
        return currentlyTracking;
    }

    @Override
    public void receiveEvent(AiEvent event, ActionRequester ar) {
        if (!event.type.equals(AiEvent.EventType.BuildingPlacementChanged))
            return;
        AiEvent.BuildingPlacementChanged changeEvent = (AiEvent.BuildingPlacementChanged) event;
        if (changeEvent.newBuildingId != null)
            currentlyTracking.add(new EntityReader(context.clientGameState.gameState, changeEvent.newBuildingId));
        if (changeEvent.constructionZone != null) {
            currentlyTracking.add(new EntityReader(context.clientGameState.gameState, changeEvent.constructionZone));
        }
    }
}
