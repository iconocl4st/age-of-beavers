package client.ai.ai2;

import client.ai.ActionRequester;
import client.state.ClientGameState;
import common.state.EntityReader;
import common.state.sst.GameState;

public class AiContext {
    public ClientGameState clientGameState;
    public GameState gameState;
    public ActionRequester requester;
    public AiLocator locator;
    public EntityReader controlling;
    public AiStack stack;


    public AiContext clone() {
        AiContext context = new AiContext();
        context.clientGameState = clientGameState;
        context.gameState = gameState;
        context.requester = requester;
        context.locator = locator;
        context.controlling = controlling;
        context.stack = stack;
        return context;
    }

    public AiContext controlling(EntityReader entity) {
        AiContext context = clone();
        context.controlling = entity;
        return context;
    }

    public AiContext stack(AiStack aiStack) {
        AiContext context = clone();
        context.stack = aiStack;
        return context;
    }
}
