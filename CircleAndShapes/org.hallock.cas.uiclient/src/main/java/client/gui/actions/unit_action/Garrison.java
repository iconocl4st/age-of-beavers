package client.gui.actions.unit_action;

import client.ai.BeRidden;
import client.ai.GarrisonAi;
import client.app.UiClientContext;
import common.state.EntityReader;
import common.state.sst.GameStateHelper;

public class Garrison extends UnitToUnitAction {

    public Garrison(UiClientContext context) {
        super(context, "Enter");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return defaultGuardStatement(entity) && (
               entity.getType().containsClass("can-garrison-in-others") ||
               entity.getType().containsClass("ridable")
        );
    }

    @Override
    public boolean canRunOn(EntityReader performer, EntityReader target) {
        return target.getType().containsClass("can-garrison-others") || target.getType().containsClass("rider");
    }

    @Override
    public void run(EntityReader entity, EntityReader target) {
        if (GameStateHelper.playerCanGarrison(c.clientGameState.currentPlayer, entity, target)) {
            c.actionQueuer.maybeQueue(entity.entityId, new GarrisonAi(c.clientGameState, entity, target));
        } else if (GameStateHelper.playerCanRide(c.clientGameState.currentPlayer, target, entity)) {
            c.actionQueuer.maybeQueue(entity.entityId, new BeRidden(c.clientGameState, entity, target));
        }
    }
}
