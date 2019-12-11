package common.util.json;

import common.state.spec.GameSpec;
import common.state.sst.GameState;

public class ReadOptions {
    public GameState state;

    public GameSpec spec() {
        if (state == null)
            return null;
        return state.gameSpec;
    }
}
