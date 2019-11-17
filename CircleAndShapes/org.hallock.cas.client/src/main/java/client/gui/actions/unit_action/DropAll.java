package client.gui.actions.unit_action;

import client.app.ClientContext;
import client.ai.ActionRequester;
import common.state.EntityReader;

public class DropAll extends UnitAction {

    public DropAll(ClientContext context) {
        super(context, "Drop all");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return entity != null && entity.isOwnedBy(c.currentPlayer) && entity.getType() != null && !entity.isNotCarryingAnything();
    }

    @Override
    public void run(EntityReader entity) {
        ar.setUnitActionToDropAll(entity);
    }
}
