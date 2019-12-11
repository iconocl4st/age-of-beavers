package server.state.range;

import common.state.EntityReader;

final class InRangeEvent {
    final EntityReader entity1;
    final EntityReader entity2;
    final double range;


    InRangeEvent(EntityReader entity1, EntityReader entity2, double range) {
        this.entity1 = entity1;
        this.entity2  = entity2;
        this.range = range;
    }

    public int hashCode() {
        return entity1.hashCode() * entity2.hashCode();
    }

    public boolean equals(Object other) {
        if (!(other instanceof InRangeEvent)) return false;
        InRangeEvent evt = (InRangeEvent) other;
        return range == evt.range &&
                (entity1.equals(evt.entity1) && entity2.equals(evt.entity2)) ||
                (entity2.equals(evt.entity1) && entity1.equals(evt.entity2));
    }
}
