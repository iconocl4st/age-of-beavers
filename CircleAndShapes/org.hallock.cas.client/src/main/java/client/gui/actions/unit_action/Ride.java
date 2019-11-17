package client.gui.actions.unit_action;

import client.ai.RideAi;
import client.app.ClientContext;
import client.ai.ActionRequester;
import client.gui.game.Command;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.sst.GameStateHelper;
import common.util.DPoint;

import java.util.Collection;

public class Ride extends UnitToUnitAction {
    public Ride(ClientContext context) {
        super(context,"Ride");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return defaultGuardStatement(entity) && entity.getType().containsClass("rider") && entity.getRiding() == null;
    }

    @Override
    public boolean canRunOn(EntityReader performer, EntityReader target) {
        return GameStateHelper.playerCanRide(c.gameState, c.currentPlayer, performer.entityId, target.entityId);
    }

    @Override
    public void run(EntityReader entity, EntityReader target) {
        c.actionQueuer.maybeQueue(entity.entityId, new RideAi(c, entity.entityId, target.entityId));
    }
}
