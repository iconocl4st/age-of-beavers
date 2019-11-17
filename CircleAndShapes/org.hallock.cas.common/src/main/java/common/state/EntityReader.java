package common.state;

import common.action.Action;
import common.state.spec.EntitySpec;
import common.state.spec.ResourceType;
import common.state.sst.GameState;
import common.state.sst.manager.ReversableManagerImpl;
import common.state.sst.sub.ConstructionZone;
import common.state.sst.sub.GateInfo;
import common.state.sst.sub.Load;
import common.state.sst.sub.WeaponSet;
import common.state.sst.sub.capacity.PrioritizedCapacitySpec;
import common.util.DPoint;
import common.util.EvolutionSpec;

import java.awt.*;
import java.util.*;

public class EntityReader implements LocatedEntitySpec {

    public final GameState state;
    public final EntityId entityId;

    public EntityReader(GameState state, EntityId entityId) {
        this.state = state;
        this.entityId = entityId;
    }


    public double getCurrentAge() {
        return state.currentTime - zin(state.ageManager.get(entityId));
    }

    public Action getCurrentAction() {
        return state.actionManager.get(entityId);
    }

    public Load getCarrying() {
        Load load = state.carryingManager.get(entityId);
        if (load == null)
            return Load.EMPTY_LOAD;
        return load;
    }

    public Object getSync() {
        return state.entityManager.get(entityId);
    }

    public Player getOwner() {
        return state.playerManager.get(entityId);
    }

    public ConstructionZone getConstructionZone() {
        return state.constructionManager.get(entityId);
    }

    public EntityId getHolder() {
        return state.garrisonManager.get(entityId);
    }

    public Set<EntityId> getGarrisoned() {
        Set<ReversableManagerImpl.Pair<EntityId>> garrisonedUnits = state.garrisonManager.getByType(entityId);
        if (garrisonedUnits == null || garrisonedUnits.isEmpty())
            return Collections.emptySet();
        HashSet<EntityId> garrisoned = new HashSet<>();
        for (ReversableManagerImpl.Pair<EntityId> p : garrisonedUnits) {
            garrisoned.add(p.entityId);
        }
        return garrisoned;
    }


    public int getNumGarrisonedUnits() {
        Set<EntityId> garrisoned = getGarrisoned();
        if (garrisoned == null) return 0;
        return garrisoned.size();
    }

    public EntityId getRiding() {
        return state.ridingManager.get(entityId);
    }

    public EntitySpec getType() {
        return state.typeManager.get(entityId);
    }

    public boolean isHidden() {
        return fin(state.hiddenManager.get(entityId));
    }

    public double getCurrentHealth() {
        return zin(state.healthManager.get(entityId));
    }

    public double getBaseHealth() {
        return zin(state.baseHealthManager.get(entityId));
    }

    public DPoint getCurrentGatherPoint() {
        return state.gatherPointManager.get(entityId);
    }

    public double getCurrentAttackSpeed() {
        return zin(state.attackSpeedManager.get(entityId));
    }

    public double getRotationSpeed() {
        return zin(state.rotationSpeedManager.get(entityId));
    }

    public double getOrientation() {
        return zin(state.orientationManager.get(entityId));
    }

    public double getMovementSpeed() {
        EntityId ridingId = getRiding();
        double movementSpeed1 = getBaseMovementSpeed();
        if (ridingId == null) {
            return movementSpeed1;
        }
        EntityReader riding = new EntityReader(state, ridingId);
        double movementSpeed2 = riding.getMovementSpeed();
        return Math.max(movementSpeed1, movementSpeed2);
    }

    public double getBaseMovementSpeed() {
        return zin(state.movementSpeedManager.get(entityId));
    }

    public boolean noLongerExists() { return state.entityManager.get(entityId) == null; }

    public PrioritizedCapacitySpec getCapacity() { return state.capacityManager.get(entityId); }

    public boolean canAccept(ResourceType resource) {
        return getCapacity().amountPossibleToAccept(getCarrying(), resource) > 0;
    }

    private static double zin(Double d) {
        if (d == null) return 0.0;
        return d;
    }

    private static boolean fin(Boolean b) {
        if (b == null) return false;
        return b;
    }

    public boolean isOwnedBy(Player somePlayer) {
        Player owner = state.playerManager.get(entityId);
        if (somePlayer == null || owner == null) return false;
        return somePlayer.equals(Player.GOD) || owner.equals(somePlayer);
    }

    public GateInfo getGateState() {
        return state.gateStateManager.get(entityId);
    }

    public Map<ResourceType, Integer> getMissingConstructionResources() {
        ConstructionZone constructionZone = getConstructionZone();
        Load load = getCarrying();
        if (constructionZone == null)
            return Collections.emptyMap();

        Map<ResourceType, Integer> requiredResources = constructionZone.constructionSpec.resultingStructure.requiredResources;
        Map<ResourceType, Integer> ret = new HashMap<>();
        if (load == null) {
            ret.putAll(requiredResources);
            return ret;
        }
        for (Map.Entry<ResourceType, Integer> entry : requiredResources.entrySet()) {
            Integer amountRequired = entry.getValue();
            if (amountRequired == null || entry.getValue() <= 0) {
                continue;
            }
            Integer amountPresent = load.quantities.get(entry.getKey());
            if (amountPresent == null) {
                amountPresent = 0;
            }
            if (amountPresent >= amountRequired) {
                continue;
            }
            ret.put(entry.getKey(), amountRequired - amountPresent);
        }
        return ret;
    }

    public boolean doesNotHave(ResourceType resourceType) {
        Load load = state.carryingManager.get(entityId);
        if (load == null) return true;
        Integer integer = load.quantities.get(resourceType);
        return integer == null || integer == 0 || integer <= 0;
    }

    public boolean isNotCarryingAnything() {
        return getCarrying().getWeight() == 0;
    }

    public WeaponSet getWeapons() {
        WeaponSet ret = state.weaponsManager.get(entityId);
        if (ret == null) return new WeaponSet();
        return ret;
    }

    public DPoint getCenterLocation() {
        DPoint location = getLocation();
        Dimension size = getType().size;
        return new  DPoint(location.x + size.width / 2.0, location.y + size.height / 2.0);
    }

    public boolean isIdle() {
        Action currentAction = getCurrentAction();
        return currentAction == null || currentAction instanceof Action.Idle;
    }

    public double getBuildSpeed() {
        Double d = state.buildSpeedManager.get(entityId);
        if (d == null)
            return 0.0;
        return d;
    }


    @Override
    public EntityId getEntityId() {
        return entityId;
    }

    public DPoint getLocation() {
        return state.locationManager.getLocation(entityId);
    }

    @Override
    public Dimension getSize() {
        return getType().size;
    }

    public Double getCurrentBuildProgress() {
        ConstructionZone zone = getConstructionZone();
        if (zone == null) return null;
        else return zone.progress;
    }

    public boolean isLowHealth() {
        return getCurrentHealth() / getBaseHealth() < 0.25;
    }

    public double getDepositSpeed() {
        return zin(state.depositSpeedManager.get(entityId));
    }

    public double getCollectSpeed() {
        return zin(state.collectSpeedManager.get(entityId));
    }

    public double getBaseLineOfSight() {
        return zin(state.lineOfSightManager.get(entityId));
    }

    public EvolutionSpec getEvolutionWeights() {
        return state.evolutionManager.get(entityId);
    }
}
