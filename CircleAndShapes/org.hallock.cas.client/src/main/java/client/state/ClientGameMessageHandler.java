package client.state;

import common.algo.quad.QuadTreeOccupancyState;
import common.event.*;
import common.msg.Message;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.sst.sub.ConstructionZone;
import common.state.sst.sub.MovableEntity;

public class ClientGameMessageHandler {

    private final ClientGameState context;

    public ClientGameMessageHandler(ClientGameState gameState) {
        this.context = gameState;
    }


    public boolean handleMessage(Message message) {
        switch (message.getMessageType()) {

            case GAME_OVER: {
                // quit using resources
            }
            break;
            case TIME_CHANGE: {
                Message.TimeChange msg = (Message.TimeChange) message;

                context.gameState.updateTime(msg.currentGameTime, msg.timeOfGameTime);
            }
            break;
            case PROJECTILE_LAUNCHED: {
                Message.ProjectileLaunched msg = (Message.ProjectileLaunched) message;
                context.gameState.projectileManager.set(msg.entityId, msg.launch);
            }
            break;
            case PROJECTILE_LANDED: {
                Message.ProjectileLanded msg = (Message.ProjectileLanded) message;
                context.gameState.projectileManager.remove(msg.entityId);
            }
            break;
            case DIRECTION_CHANGED: {
                Message.DirectedLocationChange msg = (Message.DirectedLocationChange) message;
                context.gameState.locationManager.setLocation(msg.getDirectedLocation(context.gameState));
                context.eventManager.notifyListeners(new UnitChangedDirection(msg));
            }
            break;
            case UNIT_UPDATED: {
                Message.UnitUpdated msg = (Message.UnitUpdated) message;
                if (msg.unitId == null)
                    break;
                EntityReader entity = new EntityReader(context.gameState, msg.unitId);
                Object sync = entity.getSync();
                if (sync == null) {
                    sync = new Object();
                    context.gameState.entityManager.set(msg.unitId, sync);
                }
                synchronized (sync) {
                    if (entity.noLongerExists())
                        break;
                    if (msg.isNowOfType != null)
                        context.gameState.typeManager.set(msg.unitId, msg.isNowOfType);
                    if (msg.graphics != null)
                        context.gameState.graphicsManager.set(msg.unitId, msg.graphics);
                    if (msg.location != null)
                        context.gameState.locationManager.setLocation(MovableEntity.createStationary(entity, msg.location));
                    if (msg.action != null)
                        context.gameState.actionManager.set(msg.unitId, msg.action);
                    if (msg.load != null) {
                        context.gameState.carryingManager.set(msg.unitId, msg.load);
                        context.eventManager.notifyListeners(new ResourcesChanged(msg.unitId));
                    }
                    if (msg.growthInfo != null)
                        context.gameState.crops.set(msg.unitId, msg.growthInfo);
                    if (msg.gardenSpeed != null)
                        context.gameState.gardenSpeed.set(msg.unitId, msg.gardenSpeed);
                    if (msg.plantSpeed != null)
                        context.gameState.burySpeed.set(msg.unitId, msg.plantSpeed);
                    if (msg.health != null)
                        context.gameState.healthManager.set(msg.unitId, msg.health);
                    if (msg.owner != null)
                        context.gameState.playerManager.set(msg.unitId, msg.owner);
                    if (msg.rides != null)
                        context.gameState.ridingManager.set(msg.unitId, msg.rides);
                    if (msg.isWithin != null) {
                        if (msg.isWithin.equals(EntityId.NONE)) {
                            EntityId currentHolder = context.gameState.garrisonManager.get(msg.unitId);
                            context.gameState.garrisonManager.remove(msg.unitId);
                            if (currentHolder != null)
                                context.eventManager.notifyListeners(new GarrisonedChanged(currentHolder));
                        } else {
                            context.gameState.garrisonManager.set(msg.unitId, msg.isWithin);
                            context.eventManager.notifyListeners(new GarrisonedChanged(msg.isWithin));
                        }
                    }
                    if (msg.buildSpeed != null)
                        context.gameState.buildSpeedManager.set(msg.unitId, msg.buildSpeed);
                    if (msg.newMovementSpeed != null)
                        context.gameState.movementSpeedManager.set(msg.unitId, msg.newMovementSpeed);
                    if (msg.isHidden != null)
                        context.gameState.hiddenManager.set(msg.unitId, msg.isHidden);
                    if (msg.occupancy != null)
                        context.gameState.gateStateManager.set(msg.unitId, msg.occupancy);
                    if (msg.gatherPoint != null)
                        context.gameState.gatherPointManager.set(msg.unitId, msg.gatherPoint);
                    if (msg.constructionZone != null)
                        context.gameState.constructionManager.set(msg.unitId, new ConstructionZone(msg.constructionZone.constructionSpec, msg.constructionZone.location));
                    if (msg.buildProgress != null)
                        context.gameState.constructionManager.get(msg.unitId).progress = msg.buildProgress;
                    if (msg.creationTime != null)
                        context.gameState.ageManager.set(msg.unitId, msg.creationTime);
                    if (msg.weapons != null)
                        context.gameState.weaponsManager.set(msg.unitId, msg.weapons);
                    if (msg.attackSpeed != null)
                        context.gameState.attackSpeedManager.set(msg.unitId, msg.attackSpeed);
                    if (msg.rotationSpeed != null)
                        context.gameState.rotationSpeedManager.set(msg.unitId, msg.rotationSpeed);
                    if (msg.orientation != null)
                        context.gameState.orientationManager.set(msg.unitId, msg.orientation);
                    if (msg.capacity != null)
                        context.gameState.capacityManager.set(msg.unitId, msg.capacity);
                    if (msg.collectSpeed != null)
                        context.gameState.collectSpeedManager.set(msg.unitId, msg.collectSpeed);
                    if (msg.depositSpeed != null)
                        context.gameState.depositSpeedManager.set(msg.unitId, msg.depositSpeed);
                    if (msg.evolutionWeights != null)
                        context.gameState.evolutionManager.set(msg.unitId, msg.evolutionWeights);
                    if (msg.baseHealth != null)
                        context.gameState.baseHealthManager.set(msg.unitId, msg.baseHealth);
                    if (msg.capacity != null || msg.load != null || msg.owner != null) {
                        context.supplyAndDemandManager.update(new EntityReader(context.gameState, msg.unitId));
                    }
                }
            }
            break;
            case UNIT_REMOVED: {
                EntityId unitId = ((Message.UnitRemoved) message).unitId;
                EntityReader entity = new EntityReader(context.gameState, unitId);
                context.supplyAndDemandManager.remove(entity);
                context.eventManager.notifyListeners(new UniRemovedEvent(unitId), () -> context.gameState.removeEntity(unitId));
            }
            break;
            case UNIT_CREATED: {
                EntityId unitId = ((Message.UnitCreated) message).unitId;
                context.eventManager.notifyListeners(new UnitCreatedEvent(unitId));
            }
            break;
            case AI_EVENT: {
                Message.AiEventMessage msg = (Message.AiEventMessage) message;
                context.eventManager.notifyListeners(msg.event);
            }
            break;
            case OCCUPANCY_UPDATED: {
                Message.OccupancyChanged msg = (Message.OccupancyChanged) message;
                context.gameState.staticOccupancy.set(msg.location, msg.size, msg.occupied);
                context.gameState.buildingOccupancy.set(msg.location, msg.size, msg.construction);
                context.quadTree.setType(msg.location, msg.size, msg.occupied ? QuadTreeOccupancyState.Occupied : QuadTreeOccupancyState.Empty);
            }
            break;
            default:
                throw new RuntimeException("Unknown message type: " + message.getMessageType());
        }
        return true;
    }
}
