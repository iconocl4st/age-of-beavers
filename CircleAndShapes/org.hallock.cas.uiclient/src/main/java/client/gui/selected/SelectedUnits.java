package client.gui.selected;

import client.app.UiClientContext;
import client.state.SelectionManager;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.spec.GameSpec;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collections;
import java.util.List;

public class SelectedUnits extends JPanel implements SelectionManager.SelectionListener {

    private UiClientContext context;

    private SingleUnitSelected singleSelect;
    private MultiSelect multiSelect;
    private Showing showing = Showing.None;
    private List<EntityReader> selectedUnits = Collections.emptyList();


    public SelectedUnits(UiClientContext context) {
        this.context = context;
        singleSelect = SingleUnitSelected.createSingleSelect(context);
        multiSelect = MultiSelect.createSelectedUnits(context);
        setLayout(new GridLayout(0, 1));
    }

    @Override
    public void selectionChanged(List<EntityReader> newSelectedUnits) {
        selectedUnits = newSelectedUnits;
        if (newSelectedUnits.isEmpty()) {
            if (!showing.equals(Showing.None)) {
                removeAll();
                showing = Showing.None;
            }
        } else if (newSelectedUnits.size() == 1) {
            if (!showing.equals(Showing.Single)) {
                removeAll();
                add(singleSelect);
                showing = Showing.Single;
            }
            singleSelect.setPreferredSize(new Dimension(1, 1));
            singleSelect.setSelected(newSelectedUnits.iterator().next());
        } else {
            if (!showing.equals(Showing.Multi)) {
                removeAll();
                add(multiSelect);
                showing = Showing.Multi;
            }
            singleSelect.setPreferredSize(new  Dimension(1, 1));
            multiSelect.setSelectedLocations(newSelectedUnits);
        }
        repaint();
    }

    public EntityReader cycle() {
        if (showing.equals(Showing.Multi)) {
            return multiSelect.cycle();
        } else {
            return null;
        }
    }

    public void updateInfo() {
        switch (showing) {
            case None: break;
            case Multi: break;
            case Single: singleSelect.setLabelValues(); break;
        }
        repaint();
    }

    public void initialize(GameSpec spec) {
        singleSelect.initalize(spec);
    }

    enum Showing {
        Single,
        Multi,
        None
    }

    public static SelectedUnits createSelectedUnits(final UiClientContext clientContext) {
        final SelectedUnits ret = new SelectedUnits(clientContext);
        clientContext.selectionManager.addListener(ret);
        ret.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                ret.selectionChanged(ret.selectedUnits);
            }

            @Override
            public void componentMoved(ComponentEvent componentEvent) {
                ret.selectionChanged(ret.selectedUnits);
            }
        });
        return ret;
    }
}
