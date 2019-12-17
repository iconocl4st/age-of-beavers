package common.state.edit;

import common.CommonConstants;
import common.state.edit.Interfaces.ValueCreator;
import common.state.spec.*;
import common.state.spec.attack.DamageType;
import common.state.spec.attack.ProjectileSpec;
import common.state.spec.attack.WeaponClass;
import common.state.spec.attack.WeaponSpec;
import common.state.sst.sub.capacity.PrioritizedCapacitySpec;
import common.util.Immutable;
import common.util.Util;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

class Creators {



    private static <S> Immutable.ImmutableList<S> createList(Collection<? extends Interfaces.SpecCreator<S>> creators, Interfaces.CreationContext cntxt) {
        ArrayList<S> ret = new ArrayList<>(creators.size());
        for (Interfaces.SpecCreator<S> c : creators) {
            ret.add(c.create(cntxt));
        }
        return new Immutable.ImmutableList<>(ret);
    }

    static JSONArray toArray(List<? extends Interfaces.SpecCreator> l) {
        JSONArray array = new JSONArray();
        for (Interfaces.SpecCreator c : l) {
            JSONObject obj = new JSONObject();
            c.save(obj);
            array.put(obj);
        }
        return array;
    }





    enum CreatorType {
        Capacity,
        Creations,
        DroppedOnDeath,
        EntitySpec,
        GameSpec,
        Resource,
        Generations,
        Dimension,
        Integer,
        Double,
        Enumeration,
        EntityReference,
        String,
        Boolean,
        ResourceReference,
        ResourceMap,
        Strings,
        UnitGenCreator,
        ResourceGenCreator,
        WeaponSpec,
        CraftingCreator,
        Color,
        File
    }

    static class FileCreator implements Interfaces.ValueCreator<String> {
        String fieldName;
        Path path;
        Path rootPath;

        FileCreator(String fieldName, String rootPath) {
            this.fieldName = fieldName;
            this.rootPath = Paths.get(rootPath);
        }

        @Override
        public String create(Interfaces.CreationContext cntxt) {
            if (path == null) return null;
            try {
                Path root = rootPath.toRealPath().toAbsolutePath();
                Path current = path.toRealPath().toAbsolutePath();
                return root.relativize(current).toString();
            } catch (IOException e) {
                throw new RuntimeException("Unable to determine relative path.");
            }
        }

        @Override
        public void compile(GameSpecCreator creator) {}

        @Override
        public void parse(JSONObject object) {
            if (!object.has(fieldName)) return;
            path = rootPath.resolve(object.getString(fieldName));
        }

        @Override
        public void save(JSONObject obj) {
            if (path == null) return;
            obj.put(fieldName, create(null));
        }

        @Override
        public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {
            if (path == null) {
                if (!params.canBeNull) {
                    errors.error("Cannot be null");
                    return;
                }
                return;
            }
            try {
                Path root = rootPath.toRealPath().toAbsolutePath();
                Path current = rootPath.toRealPath().toAbsolutePath();
                if (!current.startsWith(root)) {
                    errors.error("Path is not in base path: " + current);
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                errors.error("Unable to locate real path: " + path);
                return;
            }
        }

        @Override
        public String getFieldName() {
            return fieldName;
        }

        @Override
        public void setNull(boolean isNull) {
            path = null;
        }

        @Override
        public boolean isNull() {
            return path == null;
        }

        @Override
        public CreatorType getType() {
            return CreatorType.File;
        }

        public void setPath(Path s) {
            path = s;
        }
    }

    static class CraftingCreator implements Interfaces.SpecCreator<CraftingSpec> {
        ResourcesMapCreator inputs = new ResourcesMapCreator("inputs");
        ResourcesMapCreator outputs = new ResourcesMapCreator("outputs");
//        DoubleCreator craftTime = new DoubleCreator("crafting-time");

        @Override
        public CraftingSpec create(Interfaces.CreationContext cntxt) {
            return new CraftingSpec(
                inputs.create(cntxt),
                outputs.create(cntxt),
                new SpecTree.SpecNodeReference(
                    (EntitySpec) cntxt.args.get("entity"),
                    (String[]) cntxt.args.get("path")
                )
            );
        }

        @Override
        public void compile(GameSpecCreator creator) {
            inputs.compile(creator);
            outputs.compile(creator);
        }

        @Override
        public void parse(JSONObject object) {
            inputs.parse(object);
            outputs.parse(object);
        }

        @Override
        public void save(JSONObject object) {
            inputs.save(object);
            outputs.save(object);
        }

        @Override
        public CreatorType getType() {
            return CreatorType.CraftingCreator;
        }

        @Override
        public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {
            inputs.getExportErrors(creator, errors, params);
            outputs.getExportErrors(creator, errors, params);
        }
    }

    static class SpecTreeCreator<G, T extends Interfaces.SpecCreator<G>> implements ValueCreator<SpecTree<G>> {
        SpecTree<T> specTreeValue;
        Interfaces.Creator<T> creator;
        String fieldName;

        SpecTreeCreator(String fieldName, SpecTree<T> specTreeValue, Interfaces.Creator<T> creator) {
            this.fieldName = fieldName;
            this.specTreeValue = specTreeValue;
            this.creator = creator;
        }

        @Override
        public SpecTree<G> create(Interfaces.CreationContext cntxt) {
            if (isNull()) return null;
            return this.specTreeValue.toType((p, n) -> n.create(cntxt.setArg("path", p.toArray(new String[0]))));
        }

        @Override
        public void compile(GameSpecCreator creator) {
            specTreeValue.visit((p, t) -> t.compile(creator));
        }

        @Override
        public void parse(JSONObject object) {
            if (!object.has(fieldName)) return;
            specTreeValue.setRoot(rParse(object.getJSONObject(fieldName)));
        }


        void rSave(SpecTree.SpecNode<T> node, JSONObject object) {
            if (node instanceof SpecTree.SpecBranchNode) {
                object.put("type", "branch");
                JSONArray array = new JSONArray();
                for (Map.Entry<String, SpecTree.SpecNode<T>> entry : ((SpecTree.SpecBranchNode<T>) node).children.entrySet()) {
                    JSONObject o = new JSONObject();
                    o.put("key", entry.getKey());
                    JSONObject v = new JSONObject();
                    rSave(entry.getValue(), v);
                    o.put("child", v);
                    array.put(o);
                }
                object.put("children", array);
            } else if (node instanceof SpecTree.SpecLeafNode) {
                object.put("type", "leaf");
                JSONObject val = new JSONObject();
                ((SpecTree.SpecLeafNode<T>) node).value.save(val);
                object.put("value", val);
            } else {
                throw new IllegalStateException();
            }
        }
        @Override
        public void save(JSONObject obj) {
            JSONObject tree = new JSONObject();
            rSave(specTreeValue.root, tree);
            obj.put(fieldName, tree);
        }

        @Override
        public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {
            specTreeValue.visit((p, t) -> t.getExportErrors(creator, errors, params));
        }

