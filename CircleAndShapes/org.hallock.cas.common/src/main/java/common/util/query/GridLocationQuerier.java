package common.util.query;

import common.state.spec.EntityClasses;
import common.state.spec.EntitySpec;
import common.state.sst.GameState;
import common.state.sst.sub.Load;

public class GridLocationQuerier {

    public static final EntityQueryFilter ANY = entity -> true;
    public static final EntityReaderFilter ANY_ENTITY = entity -> true;

    public static EntityQueryFilter createNonEmptyNaturalResourceFilter(final GameState state, final EntitySpec naturalResourceType) {
        return entity -> {
            EntitySpec type = state.typeManager.get(entity);
            Load load = state.carryingManager.get(entity);
            return load != null && load.getWeight() > 0 && type != null && type.containsClass(EntityClasses.NATURAL_RESOURCE) && type.equals(naturalResourceType);
        };
    }

    public static EntityReaderFilter createNonEmptyNaturalResourceReaderFilter(final GameState state, final EntitySpec naturalResourceType) {
        return entity -> {
            EntitySpec type = entity.getType();
            Load load = entity.getCarrying();
            return load != null && load.getWeight() > 0 && type != null && type.containsClass(EntityClasses.NATURAL_RESOURCE) && type.equals(naturalResourceType);
        };
    }
}
