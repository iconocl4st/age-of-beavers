package client.gui.actions;

import client.app.UiClientContext;
import common.state.EntityReader;

import java.util.Collection;

public class DisabledAction extends  Action {
    protected DisabledAction(UiClientContext context, String label) {
        super(context, label);
    }

    @Override
    public boolean isEnabled(Collection<EntityReader> currentlySelected) {
        return false;
    }

    @Override
    public void run(Collection<EntityReader> currentlySelected) {}
}
