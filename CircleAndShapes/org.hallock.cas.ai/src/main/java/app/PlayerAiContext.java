package app;

import client.ai.ActionRequester;
import client.state.ClientGameState;
import common.app.PlayerAiInterface;
import common.msg.Message;
import common.msg.NoExceptionsConnectionWriter;
import common.state.Player;
import common.util.ExecutorServiceWrapper;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PlayerAiContext implements PlayerAiInterface {
    final Random random = new Random();

    private final ExecutorServiceWrapper executorService;

    ClientGameState clientGameState;
    AiUtitlities utils;

    QueueConnectionWriter msgQueue = new QueueConnectionWriter(); // one odd call


    private ActionRequester actionRequester = new ActionRequester(msgQueue);
    private PlayerAiImplementation ai;

    // TODO: should be some way to not need this...
    private Player currentPlayer;
    Point startingLocation;

    public PlayerAiContext(ExecutorServiceWrapper executorService) {
        this.executorService = executorService;
    }

    @Override
    public void handleMessage(common.msg.Message message) {
        switch (message.getMessageType()) {
            case LAUNCHED: {
                Message.Launched launchMsg = (Message.Launched) message;
                currentPlayer = launchMsg.player;
                startingLocation = launchMsg.playerStart;
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
                        startingLocation,
                        executorService
                );
                utils = new AiUtitlities(this);
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
        ai.setDebugGraphics();
    }
}
