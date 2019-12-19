package client.gui.actions.unit_action;

import client.ai.ai2.ConstructAll;
import client.app.UiClientContext;
import common.state.EntityReader;
import common.state.spec.EntityClasses;

public class Build extends UnitToUnitAction {

    public Build(UiClientContext context) {
        super(context, "Build");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return defaultGuardStatement(entity) && entity.getType().containsClass(EntityClasses.CONSTRUCTOR);
    }

    @Override
    public boolean canRunOn(EntityReader performer, EntityReader target) {
        return target.getType().containsClass(EntityClasses.CONSTRUCTION_ZONE) && target.getConstructionZone() != null;
    }

    @Override
    public void run(EntityReader entity, EntityReader target) {
        c.actionQueuer.maybeQueue(entity, new ConstructAll(entity, target));
    }
}
