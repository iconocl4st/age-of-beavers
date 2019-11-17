package client.gui.actions.unit_action;

import client.app.ClientContext;
import client.ai.ActionRequester;
import common.state.EntityId;
import common.state.EntityReader;

public class UnGarrisonAll extends UnitAction {

    public UnGarrisonAll(ClientContext context) {
        super(context, "Ungarrison all");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return defaultGuardStatement(entity) && !entity.getGarrisoned().isEmpty();
    }

    @Override
    public void run(EntityReader entity) {
        for (EntityId e : entity.getGarrisoned()) {
            ar.setUnitActionToExit(new EntityReader(c.gameState, e));
        }
    }
}
