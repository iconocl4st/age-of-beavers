package client.gui.actions;

import client.app.ClientContext;
import client.gui.actions.global_action.FilterAllPlayerUnits;
import client.gui.actions.global_action.PlaceBuilding;
import client.gui.actions.multi_unit.FilterAction;
import client.gui.actions.unit_action.*;
import common.state.EntityReader;
import common.state.spec.CreationSpec;
import common.state.spec.EntitySpec;
import common.state.spec.GameSpec;
import common.state.spec.ResourceType;
import common.state.sst.sub.GateInfo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Actions {

    Action[] multiUnitButtons;
    Action[] globalActions;
    Action[] singleUnitActions;
    Action[] pickupActions;
    Action[] garrisonButtons;
    Action[] gateButtons;
    Action[] createButtons;
    Action[] duplicates;
    Action[] setAi;
    Map<String, Action[]> buildingButtons = new HashMap<>();


    public Action[] getBuildingButtons(String stackArg) {
        return buildingButtons.getOrDefault(stackArg, new Action[0]);
    }

    private PushStack.GlobalPushStack parseBuildingPaths(ClientContext context, String currentPath, String name, GameSpec.BuildingPathNode buildingPathNode) {
        LinkedList<Action> globals = new LinkedList<>();
        for (Map.Entry<String, GameSpec.BuildingPathNode> entry : buildingPathNode.children.entrySet()) {
            globals.add(parseBuildingPaths(context, currentPath + ":" + entry.getKey(), entry.getKey(), entry.getValue()));
        }
        for (EntitySpec buildingSpec : buildingPathNode.buildings) {
            globals.add(new PlaceBuilding(context, buildingSpec));
        }
        buildingButtons.put(currentPath, globals.toArray(new Action[0]));
        return new PushStack.GlobalPushStack(context, name, UnitActions.StackItem.PlaceBuilding, currentPath);
    }

    // select high initialBaseHealth
    // select by type
    // garrison
    // delete
    // set garrison location
    // move
    // explore


    // stance
    // control group
    // formations
    // next in selected
    // next/prev options ?
    // focus on
    //


    static Actions createActions(ClientContext context) {
        Actions actions = new Actions();
        actions.setAi = new Action[]{
                new Hunt(context),
                new Deliver(context),
                new Build(context),
                new Gather(context),
        };
        actions.duplicates = new Action[]{
                new Die(context),
                new DropAll(context),
                new Idle(context),
                new SetGatherPoint(context),
                new Move(context),
        };

        actions.singleUnitActions = new Action[]{
                new PushStack(context, "Duplicates", UnitActions.StackItem.Duplicate) {
                    @Override
                    public boolean isEnabled(EntityReader entity) {
                        return defaultGuardStatement(entity) && (
                                true // new Die(context),
                                     // new DropAll(context),
                                     // new Idle(context),
                                     // new SetGatherPoint(context),
                                     // new Move(context),
                        );
                    }
                },
                new PushStack(context, "Set Ai...", UnitActions.StackItem.SetAi) {
                    @Override
                    public boolean isEnabled(EntityReader entity) {
                        return defaultGuardStatement(entity) && (
                                entity.getType().containsClass("hunter") ||
                                entity.getType().containsClass("carrier") ||
                                entity.getType().containsClass("constructor") ||
                                entity.getType().containsClass("gatherer")
                        );
                    }
                },
                new PushStack(context, "Pickup...", UnitActions.StackItem.Pickup) {
                    @Override
                    public boolean isEnabled(EntityReader entity) {
                        return defaultGuardStatement(entity) && entity.getType().containsClass("carrier");
                    }
                },
                new PushStack(context, "Create...", UnitActions.StackItem.Create) {
                    @Override
                    public boolean isEnabled(EntityReader entity) {
                        return defaultGuardStatement(entity) && !entity.getType().canCreate.isEmpty();
                    }
                },
                new PushStack(context, "Set demands...", UnitActions.StackItem.SetDemands) {
                    @Override
                    public boolean isEnabled(EntityReader entity) {
                        return defaultGuardStatement(entity) && entity.getType().containsClass("storage");
                    }
                },
                new PushStack(context, "Gate options...", UnitActions.StackItem.GateOptions) {
                    @Override
                    public boolean isEnabled(EntityReader entity) {
                        return defaultGuardStatement(entity) && entity.getType().containsClass("player-occupies");
                    }
                },
                new PushStack(context, "Garrisons...", UnitActions.StackItem.Garrisons) {
                    @Override
                    public boolean isEnabled(EntityReader entity) {
                        return entity != null && !entity.noLongerExists() && (
                                entity.getType().containsClass("can-garrison-others") ||
                                        entity.getType().containsClass("can-garrison-in-others") ||
                                        entity.getType().containsClass("rider") ||
                                        entity.getType().containsClass("ridden") ||
                                        entity.getHolder() != null ||
                                        !entity.getGarrisoned().isEmpty() ||
                                        entity.getRiding() != null
                        );
                    }
                },
        };
        actions.garrisonButtons = new UnitAction[]{
                new Ride(context),
                new StopRiding(context),
                new UnGarrison(context),
                new Garrison(context),
                new UnGarrisonAll(context),
                new SelectGarrisoned(context),
        };

        LinkedList<UnitAction> list = new LinkedList<>();
        for (GateInfo.GateState state : GateInfo.GateState.values()) {
            list.add(new GateButton(context, state));
        }
        actions.gateButtons = list.toArray(new UnitAction[0]);

        list.clear();
        for (ResourceType resourceType : context.gameState.gameSpec.resourceTypes) {
            list.add(new Pickup(context, resourceType));
        }
        actions.pickupActions = list.toArray(new UnitAction[0]);

        list.clear();
        for (CreationSpec spec : new CreationSpec[0]) {
            list.add(new Create(context, spec));
            list.add(new ContinuousCreate(context, spec));
        }
        actions.gateButtons = list.toArray(new UnitAction[0]);


        actions.multiUnitButtons = new Action[] {
            new FilterAction(context, "Select Idle", EntityReader::isIdle),
            new FilterAction(context, "Select Low initialBaseHealth", EntityReader::isLowHealth),
        };

        actions.globalActions = new Action[] {
                new DisabledAction(context,"Cancel"),
                new FilterAllPlayerUnits(context, "Select Idle", EntityReader::isIdle),
                new FilterAllPlayerUnits(context, "Select Low initialBaseHealth", EntityReader::isLowHealth),
//                new CloseGame(),
//                new SaveGame(),
                actions.parseBuildingPaths(context, "", "Place Building...", context.gameState.gameSpec.compileBuildingPaths()),
        };
        return actions;
    }

    public Action[] getCreateButtons(ClientContext context, EntityReader next) {
        LinkedList<UnitAction> list = new LinkedList<>();
        for (CreationSpec spec : next.getType().canCreate) {
            list.add(new Create(context, spec));
            list.add(new ContinuousCreate(context, spec));
        }
        return list.toArray(new UnitAction[0]);
    }
}

