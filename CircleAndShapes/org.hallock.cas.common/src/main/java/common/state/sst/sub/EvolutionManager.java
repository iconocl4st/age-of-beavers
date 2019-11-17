package client.state;

import common.state.EntityId;
import common.util.EvolutionSpec;

import java.util.HashMap;

public class EvolutionManager {
    private HashMap<EntityId, EvolutionSpec> weights = new HashMap<>();

    public EvolutionSpec getCurrentWeights(EntityId entityId) {
        return weights.get(entityId);
    }
}
