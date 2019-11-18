package client.gui.actions;

import client.app.UiClientContext;
import common.msg.Message;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.spec.GameSpec;
import common.state.spec.ResourceType;
import common.state.sst.sub.capacity.Prioritization;
import common.state.sst.sub.capacity.PrioritizedCapacitySpec;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.IOException;

public class DemandsView extends JPanel {

    private final UiClientContext context;
    private ThresholdSelector[] currentSelectors;

    DemandsView(UiClientContext context) {
        this.context = context;
    }

    public void initialize(GameSpec gameSpec) {
        setLayout(new GridLayout(0, 1));
        JPanel subPanel = new JPanel();
        subPanel.setLayout(new GridLayout(0, 1));
        JButton backButton = new JButton("Back");
        subPanel.add(backButton);
        backButton.addActionListener(e -> context.uiManager.unitActions.pop());

        currentSelectors = new ThresholdSelector[gameSpec.resourceTypes.length];
        for (int i = 0; i < currentSelectors.length; i++) {
            ResourceType resourceType = context.clientGameState.gameState.gameSpec.resourceTypes[i];
            currentSelectors[i] = createThresholdSelector(resourceType);
            subPanel.add(currentSelectors[i]);
        }
        add(new JScrollPane(subPanel));
    }

    public void drawDemands(EntityReader next, boolean recreate) {
        for (ThresholdSelector selector : currentSelectors) {
            selector.drawDemand(next, recreate);
        }
    }

    private ThresholdSelector createThresholdSelector(ResourceType resourceType) {
        ThresholdSelector selector = new ThresholdSelector();
        selector.resourceType = resourceType;

        selector.setLayout(new GridLayout(0, 3));
        selector.setBorder(new TitledBorder(resourceType.name));

        ChangeListener changeListener = changeEvent -> {
            Object source = changeEvent.getSource();
            if (source instanceof JSlider && ((JSlider) source).getValueIsAdjusting()) {
                return;
            }
            selector.sendDemand();
        };

        selector.add(selector.minimumLabel);
        selector.add(selector.maximumLabel);
        selector.add(selector.accept);
        selector.minimum.addChangeListener(changeListener);
        selector.maximum.addChangeListener(changeListener);
        selector.spinner.addChangeListener(changeListener);

        selector.add(selector.minimum);
        selector.add(selector.maximum);
        selector.add(selector.spinner);
        return selector;
    }

    private final class ThresholdSelector extends JPanel {
        private final Object sync = new Object();
        private ResourceType resourceType;

        JCheckBox accept = new JCheckBox("Accept (to do)");
        JLabel minimumLabel = new JLabel("Minimum");
        JLabel maximumLabel = new JLabel("Maximum");

        JSlider minimum = new JSlider();
        JSlider maximum = new JSlider();
        JSpinner spinner = new JSpinner();

        private EntityId currentEntity;
        private Prioritization currentPrioritization;


        void drawDemand(EntityReader entity, boolean recreate) {
            synchronized (sync) {
                currentEntity = null;


                PrioritizedCapacitySpec capacity = entity.getCapacity();
                currentPrioritization = capacity.getPrioritization(resourceType);
                if (recreate) {
                    // why don't they both go
                    int max = Math.min(currentPrioritization.maximumAmount, capacity.getTotalWeight() / resourceType.weight);
                    minimum.setModel(new DefaultBoundedRangeModel(currentPrioritization.desiredAmount, 1, 0, max + 1));
                    maximum.setModel(new DefaultBoundedRangeModel(currentPrioritization.desiredMaximum, 1, 0, max + 1));
                    spinner.setModel(new SpinnerNumberModel(currentPrioritization.priority, 0, Integer.MAX_VALUE, 1));

//                    maximum.setValue(currentPrioritization.desiredMaximum);
//                    minimum.setValue(currentPrioritization.desiredAmount);
//                    spinner.setValue(currentPrioritization.priority);
                }

                minimumLabel.setText("Min: " + currentPrioritization.desiredAmount);
                maximumLabel.setText("Max: " + currentPrioritization.desiredMaximum);

                currentEntity = entity.entityId;
            }
        }

        void sendDemand() {
            synchronized (sync) {
                if (this.currentEntity == null) return;
                int priority = ((Number) spinner.getValue()).intValue();
                int min = ((Number) minimum.getValue()).intValue();
                int max = ((Number) maximum.getValue()).intValue();
                if (currentPrioritization != null &&
                        min == currentPrioritization.desiredAmount &&
                        max == currentPrioritization.desiredMaximum &&
                        priority == currentPrioritization.priority
                ) return;
                context.executorService.submit(() -> {
                    try {
                        context.writer.send(new Message.SetDesiredCapacity(currentEntity, resourceType, priority, min, max));
                        context.writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }
}