        SpecTree.SpecNode<T> rParse(JSONObject object) {
            switch (object.getString("type")) {
                case "branch":
                    SpecTree.SpecBranchNode<T> current = new SpecTree.SpecBranchNode<T>();
                    JSONArray children = object.getJSONArray("children");
                    for (int i = 0; i < children.length(); i++) {
                        JSONObject child = children.getJSONObject(i);
                        current.children.put(child.getString("key"), rParse(child.getJSONObject("child")));
                    }
                    return current;
                case "leaf":
                    T value = creator.create(null);
                    value.parse(object.getJSONObject("value"));
                    return new SpecTree.SpecLeafNode<>(value);
                default:
                    throw new IllegalStateException();
            }
        }

        @Override
        public String getFieldName() {
            return fieldName;
        }

        @Override
        public void setNull(boolean isNull) {

        }

        @Override
        public boolean isNull() {
            return false;
        }

        @Override
        public CreatorType getType() {
            return null;
        }
    }

    static class WeaponSpecCreator implements Interfaces.SpecCreator<WeaponSpec> {
        String name;
        List<ValueCreator<?>> valueCreators = new LinkedList<>();

        DoubleCreator cooldownTime = new DoubleCreator("cooldown-time"); { valueCreators.add(cooldownTime); }
        DoubleCreator attackTime = new DoubleCreator("attack-time"); { valueCreators.add(attackTime); }
        EnumerationCreator<WeaponClass> weaponClass = new EnumerationCreator<>("weapon-class", WeaponClass.values()); { valueCreators.add(weaponClass); }
        ResourcesMapCreator requiredResources = new ResourcesMapCreator("required-resources"); { valueCreators.add(requiredResources); }
        ResourcesMapCreator fireResources = new ResourcesMapCreator("fire-resources"); { valueCreators.add(fireResources); }
        EnumerationCreator<DamageType> damageType = new EnumerationCreator<>("damage-type", DamageType.values()); { valueCreators.add(damageType); }
        DoubleCreator outerRange = new DoubleCreator("outer-range"); { valueCreators.add(outerRange); }
        DoubleCreator innerRange = new DoubleCreator("inner-range"); { valueCreators.add(innerRange); }
        DoubleCreator damage = new DoubleCreator("damage"); { valueCreators.add(damage); }
        DoubleCreator decrementCondition = new DoubleCreator("decrement-condition"); { valueCreators.add(decrementCondition); }
        BooleanCreator hasProjectile = new BooleanCreator("has-projectile");
        DoubleCreator projectileRadius = new DoubleCreator("projectile-radius"); { valueCreators.add(projectileRadius); }
        DoubleCreator projectileSpeed = new DoubleCreator("projectile-speed"); { valueCreators.add(projectileSpeed); }
        DoubleCreator projectileRange = new DoubleCreator("projectile-range"); { valueCreators.add(projectileRange); }
        BooleanCreator projectileStopsOnFirstHit = new BooleanCreator("projectile-stops-on-first-hit"); { valueCreators.add(projectileStopsOnFirstHit); }
        DoubleCreator projectileAccuracy = new DoubleCreator("projectile-accuracy"); { valueCreators.add(projectileAccuracy); }
        StringCreator projectileGraphicsType = new StringCreator("projectile-graphics-type"); { valueCreators.add(projectileGraphicsType); }

        WeaponSpecCreator(String name) {
            this.name = name;
            hasProjectile.set(false);
        }

        public String toString() {
            return name;
        }

        @Override
        public WeaponSpec create(Interfaces.CreationContext cntxt) {
            return new WeaponSpec(
                name,
                weaponClass.create(cntxt),
                damageType.create(cntxt),
                damage.create(cntxt),
                innerRange.create(cntxt),
                outerRange.create(cntxt),
                cooldownTime.create(cntxt),
                attackTime.create(cntxt),
                requiredResources.create(cntxt),
                fireResources.create(cntxt),
                hasProjectile.create(cntxt) ? new ProjectileSpec(
                    projectileRadius.create(cntxt),
                    projectileSpeed.create(cntxt),
                    projectileRange.create(cntxt),
                    projectileStopsOnFirstHit.create(cntxt)
                ) : null,
                Util.zin(decrementCondition.create(cntxt))
            );
        }

        @Override
        public void compile(GameSpecCreator creator) {
            for (ValueCreator<?> c : valueCreators)
                c.compile(creator);
        }

        @Override
        public void parse(JSONObject object) {
            name = object.getString("name");
            for (ValueCreator<?> c : valueCreators)
                c.parse(object);
            hasProjectile.set(!projectileRadius.isNull());
        }

        @Override
        public void save(JSONObject obj) {
            obj.put("name", name);
            for (ValueCreator<?> c : valueCreators)
                c.save(obj);
        }

        @Override
        public CreatorType getType() {
            return CreatorType.WeaponSpec;
        }

        @Override
        public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {
            try (P ignored = errors.withPath(name)) {
                errors.nonNull(weaponClass);
                errors.nonNull(damageType);
                errors.nonNull(damage);
                errors.nonNull(innerRange);
                errors.nonNull(outerRange);
                errors.nonNull(cooldownTime);
                errors.nonNull(attackTime);
                errors.nonNull(requiredResources);
                errors.nonNull(fireResources);
                errors.nonNull(hasProjectile, "weapon: " + name + " has projectile");
                Boolean b = hasProjectile.get();
                if (b != null && b) {
                    try (P p = errors.withPath("projectile")) {
                        errors.nonNull(projectileRadius);
                        errors.nonNull(projectileSpeed);
                        errors.nonNull(projectileRange);
                        errors.nonNull(projectileStopsOnFirstHit);
                    }
                }
            }
//            decrementCondition.create(creator)
        }

        static WeaponSpecCreator parseW(JSONObject jsonObject) {
            WeaponSpecCreator creator = new WeaponSpecCreator(jsonObject.getString("name"));
            creator.parse(jsonObject);
            return creator;
        }
    }

//    static class ProjectileCreator extends Interfaces.NullableCreator<ProjectileSpec> {
//        WeaponSpecCreator
//    }

    static class CapacityCreator extends Interfaces.NullableCreator<PrioritizedCapacitySpec> {
        IntegerCreator maximumWeight = new IntegerCreator("maximum-weight");
        ResourcesMapCreator mapCreator = new ResourcesMapCreator("maximum-amounts");
        BooleanCreator defaultToAccept = new BooleanCreator("default-to-accept");

        CapacityCreator(String name) {
            super(name);
        }

        @Override
        public PrioritizedCapacitySpec create(Interfaces.CreationContext cntxt) {
            if (isNull())
                return null;
            if (cntxt.resourceTypes == null) return null;
            if (maximumWeight.isNull() && mapCreator.isNull)
                throw new IllegalStateException(fieldName);
            if (mapCreator.isNull)
                return new PrioritizedCapacitySpec(maximumWeight.get(), defaultToAccept.get());
            PrioritizedCapacitySpec spec = PrioritizedCapacitySpec.createCapacitySpec(mapCreator.create(cntxt), false, defaultToAccept.get());
            if (!maximumWeight.isNull())
                spec.setTotalWeight(maximumWeight.get());
            return spec;
        }

