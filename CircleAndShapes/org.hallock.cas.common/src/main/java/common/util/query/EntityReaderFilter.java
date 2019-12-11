package common.util.query;

import common.state.EntityReader;

public interface EntityReaderFilter {
    boolean include(EntityReader entity);

    EntityReaderFilter Any = e -> true;
}
