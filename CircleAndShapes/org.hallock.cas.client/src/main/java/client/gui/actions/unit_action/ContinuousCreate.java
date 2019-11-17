package client.gui.actions.unit_action;

import client.ai.ConstructAi;
import client.ai.CreateAi;
import client.app.ClientContext;
import client.ai.ActionRequester;
import common.state.spec.CreationSpec;
import common.state.EntityReader;

public class ContinuousCreate extends UnitAction {

    private final CreationSpec spec;

    public ContinuousCreate(ClientContext context, CreationSpec spec) {
        super(context, "Cont. create " + spec.createdType.name);
        this.spec = spec;
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return defaultGuardStatement(entity) && entity.isIdle() && !c.aiManager.isControlling(entity) && entity.getType().canCreate.contains(spec);
    }

    @Override
    public void run(EntityReader entity) {
        c.actionQueuer.maybeQueue(entity.entityId, new CreateAi(c, entity.entityId, spec));
    }
}
