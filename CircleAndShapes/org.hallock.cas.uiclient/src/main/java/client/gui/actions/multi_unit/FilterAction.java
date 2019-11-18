package client.gui.actions.multi_unit;

import client.app.UiClientContext;
import client.gui.actions.Action;
import common.state.EntityId;
import common.state.EntityReader;
import common.util.EntityReaderFilter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FilterAction extends Action {

    private final EntityReaderFilter filter;

    public FilterAction(UiClientContext context, String label, EntityReaderFilter filter) {
        super(context, label);
        this.filter = filter;
    }

    @Override
    public boolean isEnabled(Collection<EntityReader> currentlySelected) {
        for (EntityReader reader : currentlySelected)
            if (filter.include(reader)) return true;
        return false;
    }

    @Override
    public void run(Collection<EntityReader> currentlySelected) {
        Set<EntityReader> set = new HashSet<>();
        for (EntityReader reader : currentlySelected) {
            if (filter.include(reader)) {
                set.add(reader);
            }
        }
        c.selectionManager.select(set);
    }
}
