package server.engine;

import common.state.EntityReader;
import common.util.EvolutionSpec;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import server.state.ServerGameState;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class Evolution {
    private final Random random;
    private final ServerGameState state;
    private final RealDistribution distribution;


    public Evolution(ServerGameState gameState, Random random) {
        this.state = gameState;
        this.random = random;

        // probably biased...
        this.distribution = new LogNormalDistribution(Math.log(1), 0.1);
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
        spec.initialAttackSpeed = entities[random.nextInt(entities.length)].getCurrentAttackSpeed() * distribution.sample();
        spec.initialBaseHealth = entities[random.nextInt(entities.length)].getBaseHealth() * distribution.sample();
        spec.initialLineOfSight = entities[random.nextInt(entities.length)].getLineOfSight() * distribution.sample();
        spec.initialMovementSpeed = entities[random.nextInt(entities.length)].getBaseMovementSpeed() * distribution.sample();
        spec.initialCollectSpeed = entities[random.nextInt(entities.length)].getCollectSpeed() * distribution.sample();
        spec.initialDepositSpeed = entities[random.nextInt(entities.length)].getDepositSpeed() * distribution.sample();
        spec.initialBuildSpeed = entities[random.nextInt(entities.length)].getBuildSpeed() * distribution.sample();
        spec.initialRotationSpeed = entities[random.nextInt(entities.length)].getRotationSpeed() * distribution.sample();
        return spec;
    }

}