        @Override
        public void compile(GameSpecCreator creator) {
            mapCreator.compile(creator);
        }

        @Override
        public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {
            if (isNull()) {
                if (params.canBeNull) return;
                errors.error("Cannot be null");
                return;
            }
            errors.nonNull(defaultToAccept);
            if (maximumWeight.isNull() && mapCreator.isNull) {
                errors.error("No capacity, although it is not null");
                return;
            }
            mapCreator.getExportErrors(creator, errors, params);
        }

        @Override
        public void parseNonNull(JSONObject object) {
            JSONObject obj = object.getJSONObject(fieldName);
            maximumWeight.parse(obj);
            mapCreator.parse(obj);
            defaultToAccept.parse(obj);
        }

        @Override
        public void saveNonNull(JSONObject object) {
            JSONObject obj = new JSONObject();
            maximumWeight.save(obj);
            mapCreator.save(obj);
            defaultToAccept.save(obj);
            object.put(fieldName, obj);
        }

        @Override
        public CreatorType getType() {
            return CreatorType.Capacity;
        }
    }

    static class CreationCreator implements Interfaces.SpecCreator<CreationSpec> {
        EnumerationCreator<CreationMethod> method = new EnumerationCreator<>("creation-method", CreationMethod.values());
        StringsCreator.StringMapCreator creationArgs = new StringsCreator.StringMapCreator("params");
        EntityCreatorReference created = new EntityCreatorReference("created-unit");
        ResourcesMapCreator requiredResources = new ResourcesMapCreator("required-resources");
        DoubleCreator creationTime = new DoubleCreator("creation-time");

        @Override
        public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {
            created.getExportErrors(creator, errors, params.cannotBeNull());
            method.getExportErrors(creator, errors, params.cannotBeNull());
            requiredResources.getExportErrors(creator, errors, params.cannotBeNull());
            creationTime.getExportErrors(creator, errors, params.cannotBeNull());
            creationArgs.getExportErrors(creator, errors, params.cannotBeNull());
        }

        @Override
        public CreationSpec create(Interfaces.CreationContext cntxt) {
            return new CreationSpec(
                created.create(cntxt),
                method.create(cntxt),
                requiredResources.create(cntxt),
                creationTime.create(cntxt),
                creationArgs.create(cntxt),
                new SpecTree.SpecNodeReference(
                    (EntitySpec) cntxt.args.get("entity"),
                    (String[])cntxt.args.get("path")
                )
            );
        }

        @Override
        public void compile(GameSpecCreator creator) {
            method.compile(creator);
            created.compile(creator);
            requiredResources.compile(creator);
            creationTime.compile(creator);
            creationArgs.compile(creator);
        }

        @Override
        public void parse(JSONObject object) {
            method.parse(object);
            created.parse(object);
            requiredResources.parse(object);
            creationTime.parse(object);
            creationArgs.parse(object);
        }

        @Override
        public void save(JSONObject object) {
            method.save(object);
            created.save(object);
            requiredResources.save(object);
            creationTime.save(object);
            creationArgs.save(object);
        }

        @Override
        public CreatorType getType() {
            return CreatorType.Creations;
        }
    }

    public static class DropsOnDeath extends Interfaces.NullableCreator<Set<EntitySpec>> {
        Set<EntityCreatorReference> references = new HashSet<>();

        DropsOnDeath(String name) {
            super(name);
        }

        @Override
        public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {
            errors.checkAll(references, creator, params.cannotBeNull());
        }

        @Override
        public Set<EntitySpec> create(Interfaces.CreationContext cntxt) {
            HashSet<EntitySpec> ret = new HashSet<>();
            for (EntityCreatorReference ref : references)
                ret.add(ref.create(cntxt));
            return ret;
        }

        @Override
        public void compile(GameSpecCreator creator) {
            for (EntityCreatorReference ref : references)
                ref.compile(creator);
        }

        @Override
        void parseNonNull(JSONObject object) {
            JSONArray jsonArray = object.getJSONArray(fieldName);
            for (int i = 0; i < jsonArray.length(); i++)
                references.add(new EntityCreatorReference(jsonArray.getString(i)));
        }

        @Override
        void saveNonNull(JSONObject object) {
            JSONArray jsonArray = new JSONArray();
            for (EntityCreatorReference ref : references)
                jsonArray.put(ref.referenceName);
            object.put(fieldName, jsonArray);
        }

        @Override
        public CreatorType getType() {
            return CreatorType.DroppedOnDeath;
        }
    }

    public abstract static class StringsCreator<T> extends Interfaces.NullableCreator<T> {
        StringCreator stringCreator = new StringCreator("raboof");

        StringsCreator(String name) {
            super(name);
            isNull = true;
        }

        @Override
        public void compile(GameSpecCreator creator) {}

        @Override
        public CreatorType getType() {
            return CreatorType.Strings;
        }

        static class StringMapCreator extends StringsCreator<Map<String, String>> {
            StringMapCreator(String name) {
                super(name);
            }

            @Override
            public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {

            }

            @Override
            public Map<String, String> create(Interfaces.CreationContext cntxt) {
                if (isNull) return null;
                HashMap<String, String> ret = new HashMap<>();
                for (String entry : stringCreator.get().split(",")) {
                    if (entry.length() == 0) continue;
                    String[] split = entry.split("->");
                    ret.put(split[0], split[1]);
                }
                return ret;
            }

            @Override
            public void parseNonNull(JSONObject object) {
                StringBuilder builder = new StringBuilder();
                JSONObject jsonObject = object.getJSONObject(fieldName);
                for (String key : jsonObject.keySet()) {
                    if (builder.length() != 0)
                        builder.append(',');
                    builder.append(key).append("->").append(jsonObject.getString(key));
                }
                stringCreator.set(builder.toString());
            }

            @Override
            public void saveNonNull(JSONObject obj) {
                if (isNull) return;
                JSONObject jsonObj = new JSONObject();
                for (Map.Entry<String, String> entry : create(null).entrySet())
                    jsonObj.put(entry.getKey(),  entry.getValue());
                obj.put(fieldName, jsonObj);
            }
        }

        static class StringSetCreator extends StringsCreator<Set<String>> {
            StringSetCreator(String name) {
                super(name);
            }

            @Override
            public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {

            }

            @Override
            public Set<String> create(Interfaces.CreationContext cntxt) {
                if (isNull) return null;
                return new HashSet<>(Arrays.asList(stringCreator.get().split(",")));
            }

            @Override
            public void parseNonNull(JSONObject object) {
                StringBuilder builder = new StringBuilder();
                JSONArray jsonArray = object.getJSONArray(fieldName);
                for (int i = 0; i < jsonArray.length(); i++) {
                    if (i != 0)
                        builder.append(',');
                    builder.append(jsonArray.getString(i));
                }
                stringCreator.set(builder.toString());
            }

            @Override
            public void saveNonNull(JSONObject obj) {
                JSONArray jsonArray = new JSONArray();
                for (String s : create(null))
                    jsonArray.put(s);
                obj.put(fieldName, jsonArray);
            }
        }


