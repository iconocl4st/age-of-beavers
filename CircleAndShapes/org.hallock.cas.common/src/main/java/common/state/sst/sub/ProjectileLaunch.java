package common.state.sst.sub;

import common.state.spec.attack.DamageType;
import common.state.spec.attack.ProjectileSpec;
import common.state.EntityId;
import common.state.Player;
import common.util.*;
import common.util.json.*;

import java.io.IOException;
import java.util.HashSet;

public class ProjectileLaunch implements Jsonable {
    public final ProjectileSpec projectile;
    public final double launchTime;
    public final DPoint launchLocation;
    public final double damage;
    public final DamageType damageType;
    public final double directionX;
    public final double directionY;
    public final Player launchingPlayer;

    private final HashSet<EntityId> entitiesHit = new HashSet<>();


    public ProjectileLaunch(
            ProjectileSpec projectile,
            double launchTime,
            DPoint launchLocation,
            double damage,
            DamageType damageType,
            double directionX,
            double directionY,
            Player launchingPlayer
    ) {
        this.projectile = projectile;
        this.launchTime = launchTime;
        this.launchLocation = launchLocation;
        this.damage = damage;
        this.damageType = damageType;
        this.directionX = directionX;
        this.directionY = directionY;
        this.launchingPlayer = launchingPlayer;
    }

    public DPoint getLocation(double time) {
        DPoint ret = new DPoint(
                launchLocation.x + (time - launchTime) * projectile.speed * directionX,
                launchLocation.y + (time - launchTime) * projectile.speed * directionY
        );
        if (ret.distanceTo(launchLocation) > projectile.projectileRange) {
            return null;
        }
        return ret;
    }

    public boolean hit(EntityId entityId) {
        return entitiesHit.add(entityId);
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("projectile", projectile, ProjectileSpec.Serializer, options);
        writer.write("launch-time", launchTime);
        writer.write("launch-location", launchLocation, DPoint.Serializer, options);
        writer.write("damage", damage);
        writer.write("damage-type", damageType.ordinal());
        writer.write("direction-x", directionX);
        writer.write("direction-y", directionY);
        writer.write("launching-player", launchingPlayer, Player.Serializer, options);
        writer.writeEndDocument();
    }

    public static final DataSerializer<ProjectileLaunch> Serializer = new DataSerializer.JsonableSerializer<ProjectileLaunch>() {
        @Override
        public ProjectileLaunch parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            ProjectileLaunch launch = new ProjectileLaunch(
                    reader.read("projectile", ProjectileSpec.Serializer, spec),
                    reader.readDouble("launch-time"),
                    reader.read("launch-location", DPoint.Serializer, spec),
                    reader.readDouble("damage"),
                    reader.b(DamageType.values(), reader.readInt32("damage-type")),
                    reader.readDouble("direction-x"),
                    reader.readDouble("direction-y"),
                    reader.read("launching-player", Player.Serializer, spec)
            );
            reader.readEndDocument();
            return launch;
        }
    };
}
