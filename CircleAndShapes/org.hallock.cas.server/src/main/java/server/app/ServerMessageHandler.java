package server.app;

import common.msg.Message;

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
                    c.getWriter().send(new Message.LobbyList(context.getLobbyInfos()));
                    c.getWriter().flush();
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
                    c.getWriter().send(new Message.Joined(lobby.getInfo()));
                    c.getWriter().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            break;
            case LEAVE: {
                System.out.println("Requested to leave");
                c.leave();

                try {
                    c.getWriter().send(new Message.Left());
                    c.getWriter().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            break;
            case SPECTATE: {
                System.out.println("Requested spectate");
                boolean spectate = ((Message.Spectate) message).spectate;
                c.spectate(spectate);

                try {
                    c.getWriter().send(new Message.IsSpectating(spectate));
                    c.getWriter().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            break;
            case LAUNCH: {
                System.out.println("Requested launch");
                try {
                    c.launch();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            break;
            case PLACE_BUILDING:
            case REQUEST_ACTION:
            case CHANGE_OCCUPANCY:
            case SET_GATHER_POINT:
            case GARRISON:
            case UNGARRISON:
            case DIE:
            case DROP_ALL:
            case RIDE:
            case STOP_RIDING:
            case SET_EVOLUTION_SELECTION:
            case SET_DESIRED_CAPACITY:
            case REQUEST_LISTEN_FOR_RANGE:
                return c.getMessageHandler().send(message);
            default:
                System.out.println("Server: Ignoring unknown message type " + message.getMessageType());
        }
        return true;
    }
}
