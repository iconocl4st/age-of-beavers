package client.gui.actions.unit_action;

import client.app.ClientContext;
import client.ai.ActionRequester;
import common.state.EntityReader;

public class UnGarrison extends UnitAction {

    public UnGarrison(ClientContext context) {
        super(context, "Ungarrison");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return entity != null && entity.isOwnedBy(c.currentPlayer) && entity.getHolder() != null;
    }

    @Override
    public void run(EntityReader entity) {
        ar.setUnitActionToExit(entity);
    }
}
