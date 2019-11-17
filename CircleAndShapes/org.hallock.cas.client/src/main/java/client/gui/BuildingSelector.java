package client.gui;


import client.gui.game.GameScreen;
import common.state.spec.EntitySpec;
import common.state.spec.GameSpec;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class BuildingSelector extends JPanel {

    final GameScreen gameScreen;

    public BuildingSelector(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }


    public void initialize(GameSpec gSpec) {
        Set<EntitySpec> buildingSpecs = gSpec.getUnitSpecsByClass("placeable");

        setLayout(new GridLayout(0, 1));

        for (final EntitySpec bSpec : buildingSpecs) {
            JButton button = new JButton("Build " + bSpec.name);
            button.addActionListener(actionEvent -> gameScreen.queryBuildingLocation(bSpec));
            add(button);
        }
    }
}
