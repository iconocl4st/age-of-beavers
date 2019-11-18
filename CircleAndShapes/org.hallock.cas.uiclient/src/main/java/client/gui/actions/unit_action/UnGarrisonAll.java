package client.gui.actions.unit_action;

import client.app.UiClientContext;
import common.state.EntityId;
import common.state.EntityReader;

public class UnGarrisonAll extends UnitAction {

    public UnGarrisonAll(UiClientContext context) {
        super(context, "Ungarrison all");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return defaultGuardStatement(entity) && !entity.getGarrisoned().isEmpty();
    }

    @Override
    public void run(EntityReader entity) {
        for (EntityReader e : entity.getGarrisoned()) {
            ar.setUnitActionToExit(e);
        }
    }
}
