package client.gui.actions.unit_action;

import client.ai.ai2.AiTask;
import client.ai.ai2.FarmAi;
import client.app.UiClientContext;
import common.state.EntityReader;
import common.state.spec.EntityClasses;

public class SetFarmingLocation extends UnitToUnitAction {
    public SetFarmingLocation(UiClientContext context) {
        super(context, "Set farming locations");
    }

    @Override
    public boolean canRunOn(EntityReader performer, EntityReader target) {
        return defaultGuardStatement(performer) && target.getType().containsClass(EntityClasses.FARM);
    }

    @Override
    public void run(EntityReader entity, EntityReader target) {
        AiTask aiTask = this.c.clientGameState.aiManager.get(entity);
        if (!(aiTask instanceof FarmAi)) {
            return;
        }
        FarmAi farmAi = (FarmAi) aiTask;
        farmAi.toggleFarming(target);
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        AiTask aiTask = this.c.clientGameState.aiManager.get(entity);
        return defaultGuardStatement(entity) && entity.getType().containsClass(EntityClasses.FARMER) && aiTask instanceof FarmAi;
    }
}
