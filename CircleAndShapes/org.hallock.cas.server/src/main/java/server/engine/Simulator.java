package server.engine;

import common.AiEvent;
import common.Proximity;
import common.action.Action;
import common.algo.AStar;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.Player;
import common.state.spec.EntitySpec;
import common.state.spec.ResourceType;
import common.state.spec.attack.Weapon;
import common.state.spec.attack.WeaponClass;
import common.state.spec.attack.WeaponSpec;
import common.state.sst.GameStateHelper;
import common.state.sst.sub.ConstructionZone;
import common.state.sst.sub.Load;
import common.state.sst.sub.ProjectileLaunch;
import common.state.sst.sub.capacity.Prioritization;
import common.util.DPoint;
import common.util.EvolutionSpec;
import common.util.GridLocationQuerier;
import server.algo.UnGarrisonLocation;
import server.app.ServerContext;
import server.state.ServerGameState;
import server.state.ServerStateManipulator;
import server.util.IdGenerator;

import java.awt.*;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

public class Simulator {

    final ServerContext context;
    final ServerGameState state;
    final IdGenerator generator;
    final Evolution evolution;

    public Simulator(ServerContext context, ServerGameState state, IdGenerator generator, Random random) {
        this.context = context;
        this.state = state;
        this.generator = generator;
        this.evolution = new Evolution(state, random);
    }

    public void simulateUnitActions(EntityId entityId, ServerStateManipulator ssm, double prevTime, double timeDelta) {
        EntityReader entity = new EntityReader(state.state, entityId);

        Action action = state.state.actionManager.get(entityId);
        if (action == null) {
            return;
        }

        if (state.state.hiddenManager.get(entityId)) {
            return;
        }

        switch (action.type) {
            case Attack:
                attack(entity, ssm, (Action.Attack) action, prevTime, timeDelta);
                break;
            case Move:
                move(entity, ssm, (Action.MoveSeq) action, timeDelta);
                break;
            case Collect:
                collect(entity, ssm, (Action.Collect) action, timeDelta);
                break;
            case Deposit:
                deposit(entity, ssm, (Action.Deposit) action, timeDelta);
                break;
            case Idle:
                break;
            case Wait:
                wait(entity, ssm, (Action.Wait) action, timeDelta);
                break;
            case Build:
                build(entity, ssm, (Action.Build) action, timeDelta);
                break;
            case Create:
                create(entity, ssm, (Action.Create) action, timeDelta);
                break;
//            case Chase:
//                chase(entity, ssm, (Action.Chase) action, timeDelta);
//                break;
            default:
                throw new RuntimeException("Unknown action: " + action.type);
        }
    }


    private void wait(EntityReader entity, ServerStateManipulator ssm, Action.Wait action, double timeDelta) {
        Object sync = entity.getSync();
        if (sync == null) return;
        synchronized (sync) {
            if (entity.noLongerExists())
                return;

            action.remainingTime = action.remainingTime - timeDelta;
            if (action.remainingTime <= 0) {
                ssm.done(entity, AiEvent.ActionCompletedReason.Successful);
            }
        }
    }

    private void create(EntityReader entity, ServerStateManipulator ssm, Action.Create action, double timeDelta) {
        Object sync = entity.getOwner();
        if (sync == null) return;
        synchronized (sync) {
            if (entity.noLongerExists())
                return;

//            EntitySpec creatorType = state.state.typeManager.get(entityId);
//            if (creatorType == null)
//                return;

//            Player player = state.state.playerManager.get(entityId);
//            if (player == null)
//                return;

            Set<EntityReader> contributingUnits = action.spec.getContributingUnits(state.state, entity.entityId);

            action.numberOfContributingUnits = contributingUnits.size();
            double newRemainingTime = action.timeRemaining - timeDelta * action.numberOfContributingUnits;
            if (newRemainingTime > 0) {
                ssm.setCreationProgress(entity.entityId, action, newRemainingTime);
                return;
            }

            UnGarrisonLocation unGarrisonLocation = UnGarrisonLocation.getUnGarrisonLocation(state.state, entity);
            if (unGarrisonLocation.isImossible()) {
                ssm.setCreationProgress(entity.entityId, action, 0.0);
                return;
            }

            EntityId createdUnit = generator.generateId();
            ssm.createUnit(createdUnit, action.spec.createdType, evolution.createEvolvedSpec(contributingUnits), unGarrisonLocation.point, entity.getOwner());
            ssm.setUnitAction(new EntityReader(state.state, createdUnit), new Action.MoveSeq(unGarrisonLocation.path));
            ssm.done(entity, AiEvent.ActionCompletedReason.Successful);
        }
    }