        static class StringListCreator extends StringsCreator<List<String>> {
            StringListCreator(String name) {
                super(name);
            }

            @Override
            public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {

            }

            @Override
            public List<String> create(Interfaces.CreationContext cntxt) {
                if (isNull) return null;
                return Arrays.asList(stringCreator.get().split(","));
            }

            @Override
            public void parseNonNull(JSONObject object) {
                StringBuilder builder = new StringBuilder();
                JSONArray jsonArray = object.getJSONArray(fieldName);
                for (int i = 0; i < jsonArray.length(); i++) {
                    if (i != 0)
                        builder.append(',');
                    builder.append(jsonArray.getString(i));
                }
                stringCreator.set(builder.toString());
            }

            @Override
            public void saveNonNull(JSONObject obj) {
                JSONArray jsonArray = new JSONArray();
                for (String s : create(null))
                    jsonArray.put(s);
                obj.put(fieldName, jsonArray);
            }
        }
    }


    static class EntityCreator implements Interfaces.SpecCreator<EntitySpec> {

        EntityCreatorReference parent = new EntityCreatorReference("parent");
        List<EntityCreator> children = new LinkedList<>();

        // currently not inheritable
        SpecTreeCreator<CreationSpec, CreationCreator> canCreate = new SpecTreeCreator<>("can-create", new SpecTree<>(), s -> new CreationCreator());
        SpecTreeCreator<CraftingSpec, CraftingCreator> canCraft = new SpecTreeCreator<>("can-craft", new SpecTree<>(), s -> new CraftingCreator());

        public String name;

        List<ValueCreator<? extends Object>> fields = new LinkedList<>();

        FileCreator image = new FileCreator("image", CommonConstants.IMAGE_DIRECTORY);
        DimensionCreator size = new DimensionCreator("size"); { fields.add(size); }
        DoubleCreator healthPoints = new DoubleCreator("health-points"); { fields.add(healthPoints); }
        IntegerCreator garrisonCapacity = new IntegerCreator("garrison-capacity"); { fields.add(garrisonCapacity); }
        DoubleCreator moveSpeed = new DoubleCreator("move-speed"); { fields.add(moveSpeed); }
        DoubleCreator lineOfSight = new DoubleCreator("line-of-sight"); { fields.add(lineOfSight); }
        DoubleCreator collectSpeed = new DoubleCreator("collect-speed"); { fields.add(collectSpeed); }
        DoubleCreator depositSpeed = new DoubleCreator("deposit-speed"); { fields.add(depositSpeed); }
        DoubleCreator attackRangeOuter = new DoubleCreator("attack-range-outer"); { fields.add(attackRangeOuter); }
        DoubleCreator attackRangeInner = new DoubleCreator("attack-range-inner"); { fields.add(attackRangeInner); }
        DoubleCreator rotationSpeed = new DoubleCreator("rotation-speed"); { fields.add(rotationSpeed); }
        DoubleCreator attackSpeed = new DoubleCreator("attack-speed"); { fields.add(attackSpeed); }
        DoubleCreator buildSpeed = new DoubleCreator("build-speed"); { fields.add(buildSpeed); }
        DropsOnDeath dropsOnDeath = new DropsOnDeath("drops-on-death"); { fields.add(dropsOnDeath); }
        CapacityCreator carryCapacity = new CapacityCreator("carry-capacity"); { fields.add(carryCapacity); }
        ResourcesMapCreator isCarrying = new ResourcesMapCreator("is-carrying"); { fields.add(isCarrying); }
        StringsCreator.StringSetCreator classes = new StringsCreator.StringSetCreator("classes"); { fields.add(classes); }
        BooleanCreator isExported = new BooleanCreator("is-exported-to-spec"); { fields.add(isExported); }
        ColorCreator color = new ColorCreator("minimap-color");

//        public GaiaAi ai;
//        public Map<String, String> aiArgs;
//        public Color minimapColor;

        EntityCreator(String name) {
            this.name = name;
        }

        @Override
        public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {
            if (name == null) throw new IllegalStateException();
            try (P ignore = errors.withPath(name)) {
                errors.nonNull(isExported);
                for (ValueCreator<?> a : fields) {
                    try (P p = errors.withPath(a.getFieldName())) {
                        a.getExportErrors(creator, errors, params);
                    }
                }
                try (P p = errors.withPath("can-create")) { canCreate.getExportErrors(creator, errors, params); }
                try (P p = errors.withPath("can-craft")) { canCraft.getExportErrors(creator, errors, params); }

                if (isExported.get() != null && isExported.get()) {
                    errors.nonNull(image, "image path");
                }
            }
        }

        public String toString() {
            return name;
        }

        private <T> T getInheretedValue(Interfaces.CreationContext cntxt, ValueCreator<T> creator) {
            LinkedList<Interfaces.InheritedValue<T>> inheritedValues = locateInheretedValue(cntxt, creator, new LinkedList<>());
            if (inheritedValues.isEmpty()) return null;
            return inheritedValues.getLast().value;
        }

        @Override
        public EntitySpec create(Interfaces.CreationContext cntxt) {
            if (!isExported.get()) throw new RuntimeException();

            HashSet<String> combined = new HashSet<>();
            for (Interfaces.InheritedValue<Set<String>> v : locateInheretedValue(cntxt, classes, new LinkedList<>())) {
                combined.addAll(v.value);
            }

            EntitySpec entitySpec = new EntitySpec(
                    name,
                    image.create(cntxt),
                    getInheretedValue(cntxt, size),
                    getInheretedValue(cntxt, healthPoints),
                    getInheretedValue(cntxt, carryCapacity),
                    getInheretedValue(cntxt, garrisonCapacity),
                    getInheretedValue(cntxt, moveSpeed),
                    getInheretedValue(cntxt, lineOfSight),
                    getInheretedValue(cntxt, collectSpeed),
                    getInheretedValue(cntxt, depositSpeed),
                    getInheretedValue(cntxt, rotationSpeed),
                    getInheretedValue(cntxt, attackSpeed),
                    getInheretedValue(cntxt, buildSpeed),
                    null,
                    Immutable.ImmutableMap.emptyMap(),
                    new Immutable.ImmutableSet<>(combined),
                    new Immutable.ImmutableMap<>(isCarrying.create(cntxt)), // needs to be inherited?
                    null,
                    color.get()
            );
            cntxt.setArg("entity", null);
            return entitySpec;
        }

        @Override
        public void compile(GameSpecCreator creator) {
            parent.compile(creator);
            if (parent.reference != null)
                parent.reference.children.add(this);
            for (ValueCreator<?> vc : fields)
                vc.compile(creator);
            canCreate.compile(creator);
            canCraft.compile(creator);
            image.compile(creator);
        }

