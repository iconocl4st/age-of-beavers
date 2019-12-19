package client.gui.selected;

import client.app.UiClientContext;
import client.gui.ImagePanel;
import common.action.Action;
import common.event.GrowthStage;
import common.state.EntityReader;
import common.state.Player;
import common.state.spec.EntityClasses;
import common.state.spec.EntitySpec;
import common.state.spec.GameSpec;
import common.state.spec.ResourceType;
import common.state.sst.sub.GateInfo;
import common.state.sst.sub.GrowthInfo;
import common.state.sst.sub.Load;
import common.state.sst.sub.capacity.PrioritizedCapacitySpec;
import common.util.DPoint;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.*;

public class SingleUnitSelected extends JPanel {

    private UiClientContext context;
    private final Object uiSync = new Object();

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
    private JLabel growthInfo;

    private EntityReader entity;

    private boolean isUpdating;

//    private ResourceType[] resourceTypes;
    private final Map<ResourceType, JLabel> cachedResourceLabels = new HashMap<>();

    private SingleUnitSelected(UiClientContext context) {
        this.context = context;
    }

    void setSelected(EntityReader entityId) {
        synchronized (uiSync) {
            if (entityId == null) {
                setLabelsToEmpty();
            } else {
                entity = entityId;
                setLabelValues();
            }
        }
        repaint();
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
        growthInfo.setText("");
    }


    private static boolean setsAreEqual(Set<ResourceType> s1, Set<ResourceType> s2) {
        if (s1.size() != s2.size()) {
            return false;
        }
        return s1.containsAll(s2);
    }

    private void clearResourceLabels() {
        resourcesPanel.removeAll();
        cachedResourceLabels.clear();
    }

    private void udpateCachedResourceLabels(Set<ResourceType> neededLabels) {
        if (setsAreEqual(cachedResourceLabels.keySet(), neededLabels)) return;
        clearResourceLabels();

        for (ResourceType rt : neededLabels) {
            JLabel jLabel = new JLabel();
            cachedResourceLabels.put(rt, jLabel);
            resourcesPanel.add(jLabel);
        }
    }

    void setLabelValues() {
        SwingUtilities.invokeLater(() -> {
            synchronized (uiSync) {
                if (isUpdating) return;
                isUpdating = true;
                if (entity == null) return;
                Object sync = entity.getSync();
                if (sync == null) return;
                synchronized (sync) {
                    synchronizedSetLabelValues();
                }
                isUpdating = false;
            }
        });
    }

    private void synchronizedSetLabelValues() {
        if (entity.noLongerExists()) {
            setDead();
            return;
        }

        EntitySpec currentType = entity.getType();
        imagePanel.setImage(context.imageCache.get(entity.getGraphics()));


        Load load = entity.getCarrying();
        Map<ResourceType, Integer> requiredResources = Collections.emptyMap();
        if (currentType.containsClass(EntityClasses.CONSTRUCTION_ZONE)) {
            requiredResources = currentType.carryCapacity.getMaximumAmounts();
        }

        Set<ResourceType> neededLabels = new TreeSet<>(ResourceType.COMPARATOR);
        neededLabels.addAll(load.quantities.keySet());
        neededLabels.addAll(requiredResources.keySet());
        udpateCachedResourceLabels(neededLabels);

        resourcesBorder.setTitle("Total weight carried: " + ResourceType.formatWeight(load.getWeight()));

        for (Map.Entry<ResourceType, JLabel> entry : cachedResourceLabels.entrySet()) {
            Integer has = load.quantities.getOrDefault(entry.getKey(), 0);
            Integer requires = requiredResources.get(entry.getKey());
            StringBuilder value = new StringBuilder();
            value.append(entry.getKey().name).append(": ").append(has);
            if (requires != null)
                value.append('/').append(requires);
            entry.getValue().setText(value.toString());
        }

        EntityReader riding = entity.getRiding();
        EntitySpec ridingSpec;
        if (riding != null && (ridingSpec = riding.getType()) != null) {
            isRiding.setText("Riding a " + ridingSpec.name);
        } else {
            isRiding.setText("Not riding anything");
        }

        Action currentAction = entity.getCurrentAction();
        if (currentAction != null) {
            action.setText("Current action: " + currentAction.toString());
        } else {
            action.setText("idle");
        }

        age.setText("Age: " + String.format("%.1f", entity.getCurrentAge()));

        String currentAi = context.clientGameState.aiManager.getDisplayString(entity);
        ai.setText("AiStack: " + currentAi);

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
        lineOfSight.setText("Line of sight: " + String.valueOf(entity.getLineOfSight()));

        PrioritizedCapacitySpec currentCapacity = entity.getCapacity();
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

        GrowthInfo gi = entity.getGrowthInfo();
        if (gi == null) {
            growthInfo.setText("Not a plant");
        } else {
            growthInfo.setText(gi.toString());
        }
    }

    private void setDead() {
        imagePanel.setImage(context.imageCache.get("na.png"));

        resourcesBorder.setTitle("N/A");
        clearResourceLabels();

        isRiding.setText("unknown");
        action.setText("No action");
        age.setText("Ageless");
        ai.setText("No intelligence");
        gateState.setText("Beyond the gate");

        weapons.setText("Weaponless");

        garrisoned.setText("No Garrisoned");
        buildSpeed.setText("Cannot build");
        attackSpeed.setText("Cannot attack");
        depositSpeed.setText("Cannot deposit");
        collectSpeed.setText("Cannot collect");
        moveSpeed.setText("Cannot move");
        lineOfSight.setText("Cannot see");
        capacity.setText("Cannot carry");

        type.setText("No type");
        owner.setText("Owner: Nobody");
        location.setText("No location");
        health.setText("No health");
        buildProgress.setText("Not under construction");
        growthInfo.setText("Not a crop");
    }

    static SingleUnitSelected createSingleSelect(UiClientContext context) {
        SingleUnitSelected ret = new SingleUnitSelected(context);
        ret.setLayout(new GridLayout(1, 0));
        ret.setPreferredSize(new Dimension(1, 1));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(0, 1));
        ret.imagePanel = new ImagePanel();
        leftPanel.add(ret.imagePanel);

        ret.resourcesPanel = new JPanel();
        ret.resourcesPanel.setLayout(new GridLayout(0, 2));
        ret.resourcesPanel.setBorder(ret.resourcesBorder = new TitledBorder("Carrying"));
        leftPanel.add(ret.resourcesPanel); // add scroll pane?

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
        rightPanel.add(ret.growthInfo = new JLabel());
        return ret;
    }
}
