package common.util;

import common.state.spec.EntitySpec;
import common.state.sst.sub.capacity.PrioritizedCapacitySpec;
import common.util.json.*;

import java.io.IOException;

public class EvolutionSpec implements Jsonable {
    public PrioritizedCapacitySpec carryCapacity;
    public Double initialLineOfSight;
    public Double initialBaseHealth;
    public Double initialMovementSpeed;
    public Double initialCollectSpeed;
    public Double initialDepositSpeed;
    public Double initialBuildSpeed;
    public Double initialRotationSpeed;
    public Double initialAttackSpeed;
    public Double initialPlantSpeed;
    public Double initialGardenSpeed;
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
        initialPlantSpeed = spec.initialPlantSpeed;
        initialGardenSpeed = spec.initialGardenSpeed;
    }
    
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("los", initialLineOfSight);
        writer.write("health", initialBaseHealth);
        writer.write("movement-speed", initialMovementSpeed);
        writer.write("collect-speed", initialCollectSpeed);
        writer.write("deposit-speed", initialDepositSpeed);
        writer.write("build-speed", initialBuildSpeed);
        writer.write("rotation-speed", initialRotationSpeed);
        writer.write("attack-speed", initialAttackSpeed);
        writer.write("garden-speed", initialGardenSpeed);
        writer.write("plant-speed", initialPlantSpeed);
        writer.writeEndDocument();
    }
    
    public static final DataSerializer<EvolutionSpec> Serializer = new DataSerializer.JsonableSerializer<EvolutionSpec>() {
        public EvolutionSpec parse(JsonReaderWrapperSpec reader, ReadOptions options) throws IOException {
            reader.readBeginDocument();
            EvolutionSpec spec = new EvolutionSpec();
            spec.initialLineOfSight = reader.readDouble("los");
            spec.initialBaseHealth = reader.readDouble("health");
            spec.initialMovementSpeed = reader.readDouble("movement-speed");
            spec.initialCollectSpeed = reader.readDouble("collect-speed");
            spec.initialDepositSpeed = reader.readDouble("deposit-speed");
            spec.initialBuildSpeed = reader.readDouble("build-speed");
            spec.initialRotationSpeed = reader.readDouble("rotation-speed");
            spec.initialAttackSpeed = reader.readDouble("attack-speed");
            spec.initialGardenSpeed = reader.readDouble("garden-speed");
            spec.initialPlantSpeed = reader.readDouble("plant-speed");
            reader.readEndDocument();
            return spec;
        }
    };

    public static EvolutionSpec uniformWeights() {
        EvolutionSpec ret = new EvolutionSpec();
        ret.initialLineOfSight = 1.0;
        ret.initialBaseHealth = 1.0;
        ret.initialMovementSpeed = 1.0;
        ret.initialCollectSpeed = 1.0;
        ret.initialDepositSpeed = 1.0;
        ret.initialBuildSpeed = 1.0;
        ret.initialRotationSpeed = 1.0;
        ret.initialAttackSpeed = 1.0;
        ret.initialGardenSpeed = 1.0;
        ret.initialPlantSpeed = 1.0;
        return ret;
    }
}
