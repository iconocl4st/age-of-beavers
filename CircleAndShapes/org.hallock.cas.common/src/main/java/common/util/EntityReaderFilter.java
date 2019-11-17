package common.util;

import common.state.EntityReader;

public interface EntityReaderFilter {
    boolean include(EntityReader entity);
}
