package app;

import app.ui.DebugPanel;
import app.ui.EventsDebugPanel;
import app.ui.UnitsDebugPanel;
import client.ai.ActionRequester;
import client.state.ClientGameState;
import client.ui.DemandsView;
import common.DebugGraphics;
import common.app.PlayerAiInterface;
import common.msg.Message;
import common.msg.NoExceptionsConnectionWriter;
import common.util.ExecutorServiceWrapper;
import common.util.MapUtils;

import java.util.HashMap;
import java.util.Random;

public class PlayerAiContext implements PlayerAiInterface {
    final Random random = new Random();

    private final ExecutorServiceWrapper executorService;

    public ClientGameState clientGameState;
    AiUtitlities utils;

    private PlayerAiImplementation ai;
    private QueueConnectionWriter  msgQueue = new QueueConnectionWriter();


    private DemandsView demandsDebugPanel;
    private UnitsDebugPanel debugPanel = UnitsDebugPanel.createDebugPanel();
    private EventsDebugPanel eventsDebugPanel =  EventsDebugPanel.createDebugPanel();

    public PlayerAiContext(ExecutorServiceWrapper executorService) {
        this.executorService = executorService;
    }

    @Override
    public void handleMessage(common.msg.Message message) {
        switch (message.getMessageType()) {
            case LAUNCHED: {
                Message.Launched launchMsg = (Message.Launched) message;
                clientGameState = ClientGameState.createClientGameState(
                        new ActionRequester(msgQueue),
                        executorService,
                        launchMsg
                );
                utils = new AiUtitlities(this);
                ai = new PlayerAiImplementation(this);
                demandsDebugPanel = DemandsView.createDemandsView(reader -> {
                    synchronized (common.DebugGraphics.pleaseFocusSync) {
                        DebugGraphics.pleaseFocus = reader;
                    }
                }, false);
                demandsDebugPanel.initialize(clientGameState);
                DebugPanel.showDebugFrame(
                        MapUtils.add(MapUtils.add(MapUtils.add(new HashMap<>(),
                                "Units", debugPanel),
                                "Event listeners", eventsDebugPanel),
                                "Demands", demandsDebugPanel)
                );
                clientGameState.eventManager.listenToListeners(eventsDebugPanel);
            }
            break;
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
