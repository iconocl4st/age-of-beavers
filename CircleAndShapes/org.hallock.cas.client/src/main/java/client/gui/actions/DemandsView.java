package client.gui.actions;

import client.app.ClientContext;
import common.state.EntityReader;
import common.state.spec.GameSpec;
import common.state.spec.ResourceType;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class DemandsView extends JPanel{

    private final ClientContext context;
    private ThresholdSelector[] currentSelectors;
    private EntityReader controlling;

    DemandsView(ClientContext context) {
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
            ResourceType resourceType = context.gameState.gameSpec.resourceTypes[i];
            currentSelectors[i] = ThresholdSelector.createThreshholdSelector(resourceType);
            subPanel.add(currentSelectors[i]);
        }
        add(new JScrollPane(subPanel));
    }

    public void drawDemands(EntityReader next) {
        for (ThresholdSelector selector : currentSelectors) {
            selector.drawDemand(next);
        }
    }


    private static final class ThresholdSelector extends JPanel {
        private ResourceType resourceType;

        private static ThresholdSelector createThreshholdSelector(ResourceType resouceType) {
            ThresholdSelector selector = new ThresholdSelector();
            selector.resourceType = resouceType;

            selector.setLayout(new GridLayout(1, 0));
            selector.setBorder(new TitledBorder(resouceType.name));

            JCheckBox accept = new JCheckBox("Accept");
            JSlider slider = new JSlider();
            JSpinner spinner = new JSpinner();

            selector.add(accept);
            selector.add(slider);
            selector.add(spinner);
            return selector;
        }

        void drawDemand(EntityReader entity) {

        }
    }
}