    private void attack(EntityReader attacker, ServerStateManipulator ssm, Action.Attack action, double prevTime, double timeDelta) {
        Object[] syncs = GameStateHelper.getSynchronizationObjects(state.state.entityManager, attacker.entityId, action.target);
        if (syncs == null) {
            ssm.done(attacker, AiEvent.ActionCompletedReason.Invalid);
            return;
        }

        synchronized (syncs[0]) {
            synchronized (syncs[1]) {
                EntityReader attacked = new EntityReader(state.state, action.target);
                if (attacker.noLongerExists()) return;
                if (attacked.noLongerExists()) return;
                Weapon weapon = null;
                for (Weapon existingWeapon : attacker.getWeapons().ohMy()) {
                    if (existingWeapon.weaponType.name.equals(action.weaponType)) {
                        weapon = existingWeapon;
                        break;
                    }
                }
                if (weapon == null) {
                    ssm.done(attacker, AiEvent.ActionCompletedReason.Invalid);
                    return;
                }

                DPoint attackerLocation = attacker.getCenterLocation();
                DPoint attackedLocation = attacked.getCenterLocation();
                WeaponSpec weaponSpec = weapon.weaponType;

                if (attackerLocation.distanceTo(attackedLocation) > weapon.weaponType.rangeCanFinishAttackFrom) {
                    ssm.done(attacker, AiEvent.ActionCompletedReason.TooFar);
                    return;
                }

                if (weapon.weaponType.weaponClass.equals(WeaponClass.Instant)) {
                    Load load = attacker.getCarrying();
                    if (!load.canAfford(weaponSpec.fireResources)) {
                        ssm.done(attacker, AiEvent.ActionCompletedReason.OverCapacity);
                        return;
                    }
                    ssm.subtractFromPayload(attacker, load, weaponSpec.fireResources);
                    if (ssm.receiveDamage(attacked.entityId, weaponSpec.damage * timeDelta * attacker.getCurrentAttackSpeed(), weaponSpec.damageType)) {
                        ssm.done(attacker, AiEvent.ActionCompletedReason.Successful);
                        return;
                    }
                    if (ssm.decreaseWeaponCondition(attacker, weapon, weaponSpec.decrementConditionPerUseBy)) {
                        ssm.done(attacker, AiEvent.ActionCompletedReason.OverCapacity);
                        return;
                    }
                    return;
                }

                double progressToProcess = timeDelta;
                double currentTime = prevTime;

                double cooldownTime = weaponSpec.cooldownTime / attacker.getCurrentAttackSpeed();
                double attackTime = weaponSpec.attackTime / attacker.getCurrentAttackSpeed();

                LinkedList<Double> attackTimes = new LinkedList<>();
                while (progressToProcess > 0) {
                    if (action.isOnCooldown) {
                        double cooldownRemaining = action.progress * cooldownTime;
                        if (progressToProcess >= cooldownRemaining) {
                            currentTime += cooldownRemaining;
                            progressToProcess -= cooldownRemaining;
                            action.isOnCooldown = false;
                            action.progress = 0.0;
                        } else {
                            action.progress -= progressToProcess / cooldownTime;
                            progressToProcess = 0;
                        }
                    } else {
                        double chargeRemaining = (1 - action.progress) * attackTime;
                        if (progressToProcess >= chargeRemaining) {
                            currentTime += chargeRemaining;
                            attackTimes.add(currentTime);
                            progressToProcess -= chargeRemaining;
                            action.isOnCooldown = true;
                            action.progress = 1.0;
                        } else {
                            action.progress += progressToProcess / attackTime;
                            progressToProcess = 0;
                        }
                    }
                }

                ssm.setActionProgress(attacker.entityId, action);

                Load load = attacker.getCarrying();
                for (Double attackEventTime : attackTimes) {
                    if (!load.canAfford(weaponSpec.fireResources)) {
                        ssm.done(attacker, AiEvent.ActionCompletedReason.OverCapacity);
                        return;
                    }
                    ssm.subtractFromPayload(attacker, load, weaponSpec.fireResources);

                    boolean died;
                    switch (weaponSpec.weaponClass) {
                        case Melee: {
                            died = ssm.receiveDamage(attacked.entityId, weaponSpec.damage, weaponSpec.damageType);
                        }
                        break;
                        case Instant:
                            throw new IllegalStateException("This should have been handled already.");
                        case Projectile: {
                            ssm.createProjectile(attacker, attacked, weaponSpec, attackEventTime);
                            died = false;
                        }
                        break;
                        default:
                            throw new RuntimeException("Unknown weapon class: " + weaponSpec.weaponClass);
                    }

                    if (ssm.decreaseWeaponCondition(attacker, weapon, weaponSpec.decrementConditionPerUseBy)) {
                        ssm.done(attacker, AiEvent.ActionCompletedReason.OverCapacity);
                        return;
                    }

                    if (died) {
                        ssm.done(attacker, AiEvent.ActionCompletedReason.Successful);
                        return;
                    }
                }
            }
        }
    }

