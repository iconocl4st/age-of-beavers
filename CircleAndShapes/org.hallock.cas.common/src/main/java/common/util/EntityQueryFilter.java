package common.util;

import common.state.EntityId;

public interface EntityQueryFilter { boolean include(EntityId entity); }
