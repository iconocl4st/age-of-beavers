package client.state;

import client.ai.ActionRequester;
import client.ai.ai2.AiManager;
import client.event.AiEventManager;
import client.event.supply.SupplyAndDemandManager;
import common.event.AiEventType;
import common.msg.Message;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.Player;
import common.state.los.AllVisibleLineOfSight;
import common.state.los.LineOfSightSpec;
import common.state.los.SinglePlayerLineOfSight;
import common.state.spec.GameSpec;
import common.state.sst.GameState;
import common.util.ExecutorServiceWrapper;

import java.awt.*;
import java.util.Set;

public class ClientGameState {
    public Player currentPlayer;
    public Point startingLocation;

    public GameState gameState;
    public AiManager aiManager;
    public AiEventManager eventManager;
    public ActionRequester actionRequester;
    public SupplyAndDemandManager supplyAndDemandManager;
    public ClientGameMessageHandler messageHandler;
    public EntityTracker entityTracker;
    public ExecutorServiceWrapper executor;

    public boolean isSpectating() {
        return currentPlayer == null;
    }

    public static LineOfSightSpec createLineOfSightSpec(GameSpec spec, Player player) {
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

    public static class GameCreationContext {
        public GameState gameState;
        public GameSpec gameSpec;
        public ActionRequester requester;
        public Player player;
        public Point startingLocation;
        public ExecutorServiceWrapper service;
        public Set<EntityId> startingUnits;

        public GameSpec getGameSpec() {
            if (gameSpec == null)
                return gameState.gameSpec;
            return gameSpec;
        }

        public void parseLaunchedMessage(Message.Launched msg) {
            startingUnits = msg.startingUnits;
            startingLocation = msg.playerStart;
            player = msg.player;
            gameSpec = msg.spec;
        }
    }

    public static ClientGameState createClientGameState(GameCreationContext context) {
        ClientGameState state = new ClientGameState();
        state.executor = context.service;
        state.startingLocation = context.startingLocation;
        state.aiManager = new AiManager(state);
        state.eventManager = new AiEventManager(state, context.service);
        state.supplyAndDemandManager = new SupplyAndDemandManager(state, context.getGameSpec());
        state.actionRequester = context.requester;
        state.currentPlayer = context.player;
        state.messageHandler = new ClientGameMessageHandler(state);
        state.entityTracker = new EntityTracker(state);
        state.eventManager.listenForEvents(state.supplyAndDemandManager, AiEventType.BuildingPlacementChanged);
        state.eventManager.listenForEvents(state.entityTracker, AiEventType.BuildingPlacementChanged);
        state.gameState = context.gameState;
        for (EntityId entityId : context.startingUnits)
            state.entityTracker.track(new EntityReader(state.gameState, entityId));
        return state;
    }
}
