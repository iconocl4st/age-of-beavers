package client.gui.actions.unit_action;

import client.app.UiClientContext;
import common.state.EntityReader;

public class UnGarrison extends UnitAction {

    public UnGarrison(UiClientContext context) {
        super(context, "Ungarrison");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return entity != null && entity.isOwnedBy(c.clientGameState.currentPlayer) && entity.getHolder() != null;
    }

    @Override
    public void run(EntityReader entity) {
        ar.setUnitActionToExit(entity);
    }
}
