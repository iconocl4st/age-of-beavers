package common.util.query;

import common.state.EntityId;

public interface EntityQueryFilter { boolean include(EntityId entity); }