    private void build(EntityReader entity, ServerStateManipulator ssm, Action.Build action, double timeDelta) {
        Object synchronizationObject = entity.getSync();
        if (synchronizationObject == null) {
            ssm.done(entity, AiEvent.ActionCompletedReason.Invalid);
            return;
        }
        synchronized (synchronizationObject) {
            EntityReader constructionEntity = new EntityReader(state.state, action.constructionId);

            if (entity.noLongerExists())
                return;

            if (entity.getBuildSpeed() <= 0) {
                // can't build it anyway
                ssm.done(entity, AiEvent.ActionCompletedReason.Invalid);
                return;
            }

            ConstructionZone constructionZone = constructionEntity.getConstructionZone();
            if (constructionZone == null || !constructionEntity.getMissingConstructionResources().isEmpty()) {
                ssm.done(entity, AiEvent.ActionCompletedReason.Invalid);
                return;
            }

            if (!Proximity.closeEnoughToInteract(entity, constructionEntity)) {
                ssm.done(entity, AiEvent.ActionCompletedReason.TooFar);
                return;
            }

            double newConstructionProgress = constructionZone.progress + entity.getBuildSpeed() * timeDelta;
            if (newConstructionProgress < 1) {
                ssm.updateBuildProgress(action.constructionId, constructionZone, newConstructionProgress);
                return;
            }

            if (!state.state.hasSpaceFor(constructionZone.location,  constructionZone.constructionSpec.size)) {
                return;
            }

            // TODO: move any units out of the way...

            ssm.killUnit(action.constructionId);
            EntitySpec resultingStructure = constructionZone.constructionSpec.resultingStructure;
            ssm.createUnit(generator.generateId(), resultingStructure, new EvolutionSpec(resultingStructure), constructionZone.location, Player.GAIA);

            ssm.done(entity, AiEvent.ActionCompletedReason.Successful);
        }
    }

    private static int zin(Integer i) { if (i == null) return 0; else return i; }

    // should return ActionCompletedReason
    private boolean transfer(ServerStateManipulator ssm, EntityReader from, EntityReader to, ResourceType r, int amount, int maxToCarry) {
        if (amount == 0) return true;

        if (from.equals(to)) return false;
        Load fromLoad = from.getCarrying();
        if (fromLoad == null) return false;
        Load toLoad = to.getCarrying();
        if (toLoad == null) return false;

        Prioritization prioritization = from.getCapacity().getPrioritization(r);

        int possibleToAccept = Math.min(maxToCarry, to.getCapacity().amountPossibleToAccept(toLoad, r));
        int possibleToTake = Math.max(0, fromLoad.quantities.getOrDefault(r, 0) - prioritization.desiredAmount);
        int amountToTransfer = Math.min(amount, Math.min(possibleToAccept, possibleToTake));
        if (amountToTransfer <= 0)
            return false;
        int newFrom = possibleToTake - amountToTransfer;
        int newTo = toLoad.quantities.getOrDefault(r, 0) + amountToTransfer;

        ssm.changePayload(from.entityId, fromLoad, r, newFrom);
        ssm.changePayload(to.entityId, toLoad, r, newTo);

        return amountToTransfer < possibleToAccept && newFrom < possibleToTake;
    }

