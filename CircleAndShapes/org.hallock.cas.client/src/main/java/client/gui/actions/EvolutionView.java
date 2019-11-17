package client.gui.actions;

import client.app.ClientContext;
import common.state.EntityReader;
import common.state.spec.GameSpec;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class EvolutionView extends JPanel {

    private final ClientContext context;
    private EntityReader controlling;

    private JSpinner los;
    private JSpinner health;
    private JSpinner movement;
    private JSpinner collect;
    private JSpinner deposit;
    private JSpinner build;
    private JSpinner rotation;
    private JSpinner attack;

    public EvolutionView(ClientContext context) {
        this.context = context;
    }

    private JSpinner addSpinner(String label, Setter setter) {
        add(new JLabel(label));
        JSpinner spinner = new JSpinner();
        spinner.setModel(new SpinnerNumberModel(0.0, 0.0, 1000.0, 0.1));
        spinner.addChangeListener(changeEvent -> {
            setter.set(((Number) spinner.getValue()).doubleValue());
        });
        add(spinner);
        return spinner;
    }

    public void initialize(GameSpec spec) {
        setLayout(new GridLayout(0, 2));
        setBorder(new TitledBorder("Weights for created units"));
        los = addSpinner("Line of sight", this::setLineOfSightWeight);
        health = addSpinner("Base health", this::setHealthWeight);
        movement = addSpinner("Movement speed", this::setMovementSpeedWeight);
        collect = addSpinner("Collect speed", this::setCollectSpeedWeight);
        deposit = addSpinner("Deposit speed", this::setDepositSpeedWeight);
        build = addSpinner("Build speed", this::setBuildSpeedWeight);
        rotation = addSpinner("Rotation speed", this::setRotationSpeedWeight);
        attack = addSpinner("Attack speed", this::setAttackSpeedWeight);
    }

    private void setLineOfSightWeight(double v) {}
    private void setHealthWeight(double v) {}
    private void setMovementSpeedWeight(double v) {}
    private void setCollectSpeedWeight(double v) {}
    private void setDepositSpeedWeight(double v) {}
    private void setBuildSpeedWeight(double v) {}
    private void setRotationSpeedWeight(double v) {}
    private void setAttackSpeedWeight(double v) {}

    private interface Setter {
        void set(double value);
    }

    void drawEvolution(EntityReader next) {
        this.controlling = next;

        los = ;
        health = ;
        movement = ;
        collect = ;
        deposit = ;
        build = ;
        rotation = ;
        attack = ;
    }
}
