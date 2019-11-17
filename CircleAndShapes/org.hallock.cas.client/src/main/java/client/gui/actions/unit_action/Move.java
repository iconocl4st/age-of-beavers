package client.gui.actions.unit_action;

import client.ai.MoveAi;
import client.app.ClientContext;
import common.msg.Message;
import common.state.EntityReader;
import common.util.DPoint;

import java.io.IOException;

public class Move extends UnitToLocationAction {
    public Move(ClientContext context) {
        super(context, "Move");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return defaultGuardStatement(entity) && entity.getMovementSpeed() > 0;
    }

    @Override
    public void run(EntityReader entity, DPoint location) {
        c.actionQueuer.maybeQueue(entity.entityId, new MoveAi(c, entity.entityId, location));
    }
}
