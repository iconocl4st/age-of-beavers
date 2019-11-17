package server.app;

import common.msg.Message;
import server.state.ServerStateManipulator;

import java.io.IOException;

public class ServerMessageHandler {

    private final ServerContext context;

    ServerMessageHandler(ServerContext context) {
        this.context = context;
    }

    boolean handleMessage(ServerConnectionContext c, Message message) {
        switch (message.getMessageType()) {
            case QUIT_CONNECTION: {
                System.out.println("Requested quit.");
                return false;
            }
            case LIST_LOBBIES: {
                System.out.println("Requested list.");
                try {
                    c.writer.send(new Message.LobbyList(context.getLobbyInfos()));
                    c.writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            break;
            case JOIN: {
                Message.Join joinMessage = (Message.Join) message;
                System.out.println("Requested join to " + joinMessage.lobby);

                Lobby lobby = context.getLobby(joinMessage.lobby);
                c.join(lobby);

                try {
                    c.writer.send(new Message.Joined(lobby.getInfo()));
                    c.writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            break;
            case LEAVE: {
                System.out.println("Requested to leave");
                c.leave();

                try {
                    c.writer.send(new Message.Left());
                    c.writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            break;
            case LAUNCH: {
                System.out.println("Requested launch");
                c.launch();
            }
            break;
            case PLACE_BUILDING: {
                Message.PlaceBuilding msg = (Message.PlaceBuilding) message;
                ServerStateManipulator stateManipulator = c.createStateManipulator(c);
                stateManipulator.placeBuilding(msg.spec, msg.location);
            }
            break;
            case REQUEST_ACTION: {
                Message.RequestAction msg = (Message.RequestAction) message;
                ServerStateManipulator stateManipulator = c.createStateManipulator(c);
                stateManipulator.setUnitAction(msg.performer, msg.action);
            }
            break;
            case CHANGE_OCCUPANCY: {
                Message.ChangeOccupancy msg = (Message.ChangeOccupancy) message;
                ServerStateManipulator stateManipulator = c.createStateManipulator(c);
                stateManipulator.setOccupancyState(msg.entity, msg.newState);
            }
            break;
            case SET_GATHER_POINT: {
                Message.SetGatherPoint msg = (Message.SetGatherPoint) message;
                ServerStateManipulator stateManipulator = c.createStateManipulator(c);
                stateManipulator.setGatherPoint(msg.entityId, msg.location);
            }
            break;
            case GARRISON: {
                Message.Garrison msg = (Message.Garrison) message;
                ServerStateManipulator stateManipulator = c.createStateManipulator(c);
                stateManipulator.garrison(msg.entity, msg.within);
            }
            break;
            case UNGARRISON: {
                Message.UnGarrison msg = (Message.UnGarrison) message;
                ServerStateManipulator stateManipulator = c.createStateManipulator(c);
                stateManipulator.ungarrison(msg.entity);
            }
            break;
            case DIE: {
                Message.Die msg = (Message.Die) message;
                ServerStateManipulator stateManipulator = c.createStateManipulator(c);
                stateManipulator.suicide(msg.entityId);
            }
            break;
            case DROP_ALL: {
                Message.DropAll msg = (Message.DropAll) message;
                ServerStateManipulator stateManipulator = c.createStateManipulator(c);
                stateManipulator.dropAll(msg.entityId);
            }
            break;
            case RIDE: {
                Message.Ride msg = (Message.Ride) message;
                ServerStateManipulator stateManipulator = c.createStateManipulator(c);
                stateManipulator.ride(msg.rider, msg.ridden);
            }
            break;
            case STOP_RIDING: {
                Message.StopRiding msg = (Message.StopRiding) message;
                ServerStateManipulator stateManipulator = c.createStateManipulator(c);
                stateManipulator.stopRiding(msg.rider);
            }
            break;
            case SET_EVOLUTION_SELECTION: {
                Message.SetEvolutionSelection msg = (Message.SetEvolutionSelection) message;
                ServerStateManipulator stateManipulator = c.createStateManipulator(c);
                stateManipulator.setEvolutionPreferences(msg.entity, msg.weights);
            }
            break;
            case SET_DESIRED_CAPACITY: {
                Message.SetDesiredCapacity msg = (Message.SetDesiredCapacity) message;
                ServerStateManipulator stateManipulator = c.createStateManipulator(c);
                stateManipulator.setDesiredCapacity(msg.entity, msg.resourceType, msg.priority, msg.desiredMinimum, msg.desiredMaximum);
            }
            break;
            default:
                System.out.println("Server: Ignoring unknown message type " + message.getMessageType());
        }
        return true;
    }
}
