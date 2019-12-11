package common.state;

import common.action.Action;
import common.state.spec.EntitySpec;
import common.state.spec.ResourceType;
import common.state.spec.attack.Weapon;
import common.state.sst.GameState;
import common.state.sst.OccupancyView;
import common.state.sst.manager.RevPair;
import common.state.sst.sub.ConstructionZone;
import common.state.sst.sub.GateInfo;
import common.state.sst.sub.Load;
import common.state.sst.sub.WeaponSet;
import common.state.sst.sub.capacity.PrioritizedCapacitySpec;
import common.util.DPoint;
import common.util.EvolutionSpec;
import common.util.Util;

import java.awt.*;
import java.util.*;

public class EntityReader {
    public final GameState state;
    public final EntityId entityId;

    public EntityReader(GameState state, EntityId entityId) {
        if (state == null) throw new IllegalStateException();
        this.state = state;
        this.entityId = entityId;
    }

    public int hashCode() {
        return entityId.hashCode();
    }

    public boolean equals(Object other) {
        if (!(other instanceof EntityReader)) {
            return false;
        }
        EntityReader o = (EntityReader) other;
        return o.entityId.equals(entityId);
    }

    public GameState getState() {
        return state;
    }

    public double getCurrentAge() {
        return state.currentTime - Util.zin(state.ageManager.get(entityId));
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
        Player player = state.playerManager.get(entityId);
//        if (player == null) {
//            throw new NullPointerException();
//        }
        return player;
    }

    public Map<ResourceType, Integer> getAmountOfResourceAbleToAccept() {
        HashMap<ResourceType, Integer> acceptedResourceTypes = new HashMap<>();
        PrioritizedCapacitySpec capacity = getCapacity();
        Load carrying = getCarrying();
        if (capacity == null || carrying == null)
            return acceptedResourceTypes;
        for (ResourceType resourceType : state.gameSpec.resourceTypes) {
            int amountPossibleToAccept = capacity.amountPossibleToAccept(carrying, resourceType);
            if (amountPossibleToAccept == 0)
                continue;
            acceptedResourceTypes.put(resourceType, amountPossibleToAccept);
        }
        return acceptedResourceTypes;
    }

    public ConstructionZone getConstructionZone() {
        return state.constructionManager.get(entityId);
    }

    public EntityReader getHolder() {
        EntityId holderId = state.garrisonManager.get(this.entityId);
        if (holderId == null) return null;
        return new EntityReader(state, holderId);
    }

    public Set<EntityReader> getGarrisoned() {
        Set<RevPair<EntityId>> garrisonedUnits = state.garrisonManager.getByType(entityId);
        if (garrisonedUnits == null || garrisonedUnits.isEmpty())
            return Collections.emptySet();
        HashSet<EntityReader> garrisoned = new HashSet<>();
        for (RevPair<EntityId> p : garrisonedUnits) {
            garrisoned.add(new EntityReader(state, p.entityId));
        }
        return garrisoned;
    }


    public int getNumGarrisonedUnits() {
        Set<EntityReader> garrisoned = getGarrisoned();
        if (garrisoned == null) return 0;
        return garrisoned.size();
    }

    public EntityReader getRiding() {
        EntityId entityId = state.ridingManager.get(this.entityId);
        if (entityId == null) return null;
        return new EntityReader(state, entityId);
    }

    public EntityReader getRider() {
        Set<RevPair<EntityId>> byType = state.ridingManager.getByType(entityId);
        if (byType == null || byType.isEmpty()) {
            return null;
        }
        if (byType.size() > 1)
            throw new IllegalStateException("too many riding");
        return new EntityReader(getState(), byType.iterator().next().value);
    }

    public EntitySpec getType() {
        return state.typeManager.get(entityId);
    }

    public boolean isHidden() {
        return Util.fin(state.hiddenManager.get(entityId));
    }

    public double getCurrentHealth() {
        return Util.zin(state.healthManager.get(entityId));
    }

    public double getBaseHealth() {
        return Util.zin(state.baseHealthManager.get(entityId));
    }

    public DPoint getCurrentGatherPoint() {
        return state.gatherPointManager.get(entityId);
    }

    public double getCurrentAttackSpeed() {
        return Util.zin(state.attackSpeedManager.get(entityId));
    }

    public double getRotationSpeed() {
        return Util.zin(state.rotationSpeedManager.get(entityId));
    }

    public double getOrientation() {
        return Util.zin(state.orientationManager.get(entityId));
    }

    public double getMovementSpeed() {
        EntityReader riding = getRiding();
        double movementSpeed1 = getBaseMovementSpeed();
        if (riding == null) {
            return movementSpeed1;
        }
        double movementSpeed2 = riding.getMovementSpeed();
        return Math.max(movementSpeed1, movementSpeed2);
    }

