package client.ui;


import client.ai.ai2.AiContext;
import client.event.AiEventListener;
import client.event.supply.TransportRequest;
import client.state.ClientGameState;
import common.event.AiEvent;
import common.event.AiEventType;
import common.state.EntityReader;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;

/**
 * TODO: This class has a ui and is in the client module.
 * I  didn't want that, but I am using it for debugging.
 */
public class DemandsView extends JPanel implements AiEventListener {

    private ClientGameState clientGameState;

    private final Object sync = new Object();
    private final FocusRequester requester;
    private JPanel excessPanel;
    private final ArrayList<DemandView> currentExcess = new ArrayList<>();
    private JPanel demandingPanel;
    private final ArrayList<DemandView> currentDemand = new ArrayList<>();

    private DemandsView(FocusRequester requester) {
        this.requester = requester;
    }


    public void initialize(ClientGameState gameState) {
        this.clientGameState = gameState;
        clientGameState.eventManager.listenForEvents(this, AiEventType.DemandsChanged);
    }

    @Override
    public void receiveEvent(AiContext aiContext, AiEvent event) {
        if (event.type.equals(AiEventType.DemandsChanged))
            drawComponents();
    }

    void drawComponents() {
        if (!isVisible()) return;

        synchronized (sync) {
            int index = 0;
            for (TransportRequest request : clientGameState.supplyAndDemandManager.getExceeding()) {
                if (index >= currentExcess.size()) {
                    DemandView view = createDemandView();
                    currentExcess.add(view);
                    excessPanel.add(view);
                }
                currentExcess.get(index++).drawTransportRequest(request);
            }
            while (index < currentExcess.size()) {
                excessPanel.remove(currentExcess.remove(index));
            }

            index = 0;
            for (TransportRequest request : clientGameState.supplyAndDemandManager.getDemands()) {
                if (index >= currentDemand.size()) {
                    DemandView view = createDemandView();
                    currentDemand.add(view);
                    demandingPanel.add(view);
                }
                currentDemand.get(index++).drawTransportRequest(request);
            }
            while (index < currentDemand.size()) {
                demandingPanel.remove(currentDemand.remove(index));
            }
        }
        repaint();
    }


    public DemandView createDemandView() {
        DemandView demandView = new DemandView();
        demandView.setLayout(new GridLayout(0, 2));
        demandView.resource = new JLabel(); demandView.add(demandView.resource);
        demandView.priority = new JLabel(); demandView.add(demandView.priority);
        demandView.desiredAmount = new JLabel(); demandView.add(demandView.desiredAmount);
        demandView.timeRequested = new JLabel(); demandView.add(demandView.timeRequested);
        demandView.requesterBtn = new JButton("Requester"); demandView.add(demandView.requesterBtn);
        demandView.requesterBtn.addActionListener(event -> demandView.goToRequester());
        demandView.servicerBtn = new JButton("Servicer"); demandView.add(demandView.servicerBtn);
        demandView.servicerBtn.addActionListener(event -> demandView.goToServicer());
        return demandView;
    }

    private final class DemandView extends JPanel {
        private EntityReader currentRequester;
        private EntityReader currentServicer;

        private JLabel resource;
        private JLabel priority;
        private JLabel desiredAmount;
        private JLabel timeRequested;
        private JButton requesterBtn;
        private JButton servicerBtn;

        public void drawTransportRequest(TransportRequest request) {
            this.currentRequester = request.requester;
            resource.setText("Resource: " + request.resource.name);
            priority.setText("Priority: " + request.priority);
            timeRequested.setText("Request time: " + String.format("%.2f", request.timeRequested));
            desiredAmount.setText("Serviced: " + (request.servicer != null));
            servicerBtn.setEnabled(request.servicer != null);
            if (request.servicer != null)
                currentServicer = request.servicer.getServicer();
        }

        private void goToRequester() {
            if (currentRequester == null) return;
            requester.requestFocus(currentRequester);
        }
        private void goToServicer() {
            if (currentServicer == null) return;
            requester.requestFocus(currentServicer);
        }
    }

    public static DemandsView createDemandsView(FocusRequester requester, boolean splitVertically) {
        DemandsView view = new DemandsView(requester);
        view.setLayout(splitVertically ? new GridLayout(0, 1) : new GridLayout(1, 0));
        view.excessPanel = new JPanel();
        view.excessPanel.setBorder(new TitledBorder("Excess"));
        view.excessPanel.setLayout(new GridLayout(0, 1));
        view.add(new JScrollPane(view.excessPanel));

        view.demandingPanel = new JPanel();
        view.demandingPanel.setBorder(new TitledBorder("Demands"));
        view.demandingPanel.setLayout(new GridLayout(0, 1));
        view.add(new JScrollPane(view.demandingPanel));
        return view;
    }

    public interface FocusRequester {
        void requestFocus(EntityReader reader);
    }
}
