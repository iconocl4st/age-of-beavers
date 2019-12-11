package client.gui.actions;

import client.app.UiClientContext;
import client.gui.actions.unit_action.UnitAction;
import common.state.EntityReader;

import java.util.Collection;

public class PushStack {
    public static abstract class UnitStackItemPusher extends UnitAction {
        final UnitActions.StackItem item;

        UnitStackItemPusher(UiClientContext context, String label, UnitActions.StackItem item) {
            super(context, label);
            this.item = item;
        }

        @Override
        public void run(EntityReader entity) {
            c.uiManager.unitActions.push(item);
        }
    }


    public static class GlobalStackItemPusher extends Action {
        final UnitActions.StackItem item;

        GlobalStackItemPusher(UiClientContext context, String label, UnitActions.StackItem item) {
            super(context, label);
            this.item = item;
        }

        @Override
        public boolean isEnabled(Collection<EntityReader> currentlySelected) {
            return true;
        }

        @Override
        public void run(Collection<EntityReader> currentlySelected) {
            c.uiManager.unitActions.push(item);
        }
    }

    public static final class StackArgPusher extends Action {
        StackArgPusher(UiClientContext context, String label) {
            super(context, label);
        }

        @Override
        public boolean isEnabled(Collection<EntityReader> currentlySelected) {
            return true;
        }

        @Override
        public void run(Collection<EntityReader> currentlySelected) {
            c.uiManager.unitActions.pushArg(label);
        }
    }
}
