package client.gui.actions.unit_action;

import client.app.ClientContext;
import client.ai.ActionRequester;
import common.state.EntityReader;

public class Idle extends UnitAction {

    public Idle(ClientContext context) {
        super(context, "Stop");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return defaultGuardStatement(entity) && (!entity.isIdle() || c.aiManager.isControlling(entity));
    }

    @Override
    public void run(EntityReader entity) {
        c.aiManager.removeAi(entity.entityId);
        ar.setUnitActionToIdle(entity);
    }
}
