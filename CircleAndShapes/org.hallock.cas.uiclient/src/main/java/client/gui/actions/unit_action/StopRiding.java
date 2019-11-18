package client.gui.actions.unit_action;

import client.app.UiClientContext;
import common.state.EntityReader;

public class StopRiding extends UnitAction {
    public StopRiding(UiClientContext context) {
        super(context, "Stop riding");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return defaultGuardStatement(entity) && entity.getRiding() != null;
    }

    @Override
    public void run(EntityReader entity) {
        ar.setUnitActionToDismount(entity);
    }
}
