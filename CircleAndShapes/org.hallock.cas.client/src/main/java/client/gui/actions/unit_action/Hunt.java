package client.gui.actions.unit_action;

import client.ai.HuntAi;
import client.app.ClientContext;
import client.ai.ActionRequester;
import client.gui.game.Command;
import common.state.spec.EntitySpec;
import common.state.EntityId;
import common.state.EntityReader;
import common.util.DPoint;

public class Hunt extends UnitToUnitAction {
    public Hunt(ClientContext context) {
        super(context, "Hunt");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return defaultGuardStatement(entity) && entity.getType().containsClass("hunter");
    }

    @Override
    public boolean canRunOn(EntityReader performer, EntityReader target) {
        return target.getType().containsClass("prey");
    }

    @Override
    public void run(EntityReader entity, EntityReader target) {
        c.actionQueuer.maybeQueue(entity.entityId, new HuntAi(c, entity.entityId,  target.entityId, target.getType()));
    }
}
