package app;

import app.ui.DebugPanel;
import client.ai.ActionRequester;
import client.state.ClientGameState;
import common.app.PlayerAiInterface;
import common.msg.Message;
import common.msg.NoExceptionsConnectionWriter;
import common.util.ExecutorServiceWrapper;

import java.util.Random;

public class PlayerAiContext implements PlayerAiInterface {
    final Random random = new Random();

    private final ExecutorServiceWrapper executorService;

    ClientGameState clientGameState;
    AiUtitlities utils;

    private PlayerAiImplementation ai;
    private QueueConnectionWriter  msgQueue = new QueueConnectionWriter();


    private DebugPanel debugPanel = DebugPanel.showDebugFrame();

    // TODO: should be some way to not need this...
    private ClientGameState.GameCreationContext creationContext;

    public PlayerAiContext(ExecutorServiceWrapper executorService) {
        this.executorService = executorService;
    }

    @Override
    public void handleMessage(common.msg.Message message) {
        switch (message.getMessageType()) {
            case LAUNCHED: {
                Message.Launched launchMsg = (Message.Launched) message;
                creationContext = new ClientGameState.GameCreationContext();
                creationContext.parseLaunchedMessage(launchMsg);
            }
            break;
            case UPDATE_ENTIRE_GAME: {
                if (creationContext == null)
                    throw new RuntimeException("Messages in the wrong order.");
                Message.UpdateEntireGameState gameStateMsg = (Message.UpdateEntireGameState) message;
                creationContext.gameState = gameStateMsg.gameState;
                creationContext.service = executorService;
                creationContext.requester = new ActionRequester(msgQueue);
                clientGameState = ClientGameState.createClientGameState(creationContext);
                utils = new AiUtitlities(this);
                ai = new PlayerAiImplementation(this);
                creationContext = null;
                break;
            }
            default:
                if (clientGameState == null) {
                    throw new RuntimeException("why");
                }
                clientGameState.messageHandler.handleMessage(message);
        }
    }

    @Override
    public void writeActions(NoExceptionsConnectionWriter requester) {
        debugPanel.show(clientGameState, ai.updateActions(clientGameState.actionRequester));
        msgQueue.exportActions();
        msgQueue.writeTo(requester);
        ai.setDebugGraphics();
    }
}
