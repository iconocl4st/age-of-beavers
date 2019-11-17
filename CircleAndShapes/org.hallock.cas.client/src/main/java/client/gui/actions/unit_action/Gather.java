package client.gui.actions.unit_action;

import client.app.ClientContext;
import common.state.EntityReader;

public class Gather extends UnitToUnitAction {
    public Gather(ClientContext context) {
        super(context, "Gather");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return defaultGuardStatement(entity) && entity.getType().containsClass("gatherer");
    }

    @Override
    public boolean canRunOn(EntityReader performer, EntityReader target) {
        return target.getType().containsClass("natural-resource");
    }

    @Override
    public void run(EntityReader entity, EntityReader target) {
        c.actionQueuer.maybeQueue(entity.entityId, new client.ai.Gather(c, entity.entityId, target.entityId, target.getType()));
    }
}
