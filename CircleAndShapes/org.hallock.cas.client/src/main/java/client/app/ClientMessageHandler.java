package client.app;

import common.AiEvent;
import common.msg.Message;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.los.AllVisibleLineOfSight;
import common.state.los.LineOfSightSpec;
import common.state.los.SinglePlayerLineOfSight;
import common.state.sst.GameState;
import common.state.sst.sub.ConstructionZone;
import common.util.json.EmptyJsonable;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.ReadOptions;

import java.io.IOException;

class ClientMessageHandler {

    private final ClientContext context;

    public ClientMessageHandler(ClientContext context) {
        this.context = context;
    }

    boolean handleMessage(Message message) {
        switch (message.getMessageType()) {
            case QUIT_CONNECTION: {
                context.uiManager.log("Received confirm quit.");
                return false;
            }
            case LOBBY_LIST: {
                context.uiManager.log("Received lobbies.");
                Message.LobbyList list = (Message.LobbyList) message;
                context.uiManager.lobbyBrowser.setLobbies(list.infos);
            }
            break;
            case LEFT: {
                context.uiManager.log("Left lobby");
                context.uiManager.lobbyBrowser.setCurrentLobby(null);
            }
            break;
            case JOINED: {
                context.uiManager.log("Joined lobby");
                context.uiManager.lobbyBrowser.setCurrentLobby(((Message.Joined) message).lobby);
            }
            break;
            case LAUNCHED: {
                context.uiManager.log("Game launched");
                Message.Launched launched = (Message.Launched) message;
                context.currentPlayer = launched.player;
                LineOfSightSpec spec = null;
                switch (launched.spec.visibility) {
                    case ALL_VISIBLE:
                        spec = new AllVisibleLineOfSight(launched.spec);
                        break;
                    case EXPLORED:
                        throw new RuntimeException("uh oh");
                    case FOG:
                        spec = new SinglePlayerLineOfSight(launched.spec);
                        break;
                }
                context.gameState = GameState.createGameState(launched.spec, spec);
                context.uiManager.displayGame(launched.spec);
            }
            break;
            case GAME_OVER: {
                context.uiManager.log("Game over");
                context.executorService.submit(() -> {
                    try {
                        context.uiManager.log("Sending quit");
                        context.writer.send(new Message.Quit());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            break;
            case UPDATE_ENTIRE_GAME: {
                context.uiManager.log("Update everything");
                context.updateGameState(((Message.UpdateEntireGameState) message).gameState);
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
                        context.eventManager.notifyListeners(new AiEvent.GarrisonedChanged(msg.unitId));
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
                }

                context.uiManager.unitActions.drawCurrentButtons(false);
            }
            break;
            case UNIT_REMOVED: {
                context.uiManager.log("Unit deleted");
                context.gameState.removeEntity(((Message.UnitRemoved) message).unitId);
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
                context.uiManager.log("Client: Ignoring unknown message type " + message.getMessageType());
                break;
        }
        return true;
    }

    public boolean handleNextMessage(JsonReaderWrapperSpec reader) throws IOException {
        reader.readBeginDocument();
        Message.MessageType msgType = reader.b(Message.MessageType.values(), reader.readInt32("type"));
        ReadOptions spec = new ReadOptions();
        if (context.gameState != null) {
            spec.spec = context.gameState.gameSpec;
        }

        boolean ret = true;
        switch (msgType) {
//            case ERROR: msg = Message.Error.finishParsing(reader, spec); break;
//            case INFORM: msg = Message.Inform.finishParsing(reader, spec); break;
            case AI_EVENT: ret = handleMessage(Message.AiEventMessage.finishParsing(reader, spec)); break;
            case OCCUPANCY_UPDATED: ret = handleMessage(Message.OccupancyChanged.finishParsing(reader, spec)); break;
            case QUIT_CONNECTION: ret = handleMessage(Message.Quit.finishParsing(reader, spec)); break;
            case UNIT_REMOVED: ret = handleMessage(Message.UnitRemoved.finishParsing(reader, spec)); break;
            case UNIT_UPDATED: ret = handleMessage(Message.UnitUpdated.finishParsing(reader, spec)); break;
            case GAME_OVER: ret = handleMessage(Message.GameOver.finishParsing(reader, spec)); break;
            case LOBBY_LIST: ret = handleMessage(Message.LobbyList.finishParsing(reader, spec)); break;
            case LEFT: ret = handleMessage(Message.Left.finishParsing(reader, spec)); break;
            case JOINED: ret = handleMessage(Message.Joined.finishParsing(reader, spec)); break;
            case LAUNCHED: ret = handleMessage(Message.Launched.finishParsing(reader, spec)); break;
            case TIME_CHANGE: ret = handleMessage(Message.TimeChange.finishParsing(reader, spec)); break;
            case PROJECTILE_LAUNCHED: ret = handleMessage(Message.ProjectileLaunched.finishParsing(reader, spec)); break;
            case PROJECTILE_LANDED: ret = handleMessage(Message.ProjectileLanded.finishParsing(reader, spec)); break;
            case UPDATE_ENTIRE_GAME: {
                if (context.gameState != null) {
                    reader.readName("state");
                    context.gameState.updateAll(reader, spec);
                } else
                    throw new RuntimeException("Received state message without a game state.");
            } break;
            default:
                System.out.println("Client: Ignoring unknown message type: " + msgType);
                reader.finishCurrentObject();
        }
        reader.readEndDocument();
        return ret;
    }
}
