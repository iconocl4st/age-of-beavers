package common.util.json;

import common.algo.quad.QuadTree;
import common.state.spec.GameSpec;
import common.state.sst.GameState;

public class ReadOptions {
    public GameState state;

    public QuadTree<?> tree;

    public GameSpec spec() {
        if (state == null)
            return null;
        return state.gameSpec;
    }
}
