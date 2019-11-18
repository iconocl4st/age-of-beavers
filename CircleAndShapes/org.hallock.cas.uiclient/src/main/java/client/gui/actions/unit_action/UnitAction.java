package client.gui.actions.unit_action;

import client.app.UiClientContext;
import client.gui.actions.Action;
import common.state.EntityReader;

import java.util.Collection;

public abstract class UnitAction extends Action {
    protected UnitAction(UiClientContext context, String label) {
        super(context, label);
    }

    public abstract boolean isEnabled(EntityReader entity);

    public abstract void run(EntityReader entity);

    protected boolean defaultGuardStatement(EntityReader entity) {
        return !c.clientGameState.isSpectating() && entity != null && !entity.isHidden() && entity.isOwnedBy(c.clientGameState.currentPlayer) && entity.getType() != null;
    }


    @Override
    public boolean isEnabled(Collection<EntityReader> currentlySelected) {
        for (EntityReader reader : currentlySelected) {
            if (!isEnabled(reader)) return false;
        }
        return true;
    }

    @Override
    public void run(Collection<EntityReader> currentlySelected) {
        for (EntityReader reader : currentlySelected) {
            Object sync = reader.getSync();
            synchronized (sync) {
                if (reader.noLongerExists())
                    continue;
                run(reader);
            }
        }
    }
}
