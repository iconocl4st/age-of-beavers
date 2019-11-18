package client.gui.actions.unit_action;

import client.app.UiClientContext;
import common.state.EntityReader;

public class Idle extends UnitAction {

    public Idle(UiClientContext context) {
        super(context, "Stop");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return defaultGuardStatement(entity) && (!entity.isIdle() || c.clientGameState.aiManager.isControlling(entity));
    }

    @Override
    public void run(EntityReader entity) {
        c.clientGameState.aiManager.removeAi(entity.entityId);
        ar.setUnitActionToIdle(entity);
    }
}
