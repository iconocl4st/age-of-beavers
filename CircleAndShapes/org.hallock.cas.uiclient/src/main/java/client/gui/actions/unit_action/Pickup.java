package client.gui.actions.unit_action;

import client.ai.ai2.OneTripTransportFrom;
import client.ai.ai2.OneTripTransportTo;
import client.app.UiClientContext;
import common.state.spec.ResourceType;
import common.state.EntityReader;
import common.util.MapUtils;

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
        c.actionQueuer.maybeQueue(entity, new OneTripTransportFrom(entity, target, MapUtils.from(resource, Integer.MAX_VALUE)));
    }
}
