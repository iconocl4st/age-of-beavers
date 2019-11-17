package client.gui.selected;

import client.ai.Ai;
import client.app.ClientContext;
import client.gui.ImagePanel;
import com.fasterxml.jackson.databind.deser.std.MapEntryDeserializer;
import common.action.Action;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.Player;
import common.state.spec.ConstructionSpec;
import common.state.spec.EntitySpec;
import common.state.spec.GameSpec;
import common.state.spec.ResourceType;
import common.state.sst.sub.GateInfo;
import common.state.sst.sub.Load;
import common.state.sst.sub.capacity.CapacitySpec;
import common.util.DPoint;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SingleUnitSelected extends JPanel {

    private ClientContext context;

    TitledBorder resourcesBorder;
    private ImagePanel imagePanel;
    private JPanel resourcesPanel;
    private JLabel isRiding;
    private JLabel buildProgress;
    private JLabel action;
    private JLabel age;
    private JLabel ai;
    private JLabel gateState;
    private JLabel weapons;
    private JLabel garrisoned;
    private JLabel buildSpeed;
    private JLabel attackSpeed;
    private JLabel depositSpeed;
    private JLabel collectSpeed;
    private JLabel moveSpeed;
    private JLabel lineOfSight;
    private JLabel capacity;
    private JLabel health;
    private JLabel type;
    private JLabel owner;
    private JLabel location;

    private EntityReader entity;

    private ResourceType[] resourceTypes;
    private final Map<ResourceType, JLabel> resourceLabels = new HashMap<>();

    private SingleUnitSelected(ClientContext context) {
        this.context = context;
    }

    void setSelected(EntityId entityId) {
        if (entityId == null) {
            setLabelsToEmpty();
        } else {
            entity = new EntityReader(context.gameState, entityId);
            setLabelValues();
        }
        repaint();
    }

    public void initalize(GameSpec spec) {
        this.resourceTypes = spec.resourceTypes;
        resourcesPanel.removeAll();
        for (ResourceType resourceType : spec.resourceTypes) {
            JLabel label = new JLabel();
            resourcesPanel.add(label);
            resourceLabels.put(resourceType, label);
        }
    }

    private void setLabelsToEmpty() {
//        private JLabel imagePanel;
        resourcesPanel.removeAll();
        isRiding.setText("");
        buildProgress.setText("");
        action.setText("");
        age.setText("");
        ai.setText("");
        gateState.setText("");
        weapons.setText("");
        garrisoned.setText("");
        buildSpeed.setText("");
        attackSpeed.setText("");
        depositSpeed.setText("");
        collectSpeed.setText("");
        moveSpeed.setText("");
        lineOfSight.setText("");
        capacity.setText("");
        health.setText("");
        type.setText("");
        owner.setText("");
        location.setText("");
    }


    // TODO: don't call this as often
    public void setLabelValues() {
        if (entity == null) return;
        Object sync = entity.getSync();
        if (sync == null) return;
        synchronized (sync) {
            if (entity.noLongerExists())
                return;

            EntitySpec currentType = entity.getType();
            imagePanel.setImage(context.imageCache.get(currentType.image));

            Load load = entity.getCarrying();
            resourcesBorder.setTitle("Total weight carried: " + load.getWeight());
            if (currentType instanceof ConstructionSpec) {
                Map<ResourceType, Integer> requiredResources = ((ConstructionSpec) currentType).resultingStructure.requiredResources;
                for (Map.Entry<ResourceType, JLabel> entry : resourceLabels.entrySet()) {
                    entry.getValue().setText(
                            entry.getKey().name + ": " +
                            load.quantities.getOrDefault(entry.getKey(), 0) + " of " +
                            requiredResources.getOrDefault(entry.getKey(), 0) + " required"
                    );
                }
            } else {
                for (Map.Entry<ResourceType, JLabel> entry : resourceLabels.entrySet()) {
                    entry.getValue().setText(entry.getKey().name + ": " + load.quantities.getOrDefault(entry.getKey(), 0));
                }
            }

            EntityId riding = entity.getRiding();
            EntitySpec ridingSpec;
            if (riding != null && (ridingSpec = context.gameState.typeManager.get(riding)) != null) {
                isRiding.setText("Riding a " + ridingSpec.name);
            } else {
                isRiding.setText("nothing");
            }

            Action currentAction = entity.getCurrentAction();
            if (currentAction != null) {
                action.setText("Current action: " + currentAction.toString());
            } else {
                action.setText("idle");
            }

            age.setText("Age: " + String.valueOf(entity.getCurrentAge()));

            Ai currentAi = context.aiManager.getCurrentAi(entity.entityId);
            if (currentAi != null) {
                ai.setText("Ai: " + currentAi.toString());
            } else {
                ai.setText("No Ai");
            }

            GateInfo gateState1 = entity.getGateState();
            if (gateState1 != null) {
                gateState.setText("Gate state: " + gateState1.state.name());
            } else {
                gateState.setText("not a gate");
            }

            weapons.setText("Weapons: " + entity.getWeapons().getDisplayString());

            garrisoned.setText("Garrisoned " + entity.getGarrisoned().size() + " out of " + currentType.garrisonCapacity);
            buildSpeed.setText("Build speed: " + String.valueOf(entity.getBuildSpeed()));
            attackSpeed.setText("Attack speed: " + String.valueOf(entity.getCurrentAttackSpeed()));
            depositSpeed.setText("Deposit speed: " + String.valueOf(entity.getDepositSpeed()));
            collectSpeed.setText("Collect speed: " + String.valueOf(entity.getCollectSpeed()));
            moveSpeed.setText("Move speed: " + String.valueOf(entity.getMovementSpeed()));
            lineOfSight.setText("Line of sight: " + String.valueOf(entity.getBaseLineOfSight()));

            CapacitySpec currentCapacity = entity.getCapacity();
            if (currentCapacity != null) {
                capacity.setText("Capacity: " + currentCapacity.getDisplayString());
            } else {
                capacity.setText("No capacity");
            }

            type.setText("Type: " + currentType.name);

            Player currentOwner = entity.getOwner();
            if (currentOwner != null) {
                owner.setText("Owner: " + currentOwner.toString());
            } else {
                owner.setText("Error: no owner");
            }

            DPoint currentLocation = entity.getLocation();
            if (currentLocation != null) {
                location.setText("Location: " + currentLocation.toString());
            } else {
                location.setText("Nowhere");
            }

            health.setText("Health: " + entity.getCurrentHealth() + " out of " + entity.getBaseHealth());

            Double currentBuildProgress = entity.getCurrentBuildProgress();
            if (currentBuildProgress != null) {
                buildProgress.setText("Build progress: " + String.format("%.2f%%", 100 * currentBuildProgress));
            } else {
                buildProgress.setText("Build progress: " + "N/A");
            }
        }
    }

    public static SingleUnitSelected createSingleSelect(ClientContext context) {
        SingleUnitSelected ret = new SingleUnitSelected(context);
        ret.setLayout(new GridLayout(1, 0));
        ret.setPreferredSize(new Dimension(1, 1));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(0, 1));
        ret.imagePanel = new ImagePanel();
        leftPanel.add(ret.imagePanel);

        ret.resourcesPanel = new JPanel();
        ret.resourcesPanel.setLayout(new GridLayout(0, 3));
        ret.resourcesPanel.setBorder(ret.resourcesBorder = new TitledBorder("Carrying"));
        leftPanel.add(ret.resourcesPanel);

        ret.add(leftPanel);

        JPanel rightPanel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(rightPanel);
        ret.add(scrollPane);

        rightPanel.setLayout(new GridLayout(0, 1));
        rightPanel.add(ret.owner = new JLabel());
        rightPanel.add(ret.type = new JLabel());
        rightPanel.add(ret.health = new JLabel());
        rightPanel.add(ret.action = new JLabel(""));
        rightPanel.add(ret.location = new JLabel());
        rightPanel.add(ret.weapons = new JLabel());
        rightPanel.add(ret.ai = new JLabel());
        rightPanel.add(ret.age = new JLabel());
        rightPanel.add(ret.buildProgress = new JLabel());
        rightPanel.add(ret.moveSpeed = new JLabel());
        rightPanel.add(ret.capacity = new JLabel());
        rightPanel.add(ret.lineOfSight = new JLabel());
        rightPanel.add(ret.collectSpeed = new JLabel());
        rightPanel.add(ret.depositSpeed = new JLabel());
        rightPanel.add(ret.attackSpeed = new JLabel());
        rightPanel.add(ret.buildSpeed = new JLabel());
        rightPanel.add(ret.garrisoned = new JLabel());
        rightPanel.add(ret.gateState = new JLabel());
        rightPanel.add(ret.isRiding = new JLabel());
        return ret;
    }
}
