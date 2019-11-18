package common.state.spec;

import common.state.sst.sub.capacity.PrioritizedCapacitySpec;
import common.util.json.*;

import java.awt.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.List;

public class EntitySpec implements Serializable {
    public final String image;
    public final String name;
    public Dimension size;
    public List<EntitySpec> dropOnDeath;
    public double initialBaseHealth;
    public PrioritizedCapacitySpec carryCapacity;
    public int garrisonCapacity;
    public double initialMovementSpeed;
    // TODO collection speed depends on where
    public double initialLineOfSight;
    public double initialCollectSpeed;
    public double initialDepositSpeed;
    public double initialRotationSpeed;
    public double initialAttackSpeed;
    public double initialBuildSpeed;
    public Set<String> classes;
    public String ai;
    public Map<String, String> aiArgs;
    public Map<ResourceType, Integer> requiredResources;
    public Set<CreationSpec> canCreate; // does not have a hash code...
    public double creationTime;
    public Set<CarrySpec> carrying;
    public String[] buildingPath;

    public Color minimapColor;

    public EntitySpec(String name, String image) {
        this.image = image;
        this.name = name;
    }

    public boolean equals(Object other) {
        if (!(other instanceof EntitySpec)) {
            return false;
        }
        return name.equals(((EntitySpec) other).name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return name;
    }

    public boolean containsAnyClass(Set<String> classTypes) {
        for (String classType : classTypes) {
            if (containsClass(classType))
                return true;
        }
        return false;
    }

    public boolean canHaveGatherPoint() {
        return containsClass("can-garrison-others") || !canCreate.isEmpty();
    }


    public boolean containsClass(String clazz) {
        return classes.contains(clazz);
    }


    public EntitySpec createConstructionSpec(GameSpec gSpec) {
        ConstructionSpec spec = new ConstructionSpec(this, "construction-zone", "unit/construction.png");
        spec.size = size;
        spec.initialBaseHealth = 0;
        HashMap<ResourceType, Integer> carryLimits = new HashMap<>();
        for (Map.Entry<ResourceType, Integer> entry : requiredResources.entrySet()) {
            carryLimits.put(entry.getKey(), entry.getValue());
        }
        spec.carryCapacity = PrioritizedCapacitySpec.createCapacitySpec(gSpec, carryLimits, true);
        spec.initialMovementSpeed = 0;
        spec.initialLineOfSight= 0;
        spec.initialDepositSpeed = 0;
        spec.initialCollectSpeed = 0;
        spec.classes = new HashSet<>();
        spec.classes.add("storage");
        spec.classes.add("construction-zone");
        spec.classes.add("visible-in-fog");
        spec.classes.add("unit");
        spec.ai = null;
        spec.requiredResources = Collections.emptyMap();
        spec.canCreate = Collections.emptySet();
        spec.dropOnDeath = Collections.emptyList();
        spec.carrying = Collections.emptySet();
        return spec;
    }

    public static DataSerializer<EntitySpec> IgnoreCanCreateSerializer = new DataSerializer<EntitySpec>() {
        @Override
        public void write(EntitySpec value, JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.writeBeginDocument();
            writer.write("name", value.name);
            writer.write("image", value.image);
            writer.write("client.ai", value.ai);

            writer.write("size", value.size,  DataSerializer.DimensionSerializer, options);

            writer.write("line-of-sight",  value.initialLineOfSight);
            writer.write("initial-initialBaseHealth", value.initialBaseHealth);
            writer.write("initial-movement-speed", value.initialMovementSpeed);
            writer.write("initial-collect-speed", value.initialCollectSpeed);
            writer.write("initial-deposit-speed", value.initialDepositSpeed);
            writer.write("initial-rotation-speed", value.initialRotationSpeed);
            writer.write("initial-attack-speed", value.initialAttackSpeed);
            writer.write("initial-build-speed", value.initialBuildSpeed);
            writer.write("creation-time",  value.creationTime);

            writer.write("garrison-capacity", value.garrisonCapacity);

            writer.write("drop-on-death", value.dropOnDeath, EntitySpec.Serializer, options);
            writer.write("classes", value.classes, DataSerializer.StringSerializer, options);

            writer.write("carry-capacity", value.carryCapacity, PrioritizedCapacitySpec.Serializer, options);

            writer.write("required-resources", value.requiredResources, ResourceType.Serializer, DataSerializer.IntegerSerializer, options);
            writer.write("client.ai-arguments",  value.aiArgs, DataSerializer.StringSerializer, DataSerializer.StringSerializer, options);
            writer.write("build-path", value.buildingPath, DataSerializer.StringSerializer, options);

//            writer.write("can-create", canCreate, CreationSpec.Serializer, options);
            writer.write("carrying", value.carrying, CarrySpec.Serializer, options);

            writer.writeEndDocument();
        }

        @Override
        public EntitySpec parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            String name = reader.readString("name");
            String image = reader.readString("image");
            EntitySpec ret = new EntitySpec(name, image);
            ret.dropOnDeath = new LinkedList<>();
            ret.classes = new HashSet<>();
            ret.requiredResources = new HashMap<>();
            ret.aiArgs = new HashMap<>();
            ret.canCreate = new HashSet<>();
            ret.carrying = new HashSet<>();

            ret.ai = reader.readString("client.ai");
            ret.size = reader.read("size", DataSerializer.DimensionSerializer, spec);
            ret.initialLineOfSight = reader.readDouble("line-of-sight");
            ret.initialBaseHealth = reader.readDouble("initial-initialBaseHealth");
            ret.initialMovementSpeed = reader.readDouble("initial-movement-speed");
            ret.initialCollectSpeed = reader.readDouble("initial-collect-speed");
            ret.initialDepositSpeed = reader.readDouble("initial-deposit-speed");
            ret.initialRotationSpeed = reader.readDouble("initial-rotation-speed");
            ret.initialAttackSpeed = reader.readDouble("initial-attack-speed");
            ret.initialBuildSpeed = reader.readDouble("initial-build-speed");
            ret.creationTime = reader.readDouble("creation-time");
            ret.garrisonCapacity = reader.readInt32("garrison-capacity");
            reader.read("drop-on-death", spec, ret.dropOnDeath, EntitySpec.Serializer);
            reader.read("classes", spec, ret.classes, DataSerializer.StringSerializer);
            ret.carryCapacity = reader.read("carry-capacity", PrioritizedCapacitySpec.Serializer, spec);
            reader.read("required-resources", ret.requiredResources, ResourceType.Serializer, DataSerializer.IntegerSerializer, spec);
            reader.read("client.ai-arguments", ret.aiArgs, DataSerializer.StringSerializer, DataSerializer.StringSerializer, spec);
            ret.buildingPath = reader.read("build-path", new String[0], DataSerializer.StringSerializer, spec);
//            reader.read("can-create", spec, ret.canCreate, CreationSpec.Serializer);
            ret.canCreate = new HashSet<>();
            reader.read("carrying", spec, ret.carrying, CarrySpec.Serializer);
            reader.readEndDocument();
            return ret;
        }
    };


    public static DataSerializer<EntitySpec> Serializer = new DataSerializer<EntitySpec>() {
        @Override
        public void write(EntitySpec value, JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            if (value instanceof ConstructionSpec) {
                writer.writeBeginDocument();
                writer.write("is-construction-spec", true);
                writer.write("resulting-structure", ((ConstructionSpec) value).resultingStructure.name);
                writer.writeEndDocument();
                return;
            }

            writer.writeBeginDocument();
            writer.write("is-construction-spec", false);
            writer.write("name", value.name);
            writer.writeEndDocument();
        }

        @Override
        public EntitySpec parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            boolean isConstruction = reader.readBoolean("is-construction-spec");
            if  (isConstruction) {
                String resultingStructureName = reader.readString("resulting-structure");
                reader.readEndDocument();
                return spec.spec.getUnitSpec(resultingStructureName).createConstructionSpec(spec.spec);
            }
            String name = reader.readString("name");
            reader.readEndDocument();

            EntitySpec ret = spec.spec.getNaturalResource(name);
            if (ret != null) return ret;
            return spec.spec.getUnitSpec(name);
        }
    };
}
