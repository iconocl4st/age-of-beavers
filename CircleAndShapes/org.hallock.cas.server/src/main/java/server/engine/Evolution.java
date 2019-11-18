package server.engine;

import common.state.EntityId;
import common.state.EntityReader;
import common.util.EvolutionSpec;
import server.state.ServerGameState;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class Evolution {
    private final Random random;
    private final ServerGameState state;


    public Evolution(ServerGameState gameState, Random random) {
        this.state = gameState;
        this.random = random;
    }


    EvolutionSpec createEvolvedSpec(Set<EntityReader> contributing) {
        if (contributing == null || contributing.isEmpty()) return null;
        EntityReader[] entities = new EntityReader[contributing.size()];

        Iterator<EntityReader> iterator = contributing.iterator();
        for (int i = 0; i < entities.length; i++) {
            entities[i] = iterator.next();
            if (!entities[i].getType().equals(entities[0].getType())) {
                throw new RuntimeException("Different types contributing to creation.");
            }
        }

        EvolutionSpec spec = new EvolutionSpec();
        spec.initialAttackSpeed = entities[random.nextInt(entities.length)].getCurrentAttackSpeed() + 0.1 * random.nextGaussian();
        spec.initialBaseHealth = entities[random.nextInt(entities.length)].getBaseHealth() + 0.1 * random.nextGaussian();
        spec.initialLineOfSight = entities[random.nextInt(entities.length)].getBaseLineOfSight() + 0.1 * random.nextGaussian();
        spec.initialMovementSpeed = entities[random.nextInt(entities.length)].getBaseMovementSpeed() + 0.1 * random.nextGaussian();
        spec.initialCollectSpeed = entities[random.nextInt(entities.length)].getCollectSpeed() + 0.1 * random.nextGaussian();
        spec.initialDepositSpeed = entities[random.nextInt(entities.length)].getDepositSpeed() + 0.1 * random.nextGaussian();
        spec.initialBuildSpeed = entities[random.nextInt(entities.length)].getBuildSpeed() + 0.1 * random.nextGaussian();
        spec.initialRotationSpeed = entities[random.nextInt(entities.length)].getRotationSpeed() + 0.1 * random.nextGaussian();
        return spec;
    }

}
