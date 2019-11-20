package client.state;

import common.AiEvent;
import common.msg.Message;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.sst.sub.ConstructionZone;
import common.util.json.EmptyJsonable;

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
                context.gameState.currentTime = msg.currentTime;
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
            case UNIT_UPDATED: {
                Message.UnitUpdated msg = (Message.UnitUpdated) message;
                if (msg.unitId == null)
                    break;
                EmptyJsonable sync = context.gameState.entityManager.get(msg.unitId);
                if (sync == null) {
                    sync = new EmptyJsonable();
                    context.gameState.entityManager.set(msg.unitId, sync);
                }
                synchronized (sync) {
                    if (context.gameState.entityManager.get(msg.unitId) == null)
                        break;
                    if (msg.isNowOfType != null)
                        context.gameState.typeManager.set(msg.unitId, msg.isNowOfType);
                    if (msg.location != null) {
                        context.gameState.locationManager.setLocation(new EntityReader(context.gameState, msg.unitId), msg.location);
                        context.eventManager.entityMoved(msg.unitId);
                    }
                    if (msg.action != null)
                        context.gameState.actionManager.set(msg.unitId, msg.action);
                    if (msg.load != null) {
                        context.gameState.carryingManager.set(msg.unitId, msg.load);
                        context.eventManager.notifyListeners(new AiEvent.ResourcesChanged(msg.unitId));
                    }
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
                                context.eventManager.notifyListeners(new AiEvent.GarrisonedChanged(currentHolder));
                        } else {
                            context.gameState.garrisonManager.set(msg.unitId, msg.isWithin);
                            context.eventManager.notifyListeners(new AiEvent.GarrisonedChanged(msg.isWithin));
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
                    if (msg.losDistance != null) {
                        try {
                            context.gameState.lineOfSight.updateLineOfSight(null, msg.losOldLocation, msg.losNewLocation, msg.losDistance);
                        } catch (Throwable t) {
                            System.out.println("There was an error trying to update the line of sight. " + msg.debug);
                            System.exit(0);
                        }
                    }
                    if (msg.capacity != null || msg.load != null || msg.owner != null) {
                        context.supplyAndDemandManager.update(new EntityReader(context.gameState, msg.unitId), true);
                    }
                }
            }
            break;
            case UNIT_REMOVED: {
                EntityId unitId = ((Message.UnitRemoved) message).unitId;
                context.gameState.removeEntity(unitId);
                context.supplyAndDemandManager.remove(new EntityReader(context.gameState, unitId));
            }
            break;
            case AI_EVENT: {
                Message.AiEventMessage msg = (Message.AiEventMessage) message;
                context.eventManager.notifyListeners(msg.event);
            }
            break;
            case OCCUPANCY_UPDATED: {
                Message.OccupancyChanged msg = (Message.OccupancyChanged) message;
                context.gameState.occupancyState.setOccupancy(msg.location, msg.size, msg.occupied);
            }
            break;
            default:
                throw new RuntimeException("Unkown message type: " + message.getMessageType());
        }
        return true;
    }
}
