package client.gui.actions.unit_action;

import client.ai.ai2.Produce;
import client.app.UiClientContext;
import common.state.EntityReader;
import common.state.spec.CreationSpec;

public class ContinuousCreate extends UnitAction {

    private final CreationSpec spec;

    public ContinuousCreate(UiClientContext context, CreationSpec spec) {
        super(context, "Cont. create " + spec.createdType.name);
        this.spec = spec;
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return defaultGuardStatement(entity) && entity.isIdle() && !c.clientGameState.aiManager.isControlling(entity) && entity.getType().canCreate.anyMatch(spec::equals);
    }

    @Override
    public void run(EntityReader entity) {
        c.actionQueuer.maybeQueue(entity, new Produce(entity, spec));
    }
}
