package common.state.spec.attack;

import common.Proximity;
import common.state.spec.ResourceType;

import java.util.Collections;

public class Weapons {
    // TODO: remove this class, can we already do it?
    public static final ResourceType[] RESOURCES_REQUIRED_BY_WEAPONS = new ResourceType[] {
            new ResourceType("arrow", 1),
            new ResourceType("bullet", 1),
    };

    // TODO: make this configurable within the json spec...

    public static WeaponSpec Fist = new WeaponSpec(
            "fist",
            WeaponClass.Melee,
            DamageType.Bash,
            1.0,
            Proximity.INTERACTION_DISTANCE,
            Proximity.INTERACTION_DISTANCE + 1,
            1,
            1,
            Collections.emptyMap(),
            Collections.emptyMap(),
            null,
            0.0
    );

    public static WeaponSpec Sword = new WeaponSpec(
            "sword",
            WeaponClass.Melee,
            DamageType.Slice,
            30.0,
            Proximity.INTERACTION_DISTANCE,
            Proximity.INTERACTION_DISTANCE + 1,
            1,
            1,
            Collections.emptyMap(),
            Collections.emptyMap(),
            null,
            1.0/500
    );
    public static WeaponSpec Bow = new WeaponSpec(
            "bow",
            WeaponClass.Projectile,
            DamageType.Pierce,
            10.0,
            5.0,
            6.0,
            2,
            1,
            Collections.emptyMap(),
            Collections.singletonMap(RESOURCES_REQUIRED_BY_WEAPONS[0], 1),
            new ProjectileSpec(
                    0.1,
                    4,
                    7,
                    true
            ),
            1.0/100
    );
    public static WeaponSpec Rifle = new WeaponSpec(
            "rifle",
            WeaponClass.Projectile,
            DamageType.Pierce,
            20.0,
            10.0,
            11.0,
            3,
            1,
            Collections.emptyMap(),
            Collections.singletonMap(RESOURCES_REQUIRED_BY_WEAPONS[1], 1),
            new ProjectileSpec(
                    0.1,
                    5,
                    12,
                    false
            ),
            1.0/100
    );
    public static WeaponSpec LaserGun = new WeaponSpec(
            "laser gun",
            WeaponClass.Instant,
            DamageType.Heat,
            1.0,
            10.0,
            11.0,
            0.0,
            0.0,
            Collections.emptyMap(),
            Collections.emptyMap(),
            null,
            0.0
    );
}
