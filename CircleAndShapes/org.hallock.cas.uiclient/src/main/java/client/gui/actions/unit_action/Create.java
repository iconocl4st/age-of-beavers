package client.gui.actions.unit_action;

import client.app.UiClientContext;
import common.state.EntityReader;
import common.state.spec.CreationSpec;

public class Create extends UnitAction {

    private final CreationSpec spec;

    public Create(UiClientContext context, CreationSpec spec) {
        super(context, "Create " + spec.createdType.name + " once");
        this.spec = spec;
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        if (!defaultGuardStatement(entity))
            return false;
        if (!entity.getType().canCreate.anyMatch(spec::equals))
            return false;
        if (!entity.getCarrying().canAfford(spec.requiredResources))
            return false;
        return entity.isIdle() && !c.clientGameState.aiManager.isControlling(entity);
    }

    @Override
    public void run(EntityReader entity) {
        ar.setUnitActionToCreate(entity, spec);
    }
}
