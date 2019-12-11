package client.gui.game;

import common.state.EntityReader;
import common.util.DPoint;

public interface Command {
    void perform(DPoint location);
    void perform(EntityReader entity);
}
