package client.gui.actions.unit_action;

import client.app.UiClientContext;
import common.state.EntityReader;
import common.state.spec.EntityClasses;

public class Gather extends UnitToUnitAction {
    public Gather(UiClientContext context) {
        super(context, "Gather");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return defaultGuardStatement(entity) && entity.getType().containsClass(EntityClasses.GATHERER);
    }

    @Override
    public boolean canRunOn(EntityReader performer, EntityReader target) {
        return target.getType().containsClass(EntityClasses.NATURAL_RESOURCE);
    }

    @Override
    public void run(EntityReader entity, EntityReader target) {
        c.actionQueuer.maybeQueue(entity, new client.ai.ai2.Gather(entity, target, target.getType()));
    }
}
