package server.state;

import common.action.Action;
import common.state.spec.EntitySpec;
import common.state.spec.GameSpec;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.Player;
import common.state.los.AllVisibleLineOfSight;
import common.state.los.LineOfSightSpec;
import common.state.los.MultiLineOfSight;
import common.state.los.SinglePlayerLineOfSight;
import common.state.sst.GameState;
import common.state.sst.sub.ConstructionZone;
import common.state.sst.sub.GateInfo;
import common.state.sst.sub.Load;
import common.state.sst.sub.WeaponSet;
import common.state.sst.sub.capacity.PrioritizedCapacitySpec;
import common.util.DPoint;
import client.ai.GaiaAi;
import common.util.EvolutionSpec;
import common.util.json.EmptyJsonable;

import java.awt.*;

public class ServerGameState {
    public GameState state;
    public GaiaAi gaiaAi;

    public static LineOfSightSpec createServerLineOfSightSpec(GameSpec spec, int numberOfPlayers) {
        LineOfSightSpec[] lineOfSights = new LineOfSightSpec[numberOfPlayers+1];
        switch (spec.visibility) {
            case ALL_VISIBLE:
                for (int i = 0; i < lineOfSights.length; i++) {
                    lineOfSights[i] = new AllVisibleLineOfSight(spec);
                }
                break;
            case FOG:
                for (int i = 0; i < lineOfSights.length; i++) {
                    lineOfSights[i] = new SinglePlayerLineOfSight(spec);
                }
                break;
        }
//        multiLineOfSight.lineOfSights[PlayerManager.GAIA.number].updateLineOfSight(
//                null,
//                new DPoint(spec.width / 2, spec.height / 2),
//                Double.MAX_VALUE
//        )
        return new MultiLineOfSight(lineOfSights);
    }
    public static ServerGameState createServerGameState(GameSpec spec, int numberOfPlayers) {
        ServerGameState sgs = new ServerGameState();
        sgs.gaiaAi = new GaiaAi();
        sgs.state = GameState.createGameState(spec, createServerLineOfSightSpec(spec, numberOfPlayers));
        return sgs;
    }

    /*
    TODO: combine this with the ServerStateManipulator.createUnitUpdateMessage
     */
    private void addEntityTo(EntityId entityId, GameState nextState, LineOfSightSpec los) {
        final Object sync = state.entityManager.get(entityId);
        synchronized (sync) {
            if (state.entityManager.get(entityId) == null)
                return;
            EntitySpec type = state.typeManager.get(entityId);
            DPoint location = state.locationManager.getLocation(entityId);
            if (!los.isVisible(null, location.toPoint(), type.size))
                return;

            if (nextState.entityManager.get(entityId) == null)
                nextState.entityManager.set(entityId, new EmptyJsonable());
            nextState.locationManager.setLocation(new EntityReader(state, entityId), location);
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
        }
    }

    public GameState createGameState(Player player) {
        System.out.println("Creating state for " + player);

        LineOfSightSpec los;
        if (player == null) {
            los = new AllVisibleLineOfSight(state.gameSpec);
        } else {
            los = ((MultiLineOfSight) state.lineOfSight).lineOfSights[player.number];
        }


        GameState gs = GameState.createGameState(state.gameSpec, los);
        for (EntityId entityId : state.entityManager.allKeys()) {
            addEntityTo(entityId, gs, los);
        }

        if (player != null) {
            gs.occupancyState.updateAll(this.state.occupancyState, los);
            for (EntityId entity : GateInfo.getOccupancies(player, this.state.gateStateManager, state.playerManager)) {
                Point p = state.locationManager.getLocation(entity).toPoint();
                EntitySpec type = state.typeManager.get(entity);
                gs.occupancyState.setOccupancy(p, type.size, true);
            }
        }
        return gs;
    }
}
