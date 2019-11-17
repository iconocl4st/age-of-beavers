package common.util;

import common.state.spec.EntitySpec;
import common.state.sst.sub.capacity.CapacitySpec;

public class EvolutionSpec implements Jsonable {
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
    
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) {
        writer.writeBeginDocument();
        writer.write("los", initialLineOfSight);
        writer.write("health", initialHealth);
        writer.write("movement-speed", initialMovementSpeed);
        writer.write("collect-speed", initialCollectSpeed);
        writer.write("deposit-speed", initialDepositSpeed);
        writer.write("build-speed", initialBuildSpeed);
        writer.write("rotation-speed", initialRotationSpeed);
        writer.write("attack-speed", initialAttackSpeed);
        writer.writeEndDocument();
    }
    
    public static final DataSerializer<EvolutionSpec> Serializer = new DataSerializer.JsonableSerializer {
        public void EvolutionSpec parse(JsonReaderWrapperSpec reader, ReadOptions options) {
            EvolutionSpec spec = new EvolutionSpec();
            writer.write("los", initialLineOfSight);
            writer.write("health", initialHealth);
            writer.write("movement-speed", initialMovementSpeed);
            writer.write("collect-speed", initialCollectSpeed);
            writer.write("deposit-speed", initialDepositSpeed);
            writer.write("build-speed", initialBuildSpeed);
            writer.write("rotation-speed", initialRotationSpeed);
            writer.write("attack-speed", initialAttackSpeed);
            return spec;
        }
    }
    
    public static EvolutionSpec uniformWeights() {
        EvolutionSpec ret = new EvolutionSpec();
        initialLineOfSight = spec.initialLineOfSight;
        initialBaseHealth = spec.initialBaseHealth;
        carryCapacity = spec.carryCapacity;
        initialMovementSpeed = spec.initialMovementSpeed;
        initialCollectSpeed = spec.initialCollectSpeed;
        initialDepositSpeed = spec.initialDepositSpeed;
        initialBuildSpeed = spec.initialBuildSpeed;
        initialRotationSpeed = spec.initialRotationSpeed;
        initialAttackSpeed = 1.0;
        return ret;
        }
}
