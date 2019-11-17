package server.state;

import ai.RandomlyWaitAndMove;
import common.AiEvent;
import common.Proximity;
import common.action.Action;
import common.algo.Ballistics;
import common.msg.Message;
import common.msg.UnitUpdater;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.Player;
import common.state.los.VisibilityChange;
import common.state.spec.*;
import common.state.spec.attack.DamageType;
import common.state.spec.attack.Weapon;
import common.state.spec.attack.WeaponSpec;
import common.state.spec.attack.Weapons;
import common.state.sst.GameState;
import common.state.sst.GameStateHelper;
import common.state.sst.sub.*;
import common.state.sst.sub.capacity.Prioritization;
import common.state.sst.sub.capacity.PrioritizedCapacitySpec;
import common.util.DPoint;
import common.util.GridLocationQuerier;
import common.util.json.EmptyJsonable;
import server.algo.ConnectedSet;
import server.algo.UnGarrisonLocation;
import server.app.BroadCaster;
import common.util.EvolutionSpec;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
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
        EntityReader entity = new EntityReader(game.serverState.state, entityId);
        daAction.requestingPlayer = player;
        switch (daAction.type) {
            case Attack: {
                Action.Attack action = (Action.Attack) daAction;
                if (!entity.isOwnedBy(player)) return;
                if (entity.isHidden()) return;

                Action previousAction = game.serverState.state.actionManager.get(entityId);
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
                action.timeRemaining = action.spec.createdType.creationTime;

                final Object sync = game.serverState.state.entityManager.get(entityId);
                if (sync == null) return;
                synchronized (sync) {
                    if (!entity.isOwnedBy(player)) {
                        return;
                    }
                    Action currentAction = entity.getCurrentAction();
                    if (currentAction != null && !(currentAction instanceof Action.Idle))
                        return;
                    Load load = entity.getCarrying();
                    if (!load.canAfford(action.spec.createdType.requiredResources))
                        return;
                    EntitySpec creatorType = entity.getType();
                    if (creatorType == null || !creatorType.canCreate.contains(action.spec)) {
                        return;
                    }
                    action.timeRemaining = action.spec.createdType.creationTime;
                    load.subtract(action.spec.createdType.requiredResources);
                    broadCaster.broadCast(UnitUpdater.updateUnitLoad(entityId, load));
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
            default:
                throw new RuntimeException("Unknown action " + daAction.type);
        }
        game.serverState.state.actionManager.set(entityId, daAction);
        broadCaster.broadCast(UnitUpdater.updateUnitAction(entityId, daAction));
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////


    public void placeBuilding(EntitySpec spec, Point location) {
        // check if there is space...
        if (spec.containsClass("constructed")) {
            spec = spec.createConstructionSpec(game.lobby.getCurrentSpec());
        }
        System.out.println("Building created.");

        createUnit(game.idGenerator.generateId(), spec, new EvolutionSpec(spec), new DPoint(location), Player.GAIA);
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
                if (!GameStateHelper.playerCanGarrison(game.serverState.state, player, entityId, withinId)) return;
                if (garrisonWithin.getNumGarrisonedUnits() >= garrisonWithin.getType().garrisonCapacity) return;
                if (!Proximity.closeEnoughToInteract(game.serverState.state, entityId, withinId)) return;

                Player owner = toGarrison.getOwner();
                DPoint toGarrisonLocation = toGarrison.getLocation();

                setOwner(garrisonWithin.entityId, owner);

                game.serverState.state.garrisonManager.set(toGarrison.entityId, garrisonWithin.entityId);
                broadCaster.broadCast(UnitUpdater.updateUnitIsWithin(toGarrison.entityId, garrisonWithin.entityId));

                game.serverState.state.hiddenManager.set(toGarrison.entityId, true);
                broadCaster.broadCast(UnitUpdater.updateUnitVisibiliy(toGarrison.entityId, true));

                updateLineOfSight(toGarrison.entityId, toGarrisonLocation, null);

                EntitySpec type = garrisonWithin.getType();
                if (type.containsClass("player-occupies"))
                    updateGateOccupancy(garrisonWithin.entityId);
            }
        }
    }

    public void ungarrison(EntityId entityId) {
        EntityReader entity = new EntityReader(game.serverState.state, entityId);
        Object sync = entity.getSync();
        if (sync == null) {
            return;
        }
        synchronized (sync) {
            if (entity.noLongerExists()) return;
            if (!entity.isOwnedBy(player)) return;
            EntityId holderId = entity.getHolder();
            if (holderId == null) return;
            UnGarrisonLocation unGarrisonLocation = UnGarrisonLocation.getUnGarrisonLocation(game.serverState.state, holderId);
            if (unGarrisonLocation.isImossible()) return;

            EntityReader holder = new EntityReader(game.serverState.state, holderId);

            game.serverState.state.garrisonManager.remove(entityId);
            broadCaster.broadCast(UnitUpdater.updateUnitGarrison(entityId, EntityId.NONE));

            game.serverState.state.hiddenManager.set(entityId, false);
            broadCaster.broadCast(UnitUpdater.updateUnitVisibiliy(entityId, false));

            game.serverState.state.locationManager.setLocation(entity, unGarrisonLocation.point);
            broadCaster.broadCast(UnitUpdater.updateUnitLocation(entityId, unGarrisonLocation.point));

            if (game.serverState.state.garrisonManager.getByType(holderId).isEmpty())
                setOwner(holderId, Player.GAIA);

            updateLineOfSight(entityId, null, unGarrisonLocation.point);

            EntitySpec type = holder.getType();
            if (type.containsClass("player-occupies"))
                updateGateOccupancy(holderId);

            if (unGarrisonLocation.path != null) {
                setUnitAction(entityId, new Action.MoveSeq(unGarrisonLocation.path));
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
                if (!GameStateHelper.playerCanRide(game.serverState.state, player, riderId, riddenId)) return;
                if (!Proximity.closeEnoughToInteract(game.serverState.state, riderId, riddenId)) return;

                setOwner(rider.entityId, player);

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
        EntityId ridingId = rider.getRiding();
        if (ridingId == null) return;
        EntityReader ridden = new EntityReader(game.serverState.state, ridingId);
        Object[] sync = GameStateHelper.getSynchronizationObjects(game.serverState.state.entityManager, riderId, ridingId);
        if (sync == null) {
            return;
        }
        synchronized (sync[0]) {
            synchronized (sync[1]) {
                if (rider.noLongerExists() || ridden.noLongerExists())
                    return;
                if (!rider.isOwnedBy(player)) return;
                if (rider.isHidden()) return;
                if (!rider.getRiding().equals(ridden.entityId)) return;

                // trickier: TODO: Need to reset the action of the ridden


                DPoint newLocation = rider.getLocation();

                game.serverState.state.ridingManager.set(rider.entityId, EntityId.NONE);
                broadCaster.broadCast(UnitUpdater.updateUnitRides(rider.entityId, EntityId.NONE));

                game.serverState.state.hiddenManager.set(ridden.entityId, false);
                broadCaster.broadCast(UnitUpdater.updateUnitVisibiliy(ridden.entityId, false));

                if (!rider.getType().containsClass("owned"))
                    setOwner(rider.entityId, Player.GAIA);

//                game.serverState.state.movementSpeedManager.set(rider.entityId, prev.movementSpeed);
//                broadCaster.broadCast(UnitUpdater.updateUnitMovementSpeed(rider.entityId, prev.movementSpeed));

                game.serverState.state.locationManager.setLocation(ridden, newLocation);
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
            if (!entity.isOwnedBy(player)) return;
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

    public void changeUnitLocation(EntityReader entity, DPoint desiredLocation) {
        DPoint oldLocation = game.serverState.state.locationManager.getLocation(entity.entityId);

        game.serverState.state.locationManager.setLocation(entity, desiredLocation);
        broadCaster.broadCast(UnitUpdater.updateUnitLocation(entity.entityId, desiredLocation));

        updateLineOfSight(entity.entityId, oldLocation, desiredLocation);
    }

    public void dropAll(EntityId entityId, Load load) {
        load.quantities.clear();
        broadCaster.broadCast(UnitUpdater.updateUnitLoad(entityId, load));
    }

    public void setCreationProgress(EntityId entityId, Action.Create action, double v) {
        action.timeRemaining = v;
        broadCaster.broadCast(UnitUpdater.updateUnitAction(entityId, action));
    }


    public void updateGateOccupancy(EntityId entityId) {
        DPoint location = game.serverState.state.locationManager.getLocation(entityId);
        if (game.serverState.state.gateStateManager.getByType(location.toPoint()).isEmpty()) {
            return;
        }
        for (int i = 0; i < game.lobby.getNumPlayers(); i++) {
            Player player = new Player(i + 1);
            Dimension size = game.serverState.state.typeManager.get(entityId).size;
            boolean occupiedFor = GateInfo.isOccupiedFor(entityId, player, game.serverState.state.gateStateManager, game.serverState.state.playerManager);
            broadCaster.send(player, new Message.OccupancyChanged(location.toPoint(), size, occupiedFor));
        }
    }

    public void createUnit(EntityId id, EntitySpec spec, EvolutionSpec eSpec, DPoint location, Player player) {
        GameState state = game.serverState.state;
        if (state.entityManager.get(id) == null)
            state.entityManager.set(id, new EmptyJsonable());
        state.typeManager.set(id, spec);
        state.locationManager.setLocation(new EntityReader(state, id), location);
        state.playerManager.set(id, spec.containsClass("owned") ? player : Player.GAIA);
        state.ageManager.set(id, state.currentTime);
        if (eSpec.initialBaseHealth != 0.0) state.healthManager.set(id, eSpec.initialBaseHealth);
        if (eSpec.initialBaseHealth != 0.0) state.baseHealthManager.set(id, eSpec.initialBaseHealth);
        if (eSpec.initialMovementSpeed != 0.0) state.movementSpeedManager.set(id, eSpec.initialMovementSpeed);
        state.orientationManager.set(id, 0.0);
        if (eSpec.initialRotationSpeed != 0.0) state.rotationSpeedManager.set(id, eSpec.initialRotationSpeed);
        if (eSpec.carryCapacity != null) state.capacityManager.set(id, eSpec.carryCapacity);
        if (eSpec.initialAttackSpeed != 0.0) state.attackSpeedManager.set(id, eSpec.initialAttackSpeed);
        if (eSpec.initialBuildSpeed != 0.0) state.buildSpeedManager.set(id, eSpec.initialBuildSpeed);
        if (eSpec.initialDepositSpeed != 0.0) state.depositSpeedManager.set(id, eSpec.initialDepositSpeed);
        if (eSpec.initialCollectSpeed != 0.0) state.collectSpeedManager.set(id, eSpec.initialCollectSpeed);
        if (eSpec.initialLineOfSight != 0.0) state.lineOfSightManager.set(id, eSpec.initialLineOfSight);

        Load load = new Load();
        for (CarrySpec cSpec : spec.carrying) {
            load.setQuantity(cSpec.type, cSpec.startingQuantity);
        }
        state.carryingManager.set(id, load);

        if (spec instanceof ConstructionSpec) {
            state.constructionManager.set(id, new ConstructionZone((ConstructionSpec) spec, location));
        }
        
        for (CreationSpec creationSpec : spec.canCreate) {
            if (creationSpec.method.equals(CreationMethod.Garrison)) {
                state.evolutionManager.set(id, EvolutionSpec.uniformWeights());
                break;
            }
        }

        if (spec.containsClass("player-occupies")) {
            state.gateStateManager.set(id, new GateInfo(location.toPoint(), GateInfo.GateState.UnlockedForPlayerOnly));
        }

        broadCaster.broadCast(createCreateUnitMessage(id));

        updateLineOfSight(id, null, location);

        if (spec.containsClass("occupies")) {
            game.serverState.state.occupancyState.setOccupancy(location.toPoint(), spec.size, true);
            broadCaster.broadCast(new Message.OccupancyChanged(location.toPoint(), spec.size, true));
        }

        if (spec.containsClass("player-occupies")) {
            updateGateOccupancy(id);
        }

        moveOtherUnitsOutOfTheWay(spec, location);


        if (spec.ai != null) {
            switch (spec.ai) {
                case "deer-ai":
                    double maxWait = Double.valueOf(spec.aiArgs.get("max-wait"));
                    int maxSize = Integer.valueOf(spec.aiArgs.get("max-size"));
                    game.serverState.gaiaAi.setAi(
                        id,
                        new RandomlyWaitAndMove(
                            game.serverState.state,
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
        } else {
            game.serverState.state.actionManager.set(id, new Action.Idle());
        }

        // For debugging...
        if (spec.name.equals("human")) {
            Load weaponsLoad = state.carryingManager.get(id);
            WeaponSet s = new WeaponSet();
            switch ((int)(5 * Math.random())) {
                case 0:
                    s.add(new Weapon(Weapons.Fist));
                    break;
                case 1:
                    s.add(new Weapon(Weapons.Bow));
                    weaponsLoad.setQuantity(Weapons.RESOURCES_REQUIRED_BY_WEAPONS[0], 100);
                    state.carryingManager.set(id, weaponsLoad);
                    break;
                case 2:
                    s.add(new Weapon(Weapons.LaserGun));
                    break;
                case 3:
                    s.add(new Weapon(Weapons.Rifle));
                    weaponsLoad.setQuantity(Weapons.RESOURCES_REQUIRED_BY_WEAPONS[1], 100);
                    state.carryingManager.set(id, weaponsLoad);
                    break;
                case 4:
                    s.add(new Weapon(Weapons.Sword));
                    break;
            }
            s.add(new Weapon(Weapons.Sword));

            state.weaponsManager.set(id, s);
        }
    }

    private void moveOtherUnitsOutOfTheWay(EntitySpec spec, DPoint location) {
        if (!spec.containsClass("player-occupies") && !spec.containsClass("occupies"))
            return;
        for (EntityId entityId : game.serverState.state.locationManager.getEntitiesWithin(
                location.x,
                location.y,
                location.x + spec.size.width,
                location.y + spec.size.height,
                entity -> {
                    EntitySpec entitySpec = game.serverState.state.typeManager.get(entity);
                    if (entitySpec == null) return false;
                    if (game.serverState.state.locationManager.getLocation(entity) == null) return false;
                    Double movementSpeed = game.serverState.state.movementSpeedManager.get(entity);
                    return movementSpeed != null && movementSpeed > 0;
                }
        )) {
            EntityReader entity = new EntityReader(game.serverState.state, entityId);
            DPoint toMoveLocation = entity.getLocation();
            if (toMoveLocation == null) continue;
            Point nearestEmptyTile = ConnectedSet.findNearestEmptyTile(game.serverState.state.gameSpec, toMoveLocation.toPoint(), game.serverState.state.getOccupancyForAny());
            if (nearestEmptyTile == null) continue;
            changeUnitLocation(entity, new DPoint(nearestEmptyTile));
        }
    }

    public void killUnit(EntityId target) {
        // TODO: remove all garrisoned units...
        // garrisoned...
        // ridden...
        EntityReader reader = new EntityReader(game.serverState.state, target);
        EntityId riding = reader.getRiding();
        if (riding != null) {
            stopRiding(target);
        }
        Set<EntityId> garrisoned = reader.getGarrisoned();
        if (garrisoned != null && !garrisoned.isEmpty()) {
            for (EntityId entity : garrisoned) {
                ungarrison(entity);
            }
        }

        DPoint targetLocation = game.serverState.state.locationManager.getLocation(target);
        EntitySpec targetType = game.serverState.state.typeManager.get(target);

        if (targetType.containsClass("occupies") || targetType.containsClass("player-occupies")) {
            game.serverState.state.occupancyState.setOccupancy(targetLocation.toPoint(), targetType.size, false);
            broadCaster.broadCast(new Message.OccupancyChanged(targetLocation.toPoint(), targetType.size, false));
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
        broadCaster.broadCast(new Message.AiEventMessage(new AiEvent.TargetKilled(target, dropped)));
    }

    public void done(EntityReader entity, AiEvent.ActionCompletedReason reason) {
        if (entity.getOwner().equals(Player.GAIA)) {
            game.serverState.gaiaAi.unitCompletedAction(this, entity.entityId);
            return;
        }
        if (entity.noLongerExists()) {
            // i guess the suicide ai doesn't need to be notified.
            return;
        }

        Action action = new Action.Idle();
        game.serverState.state.actionManager.set(entity.entityId, action);

        broadCaster.broadCast(UnitUpdater.updateUnitAction(entity.entityId, action));
        broadCaster.broadCast(new Message.AiEventMessage(new AiEvent.ActionCompleted(entity.entityId, reason)));
    }

    public static String getDebugString() {
        try {
            throw new RuntimeException();
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                t.printStackTrace(pw);
            }
            return sw.toString();
        }
    }

    private void setOwner(EntityId entityId, Player newOwner) {
        Player previousOwner = game.serverState.state.playerManager.get(entityId);
        if (previousOwner.equals(newOwner)) {
            return;
        }

        DPoint location = game.serverState.state.locationManager.getLocation(entityId);
        updateLineOfSight(entityId, location, null);
        game.serverState.state.playerManager.set(entityId, newOwner);
        broadCaster.broadCast(UnitUpdater.updateUnitOwner(entityId, newOwner));
        updateLineOfSight(entityId, null, location);

        updateGateOccupancy(entityId);

        // TODO: important...
        // might have to change occupancies as well...
    }

    private static double zin(Double d) {
        if (d == null) return 0.0;
        return d;
    }
    private void updateLineOfSight(EntityId entityId, DPoint oldLocation, DPoint newLocation) {
        if (oldLocation != null && newLocation != null && oldLocation.toPoint().equals(newLocation.toPoint())) return;
        if (oldLocation == null && newLocation == null) return;

        double lineOfSight = zin(game.serverState.state.lineOfSightManager.get(entityId));
        Message.UnitUpdated updateUnitLineOfSight = UnitUpdater.updateUnitLineOfSight(
                entityId,
                game.serverState.state.playerManager.get(entityId),
                oldLocation,
                newLocation,
                lineOfSight
        );
        broadCaster.send(updateUnitLineOfSight.losPlayer, updateUnitLineOfSight);

        processVisibilityChanges(
                updateUnitLineOfSight.losPlayer,
                game.serverState.state.lineOfSight.updateLineOfSight(
                        updateUnitLineOfSight.losPlayer,
                        updateUnitLineOfSight.losOldLocation,
                        updateUnitLineOfSight.losNewLocation,
                        updateUnitLineOfSight.losDistance
                )
        );
    }

    private void processVisibilityChanges(Player player, VisibilityChange change) {
        for (Point p : change.gainedVision) {
            for (EntityId entity : game.serverState.state.locationManager.getEntities(p, GridLocationQuerier.ANY)) {
                becameVisible(player, entity);
                broadCaster.send(player, new Message.OccupancyChanged(p, new Dimension(1, 1), game.serverState.state.isOccupiedFor(p, player)));
            }
        }
        for (Point p : change.lostVision) {
            for (EntityId entity : game.serverState.state.locationManager.getEntities(p, GridLocationQuerier.ANY)) {
                lostVision(player, entity);
            }
        }
    }

    private void lostVision(Player losPlayer, EntityId entity) {
        if (game.serverState.state.typeManager.get(entity).containsClass("visible-in-fog")) {
            return;
        }
        broadCaster.send(losPlayer, new Message.UnitRemoved(entity));
    }

    private void becameVisible(Player losPlayer, EntityId entity) {
        broadCaster.send(losPlayer, createCreateUnitMessage(entity));
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
        updateGateOccupancy(entity.entityId);
    }

    public void updateGameTime(double currentTime) {
        game.serverState.state.currentTime = currentTime;
        broadCaster.broadCast(new Message.TimeChange(currentTime));
    }

    public void setActionProgress(EntityId entityId, Action action) {
        broadCaster.broadCast(UnitUpdater.updateUnitAction(entityId, action));
    }

    public boolean receiveDamage(EntityId attacked, double damage, DamageType damageType) {
        double oldHealth = game.serverState.state.healthManager.get(attacked);
        // to do damage type
        double newHealth = oldHealth - damage;
        if (newHealth > 0) {
            game.serverState.state.healthManager.set(attacked, newHealth);
            broadCaster.broadCast(UnitUpdater.updateUnitHealth(attacked, newHealth));
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
            DPoint attackedDestination = attackedMove.path.points.get(attackedMove.progress);
            double dx = attackedDestination.x - attackedLocation.x;
            double dy = attackedDestination.y - attackedLocation.y;
            double n = Math.sqrt(dx * dx + dy * dy);
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

        if (launch == null) {
            double dx = attackedLocation.x - attackerLocation.x;
            double dy = attackedLocation.y - attackerLocation.y;
            double n = Math.sqrt(dx * dx + dy * dy);
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
//            Set<EntityId> entityIds = Zoom.serverState.state.garrisonManager.ungarrisonAllFrom(toUnGarrison);
//            for (EntityId entity : entityIds) {
//                DPoint location = Zoom.serverState.state.locationManager.getLocation(entity);
//                UnitSpec type = (UnitSpec) Zoom.serverState.state.typeManager.getType(entity);
//                changeUnitLocation(entity, location, unGarrisonLocation, type.initialLineOfSight);
//            }
//
//            if (hType.containsClass("return-to-gaia")) {
//                Zoom.serverState.state.playerManager.setOwnership(toUnGarrison, PlayerManager.GAIA);
//                broadCaster.broadCast(new Message.UnitUpdated(toUnGarrison, PlayerManager.GAIA));
//            }
//        }
//        broadCaster.broadCast(new Message.AllUnGarrisoned(toUnGarrison));
//    }

}
