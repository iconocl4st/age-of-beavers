package client.gui.actions;

import client.app.ClientContext;
import common.msg.Message;
import common.state.EntityReader;
import common.state.spec.GameSpec;
import common.util.EvolutionSpec;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;

public class EvolutionView extends JPanel {

    private final Object sync = new Object();

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
    private EvolutionSpec nextWeights;

    public EvolutionView(ClientContext context) {
        this.context = context;
    }

    private JSpinner addSpinner(String label, Setter setter) {
        add(new JLabel(label));
        JSpinner spinner = new JSpinner();
        spinner.setModel(new SpinnerNumberModel(0.0, 0.0, 1000.0, 0.1));
        spinner.addChangeListener(changeEvent -> {
            synchronized (sync) {
                setter.set(((Number) spinner.getValue()).doubleValue());
            }
        });
        add(spinner);
        return spinner;
    }

    public void initialize(GameSpec spec) {
        setLayout(new GridLayout(0, 2));
        JButton back = new JButton("Back");
        back.addActionListener(e->context.uiManager.unitActions.pop());
        add(back);
        add(new JPanel());
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

    private void setLineOfSightWeight(double v)     { if (nextWeights != null) { nextWeights.initialLineOfSight = v; send(); } }
    private void setHealthWeight(double v)          { if (nextWeights != null) { nextWeights.initialBaseHealth = v; send(); } }
    private void setMovementSpeedWeight(double v)   { if (nextWeights != null) { nextWeights.initialMovementSpeed = v; send(); } }
    private void setCollectSpeedWeight(double v)    { if (nextWeights != null) { nextWeights.initialCollectSpeed = v; send(); } }
    private void setDepositSpeedWeight(double v)    { if (nextWeights != null) { nextWeights.initialDepositSpeed = v; send(); } }
    private void setBuildSpeedWeight(double v)      { if (nextWeights != null) { nextWeights.initialBuildSpeed = v; send(); } }
    private void setRotationSpeedWeight(double v)   { if (nextWeights != null) { nextWeights.initialRotationSpeed = v; send(); } }
    private void setAttackSpeedWeight(double v)     { if (nextWeights != null) { nextWeights.initialAttackSpeed = v; send(); } }

    private void send() {
        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.SetEvolutionSelection(controlling.entityId, nextWeights));
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private interface Setter {
        void set(double value);
    }

    void drawEvolution(EntityReader next) {
        synchronized (sync) {
            EvolutionSpec evolutionWeights = next.getEvolutionWeights();
            this.nextWeights = null;

            this.controlling = next;
            if (evolutionWeights == null) {
                los.setValue(0.0); los.setEnabled(false);
                health.setValue(0.0); health.setEnabled(false);
                movement.setValue(0.0); movement.setEnabled(false);
                collect.setValue(0.0); collect.setEnabled(false);
                deposit.setValue(0.0); deposit.setEnabled(false);
                build.setValue(0.0); build.setEnabled(false);
                rotation.setValue(0.0); rotation.setEnabled(false);
                attack.setValue(0.0); attack.setEnabled(false);
            } else {
                los.setValue(evolutionWeights.initialLineOfSight);          los.setEnabled(true);
                health.setValue(evolutionWeights.initialBaseHealth);        health.setEnabled(true);
                movement.setValue(evolutionWeights.initialMovementSpeed);   movement.setEnabled(true);
                collect.setValue(evolutionWeights.initialCollectSpeed);     collect.setEnabled(true);
                deposit.setValue(evolutionWeights.initialDepositSpeed);     deposit.setEnabled(true);
                build.setValue(evolutionWeights.initialBuildSpeed);         build.setEnabled(true);
                rotation.setValue(evolutionWeights.initialRotationSpeed);   rotation.setEnabled(true);
                attack.setValue(evolutionWeights.initialAttackSpeed);       attack.setEnabled(true);
            }

            this.nextWeights = evolutionWeights;
        }
    }
}
