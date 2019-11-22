package client.gui.actions.unit_action;

import client.app.UiClientContext;
import common.state.EntityReader;
import common.util.DPoint;

public class Move extends UnitToLocationAction {
    public Move(UiClientContext context) {
        super(context, "Move");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return defaultGuardStatement(entity) && entity.getMovementSpeed() > 0;
    }

    @Override
    public void run(EntityReader entity, DPoint location) {
        c.actionQueuer.maybeQueue(entity, new client.ai.ai2.Move(entity, location));
    }
}
