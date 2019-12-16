package client.app;

import client.ai.ActionRequester;
import client.ai.ResponsiveConnectionWriter;
import client.state.ClientGameState;
import common.msg.Message;
import common.state.sst.GameState;
import common.util.ExecutorServiceWrapper;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.ReadOptions;

import java.io.IOException;

class ClientMessageHandler {

    private final UiClientContext context;

    public ClientMessageHandler(UiClientContext context) {
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
            case SPECTATING: {
                context.uiManager.log("Spectating");
                context.uiManager.lobbyBrowser.setCurrentlySpectating(((Message.IsSpectating) message).isSpectating);
            }
            break;
            case GAME_OVER: {
                // quit using resources
            }
            break;
            case TIME_CHANGE:
            case PROJECTILE_LAUNCHED:
            case PROJECTILE_LANDED:
            case UNIT_UPDATED:
            case UNIT_REMOVED:
            case AI_EVENT:
            case OCCUPANCY_UPDATED:
            case DIRECTION_CHANGED:
            case UNIT_CREATED:
                return context.clientGameState.messageHandler.handleMessage(message);
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
        if (context.clientGameState != null) spec.state = context.clientGameState.gameState;

        boolean ret = true;
        switch (msgType) {
//            case ERROR: msg = Message.Error.finishParsing(reader, spec); break;
//            case INFORM: msg = Message.Inform.finishParsing(reader, spec); break;
            case AI_EVENT: ret = handleMessage(Message.AiEventMessage.finishParsing(reader, spec)); break;
            case OCCUPANCY_UPDATED: ret = handleMessage(Message.OccupancyChanged.finishParsing(reader, spec)); break;
            case QUIT_CONNECTION: ret = handleMessage(Message.Quit.finishParsing(reader, spec)); break;
            case UNIT_REMOVED: ret = handleMessage(Message.UnitRemoved.finishParsing(reader, spec)); break;
            case UNIT_CREATED: ret = handleMessage(Message.UnitCreated.finishParsing(reader, spec)); break;
            case UNIT_UPDATED: ret = handleMessage(Message.UnitUpdated.finishParsing(reader, spec)); break;
            case GAME_OVER: ret = handleMessage(Message.GameOver.finishParsing(reader, spec)); break;
            case LOBBY_LIST: ret = handleMessage(Message.LobbyList.finishParsing(reader, spec)); break;
            case LEFT: ret = handleMessage(Message.Left.finishParsing(reader, spec)); break;
            case JOINED: ret = handleMessage(Message.Joined.finishParsing(reader, spec)); break;
            case TIME_CHANGE: ret = handleMessage(Message.TimeChange.finishParsing(reader, spec)); break;
            case PROJECTILE_LAUNCHED: ret = handleMessage(Message.ProjectileLaunched.finishParsing(reader, spec)); break;
            case PROJECTILE_LANDED: ret = handleMessage(Message.ProjectileLanded.finishParsing(reader, spec)); break;
            case SPECTATING: ret = handleMessage(Message.IsSpectating.finishParsing(reader, spec)); break;
            case DIRECTION_CHANGED: ret = handleMessage(Message.DirectedLocationChange.finishParsing(reader, spec)); break;
            case LAUNCHED: {
                context.clientGameState = ClientGameState.createClientGameState(
                        new ActionRequester(new ResponsiveConnectionWriter(context.writer, context.executorService)),
                        context.executorService,
                        reader,
                        spec
                );
                context.uiManager.displayGame(
                        context.clientGameState.gameState.gameSpec,
                        context.clientGameState.currentPlayer,
                        context.clientGameState.startingLocation
                );
            } break;
            default:
                System.out.println("Client parser: Ignoring unknown message type: " + msgType);
                reader.finishCurrentObject();
        }
        reader.readEndDocument();
        return ret;
    }
}
