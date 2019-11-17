package client.gui.actions.unit_action;

import client.ai.ActionRequester;
import client.ai.DeliverAi;
import client.app.ClientContext;
import common.state.EntityReader;

public class Deliver extends UnitToUnitAction {

    public Deliver(ClientContext context) {
        super(context, "Deliver");
    }

    @Override
    public boolean canRunOn(EntityReader performer, EntityReader target) {
        return target.getType().containsClass("storage");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return entity.getType().containsClass("carrier");
    }


    @Override
    public void run(EntityReader entity, EntityReader target) {
        c.actionQueuer.maybeQueue(entity.entityId, new DeliverAi(c, entity.entityId, target.entityId));
    }
}
