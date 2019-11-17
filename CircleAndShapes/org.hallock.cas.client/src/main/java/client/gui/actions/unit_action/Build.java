package client.gui.actions.unit_action;

import client.ai.ConstructAi;
import client.app.ClientContext;
import common.state.EntityReader;
import common.state.sst.sub.ConstructionZone;

public class Build extends UnitToUnitAction {

    public Build(ClientContext context) {
        super(context, "Build");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return defaultGuardStatement(entity) && entity.getType().containsClass("constructor");
    }

    @Override
    public boolean canRunOn(EntityReader performer, EntityReader target) {
        return target.getType().containsClass("construction-zone") && target.getConstructionZone() != null;
    }

    @Override
    public void run(EntityReader entity, EntityReader target) {
        c.actionQueuer.maybeQueue(entity.entityId, new ConstructAi(c, entity.entityId, target.entityId));
    }
}
