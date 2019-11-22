package common.state.spec;

import common.state.spec.attack.WeaponSpec;
import common.state.spec.attack.Weapons;
import common.util.json.*;

import java.io.IOException;
import java.util.*;

public class GameSpec implements Jsonable {

    public enum VisibilitySpec {
        FOG,
        EXPLORED,
        ALL_VISIBLE,
    }

    public int width;
    public int height;
    public int numPlayers;
    public ResourceType[] resourceTypes;
    public EntitySpec[] naturalResources;
    public EntitySpec[] unitSpecs;
    public GenerationSpec generationSpec;
    public WeaponSpec[] weaponTypes = new WeaponSpec[] {
            Weapons.Sword,
            Weapons.Bow,
            Weapons.Fist,
            Weapons.LaserGun,
            Weapons.Rifle,
    };
    public VisibilitySpec visibility = VisibilitySpec.ALL_VISIBLE;

    public ResourceType getResourceType(String name) {
        for (ResourceType rt : resourceTypes) {
            if (rt.name.equals(name)) {
                return rt;
            }
        }
        return null;
    }
    public EntitySpec getNaturalResource(String name) {
        for (EntitySpec rt : naturalResources) {
            if (rt.name.equals(name)) {
                return rt;
            }
        }
        return null;
    }
    public EntitySpec getUnitSpec(String name) {
        for (EntitySpec rt : unitSpecs) {
            if (rt.name.equals(name)) {
                return rt;
            }
        }
        return null;
    }

    public WeaponSpec getWeaponSpec(String s) {
        for (WeaponSpec rt : weaponTypes) {
            if (rt.name.equals(s)) {
                return rt;
            }
        }
        return null;
    }

    public Set<EntitySpec> getUnitSpecsByClass(String clazz) {
        Set<EntitySpec> ret = new HashSet<>();
        for (EntitySpec rt : unitSpecs) {
            if (rt.containsClass(clazz)) {
                ret.add(rt);
            }
        }
        return ret;
    }

    public static class BuildingPathNode {
        public final Map<String, BuildingPathNode> children = new TreeMap<>();
        public final List<EntitySpec> buildings = new LinkedList<>();
    }
    public BuildingPathNode compileBuildingPaths() {
        BuildingPathNode root = new BuildingPathNode();
        for (EntitySpec spec : unitSpecs) {
            if (spec.buildingPath == null)
                continue;
            BuildingPathNode node = root;
            for (String str : spec.buildingPath) {
                node = node.children.computeIfAbsent(str, e -> new BuildingPathNode());
            }
            node.buildings.add(spec);
        }
        return root;
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("width", width);
        writer.write("height", height);
        writer.write("num-players", numPlayers);
        writer.write("resource-types", resourceTypes, ResourceType.EntireSerializer, options);
        writer.write("natural-resource-types", naturalResources, EntitySpec.IgnoreCanCreateSerializer, options);
        writer.write("unit-types", unitSpecs, EntitySpec.IgnoreCanCreateSerializer, options);

        writer.writeBeginArray("creations");
        for (EntitySpec entitySpec : unitSpecs) {
            writer.writeBeginDocument();
            writer.write("creator", entitySpec, EntitySpec.Serializer, options);
            writer.write("created", entitySpec.canCreate, CreationSpec.Serializer, options);
            writer.writeEndDocument();
        }
        writer.writeEndArray();

        writer.write("weapon-types", weaponTypes, WeaponSpec.Serializer, options);
        writer.write("generator", generationSpec, GenerationSpec.Serializer, options);
        writer.write("visibility", visibility.ordinal());
        writer.writeEndDocument();
    }

    public static DataSerializer<GameSpec> Serializer = new DataSerializer.JsonableSerializer<GameSpec>() {
        @Override
        public GameSpec parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            GameSpec ret = new GameSpec();
            spec.spec = ret;
            reader.readBeginDocument();
            ret.width = reader.readInt32("width");
            ret.height = reader.readInt32("height");
            ret.numPlayers = reader.readInt32("num-players");
            ret.resourceTypes = reader.read("resource-types", new ResourceType[0], ResourceType.EntireSerializer, spec);
            ret.naturalResources = reader.read("natural-resource-types", new EntitySpec[0], EntitySpec.IgnoreCanCreateSerializer, spec);
            ret.unitSpecs = reader.read("unit-types", new EntitySpec[0], EntitySpec.IgnoreCanCreateSerializer, spec);

            reader.readBeginArray("creations");
            while (reader.hasMoreInArray()) {
                reader.readBeginDocument();
                EntitySpec creator = reader.read("creator", EntitySpec.Serializer, spec);
                reader.read("created", creator.canCreate, CreationSpec.Serializer, spec);
                reader.readEndDocument();
            }
            reader.readEndArray();

            ret.weaponTypes = reader.read("weapon-types", new WeaponSpec[0], WeaponSpec.Serializer, spec);
            ret.generationSpec = reader.read("generator", GenerationSpec.Serializer, spec);
            ret.visibility = reader.b(VisibilitySpec.values(), reader.readInt32("visibility"));
            reader.readEndDocument();
            return ret;
        }
    };
}
