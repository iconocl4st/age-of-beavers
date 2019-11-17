package common.state.spec.attack;

import common.state.spec.ResourceType;
import common.util.json.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WeaponSpec implements Jsonable {
    public final String name;
    public final double cooldownTime;
    public final double attackTime;
    public final WeaponClass weaponClass;
    public final Map<ResourceType, Integer> requiredResources;
    public final Map<ResourceType, Integer> fireResources;
    public final DamageType damageType;
    public final double rangeCanStartAttacking;
    public final double rangeCanFinishAttackFrom;
    public final double damage;
    public final ProjectileSpec projectile;
    public final double decrementConditionPerUseBy;

    public WeaponSpec(
            String name,
            WeaponClass getWeaponClass,
            DamageType damageType,
            double damage,
            double innerRange,
            double outerRange,
            double cooldownTime,
            double attackTime,
            Map<ResourceType, Integer> requiredResources,
            Map<ResourceType, Integer> fireResources,
            ProjectileSpec projectile,
            double decrimentConditionPerUseBy
    ) {
        this.name = name;
        this.cooldownTime = cooldownTime;
        this.attackTime = attackTime;
        this.weaponClass = getWeaponClass;
        this.requiredResources = requiredResources;
        this.damageType = damageType;
        this.rangeCanStartAttacking = innerRange;
        this.rangeCanFinishAttackFrom = outerRange;
        this.damage = damage;
        this.projectile = projectile;
        this.decrementConditionPerUseBy = decrimentConditionPerUseBy;
        this.fireResources = fireResources;
    }

    public String toString() {
        return name;
    }

    public boolean equals(Object other) {
        return other instanceof WeaponSpec && ((WeaponSpec) other).name.equals(name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public Set<ResourceType> getAmunitionResources() {
        HashSet<ResourceType> ret = new HashSet<>();
        ret.addAll(fireResources.keySet());
        return ret;
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("name", name);
        writer.write("weapon-class", weaponClass.ordinal());
        writer.write("damage-type", damageType.ordinal());
        writer.write("damage", damage);
        writer.write("inner-range", rangeCanStartAttacking);
        writer.write("outer-range", rangeCanFinishAttackFrom);
        writer.write("cooldown-time", cooldownTime);
        writer.write("attack-time", attackTime);
        writer.write("required-resources", requiredResources, ResourceType.Serializer, DataSerializer.IntegerSerializer, options);
        writer.write("fire-resources", fireResources, ResourceType.Serializer, DataSerializer.IntegerSerializer, options);
        writer.write("projectile-spec", projectile, ProjectileSpec.Serializer, options);
        writer.write("condition-decrement", decrementConditionPerUseBy);
        writer.writeEndDocument();
    }

    public static final DataSerializer<WeaponSpec> Serializer = new DataSerializer.JsonableSerializer<WeaponSpec>() {
        @Override
        public WeaponSpec parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            Map<ResourceType, Integer> requiredResources = new HashMap<>();
            Map<ResourceType, Integer> fireResources = new HashMap<>();

            reader.readBeginDocument();
            WeaponSpec wspec = new WeaponSpec(
                    reader.readString("name"),
                    reader.b(WeaponClass.values(), reader.readInt32("weapon-class")),
                    reader.b(DamageType.values(), reader.readInt32("damage-type")),
                    reader.readDouble("damage"),
                    reader.readDouble("inner-range"),
                    reader.readDouble("outer-range"),
                    reader.readDouble("cooldown-time"),
                    reader.readDouble("attack-time"),
                    reader.read("required-resources", requiredResources, ResourceType.Serializer, DataSerializer.IntegerSerializer, spec),
                    reader.read("fire-resources", fireResources, ResourceType.Serializer, DataSerializer.IntegerSerializer, spec),
                    reader.read("projectile-spec", ProjectileSpec.Serializer, spec),
                    reader.readDouble("condition-decrement")
            );
            reader.readEndDocument();
            return wspec;
        }
    };
}