        LinkedList<Object> locateInheretedValue_JavaIsntSmartEnough(Interfaces.CreationContext cntxt, ValueCreator<?> o, LinkedList<Object> values) {
            for (ValueCreator<? extends Object> c : fields) {
                if (!o.getFieldName().equals(c.getFieldName())) continue;
                Object curr = c.create(cntxt);
                if (curr == null) break;
                values.addFirst(new Interfaces.InheritedValue<>(curr, name));
                break;
            }
            if (parent.reference != null) {
                parent.reference.locateInheretedValue_JavaIsntSmartEnough(cntxt, o, values);
            }
            return values;
        }

        <T> LinkedList<Interfaces.InheritedValue<T>> locateInheretedValue(Interfaces.CreationContext cntxt, ValueCreator<T> o, LinkedList<Interfaces.InheritedValue<T>> values) {
            for (ValueCreator<? extends Object> c : fields) {
                if (!o.getFieldName().equals(c.getFieldName())) continue;
                T curr = (T) c.create(cntxt);
                if (curr == null) break;
                values.addFirst(new Interfaces.InheritedValue<>(curr, name));
                break;
            }
            if (parent.reference != null) {
                parent.reference.locateInheretedValue(cntxt, o, values);
            }
            return values;
        }

        @Override
        public void parse(JSONObject object) {
            parent.parse(object);
            for (ValueCreator<? extends Object> vc : fields)
                vc.parse(object);
            canCreate.parse(object);
            canCraft.parse(object);
            image.parse(object);
            color.parse(object);
        }

        @Override
        public void save(JSONObject obj) {
            parent.save(obj);
            for (ValueCreator<? extends Object> vc : fields)
                vc.save(obj);
            obj.put("name", name);
            canCreate.save(obj);
            canCraft.save(obj);
            image.save(obj);
            color.save(obj);
        }

        @Override
        public CreatorType getType() {
            return CreatorType.EntitySpec;
        }

        static EntityCreator parseEntity(JSONObject json) {
            EntityCreator creator = new EntityCreator(json.getString("name"));
            creator.parse(json);
            return creator;
        }

        void fill(Interfaces.CreationContext cntxt) {
            EntitySpec unitType = cntxt.getUnitType(name);
            unitType.canCreate = canCreate.create(cntxt);
            unitType.canCraft = canCraft.create(cntxt);
        }

        static EntityCreator NoEntity = new EntityCreator("none");
    }

    static class ResourcesMapCreator extends Interfaces.NullableCreator<Map<ResourceType, Integer>> {
        LinkedList<ResourcesMapEntry> entries = new LinkedList<>();

        ResourcesMapCreator(String fieldName) {
            super(fieldName);
        }

        @Override
        public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {

        }

        @Override
        public Map<ResourceType, Integer> create(Interfaces.CreationContext cntxt) {
            Map<ResourceType, Integer> ret = new HashMap<>();
            for (ResourcesMapEntry r  : entries)
                ret.put(r.reference.create(cntxt), r.value.create(cntxt));
            return ret;
        }

        @Override
        public void compile(GameSpecCreator creator) {
            for (ResourcesMapEntry entry : entries)
                entry.reference.compile(creator);
        }

        @Override
        public void parseNonNull(JSONObject object) {
            JSONArray array = object.getJSONArray(fieldName);
            for (int i = 0; i < array.length(); i++) {
                JSONObject entryObj = array.getJSONObject(i);
                ResourcesMapEntry entry = new ResourcesMapEntry();
                entry.reference.parse(entryObj);
                entry.value.parse(entryObj);
                entries.add(entry);
            }
        }

        @Override
        public void saveNonNull(JSONObject obj) {
            // Could make this use toArray
            JSONArray array = new JSONArray();
            for (ResourcesMapEntry entry : entries) {
                if (entry.reference.referenceName == null)
                    continue;
                JSONObject e = new JSONObject();
                entry.reference.save(e);
                entry.value.save(e);
                array.put(e);
            }
            obj.put(fieldName, array);
        }

        Set<String> getPresent() {
            HashSet<String> ret = new HashSet<>();
            for (ResourcesMapEntry entry : entries) {
                if (entry.reference.referenceName == null)
                    continue;
                ret.add(entry.reference.referenceName);
            }
            return ret;
        }

        @Override
        public CreatorType getType() {
            return CreatorType.ResourceMap;
        }

        static final class ResourcesMapEntry {
            ResourceCreatorReference reference = new ResourceCreatorReference("resource");
            IntegerCreator value = new IntegerCreator("amount");
        }
    }

    static class GameSpecCreator implements Interfaces.SpecCreator<GameSpec> {
        EnumerationCreator<VisibilitySpec> visibility = new EnumerationCreator<>("visibility", VisibilitySpec.values());
        DoubleCreator gameSpeed = new DoubleCreator("speed");
        DimensionCreator size = new DimensionCreator("size");
        GenerationCreator generation = new GenerationCreator("generator");
        final ArrayList<ResourceCreator> resources = new ArrayList<>();
        final ArrayList<EntityCreator> entities = new ArrayList<>();
        final ArrayList<WeaponSpecCreator> weapons = new ArrayList<>();
        SpecTreeCreator<CreationSpec, CreationCreator> canPlace = new SpecTreeCreator<>("globally-placeable", new SpecTree<>(), s -> new CreationCreator());

        @Override
        public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {
            try (P a = errors.withPath("spec")) {
                errors.nonNull(gameSpeed);

                try (P p = errors.withPath("size")) {
                    size.getExportErrors(creator, errors, params.cannotBeNull());
                }
                try (P p = errors.withPath("generation")) {
                    generation.getExportErrors(creator, errors, params);
                }
                try (P p = errors.withPath("resources")) {
                    errors.checkAll(resources, creator, params);
                }
                try (P p = errors.withPath("entities")) {
                    errors.checkAll(entities, creator, params);
                }
                try (P p = errors.withPath("weapons")) {
                    errors.checkAll(weapons, creator, params);
                }
            }
        }

        Collection<ResourceType> getAllResourceTypes() {
            return null;
        }

        public GameSpec create() {
            return create(new Interfaces.CreationContext(this));
        }


        @Override
        public GameSpec create(Interfaces.CreationContext cntxt) {
            ArrayList<ResourceType> resourceTypesList = new ArrayList<>(resources.size());
            for (ResourceCreator resource : resources)
                resourceTypesList.add(resource.create(cntxt));
            cntxt.resourceTypes = new Immutable.ImmutableList<>(resourceTypesList);

            ArrayList<EntitySpec> unitsList = new ArrayList<>(entities.size());
            for (EntityCreator entity : entities)
                if (entity.isExported.get())
                    unitsList.add(entity.create(cntxt));
            cntxt.unitTypes = new Immutable.ImmutableList<>(unitsList);

            for (ResourceCreator resource : resources)
                resource.fill(cntxt);
            for (EntityCreator entity : entities)
                if (entity.isExported.get())
                    entity.fill(cntxt);

            ArrayList<WeaponSpec>  weaponsList = new ArrayList<>(weapons.size());
            for (WeaponSpecCreator weapon : weapons)
                weaponsList.add(weapon.create(cntxt));
            Immutable.ImmutableList<WeaponSpec> weapons = new Immutable.ImmutableList<>(weaponsList);

            Dimension dim = size.create(cntxt);
            return new GameSpec(
                gameSpeed.getNonNull(),
                dim.width,
                dim.height,
                cntxt.resourceTypes,
                cntxt.unitTypes,
                weapons,
                generation.create(cntxt),
                visibility.create(cntxt)
            );
        }

