package client.gui.actions;

import client.app.UiClientContext;
import client.gui.actions.global_action.FilterAllPlayerUnits;
import client.gui.actions.global_action.PlaceBuilding;
import client.gui.actions.multi_unit.FilterAction;
import client.gui.actions.unit_action.*;
import common.state.EntityReader;
import common.state.spec.*;
import common.state.sst.sub.GateInfo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class Actions {

    Action[] multiUnitButtons;
    Action[] globalActions;
    Action[] singleUnitActions;
    Action[] pickupActions;
    Action[] garrisonButtons;
    Action[] gateButtons;
    Action[] duplicates;
    Action[] setAi;

    private SpecTree<CreationSpec> buildingPlacements;

    Action[] getBuildingButtons(UiClientContext context, String[] currentPath) {
        return getActionsForSpecTree(
                context,
                buildingPlacements,
                currentPath,
                input -> Collections.singleton(new PlaceBuilding(context, input))
        );
    }

    interface idk<I> {
        Collection<Action> create(I input);
    }

    private static <I> Action[] getActionsForSpecTree(
            UiClientContext context,
            SpecTree<I> tree,
            String[] currentPath,
            idk<I> idk
    ) {
        SpecTree.SpecNode<I> node = tree.get(currentPath);

        LinkedList<Action> options = new LinkedList<>();
        for (String child : node.getChildren())
            options.add(new PushStack.StackArgPusher(context, child));
        I value = node.getValue();
        if (value != null)
            options.addAll(idk.create(value));
        return options.toArray(new Action[0]);
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


    static Actions createActions(UiClientContext context) {
        Actions actions = new Actions();

        actions.buildingPlacements = context.clientGameState.gameState.gameSpec.canPlace;

        actions.setAi = new Action[]{
                new Hunt(context),
                new Deliver(context),
                new Build(context),
                new Gather(context),
                new Transport(context),
                new Farm(context),
                new SetFarmingLocation(context),
        };
        actions.duplicates = new Action[]{
                new Die(context),
                new DropAll(context),
                new Idle(context),
                new SetGatherPoint(context),
                new Move(context),
        };

        actions.singleUnitActions = new Action[]{
                new PushStack.UnitStackItemPusher(context, "Duplicates", UnitActions.StackItem.Duplicate) {
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
                new PushStack.UnitStackItemPusher(context, "Set Ai...", UnitActions.StackItem.SetAi) {
                    @Override
                    public boolean isEnabled(EntityReader entity) {
                        return defaultGuardStatement(entity) && (
                                entity.getType().containsAnyClass(
                                        EntityClasses.HUNTER,
                                        EntityClasses.CARRIER,
                                        EntityClasses.CONSTRUCTOR,
                                        EntityClasses.GATHERER
                                )
                        );
                    }
                },
                new PushStack.UnitStackItemPusher(context, "Pickup...", UnitActions.StackItem.Pickup) {
                    @Override
                    public boolean isEnabled(EntityReader entity) {
                        return defaultGuardStatement(entity) && entity.getType().containsClass(EntityClasses.CARRIER);
                    }
                },
                new PushStack.UnitStackItemPusher(context, "Create...", UnitActions.StackItem.Create) {
                    @Override
                    public boolean isEnabled(EntityReader entity) {
                        return defaultGuardStatement(entity) && entity.getType().canCreate.isNotEmpty();
                    }
                },
                new PushStack.UnitStackItemPusher(context, "Craft...", UnitActions.StackItem.Craft) {
                    @Override
                    public boolean isEnabled(EntityReader entity) {
                        return defaultGuardStatement(entity) && entity.getType().canCraft.isNotEmpty();
                    }
                },
                new PushStack.UnitStackItemPusher(context, "Set demands...", UnitActions.StackItem.SetDemands) {
                    @Override
                    public boolean isEnabled(EntityReader entity) {
                        return defaultGuardStatement(entity) && entity.getType().containsClass(EntityClasses.STORAGE);
                    }
                },
                new PushStack.UnitStackItemPusher(context, "Set evo selects...", UnitActions.StackItem.SetEvolutionSpec) {
                    @Override
                    public boolean isEnabled(EntityReader entity) {
                        return defaultGuardStatement(entity) && entity.getType().canCreate.anyMatch(c->c.method.equals(CreationMethod.Garrison));
                    }
                },
                new PushStack.UnitStackItemPusher(context, "Gate options...", UnitActions.StackItem.GateOptions) {
                    @Override
                    public boolean isEnabled(EntityReader entity) {
                        return defaultGuardStatement(entity) && entity.getType().containsClass(EntityClasses.PLAYER_OCCUPIES);
                    }
                },
                new PushStack.UnitStackItemPusher(context, "Garrisons...", UnitActions.StackItem.Garrisons) {
                    @Override
                    public boolean isEnabled(EntityReader entity) {
                        return entity != null && !entity.noLongerExists() && (
                                entity.getType().containsAnyClass(
                                        EntityClasses.GARRISONS_OTHERS,
                                        EntityClasses.CAN_GARRISON,
                                        EntityClasses.RIDER,
                                        EntityClasses.RIDABLE
                                ) ||
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
        for (ResourceType resourceType : context.clientGameState.gameState.gameSpec.resourceTypes) {
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
                new PushStack.UnitStackItemPusher(context, "Set evo selects...", UnitActions.StackItem.SetEvolutionSpec) {
                    @Override
                    public boolean isEnabled(EntityReader entity) {
                        return defaultGuardStatement(entity) && entity.getType().canCreate.anyMatch(c->c.method.equals(CreationMethod.Garrison));
                    }
                },
                new PushStack.GlobalStackItemPusher(context, "Place Building...", UnitActions.StackItem.PlaceBuilding),
//                new CloseGame(),
//                new SaveGame(),
        };
        return actions;
    }

    Action[] getCreateButtons(UiClientContext context, EntityReader next, String[] currentPath) {
        return getActionsForSpecTree(
                context,
                next.getType().canCreate,
                currentPath,
                input -> Arrays.asList(new Create(context, input), new ContinuousCreate(context, input))
        );
    }

    Action[] getCraftButtons(UiClientContext context, EntityReader next, String[] currentPath) {
        return getActionsForSpecTree(
                context,
                next.getType().canCraft,
                currentPath,
                input -> Collections.singleton(new Craft(context, input))
        );
    }
}

