package client.state;

import client.ai.ActionRequester;
import client.ai.ai2.AiManager;
import client.event.AiEventManager;
import client.event.supply.SupplyAndDemandManager;
import common.algo.quad.QuadTreeOccupancyState;
import common.algo.quad.OccupiedQuadTree;
import common.event.AiEventType;
import common.factory.PathFinder;
import common.msg.Message;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.Player;
import common.state.edit.GameSpecManager;
import common.state.los.Exploration;
import common.state.los.LineOfSight;
import common.state.spec.GameSpec;
import common.state.sst.GameState;
import common.util.ExecutorServiceWrapper;
import common.util.json.DataSerializer;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.ReadOptions;

import java.awt.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ClientGameState {
    public Player currentPlayer;
    public Point startingLocation;
    public ExecutorServiceWrapper executor;

    public GameState gameState;
    public AiManager aiManager;
    public AiEventManager eventManager;
    public ActionRequester actionRequester;
    public SupplyAndDemandManager supplyAndDemandManager;
    public ClientGameMessageHandler messageHandler;
    public EntityTracker entityTracker;

    public LineOfSight lineOfsight;
    public Exploration exploration;

    // Could be in the GameState ?
    public PathFinder pathFinder;
    public OccupiedQuadTree quadTree;

    public boolean isSpectating() {
        return currentPlayer == null;
    }

    public static ClientGameState createClientGameState(ActionRequester requester, ExecutorServiceWrapper service, JsonReaderWrapperSpec reader, ReadOptions opts) throws IOException {
        ClientGameState state = new ClientGameState();
        state.actionRequester = requester;
        state.executor = service;

        GameSpec spec = GameSpecManager.deserialize(reader.readString("spec"));
        int numberOfPlayers = reader.readInt32("number-of-players");
        opts.state = state.gameState = GameState.createGameState(spec, numberOfPlayers);
        state.currentPlayer = reader.read("player", Player.Serializer, opts);
        state.startingLocation = reader.read("location", DataSerializer.PointSerializer, opts);
        Set<EntityId> startingUnits = (Set<EntityId>) reader.read("starting-units", new HashSet<>(), EntityId.Serializer, opts);
        state.exploration = Exploration.createExploration(state.gameState.gameSpec, state.currentPlayer == null);
        reader.readName("exploration"); state.exploration.updateAll(reader, opts);
        state.lineOfsight = LineOfSight.createLineOfSight(state.gameState.gameSpec, state.currentPlayer == null);
        reader.readName("line-of-sight"); state.lineOfsight.updateAll(reader, opts);
        reader.readName("state"); state.gameState.updateAll(reader, opts);
        finishCreatingClientGameState(state, startingUnits);
        return state;
    }

    public static ClientGameState createClientGameState(ActionRequester requester, ExecutorServiceWrapper service, Message.Launched launched) {
        ClientGameState state = new ClientGameState();
        state.actionRequester = requester;
        state.executor = service;
        state.gameState = GameState.createGameState(GameSpecManager.deserialize(launched.spec), launched.numPlayers);
        state.currentPlayer = launched.player;
        state.startingLocation = launched.playerStart;
        state.gameState = launched.gameState;
        state.exploration = Exploration.createExploration(state.gameState.gameSpec, false);
        state.exploration.updateAll(launched.exploration);
        state.lineOfsight = LineOfSight.createLineOfSight(state.gameState.gameSpec, false);
        state.lineOfsight.updateAll(launched.lineOfSight);
        finishCreatingClientGameState(state, launched.startingUnits);
        return state;
    }

    private static void finishCreatingClientGameState(ClientGameState state, Set<EntityId> startingUnits) {
        state.aiManager = new AiManager(state);
        state.eventManager = new AiEventManager(state, state.executor);
        state.supplyAndDemandManager = new SupplyAndDemandManager(state, state.gameState.gameSpec);
        state.messageHandler = new ClientGameMessageHandler(state);
        state.entityTracker = new EntityTracker(state);
        state.eventManager.listenForEvents(state.supplyAndDemandManager, AiEventType.BuildingPlacementChanged);
        state.eventManager.listenForEvents(state.entityTracker, AiEventType.BuildingPlacementChanged);
        state.eventManager.listenForEvents(state.entityTracker, AiEventType.ProductionComplete);
        state.pathFinder = PathFinder.createPathFinder(state.gameState.gameSpec, PathFinder.CURRENT_SEARCH);

        for (EntityId entityId : startingUnits) {
            EntityReader entity = new EntityReader(state.gameState, entityId);
            state.entityTracker.track(entity);
        }

        state.quadTree = new OccupiedQuadTree(state.gameState.gameSpec.width, state.gameState.gameSpec.height, state.executor);
        for (EntityId entityId : state.gameState.entityManager.allKeys()) {
            EntityReader entity = new  EntityReader(state.gameState, entityId);
            if (entity.getType().containsClass("occupies")) {
                state.quadTree.setType(entity.getLocation().toPoint(), entity.getSize(), QuadTreeOccupancyState.Occupied);
//                state.gameState.staticOccupancy.set(entity.getLocation().toPoint(), entity.getSize(), true);
            }
//            if (entity.getType().containsClass("construction-zone"))
//                state.gameState.buildingOccupancy.set(entity.getLocation().toPoint(), entity.getSize(), true);
            // player-occupies
        }
        state.quadTree.setPathFinder(state.pathFinder);
        state.quadTree.updateConnectivitySync();
        System.out.println(state.quadTree.size());
    }
}