        @Override
        public void compile(GameSpecCreator creator) {
            compile();
        }

        void compile() {
            generation.compile(this);
            for (ResourceCreator r : resources) r.compile(this);
            for (EntityCreator e : entities) e.compile(this);
            for (WeaponSpecCreator w : weapons) w.compile(this);
            canPlace.compile(this);
        }

        public void parse(JSONObject obj) {
            size.parse(obj);
            gameSpeed.parse(obj);
            visibility.parse(obj);
        }

        @Override
        public void save(JSONObject obj) {
            obj.put("meta", new JSONObject("{\"version\": \"0.0.0\"}"));
            size.save(obj);
            gameSpeed.save(obj);
            visibility.save(obj);
        }

        @Override
        public CreatorType getType() {
            return CreatorType.GameSpec;
        }
    }

    static class ResourceCreator implements Interfaces.SpecCreator<ResourceType> {
        String name;
        IntegerCreator weight = new IntegerCreator("weight");
        EntityCreatorReference growsInto = new EntityCreatorReference("grows-into");

        ResourceCreator(String name) {
            this.name = name;
            weight.set(0);
        }

        @Override
        public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {
            try (P ignore = errors.withPath(name)) {
                errors.nonNull(weight);
                if (weight.value == 0) {
                    errors.error("weight cannot be 0.");
                }
            }
        }

        @Override
        public ResourceType create(Interfaces.CreationContext cntxt) {
            return new ResourceType(name, weight.getNonNull());
        }

        void fill(Interfaces.CreationContext cntxt) {
            ResourceType unitType = cntxt.getResourceType(name);
            unitType.growsInto = growsInto.create(cntxt);
        }

        @Override
        public void compile(GameSpecCreator creator) {
            growsInto.compile(creator);
        }

        @Override
        public void parse(JSONObject object) {
            weight.parse(object);
            growsInto.parse(object);
        }

        @Override
        public void save(JSONObject obj) {
            obj.put("name", name);
            weight.save(obj);
            growsInto.save(obj);
        }

        @Override
        public CreatorType getType() {
            return CreatorType.Resource;
        }

        static ResourceCreator parseResource(JSONObject o) {
            ResourceCreator creator = new ResourceCreator(o.getString("name"));
            creator.parse(o);
            return creator;
        }

        public String toString() {
            return name;
        }

        static final ResourceCreator NONE = new ResourceCreator("None");
    }

    static class GenerationCreator implements Interfaces.SpecCreator<GenerationSpec> {
        String fieldName;

        List<UnitGenCreator> gaiaUnitGens = new LinkedList<>();
        List<ResourceGenCreator> gaiaResGens = new LinkedList<>();

        List<UnitGenCreator> byPlayerUnitGens = new LinkedList<>();
        List<ResourceGenCreator> byPlayerResGens = new LinkedList<>();

        GenerationCreator(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {
            try (P ignore = errors.withPath("gaia units")) { errors.checkAll(gaiaUnitGens, creator, params); }
            try (P ignore = errors.withPath("gaia resources")) { errors.checkAll(gaiaResGens, creator, params); }
            try (P ignore = errors.withPath("player units")) { errors.checkAll(byPlayerUnitGens, creator, params); }
            try (P ignore = errors.withPath("player resources")) { errors.checkAll(byPlayerResGens, creator, params); }
        }

        @Override
        public GenerationSpec create(Interfaces.CreationContext cntxt) {
            return new GenerationSpec(
                createList(gaiaResGens, cntxt),
                createList(gaiaUnitGens, cntxt),
                createList(byPlayerResGens, cntxt),
                createList(byPlayerUnitGens, cntxt)
            );
        }

        @Override
        public void compile(GameSpecCreator creator) {
            for (UnitGenCreator g : gaiaUnitGens) g.compile(creator);
            for (ResourceGenCreator g : gaiaResGens) g.compile(creator);
            for (UnitGenCreator g : byPlayerUnitGens) g.compile(creator);
            for (ResourceGenCreator g : byPlayerResGens) g.compile(creator);
        }

        @Override
        public void parse(JSONObject object) {
            UnitGenCreator.parse(object.getJSONArray("gaia-units"), gaiaUnitGens);
            ResourceGenCreator.parse(object.getJSONArray("gaia-resources"), gaiaResGens);
            UnitGenCreator.parse(object.getJSONArray("per-player-units"), byPlayerUnitGens);
            ResourceGenCreator.parse(object.getJSONArray("per-player-resources"), byPlayerResGens);
        }

        @Override
        public void save(JSONObject object) {
            object.put("gaia-units", toArray(gaiaUnitGens));
            object.put("gaia-resources", toArray(gaiaResGens));
            object.put("per-player-units", toArray(byPlayerUnitGens));
            object.put("per-player-resources", toArray(byPlayerResGens));
        }

        @Override
        public CreatorType getType() {
            return CreatorType.Generations;
        }
    }

    interface GenCreator {}

    static class ResourceGenCreator implements Interfaces.SpecCreator<GenerationSpec.ResourceGen>, GenCreator {
        Creators.EntityCreatorReference resource = new Creators.EntityCreatorReference("resource");
        IntegerCreator numPatches = new IntegerCreator("number-of-patches");
        IntegerCreator patchSize = new IntegerCreator("patch-size");

        @Override
        public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {
            try (P ignore = errors.withPath("resource")) {
                resource.getExportErrors(creator, errors, params);
            }
            errors.nonNull(numPatches);
            errors.nonNull(patchSize);
        }

        @Override
        public GenerationSpec.ResourceGen create(Interfaces.CreationContext cntxt) {
            return new GenerationSpec.ResourceGen(resource.create(cntxt), numPatches.create(cntxt), patchSize.create(cntxt));
        }

        @Override
        public void compile(GameSpecCreator creator) {
            resource.compile(creator);
        }

        @Override
        public void parse(JSONObject object) {
            resource.parse(object);
            numPatches.parse(object);
            patchSize.parse(object);
        }

        @Override
        public void save(JSONObject obj) {
            resource.save(obj);
            numPatches.save(obj);
            patchSize.save(obj);
        }

        @Override
        public CreatorType getType() {
            return CreatorType.ResourceGenCreator;
        }

        static ResourceGenCreator parseRG(JSONObject obj) {
            ResourceGenCreator c = new ResourceGenCreator();
            c.parse(obj);
            return c;
        }
        static void parse(JSONArray arr, List<ResourceGenCreator> to) {
            to.clear();
            for (int i=0;i<arr.length();i++) to.add(parseRG(arr.getJSONObject(i)));
        }
    }

    static class UnitGenCreator implements Interfaces.SpecCreator<GenerationSpec.UnitGen>, GenCreator {
        Creators.EntityCreatorReference unit = new Creators.EntityCreatorReference("unit");
        IntegerCreator numberToGenerate = new IntegerCreator("number");

