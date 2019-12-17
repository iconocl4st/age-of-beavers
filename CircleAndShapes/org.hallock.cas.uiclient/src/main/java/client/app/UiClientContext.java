package client.app;

import client.gui.UiManager;
import client.state.ActionQueuer;
import client.state.ClientGameState;
import client.state.ImageCache;
import client.state.SelectionManager;
import common.msg.ConnectionWriter;
import common.util.ExecutorServiceWrapper;

import java.util.concurrent.Executors;

public class UiClientContext {
    public final ExecutorServiceWrapper executorService = new ExecutorServiceWrapper(Executors.newFixedThreadPool(3));
    public final ImageCache imageCache = new ImageCache();
    public ConnectionWriter writer;
    final ClientMessageHandler messageHandler = new ClientMessageHandler(this);
    public final SelectionManager selectionManager = new SelectionManager(this);
    public final ActionQueuer actionQueuer = new ActionQueuer(this);
    public final UiManager uiManager = UiManager.createUiManager(this);

    public ClientGameState clientGameState;
}
