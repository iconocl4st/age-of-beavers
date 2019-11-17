package common.state.spec;

import common.state.EntityId;
import common.state.EntityReader;
import common.state.Player;
import common.state.sst.GameState;
import common.util.DPoint;
import common.util.json.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class CreationSpec implements Jsonable {
    public final EntitySpec createdType;
    public final CreationMethod method;
    public final HashMap<String, String> creationMethodParams = new HashMap<>();

    public CreationSpec(EntitySpec t, CreationMethod m) {
        createdType = t;
        method = m;
    }

    public boolean equals(Object o) {
        if (!(o instanceof CreationSpec))
            return false;
        CreationSpec c = (CreationSpec) o;
        return c.createdType.equals(createdType) && c.method.equals(method);

    }

    public int hashCode() {
        return (createdType.name + ":" + method.name()).hashCode();
    }

    public Set<EntityId> getContributingUnits(GameState state, EntityId entityId) {
        EntityReader entity = new EntityReader(state, entityId);
        if (method.equals(CreationMethod.Garrison)) {
            return entity.getGarrisoned();
        }
        if (method.equals(CreationMethod.Aura)) {
            boolean mustBeGaia = Boolean.valueOf(creationMethodParams.get("gaia-only"));
            String creatorType = creationMethodParams.get("type-name");
            double auraWidth = Double.valueOf(creationMethodParams.get("aura-width"));

            DPoint location = state.locationManager.getLocation(entityId);
            return state.locationManager.getEntitiesWithin(
                    location.x - auraWidth,
                    location.y - auraWidth,
                    location.x + auraWidth,
                    location.y + auraWidth,
                    e -> state.typeManager.get(e).name.equals(creatorType) &&
                            (!mustBeGaia || state.playerManager.get(e).equals(Player.GAIA))
            );
        }
        return Collections.emptySet();
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("entity", createdType, EntitySpec.Serializer, options);
        writer.write("method", method.ordinal());
        writer.write("params", creationMethodParams, DataSerializer.StringSerializer, DataSerializer.StringSerializer, options);
        writer.writeEndDocument();
    }

    public static final DataSerializer<CreationSpec> Serializer = new DataSerializer.JsonableSerializer<CreationSpec>() {
        @Override
        public CreationSpec parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            EntitySpec entity = reader.read("entity", EntitySpec.Serializer, spec);
            CreationMethod method = reader.b(CreationMethod.values(), reader.readInt32("method"));
            CreationSpec cspec = new CreationSpec(entity, method);
            reader.read("params", cspec.creationMethodParams, DataSerializer.StringSerializer, DataSerializer.StringSerializer, spec);
            reader.readEndDocument();
            return cspec;
        }
    };
}
