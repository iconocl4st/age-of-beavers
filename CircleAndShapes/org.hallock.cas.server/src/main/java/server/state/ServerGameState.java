package server.state;

import client.ai.GaiaAi;
import common.action.Action;
import common.factory.PathFinder;
import common.state.EntityId;
import common.state.Player;
import common.state.los.Exploration;
import common.state.los.LineOfSight;
import common.state.spec.EntitySpec;
import common.state.spec.GameSpec;
import common.state.sst.GameState;
import common.state.sst.manager.Textures;
import common.state.sst.sub.*;
import common.state.sst.sub.capacity.PrioritizedCapacitySpec;
import common.util.DPoint;
import common.util.EvolutionSpec;
import common.util.json.EmptyJsonable;
import server.app.ServerContext;
import server.state.range.RangeEventManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ServerGameState {
    public GameState state;
    public GaiaAi gaiaAi;
    public PathFinder pathFinder;
    public RangeEventManager rangeEventManager;
    public ArrayList<Set<EntityId>> startingUnits;
    public Point[] playerStarts;
    public LineOfSight[] lineOfSights;
    public Exploration[] explorations;

    public static ServerGameState createServerGameState(GameSpec spec, int numberOfPlayers) {
        ServerGameState sgs = new ServerGameState();
        sgs.gaiaAi = new GaiaAi();
        sgs.pathFinder = PathFinder.createPathFinder(spec, PathFinder.CURRENT_SEARCH);
        sgs.state = GameState.createGameState(spec, numberOfPlayers);
        sgs.rangeEventManager = new RangeEventManager(sgs, ServerContext.executorService);
        sgs.lineOfSights = new LineOfSight[numberOfPlayers];
        for (int i = 0; i < sgs.lineOfSights.length; i++)
            sgs.lineOfSights[i] = LineOfSight.createLineOfSight(spec, false);
        sgs.explorations = new Exploration[numberOfPlayers];
        for (int i = 0; i < sgs.explorations.length; i++)
            sgs.explorations[i] = Exploration.createExploration(spec, false);
        sgs.playerStarts = new Point[numberOfPlayers];
        sgs.startingUnits = new ArrayList<>(numberOfPlayers);
        for (int i = 0; i < numberOfPlayers; i++)
            sgs.startingUnits.add(new HashSet<>());
        return sgs;
    }

    /*
    TODO: combine this with the ServerStateManipulator.createUnitUpdateMessage
     */
    private void addEntityTo(EntityId entityId, GameState nextState, LineOfSight los) {
        final Object sync = state.entityManager.get(entityId);
        synchronized (sync) {
            if (state.entityManager.get(entityId) == null)
                return;
            EntitySpec type = state.typeManager.get(entityId);
            DPoint location = state.locationManager.getLocation(entityId);
            if (!los.isVisible(location.toPoint(), type.size))
                return;

            if (nextState.entityManager.get(entityId) == null)
                nextState.entityManager.set(entityId, new EmptyJsonable());

            nextState.locationManager.setLocation(new MovableEntity(state, state.locationManager.getDirectedEntity(entityId)));
            nextState.typeManager.set(entityId, type);

            Action currentAction = state.actionManager.get(entityId);
            if (currentAction != null) nextState.actionManager.set(entityId, currentAction);

            Load load = state.carryingManager.get(entityId);
            if (load != null) nextState.carryingManager.set(entityId, load);

            Double currentHealth = state.healthManager.get(entityId);
            if (currentHealth != null) nextState.healthManager.set(entityId, currentHealth);

            Player player = state.playerManager.get(entityId);
            if (player != null) nextState.playerManager.set(entityId, player);

            ConstructionZone constructionZone = state.constructionManager.get(entityId);
            if (constructionZone != null) {
                nextState.constructionManager.set(entityId, new ConstructionZone(constructionZone.constructionSpec, constructionZone.location));
                nextState.constructionManager.get(entityId).progress = constructionZone.progress;
            }

            Double movementSpeed = state.movementSpeedManager.get(entityId);
            if (movementSpeed != null) nextState.movementSpeedManager.set(entityId, movementSpeed);

            // TODO, do we really add the hidden?
            if (state.hiddenManager.get(entityId)) nextState.hiddenManager.set(entityId, true);

            Double creationTime = state.ageManager.get(entityId);
            if (creationTime != null) nextState.ageManager.set(entityId, creationTime);

            // These could each be a method in the manager impl
            // TODO, previous information?
            EntityId riding = state.ridingManager.get(entityId);
            if (riding != null) nextState.ridingManager.set(entityId, riding);

            EntityId holder = state.garrisonManager.get(entityId);
            if (holder != null) nextState.garrisonManager.set(entityId, holder);

            GateInfo gateState = state.gateStateManager.get(entityId);
            if (gateState != null) nextState.gateStateManager.set(entityId, gateState);

            DPoint gatherPoint = state.gatherPointManager.get(entityId);
            if (gatherPoint != null) nextState.gatherPointManager.set(entityId, gatherPoint);

            Double baseHealth = state.baseHealthManager.get(entityId);
            if (baseHealth != null) nextState.baseHealthManager.set(entityId, baseHealth);

            Double orientation = state.orientationManager.get(entityId);
            if (orientation != null) nextState.orientationManager.set(entityId, orientation);

            Double rotationSpeed = state.rotationSpeedManager.get(entityId);
            if (rotationSpeed != null) nextState.rotationSpeedManager.set(entityId, rotationSpeed);

            Double attackSpeed = state.attackSpeedManager.get(entityId);
            if (attackSpeed != null) nextState.attackSpeedManager.set(entityId, attackSpeed);

            PrioritizedCapacitySpec capacity = state.capacityManager.get(entityId);
            if (capacity != null) nextState.capacityManager.set(entityId, capacity);

            WeaponSet weapons = state.weaponsManager.get(entityId);
            if (weapons != null) nextState.weaponsManager.set(entityId, weapons);

            Double buildSpeed = state.buildSpeedManager.get(entityId);
            if (buildSpeed != null) nextState.buildSpeedManager.set(entityId, buildSpeed);

            Double lineOfSight = state.lineOfSightManager.get(entityId);
            if (lineOfSight != null) nextState.lineOfSightManager.set(entityId, lineOfSight);

            Double collectSpeed = state.collectSpeedManager.get(entityId);
            if (collectSpeed != null) nextState.collectSpeedManager.set(entityId, collectSpeed);

            Double depositSpeed = state.depositSpeedManager.get(entityId);
            if (depositSpeed != null) nextState.depositSpeedManager.set(entityId, depositSpeed);
            
            EvolutionSpec weights = state.evolutionManager.get(entityId);
            if (weights != null) nextState.evolutionManager.set(entityId, weights);

            GrowthInfo growthInfo = state.crops.get(entityId);
            if (growthInfo != null) nextState.crops.set(entityId, growthInfo);

            Double gardenSpeed = state.gardenSpeed.get(entityId);
            if (gardenSpeed != null) nextState.gardenSpeed.set(entityId, gardenSpeed);

            Double burySpeed = state.burySpeed.get(entityId);
            if (burySpeed != null) nextState.burySpeed.set(entityId, burySpeed);

            nextState.graphicsManager.set(entityId, state.graphicsManager.get(entityId));

            if (type.containsClass("occupies")) nextState.staticOccupancy.set(location.toPoint(), type.size, true);
            if (type.containsClass("construction-zone")) nextState.buildingOccupancy.set(location.toPoint(), type.size, true);
        }
    }

    public GameState createGameState(Player player, LineOfSight los) {
        System.out.println("Creating state for " + player);
        GameState gs = GameState.createGameState(state.gameSpec, state.numPlayers);
        for (EntityId entityId : state.entityManager.allKeys())
            addEntityTo(entityId, gs, los);

        for (Textures.TileTexture texture : state.textures.textures.values()) {
            gs.textures.textures.put(new Point(texture.x, texture.y), texture);
        }

//        gs.occupancyState.updateAll(this.state.occupancyState, los);
//        for (EntityId entity : GateInfo.getOccupancies(player, this.state.gateStateManager, state.playerManager)) {
//            Point p = state.locationManager.getLocation(entity).toPoint();
//            EntitySpec type = state.typeManager.get(entity);
//            gs.occupancyState.setOccupancy(p, type.size, true);
//        }
        return gs;
    }
}
