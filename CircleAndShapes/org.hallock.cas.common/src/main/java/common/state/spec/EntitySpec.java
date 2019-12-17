package common.state.spec;

import common.state.sst.sub.capacity.PrioritizedCapacitySpec;
import common.util.Immutable;
import common.util.json.*;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class EntitySpec {
    public final String graphicsImage;
    public final String name;
    public final Dimension size;
    public final Double initialBaseHealth;
    public final PrioritizedCapacitySpec carryCapacity;
    public final Integer garrisonCapacity;
    public final Double initialMovementSpeed;
    // TODO collection speed depends on where
    public final Double initialLineOfSight;
    public final Double initialCollectSpeed;
    public final Double initialDepositSpeed;
    public final Double initialRotationSpeed;
    public final Double initialAttackSpeed;
    public final Double initialBuildSpeed;
    public final String ai;
    public final Immutable.ImmutableMap<String, String> aiArgs;
    public final Immutable.ImmutableSet<String> classes;
    public final Immutable.ImmutableMap<ResourceType, Integer> carrying;
    public final EntitySpec resultingStructure;
    public final Color minimapColor;

    // immutable versions?
    public /* final */ SpecTree<CreationSpec> canCreate = new SpecTree<>();
    public /* final */ SpecTree<CraftingSpec> canCraft = new SpecTree<>();

    public final List<EntitySpec> dropOnDeath = new LinkedList<>();


    // TODO:

    public EntitySpec(
            String name,
            String graphicsImage,
            Dimension size,
            Double initialBaseHealth,
            PrioritizedCapacitySpec carryCapacity,
            Integer garrisonCapacity,
            Double initialMovementSpeed,
            Double initialLineOfSight,
            Double initialCollectSpeed,
            Double initialDepositSpeed,
            Double initialRotationSpeed,
            Double initialAttackSpeed,
            Double initialBuildSpeed,
            String ai,
            Immutable.ImmutableMap<String, String> aiArgs,
            Immutable.ImmutableSet<String> classes,
            Immutable.ImmutableMap<ResourceType, Integer> carrying,
            EntitySpec resultingStructure,
            Color minimapColor
    ) {
        if (size == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.graphicsImage = graphicsImage;
        this.size = size;
        this.initialBaseHealth = initialBaseHealth;
        this.carryCapacity = carryCapacity;
        this.garrisonCapacity = garrisonCapacity;
        this.initialMovementSpeed = initialMovementSpeed;
        this.initialLineOfSight = initialLineOfSight;
        this.initialCollectSpeed = initialCollectSpeed;
        this.initialDepositSpeed = initialDepositSpeed;
        this.initialRotationSpeed = initialRotationSpeed;
        this.initialAttackSpeed = initialAttackSpeed;
        this.initialBuildSpeed = initialBuildSpeed;
        this.ai = ai;
        this.aiArgs = aiArgs;
        this.classes = classes;
        this.carrying = carrying;
        this.resultingStructure = resultingStructure;
        this.minimapColor = minimapColor;
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

    public boolean canHaveGatherPoint() {
        return containsClass("can-garrison-others") || canCreate.isNotEmpty();
    }

    public boolean containsClass(String clazz) {
        return classes.contains(clazz);
    }

    public boolean containsAnyClass(String... classTypes) {
        for (String classType : classTypes) {
            if (containsClass(classType))
                return true;
        }
        return false;
    }


    public static PrioritizedCapacitySpec getConstructionCarryCapacity(Map<ResourceType, Integer> requiredResource){
        HashMap<ResourceType, Integer> carryLimits = new HashMap<>();
        for (Map.Entry<ResourceType, Integer> entry : requiredResource.entrySet())
            carryLimits.put(entry.getKey(), entry.getValue());
        return PrioritizedCapacitySpec.createCapacitySpec(carryLimits, true, false);
    }


    public EntitySpec createConstructionSpec(PrioritizedCapacitySpec carryCapacity) {
        return new EntitySpec(
            name + " construction zone",
            "unit/construction.png", // TODO
            size,
            0d,
            carryCapacity,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            Immutable.ImmutableMap.emptyMap(),
            Immutable.ImmutableSet.from("storage", "construction-zone", "visible-in-fog", "unit"), // blocks buildings...
            Immutable.ImmutableMap.emptyMap(),
            this,
            minimapColor
        );
    }

    public static DataSerializer<EntitySpec> Serializer = new DataSerializer<EntitySpec>() {
        @Override
        public void write(EntitySpec value, JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            if (value.containsClass("construction-zone")) {
                writer.writeBeginDocument();
                writer.write("is-construction-spec", true);
                writer.write("resulting-structure", value.resultingStructure.name);
                writer.write("capacity", value.carryCapacity, PrioritizedCapacitySpec.Serializer, options);
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
                PrioritizedCapacitySpec carryCapacity = reader.read("capacity", PrioritizedCapacitySpec.Serializer, spec);
                reader.readEndDocument();
                return spec.spec().getUnitSpec(resultingStructureName).createConstructionSpec(carryCapacity);
            }
            String name = reader.readString("name");
            reader.readEndDocument();

            return spec.spec().getUnitSpec(name);
        }
    };


































































/*





    public static DataSerializer<EntitySpec> IgnoreCanCreateSerializer = new DataSerializer<EntitySpec>() {
        @Override
        public void write(EntitySpec value, JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.writeBeginDocument();
            writer.write("name", value.name);
            writer.write("image", value.graphicsImage);
            writer.write("ai", value.ai);

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
            writer.write("build-points", value.buildingPath, DataSerializer.StringSerializer, options);

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
            reader.read("drop-on-death", ret.dropOnDeath, EntitySpec.Serializer, spec);
            reader.read("classes", ret.classes, DataSerializer.StringSerializer, spec);
            ret.carryCapacity = reader.read("carry-capacity", PrioritizedCapacitySpec.Serializer, spec);
            reader.read("required-resources", ret.requiredResources, ResourceType.Serializer, DataSerializer.IntegerSerializer, spec);
            reader.read("client.ai-arguments", ret.aiArgs, DataSerializer.StringSerializer, DataSerializer.StringSerializer, spec);
            ret.buildingPath = reader.read("build-points", new String[0], DataSerializer.StringSerializer, spec);
//            reader.read("can-create", spec, ret.canCreate, CreationSpec.Serializer);
            ret.canCreate = new HashSet<>();
            reader.read("carrying", ret.carrying, CarrySpec.Serializer, spec);
            reader.readEndDocument();
            return ret;
        }
    };

    */
}
