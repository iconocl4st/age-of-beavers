package common.state.spec;

import common.state.EntityId;
import common.state.EntityReader;
import common.state.Player;
import common.state.sst.GameState;
import common.util.DPoint;
import common.util.json.*;

import java.io.IOException;
import java.util.*;

public class CreationSpec implements Jsonable {
    public final EntitySpec createdType;
    public final CreationMethod method;
    public final Map<ResourceType, Integer> requiredResources;
    public final double creationTime;
    public final Map<String, String> creationMethodParams;
    public final SpecTree.SpecNodeReference reference;

    public CreationSpec(
            EntitySpec t,
            CreationMethod m,
            Map<ResourceType, Integer> requiredResources,
            double creationTime,
            Map<String, String> creationMethodParams,
            SpecTree.SpecNodeReference reference
    ) {
        createdType = t;
        method = m;
        this.requiredResources = requiredResources;
        this.creationTime = creationTime;
        this.creationMethodParams = creationMethodParams;
        this.reference = reference;
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

    public Set<EntityReader> getContributingUnits(GameState state, EntityId entityId) {
        EntityReader entity = new EntityReader(state, entityId);
        if (method.equals(CreationMethod.Garrison)) {
            return entity.getGarrisoned();
        }
        if (method.equals(CreationMethod.Aura)) {
            boolean mustBeGaia = Boolean.valueOf(creationMethodParams.get("gaia-only"));
            String creatorType = creationMethodParams.get("type-name");
            double auraWidth = Double.valueOf(creationMethodParams.get("aura-width"));

            DPoint location = state.locationManager.getLocation(entityId);

            return new HashSet<>(state.locationManager.getEntitiesWithin(
                    location.x - auraWidth,
                    location.y - auraWidth,
                    location.x + auraWidth,
                    location.y + auraWidth,
                    e -> e.getType().name.equals(creatorType) &&
                            (!mustBeGaia || e.getOwner().equals(Player.GAIA))
            ));
        }
        return Collections.emptySet();
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("reference", reference, SpecTree.SpecNodeReference.Serializer, options);
        writer.writeEndDocument();
    }


    public static final DataSerializer<CreationSpec> Serializer = new DataSerializer.JsonableSerializer<CreationSpec>() {
        @Override
        public CreationSpec parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            SpecTree.SpecNodeReference reference = reader.read("reference", SpecTree.SpecNodeReference.Serializer, spec);
            reader.readEndDocument();

            if (reference.entity != null) {
                return reference.entity.canCreate.get(reference.path).getValue();
            } else {
                return spec.state.gameSpec.canPlace.get(reference.path).getValue();
            }
        }
    };
}
