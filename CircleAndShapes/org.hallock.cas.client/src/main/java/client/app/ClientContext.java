package client.app;

import client.ai.AiManager;
import client.event.AiEventManager;
import client.gui.UiManager;
import client.state.ActionQueuer;
import client.state.ImageCache;
import client.state.RangeManager;
import client.state.SelectionManager;
import common.msg.ConnectionReader;
import common.msg.ConnectionWriter;
import common.state.Player;
import common.state.sst.GameState;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientContext {
    public final ExecutorService executorService = Executors.newCachedThreadPool();
    public final ImageCache imageCache = new ImageCache();

    /* Connections */
    public ConnectionWriter writer;
    final ClientMessageHandler messageHandler = new ClientMessageHandler(this);


    public Player currentPlayer;
    public GameState gameState;
    public final AiManager aiManager = new AiManager(this);
    public final AiEventManager eventManager = new AiEventManager(this);
    public final SelectionManager selectionManager = new SelectionManager(this);
    public final ActionQueuer actionQueuer = new ActionQueuer(this);


    public final UiManager uiManager = UiManager.createUiManager(this);

    { uiManager.gameScreen.contextKeyListener.addContextKeyListener(actionQueuer); }

    public void updateGameState(GameState newGameState) {
        gameState.updateAll(newGameState);
    }
}