    // Could be done in the actual action class?
    private void deposit(EntityReader entity, ServerStateManipulator ssm, Action.Deposit action, double timeDelta) {
        Object[] syncs = GameStateHelper.getSynchronizationObjects(state.state.entityManager, entity.entityId, action.location);
        if (syncs == null) return;
        synchronized (syncs[0]) {
            synchronized (syncs[1]) {
                EntityReader depositLocation = new EntityReader(state.state, action.location);
                if (!Proximity.closeEnoughToInteract(entity, depositLocation)) {
                    ssm.done(entity, AiEvent.ActionCompletedReason.TooFar);
                    return;
                }
                double nextProgress = action.progress + entity.getDepositSpeed() * timeDelta * 100 / (double) action.resource.weight;
                int amount = (int) nextProgress;
                action.progress = Math.max(0.0, Math.min(1.0, nextProgress - amount));
                ssm.setActionProgress(entity.entityId, action);
                if (!transfer(ssm, entity, new EntityReader(state.state, action.location), action.resource, amount, action.maxAmount)) {
                    ssm.done(entity, AiEvent.ActionCompletedReason.OverCapacity); // ?
                }
            }
        }
    }

    private void collect(EntityReader entity, final ServerStateManipulator ssm, final Action.Collect action, double timeDelta) {
        Object[] syncs = GameStateHelper.getSynchronizationObjects(state.state.entityManager, entity.entityId, action.resourceCarrier);
        if (syncs == null) {
            // one of them does not exist anymore...
            Object sync = entity.getSync();
            if (sync == null)
                // it is the collector...
                return;
            synchronized (sync) {
                ssm.done(entity, AiEvent.ActionCompletedReason.Invalid);
            }
            return;
        }
        synchronized (syncs[0]) {
            synchronized (syncs[1]) {
                EntityReader collected = new EntityReader(state.state, action.resourceCarrier);
                if (entity.noLongerExists() || collected.noLongerExists()) {
                    return;
                }
                if (!Proximity.closeEnoughToInteract(entity, collected)) {
                    ssm.done(entity, AiEvent.ActionCompletedReason.TooFar);
                    return;
                }

                double collectionSpeed = entity.getCollectSpeed();
                if (!(collected.getType().containsClass("natural-resource"))) {
                    collectionSpeed *= 20;
                }

                double nextProgress = action.progress + collectionSpeed * timeDelta * 100 / action.resource.weight;
                int amount = (int) nextProgress;
                action.progress = Math.max(0.0, Math.min(1.0, nextProgress - amount));
                ssm.setActionProgress(entity.entityId, action);
                if (!transfer(ssm, collected, entity, action.resource, amount, action.maximumAmountToCollect)) {
                    ssm.done(entity, AiEvent.ActionCompletedReason.OverCapacity);

                    double amountLeft = collected.getCarrying().getWeight();
                    // TODO: should there be a more natural way of handling this?
                    // die on empty?
                    if (amountLeft <= 0 && collected.getType().containsClass("natural-resource")) {
                        context.executorService.submit(() -> ssm.killUnit(action.resourceCarrier));
                    }
                }
            }
        }
    }

//    private void chase(EntityReader entity, ServerStateManipulator ssm, Action.Chase action, double timeDelta) {
//        // TODO: can't travel directly there...
//        Object synchronizationObject = entity.getSync();
//        if (synchronizationObject == null) return;
//
//        synchronized (synchronizationObject) {
//            DPoint idealLocation = state.state.locationManager.getLocation(action.chased);
//            if (state.state.lineOfSight.isVisible(action.requestingPlayer, (int) idealLocation.x, (int) idealLocation.y)) {
//                action.lastKnownLocation = idealLocation;
//            }
//            DPoint targetLocation = action.lastKnownLocation;
//
//            DPoint location = entity.getLocation();
//            EntitySpec type = entity.getType();
//            if (type == null)
//                return;
//            double speed = entity.getMovementSpeed();
//
//            double dx = targetLocation.x - location.x;
//            double dy = targetLocation.y - location.y;
//            double n = Math.sqrt(dx * dx + dy * dy);
//            double distanceToTravel = timeDelta * speed;
//
//            boolean done = false;
//            final DPoint desiredLocation;
//            if (n > distanceToTravel) {
//                desiredLocation = new DPoint(
//                        location.x + distanceToTravel * dx / n,
//                        location.y + distanceToTravel * dy / n
//                );
//            } else {
//                desiredLocation = targetLocation;
//                done = true;
//            }
//
//            Point gridPoint = desiredLocation.toPoint();
//            if (state.state.isOccupiedFor(gridPoint, entity.getOwner())) {
//                ssm.done(entity, AiEvent.ActionCompletedReason.Invalid);
//                return;
//            }
//
//            ssm.changeUnitLocation(entity, desiredLocation);
//            if (done)
//                ssm.done(entity, AiEvent.ActionCompletedReason.Successful);
//        }
//    }

