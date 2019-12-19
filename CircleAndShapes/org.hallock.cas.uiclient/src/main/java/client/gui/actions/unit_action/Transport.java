package client.gui.actions.unit_action;

import client.ai.ai2.TransportAi;
import client.app.UiClientContext;
import common.state.EntityReader;
import common.state.spec.EntityClasses;

public class Transport extends UnitAction {

    public Transport(UiClientContext context) {
        super(context, "Transport");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return defaultGuardStatement(entity) && entity.getType().containsClass(EntityClasses.CARRIER);
    }

    @Override
    public void run(EntityReader entity) {
        c.actionQueuer.maybeQueue(entity, new TransportAi(entity));
    }
}
