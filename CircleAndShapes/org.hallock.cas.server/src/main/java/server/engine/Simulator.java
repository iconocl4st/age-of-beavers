package server.engine;

import common.Proximity;
import common.action.Action;
import common.event.ActionCompleted;
import common.factory.Path;
import common.factory.SearchDestination;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.Occupancy;
import common.state.Player;
import common.state.spec.EntitySpec;
import common.state.spec.ResourceType;
import common.state.spec.attack.Weapon;
import common.state.spec.attack.WeaponClass;
import common.state.spec.attack.WeaponSpec;
import common.state.sst.GameStateHelper;
import common.state.sst.OccupancyView;
import common.state.sst.sub.ConstructionZone;
import common.state.sst.sub.MovableEntity;
import common.state.sst.sub.Load;
import common.state.sst.sub.ProjectileLaunch;
import common.state.sst.sub.capacity.Prioritization;
import common.util.Bounds;
import common.util.DPoint;
import common.util.EvolutionSpec;
import common.util.GridLocation;
import common.util.json.Jsonable;
import common.util.query.NearestEntityQuery;
import common.util.query.NearestEntityQueryResults;
import server.algo.UnGarrisonLocation;
import server.app.ServerContext;
import server.state.ServerGameState;
import server.state.ServerStateManipulator;
import server.state.TimeInfo;
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
    final Random random;

    public Simulator(ServerContext context, ServerGameState state, IdGenerator generator, Random random) {
        this.context = context;
        this.state = state;
        this.generator = generator;
        this.evolution = new Evolution(state, random);
        this.random = random;
    }

    public void simulateUnitActions(EntityId entityId, ServerStateManipulator ssm, TimeInfo info) {
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
                attack(entity, ssm, (Action.Attack) action, info.prevTime, info.timeDelta);
                break;
            case Move:
                move(entity, ssm, (Action.MoveSeq) action, info.currentTime, info.timeDelta);
                break;
            case Collect:
                collect(entity, ssm, (Action.Collect) action, info.timeDelta);
                break;
            case Deposit:
                deposit(entity, ssm, (Action.Deposit) action, info.timeDelta);
                break;
            case Idle:
                break;
            case Wait:
                wait(entity, ssm, (Action.Wait) action, info.timeDelta);
                break;
            case Build:
                build(entity, ssm, (Action.Build) action, info.timeDelta);
                break;
            case Create:
                create(entity, ssm, (Action.Create) action, info.timeDelta);
                break;
            case Plant:
                plant(entity, ssm, (Action.Bury) action, info.timeDelta);
            case Garden:
                garden(entity, ssm, (Action.Garden) action, info.timeDelta);
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
                ssm.done(entity, ActionCompleted.ActionCompletedReason.Successful);
            }
        }
    }


    private void garden(EntityReader entity, ServerStateManipulator ssm, Action.Garden action, double timeDelta) {
        Object sync = entity.getSync();
        if (sync == null) return;
        synchronized (sync) {
            if (entity.noLongerExists())
                return;
            double gardenSpeed = entity.getGardenSpeed();
            double progress = action.progress + gardenSpeed * timeDelta;
            if (progress < 1.0) {
                ssm.updateActionProgress(entity, action, progress);
                return;
            }

            // update the the timer on the next time this will need to be tended...

            ssm.done(entity, ActionCompleted.ActionCompletedReason.Successful);
        }
    }

    private void plant(EntityReader entity, ServerStateManipulator ssm, Action.Bury action, double timeDelta) {
        Object sync = entity.getSync();
        if (sync == null) return;
        synchronized (sync) {
            if (entity.noLongerExists())
                return;
            Load carrying = entity.getCarrying();
            Integer numberOfSeeds = carrying.quantities.getOrDefault(action.seed, 0);
            if (numberOfSeeds < 1)
                ssm.done(entity, ActionCompleted.ActionCompletedReason.Invalid);

            // check that there is not something already there...
            // check that there is space for it...

            double plantSpeed = entity.getPlantSpeed();
            double progress = action.progress + plantSpeed * timeDelta;
            if (progress < 1.0) {
                ssm.updateActionProgress(entity, action, progress);
                return;
            }
            // update the the timer on the next time this will need to be tended...

            ssm.changePayload(entity.entityId, carrying, action.seed, numberOfSeeds - 1);

            ssm.done(entity, ActionCompleted.ActionCompletedReason.Successful);
        }
    }

    private void create(EntityReader entity, ServerStateManipulator ssm, Action.Create action, double timeDelta) {
        Object sync = entity.getSync();
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

            UnGarrisonLocation unGarrisonLocation = UnGarrisonLocation.getUnGarrisonLocation(state, entity, action.spec.createdType.size);
            if (unGarrisonLocation.isImpossible()) {
                ssm.setCreationProgress(entity.entityId, action, 0.0);
                return;
            }

            EvolutionSpec spec;
            if (action.spec.createdType.containsClass("evolves")) {
                spec = evolution.createEvolvedSpec(contributingUnits);
            } else {
                spec = new EvolutionSpec(action.spec.createdType);
            }
            EntityId createdUnit = generator.generateId();
            ssm.createUnit(createdUnit, action.spec.createdType, spec, new DPoint(unGarrisonLocation.point), entity.getOwner());
            ssm.notifyProductionCompleted(entity.entityId, createdUnit, entity.getOwner());
            if (unGarrisonLocation.path != null)
                ssm.setUnitAction(new EntityReader(state.state, createdUnit), new Action.MoveSeq(unGarrisonLocation.path));
            ssm.done(entity, ActionCompleted.ActionCompletedReason.Successful);
        }
    }

    private void attack(EntityReader attacker, ServerStateManipulator ssm, Action.Attack action, double prevTime, double timeDelta) {
        Object[] syncs = GameStateHelper.getSynchronizationObjects(state.state.entityManager, attacker.entityId, action.target);
        if (syncs == null) {
            ssm.done(attacker, ActionCompleted.ActionCompletedReason.Invalid);
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
                    ssm.done(attacker, ActionCompleted.ActionCompletedReason.Invalid);
                    return;
                }

                DPoint attackerLocation = attacker.getCenterLocation();
                DPoint attackedLocation = attacked.getCenterLocation();
                WeaponSpec weaponSpec = weapon.weaponType;

                if (attackerLocation.distanceTo(attackedLocation) > weapon.weaponType.rangeCanFinishAttackFrom) {
                    ssm.done(attacker, ActionCompleted.ActionCompletedReason.TooFar);
                    return;
                }

                if (weapon.weaponType.weaponClass.equals(WeaponClass.Instant)) {
                    Load load = attacker.getCarrying();
                    if (!load.canAfford(weaponSpec.fireResources)) {
                        ssm.done(attacker, ActionCompleted.ActionCompletedReason.OverCapacity);
                        return;
                    }
                    ssm.subtractFromPayload(attacker, load, weaponSpec.fireResources);
                    if (ssm.receiveDamage(attacked.entityId, weaponSpec.damage * timeDelta * attacker.getCurrentAttackSpeed(), weaponSpec.damageType)) {
                        ssm.done(attacker, ActionCompleted.ActionCompletedReason.Successful);
                        return;
                    }
                    if (ssm.decreaseWeaponCondition(attacker, weapon, weaponSpec.decrementConditionPerUseBy)) {
                        ssm.done(attacker, ActionCompleted.ActionCompletedReason.OverCapacity);
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
                        ssm.done(attacker, ActionCompleted.ActionCompletedReason.OverCapacity);
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
                        ssm.done(attacker, ActionCompleted.ActionCompletedReason.OverCapacity);
                        return;
                    }

                    if (died) {
                        ssm.done(attacker, ActionCompleted.ActionCompletedReason.Successful);
                        return;
                    }
                }
            }
        }
    }

    private void build(EntityReader entity, ServerStateManipulator ssm, Action.Build action, double timeDelta) {
        Object synchronizationObject = entity.getSync();
        if (synchronizationObject == null) {
            ssm.done(entity, ActionCompleted.ActionCompletedReason.Invalid);
            return;
        }
        synchronized (synchronizationObject) {
            EntityReader constructionEntity = new EntityReader(state.state, action.constructionId);

            if (entity.noLongerExists())
                return;

            if (entity.getBuildSpeed() <= 0) {
                // can't build it anyway
                ssm.done(entity, ActionCompleted.ActionCompletedReason.Invalid);
                return;
            }

            ConstructionZone constructionZone = constructionEntity.getConstructionZone();
            if (constructionZone == null || !constructionEntity.getMissingConstructionResources().isEmpty()) {
                ssm.done(entity, ActionCompleted.ActionCompletedReason.Invalid);
                return;
            }

            if (!Proximity.closeEnoughToInteract(entity, constructionEntity)) {
                ssm.done(entity, ActionCompleted.ActionCompletedReason.TooFar);
                return;
            }

            double newConstructionProgress = constructionZone.progress + entity.getBuildSpeed() * timeDelta;
            if (newConstructionProgress < 1) {
                ssm.updateBuildProgress(action.constructionId, constructionZone, newConstructionProgress);
                return;
            }

//            if (!state.state.hasSpaceFor(constructionZone.location,  constructionZone.constructionSpec.size)) {
//                return;
//            }

            ssm.killUnit(action.constructionId);
            EntitySpec resultingStructure = constructionZone.constructionSpec.resultingStructure;
            EntityId buildingId = generator.generateId();
            ssm.createUnit(buildingId, resultingStructure, new EvolutionSpec(resultingStructure), constructionZone.location, Player.GAIA);
            ssm.constructionChanged(entity.getOwner(), action.constructionId, buildingId);

            ssm.done(entity, ActionCompleted.ActionCompletedReason.Successful);
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
                    ssm.done(entity, ActionCompleted.ActionCompletedReason.TooFar);
                    return;
                }
                double nextProgress = action.progress + entity.getDepositSpeed() * timeDelta * 100 / (double) action.resource.weight;
                int amount = (int) nextProgress;
                action.progress = Math.max(0.0, Math.min(1.0, nextProgress - amount));
                ssm.setActionProgress(entity.entityId, action);
                if (!transfer(ssm, entity, new EntityReader(state.state, action.location), action.resource, amount, action.maxAmount)) {
                    ssm.done(entity, ActionCompleted.ActionCompletedReason.OverCapacity); // ?
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
                ssm.done(entity, ActionCompleted.ActionCompletedReason.Invalid);
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
                    ssm.done(entity, ActionCompleted.ActionCompletedReason.TooFar);
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
                    ssm.done(entity, ActionCompleted.ActionCompletedReason.OverCapacity);

                    double amountLeft = collected.getCarrying().getWeight();
                    // TODO: should there be a more natural way of handling this?
                    // die on empty?
                    if (amountLeft <= 0 && collected.getType().containsClass("natural-resource")) {
                        ssm.killUnit(action.resourceCarrier);
                    }
                }
            }
        }
    }

    private static boolean any(OccupancyView view, Set<Point> pnts) {
        for (Point pnt : pnts)
            if (view.isOccupied(pnt.x, pnt.y))
                return true;
        return false;
    }


    private void move(EntityReader entity, ServerStateManipulator ssm, Action.MoveSeq action, double currentTime, double timeDelta) {
        Object synchronizationObject = entity.getSync();
        if (synchronizationObject == null) return;

        synchronized (synchronizationObject) {
            DPoint location = entity.getLocation();
            EntitySpec type = entity.getType();
            if (type == null)
                return;

            if (action.path == null || action.path.points.isEmpty()) {
                ssm.noiselyUpdateUnitLocation(MovableEntity.createStationary(entity, location));
                ssm.done(entity, ActionCompleted.ActionCompletedReason.Successful);
                return;
            }

            DPoint destination;
            boolean onDetour;
            if (action.detour != null) {
                destination = action.detour.points.get(action.detourProgress);
                onDetour = true;
            } else {
                destination = action.path.points.get(action.progress);
                onDetour = false;
            }
            DPoint movementEnd = destination;

            double speed = entity.getMovementSpeed();

            double dx = destination.x - location.x;
            double dy = destination.y - location.y;
            double n = Math.sqrt(dx * dx + dy * dy);
            double distanceToTravel = timeDelta * speed;

            boolean changedDirection = false;
            boolean done = false;
            final DPoint desiredLocation;
            if (n > distanceToTravel) {
                desiredLocation = new DPoint(
                        location.x + distanceToTravel * dx / n,
                        location.y + distanceToTravel * dy / n
                );
            } else {
                desiredLocation = destination;
                changedDirection = true;
                if (onDetour) {
                    if (action.detourProgress < action.detour.points.size() - 1) {
                        movementEnd = action.detour.points.get(++action.detourProgress);
                    } else {
                        action.detour = null;
                        action.detourProgress = 0;
                        Path<? extends Jsonable> path = action.path.destination.findPath(state.pathFinder, entity);
                        if (path == null || path.successful) {
                            action.path.points.clear();
                            action.path.points.addAll(path.points);
                            action.progress = 0;
                        }
                        movementEnd = action.path.points.get(action.progress);
                    }
                } else {
                    if (action.progress < action.path.points.size() - 1) {
                        movementEnd = action.path.points.get(++action.progress);
                    } else {
                        done = true;
                    }
                }
            }

            OccupancyView structures = Occupancy.createStaticOccupancy(state.state, entity.getOwner());
            Set<Point> overlappingTiles = GridLocation.getOverlappingTiles(desiredLocation, type.size);
            if (any(structures, overlappingTiles)) {
                ssm.done(entity, ActionCompleted.ActionCompletedReason.Invalid);
                ssm.noiselyUpdateUnitLocation(MovableEntity.createStationary(entity, location));
                return;
            }

            OccupancyView unitOccupancy = Occupancy.createUnitOccupancy(entity);
            if (!any(unitOccupancy, overlappingTiles)) {
                if (done) {
                    ssm.done(entity, ActionCompleted.ActionCompletedReason.Successful);
                    ssm.noiselyUpdateUnitLocation(MovableEntity.createStationary(entity, destination));
                } else if (changedDirection) {
                    MovableEntity changeDirections = new MovableEntity();
                    changeDirections.entity = entity;
                    changeDirections.size = type.size;
                    changeDirections.currentLocation = desiredLocation;
                    changeDirections.movementBegin = desiredLocation;
                    changeDirections.movementEnd = movementEnd;
                    changeDirections.movementSpeed = speed;
                    changeDirections.movementStartTime = currentTime;
                    ssm.noiselyUpdateUnitLocation(changeDirections);
                } else if (action.blockedCount > 0){
                    MovableEntity changeDirections = new MovableEntity();
                    changeDirections.entity = entity;
                    changeDirections.size = type.size;
                    changeDirections.currentLocation = desiredLocation;
                    changeDirections.movementBegin = desiredLocation;
                    changeDirections.movementEnd = destination;
                    changeDirections.movementSpeed = speed;
                    changeDirections.movementStartTime = currentTime;
                    ssm.noiselyUpdateUnitLocation(changeDirections);
                } else {
                    ssm.quietlyUpdateUnitLocation(entity, desiredLocation);
                }
                action.blockedCount = 0;
                action.detourFailures = 0;
                return;
            }

            if (action.blockedCount == 0) {
                ssm.noiselyUpdateUnitLocation(MovableEntity.createStationary(entity, location));
                action.amountOfTimeToWait = random.nextInt(20 + 10 * action.detourFailures);
            }

            if (action.blockedCount++ < action.amountOfTimeToWait) {
                return;
            }

            action.blockedCount = 0;
            if (action.detourFailures++ > 6) {
                ssm.noiselyUpdateUnitLocation(MovableEntity.createStationary(entity, location));
                ssm.done(entity, ActionCompleted.ActionCompletedReason.Blocked);
                return;
            }


            OccupancyView view = OccupancyView.combine(unitOccupancy, structures);
            DPoint intermediateDestination = action.path.points.get(random.nextInt(action.path.points.size()));
            int r = (int) Math.ceil(Math.max(10, intermediateDestination.distanceTo(location)));
            Bounds bounds = Bounds.fromRadius((int) location.x, (int) location.y, r);

            Path<? extends Jsonable> path;
            if (action.path.destination.isWithin(bounds)) {
                path = action.path.destination.findPath(
                        state.pathFinder,
                        view,
                        entity,
                        bounds
                );
            } else {
                path = state.pathFinder.findPath(
                        view,
                        entity,
                        new SearchDestination(destination),
                        bounds
                );
            }
            if (!path.successful) {
                System.out.println("Unable to find a points");
                return;
            }
            action.detour = path;
            action.detourProgress = 0;
        }
    }

    public void processProjectile(ServerStateManipulator ssm, EntityId entityId, ProjectileLaunch launch, double previousTime, double currentTime) {
        DPoint previousLocation = launch.getLocation(previousTime);
        DPoint currentLocation = launch.getLocation(currentTime);
        if (previousLocation == null || currentLocation == null) {
            ssm.removeProjectile(entityId);
            return;
        }

        // TODO: should be intersects over the whole time period
        NearestEntityQueryResults queryResults = state.state.locationManager.query(new NearestEntityQuery(
                state.state,
                currentLocation,
                entity -> {
                    EntitySpec entitySpec = entity.getType();
                    Player player = entity.getOwner();
                    if (entitySpec == null || player == null) return false;
                    return entitySpec.containsClass("unit") && !player.equals(launch.launchingPlayer); //  should be enemies
                },
                launch.projectile.radius,
                1
        ));

        if (queryResults.successful() && launch.hit(entityId)) {
            ssm.receiveDamage(queryResults.entity.entityId, launch.damage, launch.damageType);

            if (launch.projectile.stopsOnFirst) {
                ssm.removeProjectile(entityId);
            }
        }
    }
}
