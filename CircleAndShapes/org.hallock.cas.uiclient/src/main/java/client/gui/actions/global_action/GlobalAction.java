package client.gui.actions.global_action;

import client.app.UiClientContext;
import client.gui.actions.Action;
import common.state.EntityReader;

import java.util.Collection;

public abstract class GlobalAction extends Action {

    protected GlobalAction(UiClientContext context, String label) {
        super(context, label);
    }

    @Override
    public final boolean isEnabled(Collection<EntityReader> currentlySelected) {
        return true;
    }

    @Override
    public final void run(Collection<EntityReader> currentlySelected) {
        run();
    }

    protected abstract void run();
}
