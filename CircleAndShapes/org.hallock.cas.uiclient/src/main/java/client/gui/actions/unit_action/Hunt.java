package client.gui.actions.unit_action;

import client.app.UiClientContext;
import common.state.EntityReader;

public class Hunt extends UnitToUnitAction {
    public Hunt(UiClientContext context) {
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
        c.actionQueuer.maybeQueue(entity, new client.ai.ai2.Hunt(entity, target, target.getType()));
    }
}
