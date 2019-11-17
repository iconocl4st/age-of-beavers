package client.gui.actions.global_action;


import client.app.ClientContext;
import client.gui.actions.Action;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.Player;
import common.state.sst.manager.ReversableManagerImpl;
import common.util.EntityReaderFilter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FilterAllPlayerUnits extends Action {

    private final EntityReaderFilter filter;

    public FilterAllPlayerUnits(ClientContext context, String label, EntityReaderFilter filter) {
        super(context, label);
        this.filter = filter;
    }

    // TODO: rethink how this works
    @Override
    public final boolean isEnabled(Collection<EntityReader> currentlySelected) {
        Set<ReversableManagerImpl.Pair<Player>> byType = c.gameState.playerManager.getByType(c.currentPlayer);
        for (ReversableManagerImpl.Pair<Player> pair : byType) {
            if (filter.include(new EntityReader(c.gameState, pair.entityId)))
                return true;
        }
        return false;
    }

    @Override
    public void run(Collection<EntityReader> currentlySelected) {
        Set<EntityId> set = new HashSet<>();
        Set<ReversableManagerImpl.Pair<Player>> byType = c.gameState.playerManager.getByType(c.currentPlayer);
        for (ReversableManagerImpl.Pair<Player> pair : byType) {
            if (filter.include(new EntityReader(c.gameState, pair.entityId)))
                set.add(pair.entityId);
        }
        c.selectionManager.select(set);
    }
}