    private void move(EntityReader entity, ServerStateManipulator ssm, Action.MoveSeq action, double timeDelta) {
        Object synchronizationObject = entity.getSync();
        if (synchronizationObject == null) return;

        synchronized (synchronizationObject) {
            if (action.path == null || action.path.isEmpty()) {
                ssm.done(entity, AiEvent.ActionCompletedReason.Successful);
                return;
            }
            DPoint destination = action.path.points.get(action.progress);

            DPoint location = entity.getLocation();
            EntitySpec type = entity.getType();
            if (type == null)
                return;
            double speed = entity.getMovementSpeed();

            double dx = destination.x - location.x;
            double dy = destination.y - location.y;
            double n = Math.sqrt(dx * dx + dy * dy);
            double distanceToTravel = timeDelta * speed;

            boolean done = false;
            final DPoint desiredLocation;
            if (n > distanceToTravel) {
                desiredLocation = new DPoint(
                        location.x + distanceToTravel * dx / n,
                        location.y + distanceToTravel * dy / n
                );
            } else {
                desiredLocation = destination;
                if (action.progress < action.path.points.size() - 1) {
                    action.progress++;
                } else {
                    done = true;
                }
            }

            Point gridPoint = desiredLocation.toPoint();
            if (state.state.isOccupiedFor(gridPoint, entity.getOwner())) {
                ssm.done(entity, AiEvent.ActionCompletedReason.Invalid);
                return;
            }
            if (true || state.state.locationManager.getEntitiesWithin(
                    desiredLocation.x, desiredLocation.y, location.x + type.size.width, location.y + type.size.height,
                    qEntity -> !entity.entityId.equals(qEntity)).isEmpty()) {
                action.blockedCount = 0;
                ssm.changeUnitLocation(entity, desiredLocation);
                if (done)
                    ssm.done(entity, AiEvent.ActionCompletedReason.Successful);
                return;
            }
            if (action.blockedCount++ < 3) {
                return;
            }
            AStar.PathSearch path = AStar.findPath(location, destination, state.state.locationManager.createOccupancyView(entity.entityId));
            if (!path.successful) {
                ssm.done(entity, AiEvent.ActionCompletedReason.Invalid);
                return;
            }
        }
    }

    public void processProjectile(ServerStateManipulator ssm, EntityId entityId, ProjectileLaunch launch) {
        DPoint currentLocation = launch.getLocation(state.state.currentTime);
        if (currentLocation == null) {
            ssm.removeProjectile(entityId);
            return;
        }

        GridLocationQuerier.NearestEntityQueryResults queryResults = state.state.locationManager.query(new GridLocationQuerier.NearestEntityQuery(
                state.state,
                currentLocation,
                entity -> {
                    EntitySpec entitySpec = state.state.typeManager.get(entity);
                    Player player = state.state.playerManager.get(entity);
                    if (entitySpec == null || player == null) return false;
                    return entitySpec.containsClass("unit") && !player.equals(launch.launchingPlayer); //  should be enemies
                },
                launch.projectile.radius,
                null
        ));

        if (queryResults.successful() && launch.hit(entityId)) {
            ssm.receiveDamage(queryResults.entity, launch.damage, launch.damageType);

            if (launch.projectile.stopsOnFirst) {
                ssm.removeProjectile(entityId);
            }
        }
    }
}
