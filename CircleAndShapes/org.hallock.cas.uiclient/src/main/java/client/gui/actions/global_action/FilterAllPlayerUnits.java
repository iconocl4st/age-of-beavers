package client.gui.actions.global_action;


import client.app.UiClientContext;
import client.gui.actions.Action;
import common.state.EntityReader;
import common.state.Player;
import common.state.sst.manager.RevPair;
import common.util.query.EntityReaderFilter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FilterAllPlayerUnits extends Action {

    private final EntityReaderFilter filter;

    public FilterAllPlayerUnits(UiClientContext context, String label, EntityReaderFilter filter) {
        super(context, label);
        this.filter = filter;
    }

    // TODO: rethink how this works
    @Override
    public final boolean isEnabled(Collection<EntityReader> currentlySelected) {
        Set<RevPair<Player>> byType = c.clientGameState.gameState.playerManager.getByType(c.clientGameState.currentPlayer);
        for (RevPair<Player> pair : byType) {
            if (filter.include(new EntityReader(c.clientGameState.gameState, pair.entityId)))
                return true;
        }
        return false;
    }

    @Override
    public void run(Collection<EntityReader> currentlySelected) {
        Set<EntityReader> set = new HashSet<>();
        Set<RevPair<Player>> byType = c.clientGameState.gameState.playerManager.getByType(c.clientGameState.currentPlayer);
        for (RevPair<Player> pair : byType) {
            EntityReader entityReader = new EntityReader(c.clientGameState.gameState, pair.entityId);
            if (filter.include(entityReader))
                set.add(entityReader);
        }
        c.selectionManager.select(set);
    }
}
