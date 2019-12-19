package client.gui.actions.unit_action;

import client.ai.ai2.WhileWithinProximity;
import client.app.UiClientContext;
import common.state.EntityReader;
import common.state.spec.EntityClasses;
import common.state.sst.GameStateHelper;

public class Garrison extends UnitToUnitAction {

    public Garrison(UiClientContext context) {
        super(context, "Enter");
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        return defaultGuardStatement(entity) && (
                entity.getType().containsAnyClass(EntityClasses.CAN_GARRISON, EntityClasses.RIDABLE)
        );
    }

    @Override
    public boolean canRunOn(EntityReader performer, EntityReader target) {
        return target.getType().containsAnyClass(EntityClasses.GARRISONS_OTHERS, EntityClasses.RIDER);
    }

    @Override
    public void run(EntityReader entity, EntityReader target) {
        if (GameStateHelper.playerCanGarrison(c.clientGameState.currentPlayer, entity, target)) {
            c.actionQueuer.maybeQueue(entity, WhileWithinProximity.createGarrison(entity, target));
        } else if (GameStateHelper.playerCanRide(c.clientGameState.currentPlayer, target, entity)) {
            c.actionQueuer.maybeQueue(entity, WhileWithinProximity.createBeRidden(entity, target));
        }
    }
}