        @Override
        public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {
            try (P ignore = errors.withPath("unit")) {
                unit.getExportErrors(creator, errors, params);
            }
            errors.nonNull(numberToGenerate);
        }

        @Override
        public GenerationSpec.UnitGen create(Interfaces.CreationContext cntxt) {
            return new GenerationSpec.UnitGen(unit.create(cntxt), numberToGenerate.create(cntxt));
        }

        @Override
        public void compile(GameSpecCreator creator) {
            unit.compile(creator);
        }

        @Override
        public void parse(JSONObject object) {
            unit.parse(object);
            numberToGenerate.parse(object);
        }

        @Override
        public void save(JSONObject obj) {
            unit.save(obj);
            numberToGenerate.save(obj);
        }

        @Override
        public CreatorType getType() {
            return CreatorType.UnitGenCreator;
        }

        static UnitGenCreator parseUG(JSONObject obj) {
            UnitGenCreator c = new UnitGenCreator();
            c.parse(obj);
            return c;
        }
        static void parse(JSONArray arr, List<UnitGenCreator> to) {
            to.clear();
            for (int i=0;i<arr.length();i++) to.add(parseUG(arr.getJSONObject(i)));
        }
    }


    static class DimensionCreator implements ValueCreator<Dimension> {
        String fieldName;
        IntegerCreator width = new IntegerCreator("width");
        IntegerCreator height = new IntegerCreator("height");
        boolean isNull = true;

        DimensionCreator(String name) {
            this.fieldName = name;
            width.set(0);
            height.set(0);
        }

        @Override
        public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {

        }

        @Override
        public Dimension create(Interfaces.CreationContext cntxt) {
            if (isNull) return null;
            return new Dimension(width.getNonNull(), height.getNonNull());
        }

        @Override
        public void compile(GameSpecCreator creator) {}

        @Override
        public void parse(JSONObject object) {
            if (!object.has(fieldName)) return;
            JSONObject obj = (JSONObject) object.get(fieldName);
            width.parse(obj);
            height.parse(obj);
            isNull = false;
        }

        @Override
        public void save(JSONObject object) {
            if (isNull) return;
            JSONObject obj = new JSONObject();
            width.save(obj);
            height.save(obj);
            object.put(fieldName, obj);
        }

        @Override
        public String getFieldName() {
            return fieldName;
        }

        @Override
        public boolean isNull() {
            return isNull;
        }

        @Override
        public void setNull(boolean selected) {
            isNull = selected;
        }

        @Override
        public CreatorType getType() {
            return CreatorType.Dimension;
        }
    }

    static class ColorCreator extends Interfaces.ValuedCreator<Color> {
        ColorCreator(String name) {
            super(name, CreatorType.Color);
        }

        @Override
        public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {
            if (value == null && !params.canBeNull) {
                errors.error("Color cannot be null");
            }
        }

        void saveNonNull(JSONObject obj) {
            JSONObject o = new JSONObject();
            o.put("red", value.getRed());
            o.put("green", value.getGreen());
            o.put("blue", value.getBlue());
            obj.put(fieldName, o);
        }

        @Override
        Color getDefaultValue() {
            return new Color(0, 0, 0);
        }

        @Override
        Color parseNonNull(JSONObject object) {
            JSONObject obj = (JSONObject) object.get(fieldName);
            int r = Math.max(0, Math.min(255, obj.getInt("red")));
            int g = Math.max(0, Math.min(255, obj.getInt("green")));
            int b = Math.max(0, Math.min(255, obj.getInt("blue")));
            return new Color(r, g, b);
        }
    }

    static class IntegerCreator extends Interfaces.ValuedCreator<Integer> {
        IntegerCreator(String name) {
            super(name, CreatorType.Integer);
        }

        @Override
        public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {
            if (params.canBeNull) return;
            if (value != null) return;
            errors.error("Cannot be null");
        }

        @Override
        Integer getDefaultValue() {
            return 0;
        }

        @Override
        Integer parseNonNull(JSONObject obj) {
            return obj.getInt(fieldName);
        }
    }

    static class BooleanCreator extends Interfaces.ValuedCreator<Boolean> {
        BooleanCreator(String name) {
            super(name, CreatorType.Boolean);
        }

        @Override
        public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {
            if (params.canBeNull) return;
            if (value != null) return;
            errors.error("Cannot be null");
        }

        @Override
        Boolean parseNonNull(JSONObject obj) {
            return obj.getBoolean(fieldName);
        }

        @Override
        Boolean getDefaultValue() {
            return false;
        }
    }

    static class DoubleCreator extends Interfaces.ValuedCreator<Double> {
        DoubleCreator(String name) {
            super(name, CreatorType.Double);
        }

        @Override
        public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {
            if (params.canBeNull) return;
            if (value != null) return;
            errors.error("Cannot be null");
        }

        @Override
        Double getDefaultValue() {
            return 0d;
        }

        @Override
        Double parseNonNull(JSONObject obj) {
            return obj.getDouble(fieldName);
        }
    }

    static class StringCreator extends Interfaces.ValuedCreator<String> {
        StringCreator(String name) {
            super(name, CreatorType.String);
        }

        @Override
        public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {
            if (params.canBeNull) return;
            if (value != null) return;
            errors.error("Cannot be null");
        }

        @Override
        String getDefaultValue() {
            return "";
        }

        @Override
        String parseNonNull(JSONObject obj) {
            return obj.getString(fieldName);
        }
    }

    static class EnumerationCreator<T extends Enum> extends Interfaces.NullableCreator<T> {
        T[] values;
        private String valueName;

        public EnumerationCreator(String name, T[] values) {
            super(name);
            this.values = values;
        }

        String getValueName() {
            return valueName;
        }

        void setValueName(String valueName) {
            isNull = (this.valueName = valueName) == null;
        }

        @Override
        public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {
        }

        @Override
        public T create(Interfaces.CreationContext cntxt) {
            for (T t : values)
                if (t.name().equals(valueName))
                    return t;
            throw new IllegalStateException(valueName);
        }

        @Override
        public void compile(GameSpecCreator creator) {}

        @Override
        void parseNonNull(JSONObject obj) {
           valueName = obj.getString(fieldName);
        }

        @Override
        void saveNonNull(JSONObject object) {
            object.put(fieldName, valueName);
        }

        @Override
        public CreatorType getType() {
            return CreatorType.Enumeration;
        }
    }


    static class EntityCreatorReference implements ValueCreator<EntitySpec> {
        String fieldName;
        String referenceName;
        EntityCreator reference;

