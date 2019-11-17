package client.gui.game;

import common.state.EntityId;
import common.util.DPoint;

public interface Command {
    void perform(DPoint location);
    void perform(EntityId entity);
}
