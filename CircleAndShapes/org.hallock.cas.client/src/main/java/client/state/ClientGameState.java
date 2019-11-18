package client.state;

import client.ai.ActionRequester;
import client.ai.AiManager;
import client.event.AiEventManager;
import client.event.supply.SupplyAndDemandManager;
import com.sun.xml.internal.ws.handler.ClientMessageHandlerTube;
import common.state.Player;
import common.state.los.AllVisibleLineOfSight;
import common.state.los.LineOfSightSpec;
import common.state.los.SinglePlayerLineOfSight;
import common.state.spec.GameSpec;
import common.state.sst.GameState;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.ReadOptions;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class ClientGameState {
    public Player currentPlayer;
    public GameState gameState;
    public AiManager aiManager;
    public AiEventManager eventManager;
    public ActionRequester actionRequester;
    public SupplyAndDemandManager supplyAndDemandManager;
    public ClientGameMessageHandler messageHandler;


    private static LineOfSightSpec createLineOfSightSpec(GameSpec spec) {
        switch (spec.visibility) {
            case ALL_VISIBLE:
                return new AllVisibleLineOfSight(spec);
            case EXPLORED:
                throw new RuntimeException("uh oh");
            case FOG:
                return new SinglePlayerLineOfSight(spec);
            default:
                throw new RuntimeException("Unhandled visibility");
        }
    }

    public static ClientGameState createClientGameState(GameSpec spec, ActionRequester requester, Player player, ExecutorService service) {
        ClientGameState state = new ClientGameState();
        state.gameState = GameState.createGameState(spec, createLineOfSightSpec(spec));
        state.aiManager = new AiManager(state);
        state.eventManager = new AiEventManager(state, service);
        state.supplyAndDemandManager = new SupplyAndDemandManager(state, spec);
        state.actionRequester = requester;
        state.currentPlayer = player;
        state.messageHandler = new ClientGameMessageHandler(state);
        state.gameState = GameState.createGameState(spec, createLineOfSightSpec(spec));
        return state;
    }

    public void updateAll(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
        gameState.updateAll(reader, spec);
    }
}
