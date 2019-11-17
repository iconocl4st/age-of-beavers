package common.state;

import common.util.DPoint;

import java.awt.*;

public interface LocatedEntitySpec {
    EntityId getEntityId();
    DPoint getLocation();
    Dimension getSize();
}
