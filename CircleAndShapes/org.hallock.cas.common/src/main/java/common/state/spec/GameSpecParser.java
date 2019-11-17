package common.state.spec;

import common.state.spec.attack.DamageType;
import common.state.spec.attack.ProjectileSpec;
import common.state.spec.attack.WeaponClass;
import common.state.spec.attack.WeaponSpec;
import common.state.sst.sub.capacity.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameSpecParser {
    private static JSONObject getJson(Path path) throws IOException, ParseException {
        try (FileReader fileReader = new FileReader(path.toFile());) {
            return (JSONObject) new JSONParser().parse(fileReader);
        }
    }

    // read


    private static Dimension parseDimension(JSONObject dimension) {
        return new Dimension((int)((long) dimension.get("width")), (int)((long) dimension.get("height")));
    }
    private static ResourceType parseResource(JSONObject object) {
        return new ResourceType(
                (String) object.get("name"),
                (int)(long) object.get("weight")
        );
    }

    ///

    private static void parseTop(JSONObject object, GameSpec spec) {
        Dimension size = parseDimension((JSONObject) object.get("size"));
        spec.width = size.width;
        spec.height = size.height;
    }

    private static void parseResources(Path location, GameSpec spec) throws IOException, ParseException {
        JSONArray resourceTypes = (JSONArray) getJson(location.resolve("resource_types.json")).get("resource-types");
        LinkedList<ResourceType> parsedResources = new LinkedList<>();
        for (int i = 0; i < resourceTypes.size(); i++) {
            JSONObject resourceType = (JSONObject) resourceTypes.get(i);
            parsedResources.add(new ResourceType(
                    (String) resourceType.get("name"),
                    (int)(long) resourceType.get("weight")
            ));
        }
        spec.resourceTypes = parsedResources.toArray(new ResourceType[0]);
    }

    private static void parseWeapons(Path location, GameSpec spec) throws IOException, ParseException {
        JSONArray weaponTypes = (JSONArray) getJson(location.resolve("weapons.json")).get("weapons");
        LinkedList<WeaponSpec> weaponSpecs = new LinkedList<>();
        for (int i = 0; i < weaponTypes.size(); i++) {
            JSONObject weaponType = (JSONObject) weaponTypes.get(i);
            String name = (String) weaponType.get("name");
            WeaponClass wclazz = WeaponClass.valueOf((String) weaponType.get("weapon-class"));
            DamageType dclazz = DamageType.valueOf((String) weaponType.get("damage-type"));
            double damage = (double) weaponType.get("damage");
            double innerRadius = (double) weaponType.get("inner-range");
            double outerRadius = (double) weaponType.get("outer-range");
            double coolDownTime = (double) weaponType.get("cooldown-time");
            double attackTime = (double) weaponType.get("attack-time");
            double conditionDecremnt = (double) weaponType.get("condition-decrement");

            HashMap<ResourceType, Integer> requiredResources = new HashMap<>();
            JSONArray requiredResourcesArray = (JSONArray) weaponType.get("required-resources");
            for (int j = 0; j < requiredResourcesArray.size(); j++) {
                JSONObject jsonObject = (JSONObject) requiredResourcesArray.get(j);
                requiredResources.put(spec.getResourceType(
                        (String) jsonObject.get("resource")),
                        (int)(long) jsonObject.get("amount")
                );
            }
            HashMap<ResourceType, Integer> fireResources = new HashMap<>();
            JSONArray fireResourcesArray = (JSONArray) weaponType.get("fire-resources");
            for (int j = 0; j < fireResourcesArray.size(); j++) {
                JSONObject jsonObject = (JSONObject) fireResourcesArray.get(j);
                fireResources.put(spec.getResourceType(
                        (String) jsonObject.get("resource")),
                        (int)(long) jsonObject.get("amount")
                );
            }


            ProjectileSpec projectile;
            if (weaponType.get("projectile") == null) {
                projectile = null;
            } else {
                JSONObject projectileType = (JSONObject) weaponType.get("projectile");
                projectile = new ProjectileSpec(
                        (double) projectileType.get("radius"),
                        (double) projectileType.get("speed"),
                        (double) projectileType.get("range"),
                        (boolean) projectileType.get("stops-on-first-hit")
                );
            }
            weaponSpecs.add(new WeaponSpec(
                    name,
                    wclazz,
                    dclazz,
                    damage,
                    innerRadius,
                    outerRadius,
                    coolDownTime,
                    attackTime,
                    requiredResources,
                    fireResources,
                    projectile,
                    conditionDecremnt
            ));
        }
        spec.weaponTypes = weaponSpecs.toArray(new WeaponSpec[0]);
    }

    private static final HashSet<String> NATURAL_RESOURCE_CLASSES = new HashSet<>(Arrays.asList("natural-resource", "visible-in-fog", "occupies"));
    private static void parseNaturalResources(Path location, GameSpec spec) throws IOException, ParseException {
        JSONArray resourceTypes = (JSONArray) getJson(location.resolve("natural_resources.json")).get("natural-resources");
        LinkedList<EntitySpec> parsedResources = new LinkedList<>();
        for (int i = 0; i < resourceTypes.size(); i++) {
            JSONObject resourceType = (JSONObject) resourceTypes.get(i);
            EntitySpec eSpec = new EntitySpec(
                    (String) resourceType.get("name"),
                    (String) resourceType.get("image")
            );
            eSpec.size = new Dimension(1, 1);
            eSpec.carrying = Collections.singleton(new CarrySpec(
                    spec.getResourceType((String) resourceType.get("resource-type")),
                    (int) ((long) resourceType.get("quantity"))
            ));
            eSpec.dropOnDeath = Collections.emptyList();
            HashMap<ResourceType, Integer> carryLimits = new HashMap<>();
            for (CarrySpec carrySpec : eSpec.carrying) {
                carryLimits.put(carrySpec.type, carrySpec.startingQuantity);
            }
            eSpec.carryCapacity = new CarryLimitCapacitySpec(carryLimits);
            eSpec.classes = NATURAL_RESOURCE_CLASSES;
            eSpec.requiredResources = Collections.emptyMap();
            eSpec.canCreate = Collections.emptySet();
            eSpec.aiArgs = Collections.emptyMap();
            parsedResources.add(eSpec);
        }
        spec.naturalResources = parsedResources.toArray(new EntitySpec[0]);
    }

    private static void parseMapGenerator(Path location, GameSpec spec) throws IOException, ParseException {
        JSONObject generation =(JSONObject) getJson(location.resolve("generation.json")).get("generation");
        spec.generationSpec = new GenerationSpec();

        {
            JSONArray patches = (JSONArray) generation.get("patches");
            for (int j = 0; j < patches.size(); j++) {
                JSONObject patch = (JSONObject) patches.get(j);
                GenerationSpec.ResourceGen g = new GenerationSpec.ResourceGen();
                g.type = spec.getNaturalResource((String) patch.get("type"));
                if (g.type == null)
                    throw new IllegalStateException("Unknown resource type: " + patch.get("type"));
                g.numberOfPatches = (int) (long) patch.get("num-patches");
                g.patchSize = (int) (long) patch.get("patch-size");
                spec.generationSpec.resources.add(g);
            }
        }

        {
            JSONArray gaia = (JSONArray) generation.get("gaia");
            for (int j = 0; j < gaia.size(); j++) {
                JSONObject patch = (JSONObject) gaia.get(j);
                GenerationSpec.UnitGen g = new GenerationSpec.UnitGen();
                g.type = spec.getUnitSpec((String) patch.get("type"));
                if (g.type == null)
                    throw new IllegalStateException("Unknown unit type: " + patch.get("type"));
                g.number = (int) (long) patch.get("number");
                spec.generationSpec.gaia.add(g);
            }
        }

        {
            JSONArray perPlayer = (JSONArray) ((JSONObject) generation.get("per-player")).get("units");
            for (int j = 0; j < perPlayer.size(); j++) {
                JSONObject patch = (JSONObject) perPlayer.get(j);
                GenerationSpec.UnitGen g = new GenerationSpec.UnitGen();
                g.type = spec.getUnitSpec((String) patch.get("type"));
                if (g.type == null)
                    throw new IllegalStateException("Unknown unit type: " + patch.get("type"));
                g.number = (int) (long) patch.get("number");
                spec.generationSpec.perPlayerUnits.add(g);
            }
        }
    }


    private static CapacitySpec parseCapacitySpec(JSONObject o, GameSpec spec) {
        switch ((String) o.get("type")) {
            case "simple":
                return new SimpleCapacitySpec((int)(long) o.get("capacity"));
            case "limits":
                HashMap<ResourceType, Integer> carryLimits = new HashMap<>();
                JSONArray limitsList = (JSONArray) o.get("limits");
                for (int j = 0; j < limitsList.size(); j++) {
                    JSONObject limit = (JSONObject) limitsList.get(j);
                    carryLimits.put(
                            spec.getResourceType((String) limit.get("resource")),
                            (int)(long)limit.get("amount")
                    );
                }
                return new CarryLimitCapacitySpec(carryLimits);
            default:
                throw new RuntimeException("Unknown capacity spec: " + o.get("type"));
        }
    }


    private static void fillEntity(GameSpec spec, EntitySpecBuilder toFill, Map<String, EntitySpecBuilder> builders, EntitySpecBuilder current) {
        for (String parent : current.parents) {
            EntitySpecBuilder parentBuilder = builders.get(parent);
            if (parentBuilder == null) throw new RuntimeException("Parent does not exist: " + parent);
            fillEntity(spec, toFill, builders, parentBuilder);
        }
        toFill.addAttributesFromJson(spec, current.jsonObject);
    }

    private static void parseUnits(Path location, GameSpec spec) throws IOException, ParseException {
        Map<String, EntitySpecBuilder> builders = new HashMap<>();
        try (Stream<Path> paths = Files.walk(location.resolve("units")).filter(Files::isRegularFile)) {
            for (Path p : paths.collect(Collectors.toList())) {
                EntitySpecBuilder builder = new EntitySpecBuilder();
                builder.jsonObject = getJson(p);
                builder.unitSpec = new EntitySpec(
                        (String) builder.jsonObject.get("name"),
                        (String) builder.jsonObject.getOrDefault("image", null)
                );

                builder.unitSpec.carrying = Collections.emptySet();
                builder.unitSpec.carryCapacity = new InCapableCapacitySpec();
                builder.unitSpec.canCreate = new HashSet<>();
                builder.unitSpec.classes = new HashSet<>();
                builder.unitSpec.classes.add("unit");
                builder.unitSpec.dropOnDeath = new LinkedList<>();
                builder.unitSpec.requiredResources = new HashMap<>();
                builder.unitSpec.aiArgs = new HashMap<>();

                JSONArray parents = (JSONArray) builder.jsonObject.get("parents");
                if (parents == null) {
                    builder.parents = Collections.emptySet();
                } else {
                    builder.parents = new HashSet<>();
                    for (int i = 0; i < parents.size(); i++)
                        builder.parents.add((String) parents.get(i));
                }

                builders.put(builder.unitSpec.name, builder);
            }
        }

        for (EntitySpecBuilder builder : builders.values())
            fillEntity(spec, builder, builders, builder);

        LinkedList<EntitySpec> entities = new LinkedList<>();
        for (EntitySpecBuilder builder : builders.values())
            if (builder.isComplete())
                entities.add(builder.unitSpec);
        spec.unitSpecs = entities.toArray(new EntitySpec[0]);

        for (EntitySpecBuilder builder : builders.values())
            builder.compile(spec);
    }


    public static GameSpec parseGameSpec(Path location) throws IOException, ParseException {
        GameSpec spec = new GameSpec();
        parseTop(getJson(location.resolve("top_lvl.json")), spec);
        parseResources(location, spec);
        parseNaturalResources(location, spec);
        parseWeapons(location, spec);
        parseUnits(location, spec);
        parseMapGenerator(location, spec);
        return  spec;
    }


    private static final class EntitySpecBuilder {
        Set<String> parents;
        JSONObject jsonObject;
        EntitySpec unitSpec;
        HashMap<String, List<Map<String, Object>>> canCreate = new HashMap<>();

        public void addAttributesFromJson(GameSpec spec, JSONObject unitJson) {
            if (unitJson.containsKey("line-of-sight")) {
                unitSpec.initialLineOfSight = (double) unitJson.get("line-of-sight");
            }
            if (unitJson.containsKey("initialBaseHealth-points")) {
                unitSpec.initialBaseHealth = (double) unitJson.get("initialBaseHealth-points");
            }
            if (unitJson.containsKey("size")) {
                unitSpec.size = parseDimension((JSONObject) unitJson.get("size"));
            }
            if (unitJson.containsKey("garrison-capacity")) {
                unitSpec.garrisonCapacity = (int) ((long) unitJson.get("garrison-capacity"));
            }
            if (unitJson.containsKey("rotation-speed")) {
                unitSpec.initialRotationSpeed = (double) unitJson.get("rotation-speed");
            }
            if (unitJson.containsKey("move-speed")) {
                unitSpec.initialMovementSpeed = (double) unitJson.get("move-speed");
            }
            if (unitJson.containsKey("collect-speed")) {
                unitSpec.initialCollectSpeed = (double) unitJson.get("collect-speed");
            }
            if (unitJson.containsKey("deposit-speed")) {
                unitSpec.initialDepositSpeed = (double) unitJson.get("deposit-speed");
            }
            if (unitJson.containsKey("attack-speed")) {
                unitSpec.initialAttackSpeed = (double) unitJson.get("attack-speed");
            }
            if (unitJson.containsKey("build-speed")) {
                unitSpec.initialBuildSpeed = (double) unitJson.get("build-speed");
            }
            if (unitJson.containsKey("creation-time")) {
                unitSpec.creationTime = (double) unitJson.get("creation-time");
            }
            if (unitJson.containsKey("building-path")) {
                LinkedList<String> p = new LinkedList<>();
                JSONArray classList = (JSONArray) unitJson.get("building-path");
                for (int j = 0; j < classList.size(); j++) {
                    p.addLast((String) classList.get(j));
                }
                unitSpec.buildingPath =  p.toArray(new String[0]);
            }
            if (unitJson.containsKey("classes")) {
                JSONArray classList = (JSONArray) unitJson.get("classes");
                for (int j = 0; j < classList.size(); j++) {
                    unitSpec.classes.add((String) classList.get(j));
                }
            }
            if (unitJson.containsKey("carry-capacity")) {
                if (unitSpec.carryCapacity instanceof InCapableCapacitySpec) {
                    unitSpec.carryCapacity = new CapableCapacitySpec();
                }
                JSONArray specs = (JSONArray) unitJson.get("carry-capacity");
                for (int j = 0; j < specs.size(); j++) {
                    unitSpec.carryCapacity = new CombinationCapacitySpec(
                            unitSpec.carryCapacity,
                            parseCapacitySpec((JSONObject) specs.get(j), spec)
                    );
                }
            }
            if (unitJson.containsKey("can-create")) {
                List<Map<String, Object>> canCreateList = new LinkedList<>();
                JSONArray classList = (JSONArray) unitJson.get("can-create");
                for (int j = 0; j < classList.size(); j++) {
                    JSONObject classC = (JSONObject) classList.get(j);

                    Map<String, Object> foo = new HashMap<>();
                    foo.put("created-unit", (String) classC.get("created-unit"));
                    foo.put("creation-method", (String) classC.get("creation-method"));
                    foo.put("params", new HashMap<>());
                    JSONObject parameters = (JSONObject) classC.get("params");
                    for (Object entry : parameters.keySet()) {
                        ((HashMap<String, String>) foo.get("params")).put((String) entry, (String) parameters.get(entry));
                    }
                    canCreateList.add(foo);
                }
                canCreate.put(unitSpec.name, canCreateList);
            }
            if (unitJson.containsKey("ai")) {
                JSONObject aiObject = (JSONObject) unitJson.get("ai");
                unitSpec.ai = (String) aiObject.get("name");
                unitSpec.aiArgs = new HashMap<>();
                JSONObject parameters = (JSONObject) aiObject.get("params");
                for (Object entry : parameters.keySet()) {
                    unitSpec.aiArgs.put((String) entry, (String) parameters.get(entry));
                }
            }
            if (unitJson.containsKey("drop-on-death")) {
                JSONArray toDrop = (JSONArray) unitJson.get("drop-on-death");
                for (int j = 0; j < toDrop.size(); j++) {
                    JSONObject dropping = (JSONObject) toDrop.get(j);
                    switch ((String) dropping.get("type")) {
                        case "natural-resource":
                            unitSpec.dropOnDeath.add(spec.getNaturalResource((String) dropping.get("value")));
                            break;
                        default:
                            throw new RuntimeException("Unable to read drop type: " + dropping.get("type"));
                    }
                }
            }
            if (unitJson.containsKey("required-resources")) {
                JSONArray requiredResourcesJson = (JSONArray) unitJson.get("required-resources");
                for (Object aRequiredResourcesJson : requiredResourcesJson) {
                    JSONObject requiredResource = (JSONObject) aRequiredResourcesJson;
                    ResourceType resource = spec.getResourceType((String) requiredResource.get("resource"));
                    unitSpec.requiredResources.put(resource, (int) (long) requiredResource.get("amount"));
                }
            }
        }

        private void compile(GameSpec spec) {
            for (Map.Entry<String, List<Map<String, Object>>> entry : canCreate.entrySet()) {
                EntitySpec creator = spec.getUnitSpec(entry.getKey());
                for (Map<String, Object> cSpecParams : entry.getValue()) {
                    EntitySpec us = spec.getUnitSpec((String) cSpecParams.get("created-unit"));
                    CreationMethod m = CreationMethod.valueOf((String) cSpecParams.get("creation-method"));

                    if (us == null) throw new NullPointerException((String) cSpecParams.get("created-unit"));
                    if (m == null) throw new NullPointerException((String) cSpecParams.get("creation-method"));

                    CreationSpec cSpec = new CreationSpec(us, m);
                    for (Map.Entry<String, String> e : ((Map<String, String>) cSpecParams.get("params")).entrySet()) {
                        cSpec.creationMethodParams.put(e.getKey(), e.getValue());
                    }

                    creator.canCreate.add(cSpec);
                }
            }
        }

        public boolean isComplete() {
            return unitSpec.image != null;
        }
    }



    private static final class GameSpecBuilder {
        LinkedList<ResourceType> resources = new LinkedList<>();
    }
}
