package server.app;

import common.msg.ConnectionWriter;
import common.msg.Message;
import common.msg.NoExceptionsConnectionWriter;
import server.state.ServerGameState;
import server.state.ServerStateManipulator;

public class GamePlayerMessageHandler implements NoExceptionsConnectionWriter {

    private final ServerGameState serverGameState;
    private final ServerStateManipulator stateManipulator;
    private final ConnectionWriter writer;

    public GamePlayerMessageHandler(ConnectionWriter writer, ServerGameState serverGameState, ServerStateManipulator manipulator) {
        this.serverGameState = serverGameState;
        this.stateManipulator = manipulator;
        this.writer = writer;
    }

    // TODO: These should be handled in the game tick cycle, like ai actions...
    public boolean send(Message message) {
        switch (message.getMessageType()) {
            case PLACE_BUILDING: {
                Message.PlaceBuilding msg = (Message.PlaceBuilding) message;
                stateManipulator.placeBuilding(msg.spec, msg.location);
            }
            break;
            case REQUEST_ACTION: {
                Message.RequestAction msg = (Message.RequestAction) message;
                stateManipulator.setUnitAction(msg.performer, msg.action);
            }
            break;
            case CHANGE_OCCUPANCY: {
                Message.ChangeOccupancy msg = (Message.ChangeOccupancy) message;
                stateManipulator.setOccupancyState(msg.entity, msg.newState);
            }
            break;
            case SET_GATHER_POINT: {
                Message.SetGatherPoint msg = (Message.SetGatherPoint) message;
                stateManipulator.setGatherPoint(msg.entityId, msg.location);
            }
            break;
            case GARRISON: {
                Message.Garrison msg = (Message.Garrison) message;
                stateManipulator.garrison(msg.entity, msg.within);
            }
            break;
            case UNGARRISON: {
                Message.UnGarrison msg = (Message.UnGarrison) message;
                stateManipulator.ungarrison(msg.entity);
            }
            break;
            case DIE: {
                Message.Die msg = (Message.Die) message;
                stateManipulator.suicide(msg.entityId);
            }
            break;
            case DROP_ALL: {
                Message.DropAll msg = (Message.DropAll) message;
                stateManipulator.dropAll(msg.entityId);
            }
            break;
            case RIDE: {
                Message.Ride msg = (Message.Ride) message;
                stateManipulator.ride(msg.rider, msg.ridden);
            }
            break;
            case STOP_RIDING: {
                Message.StopRiding msg = (Message.StopRiding) message;
                stateManipulator.stopRiding(msg.rider);
            }
            break;
            case SET_EVOLUTION_SELECTION: {
                Message.SetEvolutionSelection msg = (Message.SetEvolutionSelection) message;
                stateManipulator.setEvolutionPreferences(msg.entity, msg.weights);
            }
            break;
            case SET_DESIRED_CAPACITY: {
                Message.SetDesiredCapacity msg = (Message.SetDesiredCapacity) message;
                stateManipulator.setDesiredCapacity(msg.entity, msg.resourceType, msg.priority, msg.desiredMinimum, msg.desiredMaximum);
            }
            break;
            case REQUEST_LISTEN_FOR_RANGE: {
                Message.ListenForTargetInRange msg = (Message.ListenForTargetInRange) message;
                if (msg.listen)
                    serverGameState.rangeEventManager.listenForRangeEventsFrom(writer, msg.targetWithinRange.entity, msg.targetWithinRange.target, msg.targetWithinRange.range);
                else
                    serverGameState.rangeEventManager.stopListeningToRangeEvents(writer, msg.targetWithinRange.entity);
            }
            break;
            default:
                System.out.println("Server: Ignoring unknown message type " + message.getMessageType());
        }
        return true;
    }
}
