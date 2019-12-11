package common.msg;

import common.action.Action;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.Player;
import common.state.spec.EntitySpec;
import common.state.sst.sub.GateInfo;
import common.state.sst.sub.Load;
import common.state.sst.sub.WeaponSet;
import common.state.sst.sub.capacity.PrioritizedCapacitySpec;
import common.util.DPoint;
import common.util.EvolutionSpec;

public class UnitUpdater {

    public static Message.UnitUpdated updateUnitLocation(EntityId unitId, DPoint newLocation) {
        Message.UnitUpdated unitUpdated = new Message.UnitUpdated();
        unitUpdated.unitId = unitId;
        unitUpdated.location = newLocation;
        return unitUpdated;
    }

    public static Message.UnitUpdated updateUnitAction(EntityId unitId, Action newAction) {
        Message.UnitUpdated unitUpdated = new Message.UnitUpdated();
        unitUpdated.unitId = unitId;
        unitUpdated.action = newAction;
        return unitUpdated;
    }

    public static Message.UnitUpdated updateUnitLoad(EntityId entityId, Load load) {
        Message.UnitUpdated unitUpdated = new Message.UnitUpdated();
        unitUpdated.unitId = entityId;
        unitUpdated.load = load;
        return unitUpdated;
    }

    public static Message.UnitUpdated updateUnitHealth(EntityId entityId, double newHealth) {
        Message.UnitUpdated unitUpdated = new Message.UnitUpdated();
        unitUpdated.unitId = entityId;
        unitUpdated.health = newHealth;
        return unitUpdated;
    }

    public static Message.UnitUpdated updateUnitOwner(EntityId entityId, Player owner) {
        Message.UnitUpdated unitUpdated = new Message.UnitUpdated();
        unitUpdated.unitId = entityId;
        unitUpdated.owner = owner;
        return unitUpdated;
    }

    public static Message.UnitUpdated updateUnitBuildProgress(EntityId entityId, double buildProgress) {
        Message.UnitUpdated unitUpdated = new Message.UnitUpdated();
        unitUpdated.unitId = entityId;
        unitUpdated.buildProgress = buildProgress;
        return unitUpdated;
    }

    public static Message.UnitUpdated updateUnitMovementSpeed(EntityId entityId, double movementSpeed) {
        Message.UnitUpdated unitUpdated = new Message.UnitUpdated();
        unitUpdated.unitId = entityId;
        unitUpdated.newMovementSpeed = movementSpeed;
        return unitUpdated;
    }

    public static Message.UnitUpdated updateUnitVisibiliy(EntityId entityId, boolean hidden) {
        Message.UnitUpdated unitUpdated = new Message.UnitUpdated();
        unitUpdated.unitId = entityId;
        unitUpdated.isHidden = hidden;
        return unitUpdated;
    }

    public static Message.UnitUpdated updateUnitType(EntityId entityId, EntitySpec spec) {
        Message.UnitUpdated unitUpdated = new Message.UnitUpdated();
        unitUpdated.unitId = entityId;
        unitUpdated.isNowOfType = spec;
        return unitUpdated;
    }

    public static Message.UnitUpdated updateUnitGarrison(EntityId entityId, EntityId load) {
        Message.UnitUpdated unitUpdated = new Message.UnitUpdated();
        unitUpdated.unitId = entityId;
        unitUpdated.isWithin = load;
        return unitUpdated;
    }

    public static Message.UnitUpdated updateUnitIsWithin(EntityId entityId, EntityId other) {
        Message.UnitUpdated unitUpdated = new Message.UnitUpdated();
        unitUpdated.unitId = entityId;
        unitUpdated.isWithin = other;
        return unitUpdated;
    }

    public static Message.UnitUpdated updateUnitRides(EntityId entityId, EntityId ridden) {
        Message.UnitUpdated unitUpdated = new Message.UnitUpdated();
        unitUpdated.unitId = entityId;
        unitUpdated.rides = ridden;
        return unitUpdated;
    }

    public static Message.UnitUpdated changeGateState(EntityId entityId, GateInfo state) {
        Message.UnitUpdated unitUpdated = new Message.UnitUpdated();
        unitUpdated.unitId = entityId;
        unitUpdated.occupancy = state;
        return unitUpdated;
    }

    public static Message updateUnitGatherPoint(EntityId entityId, DPoint location) {
        Message.UnitUpdated unitUpdated = new Message.UnitUpdated();
        unitUpdated.unitId = entityId;
        unitUpdated.gatherPoint = location;
        return unitUpdated;
    }

    public static Message weaponsChanged(EntityReader attacker, WeaponSet weapons) {
        Message.UnitUpdated unitUpdated = new Message.UnitUpdated();
        unitUpdated.unitId = attacker.entityId;
        unitUpdated.weapons = weapons;
        return unitUpdated;
    }

    public static Message updateUnitEvoWeights(EntityId entityId, EvolutionSpec weights) {
        Message.UnitUpdated unitUpdated = new Message.UnitUpdated();
        unitUpdated.unitId = entityId;
        unitUpdated.evolutionWeights = weights;
        return unitUpdated;
    }

    public static Message updateUnitCapacity(EntityId entityId, PrioritizedCapacitySpec capacity) {
        Message.UnitUpdated  unitUpdated = new Message.UnitUpdated();
        unitUpdated.unitId  = entityId;
        unitUpdated.capacity = capacity;
        return unitUpdated;
    }
}
