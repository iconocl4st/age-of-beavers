package client.gui.actions.unit_action;

import client.app.UiClientContext;
import common.state.EntityReader;
import common.state.spec.EntityClasses;

public class Farm extends UnitAction {
    public Farm(UiClientContext context) {
        super(context, "farm");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return defaultGuardStatement(entity) && entity.getType().containsClass(EntityClasses.FARMER);
    }

    @Override
    public void run(EntityReader entity) {
        c.actionQueuer.maybeQueue(entity, new client.ai.ai2.FarmAi(entity));
    }
}
