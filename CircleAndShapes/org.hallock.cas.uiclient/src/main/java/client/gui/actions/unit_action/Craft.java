package client.gui.actions.unit_action;

import client.app.UiClientContext;
import common.state.EntityReader;
import common.state.spec.CraftingSpec;
import common.state.spec.CreationSpec;
import common.state.spec.ResourceType;
import common.util.MapUtils;

import java.util.Map;

public class Craft extends UnitAction {

    private final CraftingSpec spec;

    public Craft(UiClientContext context, CraftingSpec spec) {
        super(context, "Craft");
        this.spec = spec;
    }

    @Override
    public boolean isEnabled(EntityReader entity) {
        if (!defaultGuardStatement(entity))
            return false;
        if (!entity.getCarrying().canAfford(spec.inputs))
            return false;

        Map<ResourceType, Integer> outputAmounts =
            MapUtils.add(MapUtils.subtract(MapUtils.copy(entity.getCarrying().quantities), spec.inputs), spec.outputs);
        // has to be able to hold the resulting outputs.
        return entity.isIdle() && !c.clientGameState.aiManager.isControlling(entity);
    }

    @Override
    public void run(EntityReader entity) {
        ar.craft(entity, spec);
    }
}
