package client.gui.actions.unit_action;

import client.ai.BeRidden;
import client.ai.GarrisonAi;
import client.app.ClientContext;
import client.ai.ActionRequester;
import client.gui.game.Command;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.sst.GameStateHelper;
import common.util.DPoint;

public class Garrison extends UnitToUnitAction {

    public Garrison(ClientContext context) {
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
        if (GameStateHelper.playerCanGarrison(c.gameState, c.currentPlayer, entity.entityId, target.entityId)) {
            c.actionQueuer.maybeQueue(entity.entityId, new GarrisonAi(c, entity.entityId, target.entityId));
        } else if (GameStateHelper.playerCanRide(c.gameState, c.currentPlayer, target.entityId, entity.entityId)) {
            c.actionQueuer.maybeQueue(entity.entityId, new BeRidden(c, entity.entityId, target.entityId));
        }
    }
}
