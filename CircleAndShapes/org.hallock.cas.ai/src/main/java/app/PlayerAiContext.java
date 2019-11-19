package app;

import client.ai.ActionRequester;
import client.state.ClientGameState;
import common.app.PlayerAiInterface;
import common.msg.Message;
import common.msg.NoExceptionsConnectionWriter;
import common.state.Player;
import common.util.ExecutorServiceWrapper;

public class PlayerAiContext implements PlayerAiInterface {

    private final ExecutorServiceWrapper executorService;

    ClientGameState clientGameState;

    QueueConnectionWriter msgQueue = new QueueConnectionWriter(); // one odd call


    private ActionRequester actionRequester = new ActionRequester(msgQueue);
    private AiUtitlities utils;
    private PlayerAiImplementation ai;

    // TODO: should be some way to not need this...
    private Player currentPlayer;

    public PlayerAiContext(ExecutorServiceWrapper executorService) {
        this.executorService = executorService;
    }

    @Override
    public void handleMessage(common.msg.Message message) {
        switch (message.getMessageType()) {
            case LAUNCHED: {
                Message.Launched launchMsg = (Message.Launched) message;
                currentPlayer = launchMsg.player;
            }
            break;
            case UPDATE_ENTIRE_GAME: {
                if (currentPlayer == null)
                    throw new RuntimeException("Messages in the wrong order.");
                Message.UpdateEntireGameState gameStateMsg = (Message.UpdateEntireGameState) message;
                clientGameState = ClientGameState.createClientGameState(
                        gameStateMsg.gameState,
                        actionRequester,
                        currentPlayer,
                        executorService
                );
                utils = new AiUtitlities(clientGameState.gameState);
                ai = new PlayerAiImplementation(this);
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
        ai.updateActions(actionRequester);
        msgQueue.exportActions();
        msgQueue.writeTo(requester);
    }
}
