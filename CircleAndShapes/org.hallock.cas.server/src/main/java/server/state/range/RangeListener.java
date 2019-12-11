package server.state.range;

import common.state.EntityReader;

public final class RangeListener {
    public final EntityReader entity;
    public final double range;

    public RangeListener(EntityReader entity, double range) {
        this.entity = entity;
        this.range = range;
    }
}
