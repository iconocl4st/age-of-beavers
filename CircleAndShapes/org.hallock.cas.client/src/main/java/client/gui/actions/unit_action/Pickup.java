package client.gui.actions.unit_action;

import client.ai.PickUpAi;
import client.app.ClientContext;
import client.ai.ActionRequester;
import client.gui.game.Command;
import common.state.spec.ResourceType;
import common.state.EntityId;
import common.state.EntityReader;
import common.util.DPoint;

public class Pickup extends UnitToUnitAction {

    private final ResourceType resource;

    public Pickup(ClientContext context, ResourceType resource) {
        super(context, "Pickup " + resource);
        this.resource = resource;
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return defaultGuardStatement(entity) && entity.getType().containsClass("carrier") && entity.canAccept(resource);
    }

    @Override
    public boolean canRunOn(EntityReader performer, EntityReader target) {
        return !target.doesNotHave(resource);
    }

    @Override
    public void run(EntityReader entity, EntityReader target) {
        c.actionQueuer.maybeQueue(entity.entityId, new PickUpAi(c, entity.entityId, target.entityId, resource));
    }
}
