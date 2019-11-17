package server.app;

import common.msg.Message;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.ReadOptions;

import java.io.IOException;

public class ServerMessageReader {
    ServerConnectionContext context;

    public ServerMessageReader(ServerConnectionContext context) {
        this.context = context;
    }

    public Message readMessage(JsonReaderWrapperSpec reader) throws IOException {
        reader.readBeginDocument();
        Message.MessageType msgType = reader.b(Message.MessageType.values(), reader.readInt32("type"));
        ReadOptions readOptions = new ReadOptions();
        readOptions.spec = context.getCurrentGameSpec();
        Message msg = null;
        switch (msgType) {
            case QUIT_CONNECTION: msg = Message.Quit.finishParsing(reader, readOptions); break;
            case LIST_LOBBIES: msg = Message.ListLobbies.finishParsing(reader, readOptions); break;
            case JOIN: msg = Message.Join.finishParsing(reader, readOptions); break;
            case LEAVE: msg = Message.Leave.finishParsing(reader, readOptions); break;
            case LAUNCH: msg = Message.Launch.finishParsing(reader, readOptions); break;
            case PLACE_BUILDING: msg = Message.PlaceBuilding.finishParsing(reader, readOptions); break;
            case REQUEST_ACTION: msg = Message.RequestAction.finishParsing(reader, readOptions); break;
            case CHANGE_OCCUPANCY: msg = Message.ChangeOccupancy.finishParsing(reader, readOptions); break;
            case SET_GATHER_POINT: msg = Message.SetGatherPoint.finishParsing(reader, readOptions); break;
            case GARRISON: msg = Message.Garrison.finishParsing(reader, readOptions); break;
            case UNGARRISON: msg = Message.UnGarrison.finishParsing(reader, readOptions); break;
            case DIE: msg = Message.Die.finishParsing(reader, readOptions); break;
            case DROP_ALL: msg = Message.DropAll.finishParsing(reader, readOptions); break;
            case RIDE: msg = Message.Ride.finishParsing(reader, readOptions); break;
            case STOP_RIDING: msg = Message.StopRiding.finishParsing(reader, readOptions); break;
            default:
                System.out.println("Ignoring unknown message type: " + msgType);
                reader.finishCurrentObject();
        }
        reader.readEndDocument();
        return msg;
    }
}