    public double getBaseMovementSpeed() {
        return Util.zin(state.movementSpeedManager.get(entityId));
    }

    public boolean noLongerExists() { return state.entityManager.get(entityId) == null; }

    public PrioritizedCapacitySpec getCapacity() { return state.capacityManager.get(entityId); }

    public boolean canAccept(ResourceType resource) {
        // null pointer?
        PrioritizedCapacitySpec capacity = getCapacity();
        if (capacity == null) return false;
        return capacity.amountPossibleToAccept(getCarrying(), resource) > 0;
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
        if (constructionZone == null)
            return Collections.emptyMap();

        return getAmountOfResourceAbleToAccept();
//
//        Load load = getCarrying();
//        Map<ResourceType, Integer> requiredResources = constructionZone.constructionSpec.resultingStructure.requiredResources;
//        Map<ResourceType, Integer> ret = new HashMap<>();
//        if (load == null) {
//            ret.putAll(requiredResources);
//            return ret;
//        }
//        for (Map.Entry<ResourceType, Integer> entry : requiredResources.entrySet()) {
//            Integer amountRequired = entry.getValue();
//            if (amountRequired == null || entry.getValue() <= 0) {
//                continue;
//            }
//            Integer amountPresent = load.quantities.get(entry.getKey());
//            if (amountPresent == null) {
//                amountPresent = 0;
//            }
//            if (amountPresent >= amountRequired) {
//                continue;
//            }
//            ret.put(entry.getKey(), amountRequired - amountPresent);
//        }
//        return ret;
    }

    public boolean isCarrying(ResourceType resourceType) {
        return !doesNotHave(resourceType);
    }

    public boolean doesNotHave(ResourceType resourceType) {
        Load load = state.carryingManager.get(entityId);
        if (load == null) return true;
        Integer integer = load.quantities.get(resourceType);
        if (integer == null || integer == 0 || integer <= 0)
            return true;
        PrioritizedCapacitySpec capacity = getCapacity();
        return capacity.getPrioritization(resourceType).desiredAmount >= integer;
    }

    public Map<ResourceType, Integer> getExtraResources() {
        Map<ResourceType, Integer> ret = new HashMap<>();
        PrioritizedCapacitySpec capacity = getCapacity();
        Load load = getCarrying();
        if (load == null)  return Collections.emptyMap();
        for (Map.Entry<ResourceType, Integer> entry : load.quantities.entrySet()) {
            Integer integer = entry.getValue();
            if (integer == null || integer == 0 || integer <= 0)
                continue;
            int amount = integer - capacity.getPrioritization(entry.getKey()).desiredAmount;
            if (amount <= 0)
                continue;
            ret.put(entry.getKey(), amount);
        }
        return ret;
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
        if (location == null) return null;
        EntitySpec type = getType();
        if (type == null) return null;
        Dimension size = type.size;
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

    public Weapon getPreferredWeaponWithAmmunition(Comparator<Weapon> comparator) {
        LinkedList<Weapon> allWeapons = new LinkedList<>(getWeapons().ohMy());
        if (allWeapons.isEmpty())
            return null;
        allWeapons.sort(comparator);
        Collections.reverse(allWeapons);
        for (Weapon weapon : allWeapons) {
            if (weapon.hasAmmunition(getCarrying())) {
                return weapon;
            }
        }
        return null;
    }

    public DPoint getLocation() {
        return state.locationManager.getLocation(entityId);
    }

    public DPoint getLocation(double currentGameTime) {
        return state.locationManager.getLocation(entityId, currentGameTime);
    }

    // TODO: use instead of getType all over the place...
    public Dimension getSize() {
        EntitySpec type = getType();
        if (type == null) return null;
        return type.size;
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
        return Util.zin(state.depositSpeedManager.get(entityId));
    }

    public double getCollectSpeed() {
        return Util.zin(state.collectSpeedManager.get(entityId));
    }

//    public double getBaseLineOfSight() {
//        return zin(state.lineOfSightManager.get(entityId));
//    }

    public double getLineOfSight() {
        return Util.zin(state.lineOfSightManager.get(entityId));
    }

    public EvolutionSpec getEvolutionWeights() {
        return state.evolutionManager.get(entityId);
    }


    public static final Comparator<EntityReader> COMPARATOR = Comparator.comparingInt(entity -> entity.entityId.id);

    public String getGraphics() {
        String image = state.graphicsManager.get(entityId);
        if (image != null)
            return image;
        EntitySpec type = getType();
        if (type == null) {
            return null;
        }
        return type.graphicsImage;
    }

    public double getGardenSpeed() {
        return Util.zin(state.gardenSpeed.get(entityId));
    }

    public double getPlantSpeed() {
        return Util.zin(state.burySpeed.get(entityId));
    }
}
