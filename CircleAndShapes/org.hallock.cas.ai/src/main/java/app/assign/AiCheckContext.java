package app.assign;

import app.AiUtitlities;
import app.DropoffManager;
import app.Goals;
import app.TickProcessingState;
import client.ai.ActionRequester;
import client.ai.ai2.AiManager;
import client.state.ClientGameState;
import common.state.spec.GameSpec;

public class AiCheckContext {
    public Assignments assignments;
    public TickProcessingState tickState;
    public AiUtitlities utils;
    public Goals goals;
    public DropoffManager dropoffManager;
    public ClientGameState clientGameState;


    public GameSpec gameSpec() { return clientGameState.gameState.gameSpec; }
    public ActionRequester requester() { return clientGameState.actionRequester; }
    public AiManager aiManager() { return clientGameState.aiManager; }
    public double currentTime() { return clientGameState.gameState.currentTime; }
}
