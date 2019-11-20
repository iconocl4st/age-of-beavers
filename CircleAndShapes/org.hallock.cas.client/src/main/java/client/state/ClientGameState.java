package client.state;

import client.ai.ActionRequester;
import client.ai.AiManager;
import client.event.AiEventManager;
import client.event.supply.SupplyAndDemandManager;
import common.AiEvent;
import common.state.Player;
import common.state.los.AllVisibleLineOfSight;
import common.state.los.LineOfSightSpec;
import common.state.los.SinglePlayerLineOfSight;
import common.state.spec.GameSpec;
import common.state.sst.GameState;
import common.util.ExecutorServiceWrapper;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.ReadOptions;

import java.awt.*;
import java.io.IOException;

public class ClientGameState {
    public Player currentPlayer;
    public Point startingLocation;

    public GameState gameState;
    public AiManager aiManager;
    public AiEventManager eventManager;
    public ActionRequester actionRequester;
    public SupplyAndDemandManager supplyAndDemandManager;
    public ClientGameMessageHandler messageHandler;

    public boolean isSpectating() {
        return currentPlayer == null;
    }

    private static LineOfSightSpec createLineOfSightSpec(GameSpec spec, Player player) {
        if (player == null) {
            return new AllVisibleLineOfSight(spec);
        }
        switch (spec.visibility) {
            case ALL_VISIBLE:
                return new AllVisibleLineOfSight(spec);
            case EXPLORED:
                throw new RuntimeException("uh oh");
            case FOG:
                return new SinglePlayerLineOfSight(spec);
            default:
                throw new RuntimeException("Unhandled visibility");
        }
    }

    public static ClientGameState createClientGameState(GameSpec spec, ActionRequester requester, Player player, Point startingLocation, ExecutorServiceWrapper service) {
        ClientGameState state = new ClientGameState();
        state.startingLocation = startingLocation;
        state.aiManager = new AiManager(state);
        state.eventManager = new AiEventManager(state, service);
        state.supplyAndDemandManager = new SupplyAndDemandManager(state, spec);
        state.eventManager.listenForEvents(state.supplyAndDemandManager, AiEvent.EventType.BuildingPlacementChanged);
        state.actionRequester = requester;
        state.currentPlayer = player;
        state.messageHandler = new ClientGameMessageHandler(state);
        state.gameState = GameState.createGameState(spec, createLineOfSightSpec(spec, player));
        return state;
    }

    public static ClientGameState createClientGameState(GameState gameState, ActionRequester requester, Player player, Point startingLocation, ExecutorServiceWrapper service) {
        ClientGameState state = new ClientGameState();
        state.startingLocation = startingLocation;
        state.aiManager = new AiManager(state);
        state.eventManager = new AiEventManager(state, service);
        state.supplyAndDemandManager = new SupplyAndDemandManager(state, gameState.gameSpec);
        state.eventManager.listenForEvents(state.supplyAndDemandManager, AiEvent.EventType.BuildingPlacementChanged);
        state.actionRequester = requester;
        state.currentPlayer = player;
        state.messageHandler = new ClientGameMessageHandler(state);
        state.gameState = gameState;
        return state;
    }

    public void updateAll(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
        gameState.updateAll(reader, spec);
    }
}
