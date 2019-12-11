package server.state;

import client.ai.RandomlyWaitAndMove;
import common.Proximity;
import common.action.Action;
import common.algo.Ballistics;
import common.algo.ConnectedSet;
import common.event.ActionCompleted;
import common.event.BuildingPlacementChanged;
import common.event.ProductionComplete;
import common.event.TargetKilled;
import common.msg.Message;
import common.msg.UnitUpdater;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.Occupancy;
import common.state.Player;
import common.state.los.VisibilityChange;
import common.state.spec.*;
import common.state.spec.attack.DamageType;
import common.state.spec.attack.Weapon;
import common.state.spec.attack.WeaponSpec;
import common.state.sst.GameState;
import common.state.sst.GameStateHelper;
import common.state.sst.OccupancyView;
import common.state.sst.sub.*;
import common.state.sst.sub.capacity.Prioritization;
import common.state.sst.sub.capacity.PrioritizedCapacitySpec;
import common.util.DPoint;
import common.util.EvolutionSpec;
import common.util.Util;
import common.util.query.GridLocationQuerier;
import server.algo.UnGarrisonLocation;
import server.app.BroadCaster;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ServerStateManipulator {

    private final Player player;
    private final Game game;
    private final BroadCaster broadCaster;

    public ServerStateManipulator(Game game, Player player, BroadCaster broadCaster) {
        this.player = player;
        this.game = game;
        this.broadCaster = broadCaster;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SET ACTION
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // could be part of the actions themselves now
    public void setUnitAction(EntityId entityId, Action daAction) {
        setUnitAction(new EntityReader(game.serverState.state, entityId), daAction);
    }
    public void setUnitAction(EntityReader entity, Action daAction) {
        daAction.requestingPlayer = player;
        if (daAction instanceof Action.DoubleProgressAction)
            ((Action.DoubleProgressAction) daAction).progress = 0;
        switch (daAction.type) {
            case Attack: {
                Action.Attack action = (Action.Attack) daAction;
                if (!entity.isOwnedBy(player)) return;
                if (entity.isHidden()) return;

                Action previousAction = entity.getCurrentAction();
                if (previousAction instanceof Action.Attack &&
                    ((Action.Attack) previousAction).target.equals(action.target)) {
                    return;
                }
                action.progress = 0;

                // TODO check if the other entity is not owned by this player?
                // TODO notify attacked that they are being attacked?
            }
            break;
            case Collect: {
                Action.Collect action = (Action.Collect) daAction;
                if (!entity.isOwnedBy(player)) return;
                if (entity.isHidden()) return;
                action.progress = 0;
            }
            break;
            case Deposit: {
                Action.Deposit action = (Action.Deposit) daAction;
                if (!entity.isOwnedBy(player)) return;
                if (entity.isHidden()) return;
                action.progress = 0;
            }
            break;
            case Move: {
                Action.MoveSeq action = (Action.MoveSeq) daAction;
                if (!entity.isOwnedBy(player)) return;
                if (entity.isHidden()) return;
                if (entity.getMovementSpeed() <= 0) return;
            }
            break;
            case Build: {
                Action.Build action = (Action.Build) daAction;
                if (!entity.isOwnedBy(player)) return;
                if (entity.isHidden()) return;
//                if (!Zoom.serverState.state.playerManager.playerOwns(player, action.constructionId)) return;
            }
            break;
            case Create: {
                // todo: check that the spec actually exists in our spec...
                Action.Create action = (Action.Create) daAction;
                if (!entity.isOwnedBy(player)) return;
                if (entity.isHidden()) return;
                action.timeRemaining = action.spec.creationTime;

                final Object sync = entity.getSync();
                if (sync == null) return;
                synchronized (sync) {
                    if (!entity.isOwnedBy(player)) {
                        return;
                    }
                    Action currentAction = entity.getCurrentAction();
                    if (currentAction != null && !(currentAction instanceof Action.Idle))
                        return;
                    Load load = entity.getCarrying();
                    if (!load.canAfford(action.spec.requiredResources))
                        return;
                    EntitySpec creatorType = entity.getType();
                    if (creatorType == null /* || !creatorType.canCreate.contains(action.spec) */) {
                        return;
                    }
                    action.timeRemaining = action.spec.creationTime;
                    load.subtract(action.spec.requiredResources);
                    broadCaster.broadCast(UnitUpdater.updateUnitLoad(entity.entityId, load));
                }
            }
            break;
            case Idle: {
                Action.Idle action = (Action.Idle) daAction;
                if (!entity.isOwnedBy(player)) return;
                if (entity.isHidden()) return;
            }
            break;
            case Wait: {
                Action.Wait action = (Action.Wait) daAction;
                if (!entity.isOwnedBy(player)) return;
                if (entity.isHidden()) return;

            }
            break;
            case Garden: {
                Action.Wait action = (Action.Wait) daAction;
                if (!entity.isOwnedBy(player)) return;
                if (entity.isHidden()) return;
                // check that the plant is actually a plant...

            }
            break;
            case Plant: {
                Action.Wait action = (Action.Wait) daAction;
                if (!entity.isOwnedBy(player)) return;
                if (entity.isHidden()) return;
                // check if there is anything there
                // check if the entity has the seed

            }
            break;
            default:
                throw new RuntimeException("Unknown action " + daAction.type);
        }
        game.serverState.state.actionManager.set(entity.entityId, daAction);
        broadCaster.broadCast(UnitUpdater.updateUnitAction(entity.entityId, daAction));
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////


    public void placeBuilding(EntitySpec spec, Point location) {
        OccupancyView constructionOccupancy = Occupancy.createConstructionOccupancy(game.serverState.state, game.serverState.explorations[player.number - 1]);
        if (Occupancy.isOccupied(constructionOccupancy, location, spec.size))
            return;

        if (spec.containsClass("constructed")) {
            spec = spec.createConstructionSpec(EntitySpec.getCarryCapacity(game.serverState.state.gameSpec, null));
        }

        EntityId constructionId = game.idGenerator.generateId();
        createUnit(constructionId, spec, new EvolutionSpec(spec), new DPoint(location), Player.GAIA);
        constructionChanged(player, constructionId, null);

        System.out.println("Building created.");
    }


    public void setGatherPoint(EntityId entityId, DPoint location) {
        EntityReader entity = new EntityReader(game.serverState.state, entityId);
        Object synchronizationObject = entity.getSync();
        if (synchronizationObject == null) return;
        synchronized (synchronizationObject) {
            if (!entity.isOwnedBy(player)) return;
            EntitySpec type = game.serverState.state.typeManager.get(entityId);
            if (!type.canHaveGatherPoint()) return;

            game.serverState.state.gatherPointManager.set(entityId, location);
            broadCaster.broadCast(UnitUpdater.updateUnitGatherPoint(entityId, location));
        }
    }


    public void garrison(EntityId entityId, EntityId withinId) {
        Object[] syncs = GameStateHelper.getSynchronizationObjects(game.serverState.state.entityManager, entityId, withinId);
        synchronized (syncs[0]) {
            synchronized (syncs[1]) {
                EntityReader toGarrison = new EntityReader(game.serverState.state, entityId);
                EntityReader garrisonWithin = new EntityReader(game.serverState.state, withinId);
                if (!toGarrison.isOwnedBy(player)) return;
                if (toGarrison.isHidden()) return;
                if (toGarrison.noLongerExists() || garrisonWithin.noLongerExists()) return;
                if (!GameStateHelper.playerCanGarrison(player, toGarrison, garrisonWithin)) return;
                if (garrisonWithin.getNumGarrisonedUnits() >= garrisonWithin.getType().garrisonCapacity) return;
                if (!Proximity.closeEnoughToInteract(toGarrison, garrisonWithin)) return;

                Player owner = toGarrison.getOwner();
                DPoint toGarrisonLocation = toGarrison.getLocation();

                setOwner(garrisonWithin, owner);

                game.serverState.state.garrisonManager.set(toGarrison.entityId, garrisonWithin.entityId);
                broadCaster.broadCast(UnitUpdater.updateUnitIsWithin(toGarrison.entityId, garrisonWithin.entityId));

                game.serverState.state.hiddenManager.set(toGarrison.entityId, true);
                broadCaster.broadCast(UnitUpdater.updateUnitVisibiliy(toGarrison.entityId, true));

                updateLineOfSight(toGarrison.entityId, toGarrisonLocation, null);

                EntitySpec type = garrisonWithin.getType();
                if (type.containsClass("player-occupies"))
                    updateGateOccupancy(garrisonWithin);
            }
        }
    }

    public void ungarrison(EntityId entityId) {
        ungarrison(new EntityReader(game.serverState.state, entityId));
    }
    public void ungarrison(EntityReader entity) {
        Object sync = entity.getSync();
        if (sync == null) {
            return;
        }
        synchronized (sync) {
            if (entity.noLongerExists()) return;
            if (!entity.isOwnedBy(player)) return;
            EntityReader holder = entity.getHolder();
            if (holder == null) return;
            UnGarrisonLocation unGarrisonLocation = UnGarrisonLocation.getUnGarrisonLocation(game.serverState, holder, entity.getType().size);
            if (unGarrisonLocation.isImpossible()) return;
            DPoint start = new DPoint(unGarrisonLocation.point);

            game.serverState.state.garrisonManager.remove(entity.entityId);
            broadCaster.broadCast(UnitUpdater.updateUnitGarrison(entity.entityId, EntityId.NONE));

            game.serverState.state.hiddenManager.set(entity.entityId, false);
            broadCaster.broadCast(UnitUpdater.updateUnitVisibiliy(entity.entityId, false));

            game.serverState.state.locationManager.setLocation(MovableEntity.createStationary(entity, start));
            broadCaster.broadCast(UnitUpdater.updateUnitLocation(entity.entityId, start));

            if (game.serverState.state.garrisonManager.getByType(holder.entityId).isEmpty())
                setOwner(holder, Player.GAIA);

            updateLineOfSight(entity.entityId, null, start);

            EntitySpec type = holder.getType();
            if (type.containsClass("player-occupies"))
                updateGateOccupancy(holder);

            if (unGarrisonLocation.path != null) {
                setUnitAction(entity, new Action.MoveSeq(unGarrisonLocation.path));
            }
        }
    }

    public void ride(EntityId riderId, EntityId riddenId) {
        EntityReader rider = new EntityReader(game.serverState.state, riderId);
        EntityReader ridden = new EntityReader(game.serverState.state, riddenId);
        Object[] sync = GameStateHelper.getSynchronizationObjects(game.serverState.state.entityManager, riderId, riddenId);
        if (sync == null) {
            return;
        }
        synchronized (sync[0]) {
            synchronized (sync[1]) {
                if (rider.noLongerExists() || ridden.noLongerExists())
                    return;
                if (!GameStateHelper.playerCanRide(player, rider, ridden)) return;
                if (!Proximity.closeEnoughToInteract(rider, ridden)) return;

                setOwner(rider, player);

//                game.serverState.state.movementSpeedManager.set(rider.entityId, newMovementSpeed);
//                broadCaster.broadCast(UnitUpdater.updateUnitMovementSpeed(rider.entityId, newMovementSpeed));

                updateLineOfSight(ridden.entityId, ridden.getLocation(), null);

                game.serverState.state.ridingManager.set(rider.entityId, ridden.entityId);
                broadCaster.broadCast(UnitUpdater.updateUnitRides(rider.entityId, ridden.entityId));

                game.serverState.state.hiddenManager.set(ridden.entityId, true);
                broadCaster.broadCast(UnitUpdater.updateUnitVisibiliy(ridden.entityId, true));
            }
        }
    }

    public void stopRiding(EntityId riderId) {
        EntityReader rider = new EntityReader(game.serverState.state, riderId);
        EntityReader ridden = rider.getRiding();
        if (ridden == null) return;
        Object[] sync = GameStateHelper.getSynchronizationObjects(game.serverState.state.entityManager, riderId, ridden.entityId);
        if (sync == null) {
            return;
        }
        synchronized (sync[0]) {
            synchronized (sync[1]) {
                if (rider.noLongerExists() || ridden.noLongerExists())
                    return;
                if (!rider.isOwnedBy(player)) return;
                if (rider.isHidden()) return;
                if (rider.getRiding() == null || !rider.getRiding().equals(ridden)) return;

                // trickier: TODO: Need to reset the action of the ridden


                DPoint newLocation = rider.getLocation();

                game.serverState.state.ridingManager.set(rider.entityId, EntityId.NONE);
                broadCaster.broadCast(UnitUpdater.updateUnitRides(rider.entityId, EntityId.NONE));

                game.serverState.state.hiddenManager.set(ridden.entityId, false);
                broadCaster.broadCast(UnitUpdater.updateUnitVisibiliy(ridden.entityId, false));

                if (!rider.getType().containsClass("owned"))
                    setOwner(rider, Player.GAIA);

//                game.serverState.state.movementSpeedManager.set(rider.entityId, prev.movementSpeed);
//                broadCaster.broadCast(UnitUpdater.updateUnitMovementSpeed(rider.entityId, prev.movementSpeed));

                game.serverState.state.locationManager.setLocation(MovableEntity.createStationary(ridden, newLocation));
                broadCaster.broadCast(UnitUpdater.updateUnitLocation(ridden.entityId, newLocation));

                updateLineOfSight(ridden.entityId, null, newLocation);
            }
        }
    }

    public void dropAll(EntityId entityId) {
        EntityReader entity = new EntityReader(game.serverState.state, entityId);
        Object sync = entity.getSync();
        if (sync == null) {
            return;
        }
        synchronized (sync) {
            if (entity.noLongerExists()) return;
            if (!entity.isOwnedBy(player)) return;
            final Load currentLoad = entity.getCarrying();
            dropAll(entity.entityId, currentLoad);
        }
    }

    public void suicide(EntityId entityId) {
        EntityReader entity = new EntityReader(game.serverState.state, entityId);
        Object sync = entity.getSync();
        if (sync == null) {
            return;
        }
        synchronized (sync) {
            if (entity.noLongerExists()) return;
            if (!entity.isOwnedBy(player)) return;
            killUnit(entity.entityId);
        }
    }

    public void setEvolutionPreferences(EntityId entityId, EvolutionSpec weights) {
        EntityReader entity = new EntityReader(game.serverState.state, entityId);
        Object sync = entity.getSync();
        if (sync == null) {
            return;
        }
        synchronized (sync) {
            if (entity.noLongerExists()) return;
            if (!entity.isOwnedBy(player)) return;

            game.serverState.state.evolutionManager.set(entityId, weights);
            broadCaster.broadCast(UnitUpdater.updateUnitEvoWeights(entityId, weights));
        }
    }

    public void setDesiredCapacity(EntityId entityId, ResourceType resourceType, int priority, int desiredMinimum, int desiredMaximum) {
        EntityReader entity = new EntityReader(game.serverState.state, entityId);
        Object sync = entity.getSync();
        if (sync == null) {
            return;
        }
        synchronized (sync) {
            if (entity.noLongerExists()) return;
            if (!entity.isOwnedBy(player) && !entity.isOwnedBy(Player.GAIA)) return;
            if (!entity.getType().containsClass("storage")) return;

            PrioritizedCapacitySpec capacitySpec = game.serverState.state.capacityManager.get(entityId);
            Prioritization prioritization = capacitySpec.getPrioritization(resourceType);
            prioritization.desiredMaximum = Math.min(desiredMaximum, prioritization.maximumAmount);
            prioritization.desiredAmount = Math.min(desiredMinimum, prioritization.desiredMaximum);
            prioritization.priority = priority;
            broadCaster.broadCast(UnitUpdater.updateUnitCapacity(entityId, capacitySpec));
        }
    }


//
//
//    public void setUnitAiToDeer(EntityId id, GameSpec spec, Random random, double maxWait, double maxDist) {
//        RandomlyWaitAndMove deerAi = new RandomlyWaitAndMove(Zoom.serverState, random, spec, id, maxWait, maxDist);
//        Zoom.serverState.aiManager.setAi(id, deerAi);
//        deerAi.nextStep(this);
//        if (!deerAi.nextStep(this)) {
//            Zoom.serverState.aiManager.removeAi(id);
//        }
//    }






























    //////////////////////////////////////////////////////////////////

    public void changePayload(EntityId entityId, Load load, ResourceType resource, int newValue) {
        load.setQuantity(resource, newValue);
        broadCaster.broadCast(UnitUpdater.updateUnitLoad(entityId, load));
    }

    public void subtractFromPayload(EntityReader entity, Load load, Map<ResourceType, Integer> toSubtract) {
        load.subtract(toSubtract);
        broadCaster.broadCast(UnitUpdater.updateUnitLoad(entity.entityId, load));
    }

    public void updateBuildProgress(EntityId constructionId, ConstructionZone zone, double amount) {
        zone.progress = amount;
        broadCaster.broadCast(UnitUpdater.updateUnitBuildProgress(constructionId, amount));
    }

    public void setUnitHealth(EntityId entityId, double newHealth) {
        game.serverState.state.healthManager.set(entityId, newHealth);
        broadCaster.broadCast(UnitUpdater.updateUnitHealth(entityId, newHealth));
    }

    public void noiselyUpdateUnitLocation(MovableEntity newLocation) {
        DPoint oldLocation = game.serverState.state.locationManager.getLocation(newLocation.entity.entityId);
        game.serverState.state.locationManager.setLocation(newLocation);
        broadCaster.broadCast(new Message.DirectedLocationChange(newLocation));
        updateLineOfSight(newLocation.entity.entityId, oldLocation, newLocation.movementBegin);
    }

    public void quietlyUpdateUnitLocation(EntityReader entity, DPoint newLocation) {
        DPoint oldLocation = game.serverState.state.locationManager.getLocation(entity.entityId);
        game.serverState.state.locationManager.updateCachedLocation(entity, newLocation);
        updateLineOfSight(entity.entityId, oldLocation, newLocation);
    }

    public void dropAll(EntityId entityId, Load load) {
        load.quantities.clear();
        broadCaster.broadCast(UnitUpdater.updateUnitLoad(entityId, load));
    }

    public void setCreationProgress(EntityId entityId, Action.Create action, double v) {
        action.timeRemaining = v;
        broadCaster.broadCast(UnitUpdater.updateUnitAction(entityId, action));
    }

    public void updateActionProgress(EntityReader entity, Action.DoubleProgressAction action, double v) {
        action.progress = v;
        broadCaster.broadCast(UnitUpdater.updateUnitAction(entity.entityId, action));
    }


    private void updateGateOccupancy(EntityReader entity) {
        DPoint location = entity.getLocation();
        if (game.serverState.state.gateStateManager.getByType(location.toPoint()).isEmpty()) {
            return;
        }
        Dimension size = entity.getSize();
        for (int i = 0; i < game.serverState.state.numPlayers; i++) {
            Player player = new Player(i + 1);
            boolean occupiedFor = GateInfo.isOccupiedFor(entity.entityId, player, game.serverState.state.gateStateManager, game.serverState.state.playerManager);
            game.serverState.state.staticOccupancy.set(location.toPoint(), size, occupiedFor);
            broadCaster.send(player, new Message.OccupancyChanged(location.toPoint(), size, occupiedFor, false));
        }
    }

    public void createUnit(EntityId id, EntitySpec spec, EvolutionSpec eSpec, DPoint location, Player player) {
        GameState state = game.serverState.state;

        if (state.entityManager.get(id) == null)
            state.entityManager.set(id, new Object());
        state.typeManager.set(id, spec);
        state.locationManager.setLocation(MovableEntity.createStationary(new EntityReader(state, id), location));
        state.playerManager.set(id, spec.containsClass("owned") ? player : Player.GAIA);
        state.ageManager.set(id, state.currentTime);
        if (eSpec.initialBaseHealth != 0.0) state.healthManager.set(id, eSpec.initialBaseHealth);
        if (eSpec.initialBaseHealth != 0.0) state.baseHealthManager.set(id, eSpec.initialBaseHealth);
        if (eSpec.initialMovementSpeed != 0.0) state.movementSpeedManager.set(id, eSpec.initialMovementSpeed);
        if (eSpec.initialRotationSpeed != 0.0) state.rotationSpeedManager.set(id, eSpec.initialRotationSpeed);
        if (eSpec.carryCapacity != null) state.capacityManager.set(id, eSpec.carryCapacity);
        else state.capacityManager.set(id, new PrioritizedCapacitySpec(spec.carryCapacity));
        if (eSpec.initialAttackSpeed != 0.0) state.attackSpeedManager.set(id, eSpec.initialAttackSpeed);
        if (eSpec.initialBuildSpeed != 0.0) state.buildSpeedManager.set(id, eSpec.initialBuildSpeed);
        if (eSpec.initialDepositSpeed != 0.0) state.depositSpeedManager.set(id, eSpec.initialDepositSpeed);
        if (eSpec.initialCollectSpeed != 0.0) state.collectSpeedManager.set(id, eSpec.initialCollectSpeed);
        if (eSpec.initialLineOfSight != 0.0) state.lineOfSightManager.set(id, eSpec.initialLineOfSight);
        state.orientationManager.set(id, 0.0); // might should be passed in
        state.graphicsManager.set(id, spec.graphicsImage);

        if (spec.name.equals("human")) {
            WeaponSet s = new WeaponSet();
            // TODO: make this generic...
            s.add(new Weapon(state.gameSpec.getWeaponSpec("fist")));
            state.weaponsManager.set(id, s);
        }

        Load load = new Load();
        for (CarrySpec cSpec : spec.carrying) {
            load.setQuantity(cSpec.type, cSpec.startingQuantity);
        }
        state.carryingManager.set(id, load);

        if (spec.containsClass("construction-zone")) {
            state.constructionManager.set(id, new ConstructionZone(spec, location));
        }
        
        for (CreationSpec creationSpec : spec.canCreate.collect()) {
            if (creationSpec.method.equals(CreationMethod.Garrison)) { // Is this necessary?
                state.evolutionManager.set(id, EvolutionSpec.uniformWeights());
                break;
            }
        }

        if (spec.containsClass("player-occupies")) {
            state.gateStateManager.set(id, new GateInfo(location.toPoint(), GateInfo.GateState.UnlockedForPlayerOnly));
        }
        game.serverState.state.actionManager.set(id, new Action.Idle());
        broadCaster.broadCast(createCreateUnitMessage(id));
        broadCaster.broadCast(new Message.UnitCreated(id));

        EntityReader createdEntity = new EntityReader(state, id);

        updateLineOfSight(id, null, createdEntity.getCenterLocation());
        moveOtherUnitsOutOfTheWay(spec, location);
        addOccupancies(createdEntity);

        if (spec.ai != null) {
            switch (spec.ai) {
                case "deer-ai":
                    double maxWait = Double.valueOf(spec.aiArgs.get("max-wait"));
                    int maxSize = Integer.valueOf(spec.aiArgs.get("max-size"));
                    game.serverState.gaiaAi.setAi(
                        id,
                        new RandomlyWaitAndMove(
                            game.serverState,
                            id,
                            game.lobby.random,
                            game.serverState.state.gameSpec,
                            maxWait,
                            maxSize
                        ),
                        this
                    );
                    break;
                default:
                    throw new RuntimeException("Unknown ai: " + spec.ai);
            }
        }
    }

    private void addOccupancies(EntityReader entity) {
        EntitySpec spec = entity.getType();
        DPoint location = entity.getLocation();
        if (spec.containsClass("construction-zone")) {
            game.serverState.state.buildingOccupancy.set(location.toPoint(), spec.size, true);
            broadCaster.broadCast(new Message.OccupancyChanged(location.toPoint(), spec.size, false, true));
        }
        if (spec.containsClass("occupies")) {
            game.serverState.state.staticOccupancy.set(location.toPoint(), spec.size, true);
            broadCaster.broadCast(new Message.OccupancyChanged(location.toPoint(), spec.size, true, false));
        }
        if (spec.containsClass("player-occupies"))
            updateGateOccupancy(entity);
    }

    private void moveOtherUnitsOutOfTheWay(EntitySpec spec, DPoint location) {
        if (!spec.containsClass("player-occupies") && !spec.containsClass("occupies"))
            return;
        for (EntityReader entity : game.serverState.state.locationManager.getEntitiesWithin(
                location.x,
                location.y,
                location.x + spec.size.width,
                location.y + spec.size.height,
                entity -> {
                    EntitySpec entitySpec = entity.getType();
                    if (entitySpec == null) return false;
                    DPoint location1 = entity.getLocation();
                    if (location1 == null) return false;
                    double movementSpeed = entity.getMovementSpeed();
                    return movementSpeed > 0;
                }
        )) {
            DPoint toMoveLocation = entity.getLocation();
            if (toMoveLocation == null) continue;
            // TODO: This didn't work

            Point nearestEmptyTile = ConnectedSet.findNearestEmptyTile(
                    game.serverState.state.gameSpec,
                    toMoveLocation.toPoint(),
                    Occupancy.createUnitOccupancy(entity)
            );
            if (nearestEmptyTile == null) continue;
            // not quite right...
            noiselyUpdateUnitLocation(MovableEntity.createStationary(entity, new DPoint(nearestEmptyTile)));
        }
    }

    public void killUnit(EntityId target) {
        EntityReader reader = new EntityReader(game.serverState.state, target);
        EntityReader riding = reader.getRiding();
        if (riding != null) {
            stopRiding(target);
        }
        Set<EntityReader> garrisoned = reader.getGarrisoned();
        if (garrisoned != null && !garrisoned.isEmpty()) {
            for (EntityReader entity : garrisoned) {
                ungarrison(entity);
            }
        }

        DPoint targetLocation = reader.getLocation();
        EntitySpec targetType = reader.getType();

        if (targetType.containsAnyClass("occupies", "construction-zone", "player-occupies")) {
            game.serverState.state.staticOccupancy.set(targetLocation.toPoint(), targetType.size, false);
            game.serverState.state.buildingOccupancy.set(targetLocation.toPoint(), targetType.size, false);
            broadCaster.broadCast(new Message.OccupancyChanged(targetLocation.toPoint(), targetType.size, false, false));
        }

        updateLineOfSight(target, targetLocation, null);

        broadCaster.broadCast(new Message.UnitRemoved(target));
        game.serverState.state.removeEntity(target);

        List<EntityId> dropped = new LinkedList<>();
        for (EntitySpec spec : targetType.dropOnDeath) {
            EntityId droppedId = game.idGenerator.generateId();
            createUnit(droppedId, spec, new EvolutionSpec(spec), targetLocation, Player.GAIA);
            dropped.add(droppedId);
        }
        broadCaster.broadCast(new Message.AiEventMessage(new TargetKilled(target, dropped)));
    }

    public void done(EntityReader entity, ActionCompleted.ActionCompletedReason reason) {
        if (entity.getOwner().equals(Player.GAIA)) {
            game.serverState.gaiaAi.unitCompletedAction(this, entity.entityId);
            return;
        }
        if (entity.noLongerExists()) {
            // i guess the suicide client.ai doesn't need to be notified.
            return;
        }

        Action action = new Action.Idle();
        game.serverState.state.actionManager.set(entity.entityId, action);

        broadCaster.broadCast(UnitUpdater.updateUnitAction(entity.entityId, action));
        broadCaster.broadCast(new Message.AiEventMessage(new ActionCompleted(entity.entityId, reason)));
    }

    private void setOwner(EntityReader entity, Player newOwner) {
        Player previousOwner = entity.getOwner();
        if (previousOwner.equals(newOwner)) {
            return;
        }

        DPoint location = entity.getLocation();
        updateLineOfSight(entity.entityId, location, null);
        game.serverState.state.playerManager.set(entity.entityId, newOwner);
        broadCaster.broadCast(UnitUpdater.updateUnitOwner(entity.entityId, newOwner));
        updateLineOfSight(entity.entityId, null, location);

        updateGateOccupancy(entity);

        // TODO: important...
        // might have to change occupancies as well...
    }

    private void updateLineOfSight(EntityId entityId, DPoint oldLocation, DPoint newLocation) {
        if (oldLocation == null && newLocation == null) return;
        Player player = game.serverState.state.playerManager.get(entityId);
        if (player.number == 0) return;
        double lineOfSight = Util.zin(game.serverState.state.lineOfSightManager.get(entityId));
        VisibilityChange change = game.serverState.lineOfSights[player.number - 1].move(oldLocation, newLocation, lineOfSight);
        for (Point p : change.gainedVision) {
            for (EntityReader entity : game.serverState.state.locationManager.getEntities(p, GridLocationQuerier.ANY_ENTITY)) {
                broadCaster.send(player, createCreateUnitMessage(entity.entityId));
                addOccupancies(entity);
            }
        }
        for (Point p : change.lostVision) {
            for (EntityReader entity : game.serverState.state.locationManager.getEntities(p, GridLocationQuerier.ANY_ENTITY)) {
                if (game.serverState.state.typeManager.get(entity.entityId).containsClass("visible-in-fog"))
                    continue;
                broadCaster.send(player, new Message.UnitRemoved(entity.entityId));
            }
        }
    }

    private Message.UnitUpdated createCreateUnitMessage(EntityId entity) {
        GameState state = game.serverState.state;
        Message.UnitUpdated unitUpdate = new Message.UnitUpdated();
        unitUpdate.unitId = entity;
        unitUpdate.location = state.locationManager.getLocation(entity);
        unitUpdate.isNowOfType = state.typeManager.get(entity);
        unitUpdate.action = state.actionManager.get(entity);
        unitUpdate.load = state.carryingManager.get(entity);
        unitUpdate.health = state.healthManager.get(entity);
        unitUpdate.owner = state.playerManager.get(entity);
        unitUpdate.newMovementSpeed = state.movementSpeedManager.get(entity);
        unitUpdate.isHidden = state.hiddenManager.get(entity);
        unitUpdate.creationTime = state.ageManager.get(entity);
        unitUpdate.isWithin = state.garrisonManager.get(entity);
        unitUpdate.rides = state.ridingManager.get(entity);
        unitUpdate.constructionZone = state.constructionManager.get(entity); // add to client
        unitUpdate.constructionProgress = unitUpdate.constructionZone == null ? null : unitUpdate.constructionZone.progress;
        unitUpdate.occupancy = state.gateStateManager.get(entity);
        unitUpdate.gatherPoint = state.gatherPointManager.get(entity);
        unitUpdate.weapons = state.weaponsManager.get(entity);
        unitUpdate.attackSpeed = state.attackSpeedManager.get(entity);
        unitUpdate.rotationSpeed = state.rotationSpeedManager.get(entity);
        unitUpdate.orientation = state.orientationManager.get(entity);
        unitUpdate.capacity = state.capacityManager.get(entity);
        unitUpdate.buildSpeed = state.buildSpeedManager.get(entity);
        unitUpdate.evolutionWeights = state.evolutionManager.get(entity);
        unitUpdate.graphics = state.graphicsManager.get(entity);
        return unitUpdate;
    }

    public void setOccupancyState(EntityId entityId, GateInfo.GateState newState) {
        EntityReader entity = new EntityReader(game.serverState.state, entityId);
        if (!entity.isOwnedBy(player)) return;
        if (!entity.getType().containsClass("player-occupies")) return;

        GateInfo info = entity.getGateState();
        if (info != null && info.state.equals(newState)) {
            return;
        }

        GateInfo newInfo = new GateInfo(info.location, newState);
        game.serverState.state.gateStateManager.set(entity.entityId, newInfo);
        broadCaster.broadCast(UnitUpdater.changeGateState(entity.entityId, newInfo));
        updateGateOccupancy(entity);
    }

    public void updateGameTime(TimeInfo info) {
        game.serverState.state.currentTime = info.currentTime;
        broadCaster.broadCast(new Message.TimeChange(info.currentTime, info.timeOfGameTime));
    }

    public void setActionProgress(EntityId entityId, Action action) {
        broadCaster.broadCast(UnitUpdater.updateUnitAction(entityId, action));
    }

    public boolean receiveDamage(EntityId attacked, double damage, DamageType damageType) {
        double oldHealth = game.serverState.state.healthManager.get(attacked);
        // to do damage type
        double newHealth = oldHealth - damage;
        if (newHealth > 0) {
            setUnitHealth(attacked, newHealth);
            return false;
        }

        killUnit(attacked);
        return true;
    }

    public void createProjectile(EntityReader attacker, EntityReader attacked, WeaponSpec weaponSpec, Double attackTime) {
        Player launchingPlayer = attacker.getOwner();
        DPoint attackerLocation = attacker.getCenterLocation();
        DPoint attackedLocation = attacked.getCenterLocation();

        ProjectileLaunch launch = null;
        Action attackedAction = attacked.getCurrentAction();
        if (attackedAction instanceof Action.MoveSeq) {
            Action.MoveSeq attackedMove = (Action.MoveSeq) attackedAction;
            DPoint attackedDestination = (DPoint) attackedMove.path.points.get(attackedMove.progress);
            double dx = attackedDestination.x - attackedLocation.x;
            double dy = attackedDestination.y - attackedLocation.y;
            double n = Math.sqrt(dx * dx + dy * dy);
            if (n > 1e-4) {
                Ballistics.Solution intersection = Ballistics.getIntersections(
                        attackerLocation.x,
                        attackerLocation.y,
                        weaponSpec.projectile.speed,
                        attackedLocation.x,
                        attackedLocation.y,
                        attacked.getMovementSpeed(),
                        dx / n,
                        dy / n
                ).minimumTimeSolution();
                launch = new ProjectileLaunch(
                        weaponSpec.projectile,
                        attackTime,
                        attackerLocation,
                        weaponSpec.damage,
                        weaponSpec.damageType,
                        intersection.dx, intersection.dy,
                        launchingPlayer
                );
            }
        }

        if (launch == null) {
            double dx = attackedLocation.x - attackerLocation.x;
            double dy = attackedLocation.y - attackerLocation.y;
            double n = Math.sqrt(dx * dx + dy * dy);
            if (n < 1e-4) {
                dx = 1.0;
                dy = 0.0;
                n = 1.0;
            }
            launch = new ProjectileLaunch(
                    weaponSpec.projectile,
                    attackTime,
                    attackerLocation,
                    weaponSpec.damage,
                    weaponSpec.damageType,
                    dx / n, dy / n,
                    launchingPlayer
            );
        }

        EntityId entityId = game.idGenerator.generateId();
        game.serverState.state.projectileManager.set(entityId, launch);
        // TODO: currently, projectiles are visible to everyone, regardless of visibility
        broadCaster.broadCast(new Message.ProjectileLaunched(entityId, launch));
    }


    public void removeProjectile(EntityId entityId) {
        game.serverState.state.projectileManager.remove(entityId);
        broadCaster.broadCast(new Message.ProjectileLanded(entityId));
    }


    public boolean decreaseWeaponCondition(EntityReader attacker, Weapon weapon, double decrementCondition) {
        WeaponSet weapons = attacker.getWeapons();

        weapon.condition -= decrementCondition;

        boolean usedUp = weapon.condition <= 0;
        if (usedUp) {
            weapons.remove(weapon);
        }

        broadCaster.broadCast(UnitUpdater.weaponsChanged(attacker, weapons));

        return usedUp;
    }

    public void constructionChanged(Player owner, EntityId constructionId, EntityId buildingId) {
        broadCaster.send(owner, new Message.AiEventMessage(new BuildingPlacementChanged(constructionId, buildingId)));
    }

    public void notifyProductionCompleted(EntityId creator, EntityId createdUnit, Player owner) {
        broadCaster.send(owner, new Message.AiEventMessage(new ProductionComplete(creator, createdUnit)));
    }

//
//    public void die(EntityId entityId) {
//        final Object sync = Zoom.serverState.state.entityManager.getSynchronizationObject(entityId);
//        if (sync == null) {
//            return;
//        }
//        synchronized (sync) {
//            if (!Zoom.serverState.state.playerManager.playerOwns(player, entityId)) {
//                return;
//            }
//
//            EntitySpec targetType = Zoom.serverState.state.typeManager.getType(entityId);
//            if (targetType == null) {
//                return;
//            }
//
//            DPoint targetLocation = Zoom.serverState.state.locationManager.getLocation(entityId);
//            if (targetLocation == null) {
//                return;
//            }
//
//            // TODO: should worry about the garrisoned units
//            // TODO: should worry about the ridden units
//
//            killUnit(entityId, targetType, targetLocation);
//        }
//    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SET AI
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CREATION / DESTRUCTION
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CHANGE OTHER STATE...
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


//    public void clearCurrentAi(EntityId unitId) {
//        if (!Zoom.serverState.state.playerManager.playerOwns(player, unitId)) {
//            return;
//        }
//        Zoom.serverState.aiManager.removeAi(unitId);
//    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Helpers
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


//    public enum AttemptToInteractResult {
//        TOO_FAR,
//        IMPOSSIBLE,
//        SUCCESSFUL
//    }


//    public void ungarrisonAll(EntityId toUnGarrison) {
//        Object sync = Zoom.serverState.state.entityManager.getSynchronizationObject(toUnGarrison);
//        if (sync == null) return;
//        EntitySpec hType = Zoom.serverState.state.typeManager.getType(toUnGarrison);
//        if (hType == null) return;
//        synchronized (sync) {
//            if (!Zoom.serverState.state.playerManager.playerOwns(player, toUnGarrison)) {
//                return;
//            }
//
//            DPoint unGarrisonLocation = Zoom.serverState.state.locationManager.getLocation(toUnGarrison);
//            if (unGarrisonLocation == null) {
//                return;
//            }
//
//            Set<EntityId> entityId = Zoom.serverState.state.garrisonManager.ungarrisonAllFrom(toUnGarrison);
//            for (EntityId entity : entityId) {
//                DPoint location = Zoom.serverState.state.locationManager.getLocation(entity);
//                UnitSpec type = (UnitSpec) Zoom.serverState.state.typeManager.getType(entity);
//                changeUnitsDirectedLocation(entity, location, unGarrisonLocation, type.initialLineOfSight);
//            }
//
//            if (hType.containsClass("return-to-gaia")) {
//                Zoom.serverState.state.playerManager.setOwnership(toUnGarrison, PlayerManager.GAIA);
//                broadCaster.broadCast(new Message.UnitUpdated(toUnGarrison, PlayerManager.GAIA));
//            }
//        }
//        broadCaster.broadCast(new Message.AllUnGarrisoned(toUnGarrison));
//    }



//
//    // For debugging...
//        if (spec.name.equals("human")) {
//        Load weaponsLoad = state.carryingManager.get(id);
//        WeaponSet s = new WeaponSet();
//        switch ((int)(5 * Math.random())) {
//            case 0:
//                s.add(new Weapon(Weapons.Sword));
//                break;
//            case 1:
//                s.add(new Weapon(Weapons.Bow));
//                weaponsLoad.setQuantity(Weapons.RESOURCES_REQUIRED_BY_WEAPONS[0], 100);
//                state.carryingManager.set(id, weaponsLoad);
//                break;
//            case 2:
//                s.add(new Weapon(Weapons.LaserGun));
//                break;
//            case 3:
//                s.add(new Weapon(Weapons.Rifle));
//                weaponsLoad.setQuantity(Weapons.RESOURCES_REQUIRED_BY_WEAPONS[1], 100);
//                state.carryingManager.set(id, weaponsLoad);
//                break;
//            case 4:
//                s.add(new Weapon(Weapons.Sword));
//                break;
//        }
//        s.add(new Weapon(Weapons.Fist));
//        state.weaponsManager.set(id, s);
//    }
}
