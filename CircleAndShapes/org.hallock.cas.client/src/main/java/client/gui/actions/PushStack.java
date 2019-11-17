package client.gui.actions;

import client.app.ClientContext;
import client.gui.actions.unit_action.UnitAction;
import common.state.EntityReader;

import java.util.Collection;

public abstract class PushStack extends UnitAction {
    final UnitActions.StackItem item;
    final String arg;

    PushStack(ClientContext context, String label, UnitActions.StackItem item) {
        super(context, label);
        this.item = item;
        this.arg = null;
    }
    PushStack(ClientContext context, String label, UnitActions.StackItem item, String arg) {
        super(context, label);
        this.item = item;
        this.arg = arg;
    }

    @Override
    public void run(EntityReader entity) {
        c.uiManager.unitActions.push(item, arg);
    }

    public static final class GlobalPushStack extends PushStack {
        public GlobalPushStack(ClientContext context, String label, UnitActions.StackItem item, String arg) {
            super(context, label, item, arg);
        }

        @Override
        public boolean isEnabled(EntityReader entity) {
            return true;
        }

        @Override
        public void run(Collection<EntityReader> currentlySelected) {
            c.uiManager.unitActions.push(item, arg);
        }
    }
}
