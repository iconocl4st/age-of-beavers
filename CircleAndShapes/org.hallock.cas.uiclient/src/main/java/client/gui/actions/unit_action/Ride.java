package client.gui.actions.unit_action;

import client.ai.RideAi;
import client.app.UiClientContext;
import common.state.EntityReader;
import common.state.sst.GameStateHelper;

public class Ride extends UnitToUnitAction {
    public Ride(UiClientContext context) {
        super(context,"Ride");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return defaultGuardStatement(entity) && entity.getType().containsClass("rider") && entity.getRiding() == null;
    }

    @Override
    public boolean canRunOn(EntityReader performer, EntityReader target) {
        return GameStateHelper.playerCanRide(c.clientGameState.currentPlayer, performer, target);
    }

    @Override
    public void run(EntityReader entity, EntityReader target) {
        c.actionQueuer.maybeQueue(entity.entityId, new RideAi(c.clientGameState, entity, target));
    }
}
