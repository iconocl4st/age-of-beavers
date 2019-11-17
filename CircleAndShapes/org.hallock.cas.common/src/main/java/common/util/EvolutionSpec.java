package common.util;

import common.state.spec.EntitySpec;
import common.state.sst.sub.capacity.CapacitySpec;

public class EvolutionSpec {
    public double initialLineOfSight;
    public double initialBaseHealth;
    public CapacitySpec carryCapacity;
    public double initialMovementSpeed;
    public double initialCollectSpeed;
    public double initialDepositSpeed;
    public double initialBuildSpeed;
    public double initialRotationSpeed;
    public double initialAttackSpeed;
//        public List<EntitySpec> dropOnDeath;

    public EvolutionSpec() {}

    public EvolutionSpec(EntitySpec spec) {
        initialLineOfSight = spec.initialLineOfSight;
        initialBaseHealth = spec.initialBaseHealth;
        carryCapacity = spec.carryCapacity;
        initialMovementSpeed = spec.initialMovementSpeed;
        initialCollectSpeed = spec.initialCollectSpeed;
        initialDepositSpeed = spec.initialDepositSpeed;
        initialBuildSpeed = spec.initialBuildSpeed;
        initialRotationSpeed = spec.initialRotationSpeed;
        initialAttackSpeed = spec.initialAttackSpeed;
    }
}
