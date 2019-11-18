package client.gui.actions.unit_action;

import client.app.UiClientContext;
import common.state.spec.ResourceType;
import common.state.EntityReader;

public class Pickup extends UnitToUnitAction {

    private final ResourceType resource;

    public Pickup(UiClientContext context, ResourceType resource) {
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
        c.actionQueuer.maybeQueue(entity.entityId, new client.ai.PickUpAi(c.clientGameState, entity, target, resource));
    }
}