        EntityCreatorReference(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {
            if (referenceName != null) {
                if (reference == null)
                    errors.error("Entity reference is undefined: " + referenceName);
            } else if (!params.canBeNull) {
                errors.error("Entity reference is null.");
            }
        }

        @Override
        public EntitySpec create(Interfaces.CreationContext cntxt) {
            if (referenceName == null)
                return null;
            for (EntitySpec spec : cntxt.unitTypes)
                if (spec.name.equals(referenceName))
                    return spec;
            throw new IllegalStateException("Unable to find: " + referenceName);
        }

        @Override
        public void compile(GameSpecCreator creator) {
            for (EntityCreator ec : creator.entities) {
                if (ec.name.equals(referenceName)) {
                    reference = ec;
                    return;
                }
            }
        }

        public boolean equals(Object other) {
            if (!(other instanceof EntityCreatorReference))
                return false;
            EntityCreatorReference o = (EntityCreatorReference) other;
            return !(
                (referenceName == null && o.referenceName != null) ||
                (reference == null && o.reference != null) ||
                (referenceName != null && !referenceName.equals(o.referenceName)) ||
                (reference != null && o.reference == null) ||
                (reference != null && !reference.name.equals(o.reference.name))
            );
        }

        @Override
        public void parse(JSONObject object) {
            if (!object.has(fieldName)) return;
            referenceName = object.getString(fieldName);
        }

        @Override
        public void save(JSONObject obj) {
            obj.put(fieldName, referenceName);
        }

        public void set(EntityCreator item) {
            if (item.name.equals(EntityCreator.NoEntity.name)) {
                reference = null;
                referenceName = null;
            } else {
                this.referenceName = item.name;
                this.reference = item;
            }
        }

        public void set(EntityCreatorReference toAdd) {
            reference = toAdd.reference;
            referenceName = toAdd.referenceName;
        }

        @Override
        public String getFieldName() {
            return fieldName;
        }

        @Override
        public boolean isNull() {
            return referenceName == null;
        }

        @Override
        public void setNull(boolean selected) {
            if (selected) referenceName = null;
        }

        @Override
        public CreatorType getType() {
            return CreatorType.EntityReference;
        }

        public String toString() {
            return referenceName;
        }
    }


    static class ResourceCreatorReference implements ValueCreator<ResourceType> {
        String fieldName;
        String referenceName;
        ResourceCreator reference;

        public ResourceCreatorReference(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public void getExportErrors(GameSpecCreator creator, Interfaces.Errors errors, Interfaces.ErrorCheckParams params) {
            if (referenceName != null) {
                if (reference == null)
                    errors.error("Resource reference is undefined: " + referenceName);
            } else if (!params.canBeNull) {
                errors.error("Resource reference is null.");
            }
        }

        @Override
        public ResourceType create(Interfaces.CreationContext cntxt) {
            if (referenceName == null) return null;
            for (ResourceType spec : cntxt.resourceTypes)
                if (spec.name.equals(referenceName)) return spec;
            throw new IllegalStateException("Unable to find: " + referenceName);
        }

        @Override
        public void compile(GameSpecCreator creator) {
            for (ResourceCreator rt : creator.resources) {
                if (rt.name.equals(referenceName)) {
                    reference = rt;
                    return;
                }
            }
        }

        @Override
        public void parse(JSONObject object) {
            if (!object.has(fieldName)) return;
            referenceName = object.getString(fieldName);
        }

        @Override
        public void save(JSONObject obj) {
            obj.put(fieldName, referenceName);
        }

        public void set(ResourceCreator item) {
            if (item.name.equals(ResourceCreator.NONE.name)) {
                reference = null;
                referenceName = null;
            } else {
                this.referenceName = item.name;
                this.reference = item;
            }
        }

        @Override
        public String getFieldName() {
            return fieldName;
        }

        @Override
        public boolean isNull() {
            return referenceName == null;
        }

        @Override
        public void setNull(boolean selected) {
            if (selected) referenceName = null;
        }

        @Override
        public CreatorType getType() {
            return CreatorType.ResourceReference;
        }

    }


    //    public static final EntityCreator rootEntity = new EntityCreator();
    //    static {
    //        rootEntity.name = "entity";
    //        rootEntity.size = new Dimension(1, 1);
    //        rootEntity.initialBaseHealth = 100d;
    //        rootEntity.carryCapacity = null;
    //        rootEntity.garrisonCapacity = 0;
    //        rootEntity.speed = 0d;
    //        rootEntity.los = 0d;
    //        rootEntity.collectSpeed = 0d;
    //        rootEntity.depositSpeed = 0d;
    //        rootEntity.rotationSpeed = 0d;
    //        rootEntity.attackSpeed = 0d;
    //        rootEntity.buildSpeed = 0d;
    //        rootEntity.pathSegment = "";
    //        rootEntity.minimapColor = Color.black;
    //    }
    //
    //    public static final EntityCreator rootBuilding = new EntityCreator();
    //    static {
    //        rootBuilding.parent = rootEntity;
    //        rootBuilding.name = "entity";
    //        rootBuilding.size = new Dimension(1, 1);
    //        rootBuilding.initialBaseHealth = 100d;
    //        rootBuilding.carryCapacity = null;
    //        rootBuilding.garrisonCapacity = 0;
    //        rootBuilding.speed = 0d;
    //        rootBuilding.los = 0d;
    //        rootBuilding.collectSpeed = 0d;
    //        rootBuilding.depositSpeed = 0d;
    //        rootBuilding.rotationSpeed = 0d;
    //        rootBuilding.attackSpeed = 0d;
    //        rootBuilding.buildSpeed = 0d;
    //        rootBuilding.pathSegment = "building";
    //        rootBuilding.minimapColor = Color.black;
    //    }
    //
    //    public static final EntityCreator rootUnit = new EntityCreator();
    //    static {
    //        rootUnit.parent = rootEntity;
    //        rootUnit.name = "entity";
    //        rootUnit.size = new Dimension(1, 1);
    //        rootUnit.initialBaseHealth = 100d;
    //        rootUnit.carryCapacity = null;
    //        rootUnit.garrisonCapacity = 0;
    //        rootUnit.speed = 0d;
    //        rootUnit.los = 0d;
    //        rootUnit.collectSpeed = 0d;
    //        rootUnit.depositSpeed = 0d;
    //        rootUnit.rotationSpeed = 0d;
    //        rootUnit.attackSpeed = 0d;
    //        rootUnit.buildSpeed = 0d;
    //        rootUnit.pathSegment = "unit";
    //        rootUnit.minimapColor = Color.black;
    //    }
    //
    //
    //    public static final EntityCreator rootNaturalResource = new EntityCreator();
    //    static {
    //        rootUnit.parent = rootEntity;
    //        rootUnit.name = "natural-unit";
    //        rootUnit.size = new Dimension(1, 1);
    //        rootUnit.initialBaseHealth = 100d;
    //        rootUnit.carryCapacity = null;
    //        rootUnit.garrisonCapacity = 0;
    //        rootUnit.speed = 0d;
    //        rootUnit.los = 0d;
    //        rootUnit.collectSpeed = 0d;
    //        rootUnit.depositSpeed = 0d;
    //        rootUnit.rotationSpeed = 0d;
    //        rootUnit.attackSpeed = 0d;
    //        rootUnit.buildSpeed = 0d;
    //        rootUnit.pathSegment = "natural-unit";
    //        rootUnit.minimapColor = Color.black;
    //    }
}
