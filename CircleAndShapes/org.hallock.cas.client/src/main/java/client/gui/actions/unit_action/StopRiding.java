package client.gui.actions.unit_action;

import client.app.ClientContext;
import client.ai.ActionRequester;
import common.state.EntityReader;

public class StopRiding extends UnitAction {
    public StopRiding(ClientContext context) {
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
