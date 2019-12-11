package common.state.edit;

import common.state.spec.SpecTree;
import common.util.Util;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

class Editors {
    // TODO: move out the nullable parts...
    static <T> Component createEditor(GameSpecEditorContext c, Interfaces.SpecCreator<T> creator, boolean allowNulls) {
        switch (creator.getType()) {
            case Capacity: return createNullable(allowNulls, (Interfaces.ValueCreator<?>) creator, createEditButton(c, () -> createCapacitySelector(c, (Creators.CapacityCreator) creator)));
            case Double: return createNullable(allowNulls, (Interfaces.ValueCreator<?>) creator, createNonNegativeDoubleSelector((Creators.DoubleCreator) creator));
            case String: return createNullable(allowNulls, (Interfaces.ValueCreator<?>) creator, createStringSelector((Creators.StringCreator) creator));
            case Boolean: return createNullable(allowNulls, (Interfaces.ValueCreator<?>) creator, createBooleanSelector((Creators.BooleanCreator) creator));
            case Integer: return createNullable(allowNulls, (Interfaces.ValueCreator<?>) creator, createNonNegativeSelector((Creators.IntegerCreator) creator));
            case Dimension: return createNullable(allowNulls, (Interfaces.ValueCreator<?>) creator, createDimensionEditor((Creators.DimensionCreator) creator));
            case DroppedOnDeath: return createNullable(allowNulls, (Interfaces.ValueCreator<?>) creator, createEditButton(c, () -> createEntitySetSelector(c, ((Creators.DropsOnDeath)creator).references)));
            case ResourceMap: return createNullable(allowNulls, (Interfaces.ValueCreator<?>) creator, createEditButton(c, () -> createResourceMapSelector(c, (Creators.ResourcesMapCreator) creator)));
            case GameSpec: return createGameSpecEditor(c, (Creators.GameSpecCreator) creator);
            case Resource: return createResourceEditor(c, (Creators.ResourceCreator) creator);
            case EntitySpec: return createEntityEditor(c, (Creators.EntityCreator) creator);
            case Creations: return createCreations(c, (Creators.CreationCreator) creator);
            case Enumeration: return createEnumerationSelector((Creators.EnumerationCreator<?>) creator);
            case Generations: return createGenerationEditor(c, (Creators.GenerationCreator) creator);
            case EntityReference: return createEntityReferenceSelector(c, (Creators.EntityCreatorReference) creator);
            case ResourceReference: return createResourceReferenceSelector(c, (Creators.ResourceCreatorReference) creator, Collections.emptySet());
            case CraftingCreator: return createCraftingEditor(c, (Creators.CraftingCreator) creator);
            case Strings: return createNullable(allowNulls, (Interfaces.ValueCreator<?>) creator, createStringSelector(((Creators.StringsCreator<?>) creator).stringCreator));
            case ResourceGenCreator: throw new IllegalStateException();
            case UnitGenCreator: throw new IllegalStateException();
            case WeaponSpec: return createWeaponSpecEditor(c, (Creators.WeaponSpecCreator) creator);
        }
        throw new UnsupportedOperationException(creator.getType().name());
    }

    private static JButton createEditButton(GameSpecEditorContext c, Interfaces.ComponentCreator cr) {
        return Swing.createButton("Edit...", () -> c.stack.push(cr.create()));
    }

    private static Component createBooleanSelector(Creators.BooleanCreator creator) {
        JCheckBox jComboBox = new JCheckBox();
        jComboBox.setSelected(!creator.isNull() && creator.get());
        jComboBox.addItemListener(e -> creator.set(jComboBox.isSelected()));
        return jComboBox;
    }

    private static JComboBox createEntityReferenceSelector(GameSpecEditorContext c, Creators.EntityCreatorReference current) {
        Vector<Creators.EntityCreator> creators = new Vector<>(c.spec.entities);
        creators.add(Creators.EntityCreator.NoEntity);
        JComboBox<Creators.EntityCreator> jComboBox = new JComboBox<>(new DefaultComboBoxModel<>(creators));
        if (current.reference != null)
            jComboBox.setSelectedItem(current.reference);
        else
            jComboBox.setSelectedItem(Creators.EntityCreator.NoEntity);
        jComboBox.addItemListener(itemEvent -> current.set((Creators.EntityCreator) itemEvent.getItem()));
        return jComboBox;
    }

    private static DefaultComboBoxModel<Creators.ResourceCreator> createResourceCreatorModel(GameSpecEditorContext c, Set<String> present) {
        Vector<Creators.ResourceCreator> creators = new Vector<>(c.spec.resources);
        creators.removeIf(r -> present.contains(r.name));
        creators.add(Creators.ResourceCreator.NONE);
        return new DefaultComboBoxModel<>(creators);
    }

    private static JComboBox<Creators.ResourceCreator> createResourceReferenceSelector(GameSpecEditorContext c, Creators.ResourceCreatorReference current, Set<String> present) {
        JComboBox<Creators.ResourceCreator> jComboBox = new JComboBox<>(createResourceCreatorModel(c, present));
        if (current.reference != null)
            jComboBox.setSelectedItem(current.reference);
        else
            jComboBox.setSelectedItem(Creators.ResourceCreator.NONE);
        jComboBox.addItemListener(itemEvent -> current.set((Creators.ResourceCreator) itemEvent.getItem()));
        return jComboBox;
    }

    private static Component createNullable(boolean allowNulls, Interfaces.ValueCreator<? extends Object> creator, Component c) {
        if (!allowNulls)
            return c;
        JCheckBox jCheckBox = new JCheckBox("Is null");
        jCheckBox.setSelected(creator.isNull());
        c.setEnabled(!creator.isNull());
        jCheckBox.addItemListener(itemEvent -> {
            c.setEnabled(!jCheckBox.isSelected());
            creator.setNull(!jCheckBox.isSelected());
        });
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 0));
        panel.add(jCheckBox);
        panel.add(c);
        return panel;
    }

    private static JSpinner createNonNegativeDoubleSelector(Creators.DoubleCreator value) {
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(Util.zin(value.get()), 0, Integer.MAX_VALUE, 0.01);
        JSpinner spinner = new JSpinner(spinnerNumberModel);
        spinner.addChangeListener(changeEvent -> value.set(spinnerNumberModel.getNumber().doubleValue()));
        return spinner;
    }

    private static JSpinner createNonNegativeSelector(Creators.IntegerCreator value) {
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(Util.zin(value.get()), 0, Integer.MAX_VALUE, 1);
        JSpinner spinner = new JSpinner(spinnerNumberModel);
        spinner.addChangeListener(changeEvent -> value.set(spinnerNumberModel.getNumber().intValue()));
        return spinner;
    }

    private static <T extends Enum> JComboBox createEnumerationSelector(Creators.EnumerationCreator<T> creator) {
        String[] stringValues = new String[creator.values.length];
        for (int i = 0; i < creator.values.length; i++)
            stringValues[i] = creator.values[i].name();
        JComboBox<String> jComboBox = new JComboBox<>(stringValues);
        jComboBox.setSelectedItem(creator.getValueName());
        jComboBox.addItemListener(itemEvent -> creator.setValueName((String) itemEvent.getItem()));
        return jComboBox;
    }

    private static JTextComponent createStringSelector(Creators.StringCreator value) {
        return Swing.createTextField(value.get(), value::set);
    }

    private static JPanel createDimensionEditor(Creators.DimensionCreator value) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2));
        panel.add(createNonNegativeSelector(value.width));
        panel.add(createNonNegativeSelector(value.height));
        return panel;
    }

    private static JPanel createCapacitySelector(GameSpecEditorContext c, Creators.CapacityCreator creator) {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        JPanel weightPanel = new JPanel(new GridLayout(0,  2));
        weightPanel.add(Swing.createLabel("Total weight"));
        weightPanel.add(createEditor(c, creator.maximumWeight, false));
        panel.add(weightPanel);
        panel.add(createResourceMapSelector(c, creator.mapCreator));
        return panel;
    }

    private static void addResourceMapEntry(
            GameSpecEditorContext c,
            Creators.ResourcesMapCreator creator,
            Creators.ResourcesMapCreator.ResourcesMapEntry entry,
            JComboBox<Creators.ResourceCreator> box,
            ActuallyUsefulTable table
    ) {
        JButton removeButton = new JButton("Remove");
        Component[] components = new Component[]{
                Swing.createLabel(entry.reference.referenceName),
                createEditor(c, entry.value, false),
                removeButton
        };
        table.addRow(components);
        removeButton.addActionListener(e -> {
            creator.entries.remove(entry);
            box.setModel(createResourceCreatorModel(c, creator.getPresent()));
            box.setSelectedItem(Creators.ResourceCreator.NONE);
            table.removeRow(components);
        });
    }

    private static Component createResourceMapSelector(GameSpecEditorContext c, Creators.ResourcesMapCreator creator) {
        Set<String> present = creator.getPresent();

        JPanel addPanel = new JPanel();
        addPanel.setLayout(new GridLayout(0, 1));

        ActuallyUsefulTable table = ActuallyUsefulTable.createTable("Resource", "Amount", "");

        addPanel.setLayout(new GridLayout(0, 2));
        Creators.ResourceCreatorReference reference = new Creators.ResourceCreatorReference("type");
        JComboBox<Creators.ResourceCreator> box = createResourceReferenceSelector(c, reference, present);
        addPanel.add(box);
        addPanel.add(Swing.createButton("Add", () -> {
            if (reference.referenceName == null || reference.referenceName.equals(Creators.ResourceCreator.NONE.name))
                return;
            Creators.ResourcesMapCreator.ResourcesMapEntry entry = new Creators.ResourcesMapCreator.ResourcesMapEntry();
            entry.reference.set(reference.reference);
            entry.value.set(0);
            creator.entries.add(entry);
            box.setModel(createResourceCreatorModel(c, creator.getPresent()));
            box.setSelectedItem(Creators.ResourceCreator.NONE);
            addResourceMapEntry(c, creator, entry, box, table);
        }));

        for (Creators.ResourcesMapCreator.ResourcesMapEntry entry : creator.entries)
            addResourceMapEntry(c, creator, entry, box, table);

        JPanel panel = new JPanel();
        ProportionalLayout proportionalLayout = new ProportionalLayout(panel);
        panel.setLayout(proportionalLayout);

        panel.add(addPanel);
        proportionalLayout.setPosition(addPanel, new Rectangle2D.Double(0, 0, 1, 0.1));

        panel.add(table);
        proportionalLayout.setPosition(table, new Rectangle2D.Double(0, 0.1, 1, 0.9));

        return panel;
    }

    private static Component createEntitySetSelector(
            GameSpecEditorContext c,
            Set<Creators.EntityCreatorReference> currentSet
    ) {
        Creators.EntityCreatorReference toAdd = new Creators.EntityCreatorReference(null);
        Interfaces.Container<Creators.EntityCreatorReference> currentlySelected = new Interfaces.Container<>(null);

        JComboBox box = createEntityReferenceSelector(c, toAdd);
        DefaultListModel<Creators.EntityCreatorReference> defaultListModel = new DefaultListModel<>();
        for (Creators.EntityCreatorReference t : currentSet) defaultListModel.addElement(t);

        JList<Creators.EntityCreatorReference> jList = new JList<>(defaultListModel);
        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton addButton = Swing.createButton("Add", () -> {
            if (toAdd.reference == null) return;
            Creators.EntityCreatorReference reference = new Creators.EntityCreatorReference(null);
            reference.set(toAdd);
            currentSet.add(reference);
            defaultListModel.removeElement(reference);
            defaultListModel.addElement(reference);

            box.setSelectedItem(Creators.EntityCreator.NoEntity);
            jList.updateUI();
        });
//            addButton.setEnabled(s.length() != 0 && !Interfaces.NameGetter.nameExists(currentList, names, s));

        JButton removeButton = Swing.createButton("Remove", () -> {
            Creators.EntityCreatorReference selected = currentlySelected.value;
            if (selected == null) throw new IllegalStateException();
            currentSet.remove(currentlySelected.value);
            defaultListModel.removeElement(currentlySelected.value);
            currentlySelected.value = null;
        });
        removeButton.setEnabled(false);
        jList.addListSelectionListener(listSelectionEvent -> {
            currentlySelected.value = jList.getSelectedValue();
            removeButton.setEnabled(currentlySelected.value != null);
        });

        return createAddAndRemovePanel(addButton, box, null, removeButton, jList);
    }

    private static <G, T extends Interfaces.SpecCreator<G>> Component createListSelector(
            GameSpecEditorContext c,
            ArrayList<T> currentList,
            Interfaces.NameGetter<T> names,
            Interfaces.Creator<T> creator
    ) {
        Interfaces.Container<String> currentNewName = new Interfaces.Container<>("");
        Interfaces.Container<T> currentlySelected = new Interfaces.Container<>(null);
        Interfaces.Container<JTextComponent> nameSelector = new Interfaces.Container<>(null);

        DefaultListModel<T> defaultListModel = new DefaultListModel<>();
        for (T t : currentList) defaultListModel.addElement(t);
        JList<T> jList = new JList<>(defaultListModel);
        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton addButton = Swing.createButton("Add", () -> {
            T t = creator.create(currentNewName.value);
            currentList.add(t);
            defaultListModel.addElement(t);
            nameSelector.value.setText("");
        });
        addButton.setEnabled(false);

        nameSelector.value = Swing.createTextField(currentNewName.value, s -> {
            currentNewName.value = s;
            addButton.setEnabled(s.length() != 0 && !Interfaces.NameGetter.nameExists(currentList, names, s));
        });

        JButton removeButton = Swing.createButton("Remove", () -> {
            T selected = currentlySelected.value;
            if (selected == null) throw new IllegalStateException();
            currentList.remove(selected);
            defaultListModel.removeElement(selected);
        });
        removeButton.setEnabled(false);
        JButton editButton = createEditButton(c, () -> createEditor(c, currentlySelected.value, false));

//        Swing.createButton("Edit", e -> {
//            T selected = Interfaces.NameGetter.get(currentList, names, currentlySelected.value);
//            if (selected == null) throw new IllegalStateException();
//            editor.edit(selected);
//        });
        editButton.setEnabled(false);

        jList.addListSelectionListener(listSelectionEvent -> {
            currentlySelected.value = jList.getSelectedValue();
            removeButton.setEnabled(currentlySelected.value != null);
            editButton.setEnabled(currentlySelected.value != null);
        });

        return createAddAndRemovePanel(addButton, nameSelector.value, editButton, removeButton, jList);
    }

    private static void createNode(Creators.EntityCreator creator, DefaultMutableTreeNode parent) {
        DefaultMutableTreeNode defaultMutableTreeNode = new DefaultMutableTreeNode(creator);
        for (Creators.EntityCreator child : creator.children)
            createNode(child, defaultMutableTreeNode);
        parent.add(defaultMutableTreeNode);
    }

    private static void removeEntity(GameSpecEditorContext context, Creators.EntityCreator value) {
        context.spec.entities.remove(value);
        for (Creators.EntityCreator creator : value.children)
            removeEntity(context, creator);
    }


    private static Component createCreations(GameSpecEditorContext c, Creators.CreationCreator creator) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2));

        panel.add(Swing.createLabel("Creation Method"));
        panel.add(createEditor(c, creator.method, false));

        panel.add(Swing.createLabel("Creation method args"));
        panel.add(createEditor(c, creator.creationArgs, false));

        panel.add(Swing.createLabel("Created Entity"));
        panel.add(createEditor(c, creator.created, false));

        panel.add(Swing.createLabel("Required resources"));
        panel.add(createEditor(c, creator.requiredResources, false));

        panel.add(Swing.createLabel("Creation time"));
        panel.add(createEditor(c, creator.creationTime, false));

        return panel;
    }

    private static void addCreationPanel(GameSpecEditorContext c, ArrayList<Creators.CreationCreator> creations, ActuallyUsefulTable table, Creators.CreationCreator creator) {
        JButton removeButton = new JButton("Remove");
        Component[] row = new Component[]{
            createEditor(c, creator.method, false),
            createEditor(c, creator.creationArgs, false),
            createEditor(c, creator.created, false),
            createEditor(c, creator.requiredResources, false),
            createEditor(c, creator.creationTime, false),
            removeButton
        };

        removeButton.addActionListener(e -> {
            table.removeRow(row);
            creations.remove(creator);
        });

        table.addRow(row);
    }







    private static final class TreeEditNode<T> {
        SpecTree.SpecNode<T> node;
        String name;
        DefaultMutableTreeNode treeNode;
        TreeEditNode<T> parent;

        public String toString() {
            return name;
        }
    }

    private static <T> TreeEditNode<T> addNodeToTreeUi(SpecTree.SpecNode<T> node, String name, TreeEditNode<T> parent) {
        TreeEditNode<T> ten = new TreeEditNode<>();
        ten.node = node;
        ten.name = name;
        ten.treeNode = new DefaultMutableTreeNode(ten);
        ten.parent = parent;

        if (node instanceof SpecTree.SpecBranchNode) {
            SpecTree.SpecBranchNode<T> branchNode = (SpecTree.SpecBranchNode<T>) node;
            for (Map.Entry<String, SpecTree.SpecNode<T>> entry : branchNode.children.entrySet())
                addNodeToTreeUi(entry.getValue(), entry.getKey(), ten);
        } else if (!(node instanceof SpecTree.SpecLeafNode)) {
            throw new IllegalStateException();
        }
        if (parent != null)
            parent.treeNode.add(ten.treeNode);
        return ten;
    }

    private static <G, T extends Interfaces.SpecCreator<G>> JPanel createSpecTree(GameSpecEditorContext c, Creators.SpecTreeCreator<G, T> creator) {
        Interfaces.Container<TreeEditNode<T>> selectedNode = new Interfaces.Container<>(null);
        Interfaces.Container<String> newName = new Interfaces.Container<>(null);

        DefaultMutableTreeNode rootNode = addNodeToTreeUi(creator.specTreeValue.root, "root", null).treeNode;
        JTree tree = new JTree(rootNode);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridLayout(1, 0));
        JButton addLeafButton = new JButton("Add leaf");
        optionsPanel.add(addLeafButton);
        addLeafButton.setEnabled(false);
        JButton addBranchButton = new JButton("Add branch");
        optionsPanel.add(addBranchButton);
        addBranchButton.setEnabled(false);
        JTextComponent newNameField = Swing.createTextField("", s -> {
            newName.value = s;
            boolean b = s.length() != 0 && selectedNode.value != null && selectedNode.value.node instanceof SpecTree.SpecBranchNode;
            addBranchButton.setEnabled(b);
            addLeafButton.setEnabled(b);
        });
        optionsPanel.add(newNameField);
        JButton removeButton = new JButton("Remove");
        optionsPanel.add(removeButton);
        removeButton.setEnabled(false);
        JButton editButton = new JButton("Edit");
        optionsPanel.add(editButton);
        editButton.setEnabled(false);

        removeButton.addActionListener(e -> {
            SpecTree.SpecBranchNode<T> branchNode = (SpecTree.SpecBranchNode<T>) selectedNode.value.parent.node;
            branchNode.children.remove(selectedNode.value.name);
            selectedNode.value.treeNode.removeFromParent();
            tree.updateUI(); tree.revalidate(); tree.repaint();
            selectedNode.value = null;

            editButton.setEnabled(false);
            removeButton.setEnabled(false);
            addBranchButton.setEnabled(false);
            addLeafButton.setEnabled(false);
        });
        editButton.addActionListener(e -> {
            SpecTree.SpecLeafNode<T> leafNode = (SpecTree.SpecLeafNode<T>) selectedNode.value.node;
            c.stack.push(createEditor(c, leafNode.value, false));
        });
        addBranchButton.addActionListener(e -> {
            SpecTree.SpecBranchNode<T> branchNode = (SpecTree.SpecBranchNode<T>) selectedNode.value.node;
            SpecTree.SpecNode<T> leaf = new SpecTree.SpecBranchNode<>();
            branchNode.children.put(newName.value, leaf);
            addNodeToTreeUi(leaf, newName.value, selectedNode.value);
            newName.value = null;
            newNameField.setText("");
            tree.updateUI(); tree.revalidate(); tree.repaint();
        });
        addLeafButton.addActionListener(e -> {
            SpecTree.SpecBranchNode<T> branchNode = (SpecTree.SpecBranchNode<T>) selectedNode.value.node;
            SpecTree.SpecNode<T> leaf = new SpecTree.SpecLeafNode<>(creator.creator.create(null));
            branchNode.children.put(newName.value, leaf);
            addNodeToTreeUi(leaf, newName.value, selectedNode.value);
            newName.value = null;
            addBranchButton.setEnabled(false);
            addLeafButton.setEnabled(false);
            newNameField.setText("");
            tree.updateUI(); tree.revalidate(); tree.repaint();
        });

        tree.getSelectionModel().addTreeSelectionListener(treeSelectionEvent -> {
            TreePath path = treeSelectionEvent.getPath();
            if (path == null) {
                selectedNode.value = null;
            } else {
                selectedNode.value = (TreeEditNode<T>) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
            }

            boolean hasNewName = newName.value != null && newName.value.length() != 0;
            removeButton.setEnabled(selectedNode.value != null && selectedNode.value.parent != null);
            editButton.setEnabled(selectedNode.value.node instanceof SpecTree.SpecLeafNode);
            addBranchButton.setEnabled(hasNewName && selectedNode.value.node instanceof SpecTree.SpecBranchNode);
            addLeafButton.setEnabled(hasNewName && selectedNode.value.node instanceof SpecTree.SpecBranchNode);
        });


        JPanel ret = new JPanel();
        ProportionalLayout layout = new ProportionalLayout(ret);

        ret.add(optionsPanel);
        layout.setPosition(optionsPanel, new Rectangle2D.Double(0, 0, 1, 0.1));

        ret.add(tree);
        layout.setPosition(tree, new Rectangle2D.Double(0, 0.1, 1, 0.9));

        layout.setPositions();

        return ret;
    }

    private static JPanel createCraftingEditor(GameSpecEditorContext c, Creators.CraftingCreator creator) {
        JPanel inputPanel = new JPanel();
        inputPanel.setBorder(new TitledBorder("Crafting inputs"));
        inputPanel.setLayout(new GridLayout(0, 1));
        inputPanel.add(createResourceMapSelector(c, creator.inputs));

        JPanel outputPanel = new JPanel();
        outputPanel.setBorder(new TitledBorder("Crafting outputs"));
        outputPanel.setLayout(new GridLayout(0, 1));
        outputPanel.add(createResourceMapSelector(c, creator.outputs));

        JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(inputPanel);
        splitPane.setRightComponent(outputPanel);
        splitPane.setDividerLocation(c.stack.getParent().getWidth() / 2);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));
        panel.add(splitPane);
        return panel;
    }

//    private static JPanel createCreationsList(GameSpecEditorContext c, ArrayList<Creators.CreationCreator> creations) {
//        JPanel ret = new JPanel();
//        ProportionalLayout pl = new ProportionalLayout(ret);
//        ret.setLayout(pl);
//
//        ActuallyUsefulTable table = ActuallyUsefulTable.createTable(
//                "Creation method",
//                "Creation method args",
//                "Created entity",
//                "Required resources",
//                "Creation time",
//                ""
//        );
//
//        for (Creators.CreationCreator creator : creations)
//            addCreationPanel(c, creations, table, creator);
//
//        JPanel addPanel = new JPanel();
//        addPanel.setLayout(new GridLayout(1, 0));
//        addPanel.add(Swing.createButton("Add", () -> {
//            Creators.CreationCreator creationCreator = new Creators.CreationCreator();
//            creations.add(creationCreator);
//            addCreationPanel(c, creations, table, creationCreator);
//            table.revalidate();
//            table.repaint();
//        }));
//
//        ret.add(table);
//        pl.setPosition(table, new Rectangle2D.Double(0, 0, 1, 0.9));
//
//        ret.add(addPanel);
//        pl.setPosition(addPanel, new Rectangle2D.Double(0, 0.9, 1, 0.1));
//
//        return ret;
//    }

    private static Component createTreeSelector(
            GameSpecEditorContext context,
            ArrayList<Creators.EntityCreator> currentList
    ) {
        Interfaces.Container<String> currentNewName = new Interfaces.Container<>("");
        Interfaces.Container<Creators.EntityCreator> selectedCreator = new Interfaces.Container<>(null);
        Interfaces.Container<DefaultMutableTreeNode> selectedNode = new Interfaces.Container<>(null);
        Interfaces.Container<JTextComponent> nameSelector = new Interfaces.Container<>(null);

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("root");
        for (Creators.EntityCreator c : currentList) {
            if (c.parent.reference != null)
                continue;
            createNode(c, rootNode);
        }
        JTree jTree = new JTree(rootNode);
        jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        JButton addButton = Swing.createButton("Add", () -> {
            Creators.EntityCreator creator = new Creators.EntityCreator(currentNewName.value);
            currentList.add(creator);
            if (selectedCreator.value == null) {
                rootNode.add(new DefaultMutableTreeNode(creator));
            } else {
                creator.parent.set(selectedCreator.value);
                selectedCreator.value.children.add(creator);
                selectedNode.value.add(new DefaultMutableTreeNode(creator));
            }
            nameSelector.value.setText("");
            jTree.updateUI();
            jTree.revalidate();
            jTree.repaint();
        });
        addButton.setEnabled(false);

        nameSelector.value = Swing.createTextField(currentNewName.value, s -> {
            currentNewName.value = s;
            addButton.setEnabled(s.length() != 0 && !s.equals(Creators.EntityCreator.NoEntity.name) && !Interfaces.NameGetter.nameExists(currentList, Creators.EntityCreator::toString, s));
        });

        JButton removeButton = Swing.createButton("Remove", () -> {
            selectedNode.value.removeFromParent();
            if (selectedCreator.value.parent.reference != null) {
                selectedCreator.value.parent.reference.children.remove(selectedCreator.value);
            }
            removeEntity(context, selectedCreator.value);
            jTree.updateUI();
        });
        removeButton.setEnabled(false);
        JButton editButton = createEditButton(context, () -> createEditor(context, selectedCreator.value, false));
        editButton.setEnabled(false);

        jTree.getSelectionModel().addTreeSelectionListener(treeSelectionEvent -> {
            TreePath path = treeSelectionEvent.getPath();
            if (path == null) {
                selectedCreator.value = null;
                selectedNode.value = null;
            } else {
                selectedNode.value = (DefaultMutableTreeNode) path.getLastPathComponent();
                selectedCreator.value = (Creators.EntityCreator) (selectedNode.value).getUserObject();
            }
            removeButton.setEnabled(selectedCreator.value != null);
            editButton.setEnabled(selectedCreator.value != null);
        });

        // TODO: makes two scroll panes...
        return createAddAndRemovePanel(addButton, nameSelector.value, editButton, removeButton, jTree);
    }

    private static JPanel createAddAndRemovePanel(
            JButton addButton,
            Component nameSelector,
            JButton editButton,
            JButton removeButton,
            Container displayer
    ) {
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridLayout(1, 0));
        optionsPanel.add(addButton);
        optionsPanel.add(nameSelector);
        optionsPanel.add(removeButton);
        if (editButton != null)
            optionsPanel.add(editButton);

        JScrollPane jScrollPane = new JScrollPane(displayer);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);


        JPanel ret = new JPanel();
        ProportionalLayout layout = new ProportionalLayout(ret);

        ret.add(optionsPanel);
        layout.setPosition(optionsPanel, new Rectangle2D.Double(0, 0, 1, 0.1));

        ret.add(jScrollPane);
        layout.setPosition(jScrollPane, new Rectangle2D.Double(0, 0.1, 1, 0.9));

        layout.setPositions();

        return ret;
    }

    private static JPanel createGameSpecEditor(GameSpecEditorContext c, Creators.GameSpecCreator creator) {
        JPanel editor = new JPanel();

        editor.setLayout(new GridLayout(0, 2));

        editor.add(Swing.createLabel("Visibility"));
        editor.add(createEditor(c, creator.visibility, false));

        editor.add(Swing.createLabel("Game speed"));
        editor.add(createEditor(c, creator.gameSpeed, false));

        editor.add(Swing.createLabel("Size"));
        editor.add(createEditor(c, creator.size, false));

        editor.add(Swing.createLabel("Resources"));
        editor.add(createEditButton(c, () -> Editors.createListSelector(
            c,
            creator.resources,
            r -> r.name,
            Creators.ResourceCreator::new
        )));

        editor.add(Swing.createLabel("Entities"));
        editor.add(createEditButton(c, () -> Editors.createTreeSelector(c, creator.entities)));

        editor.add(Swing.createLabel("Weapons"));
        editor.add(createEditButton(c, () -> Editors.createListSelector(
                c,
                creator.weapons,
                r -> r.name,
                Creators.WeaponSpecCreator::new
        )));

        editor.add(Swing.createLabel("Generation"));
        editor.add(createEditButton(c, () -> Editors.createEditor(c, creator.generation, false)));

        editor.add(Swing.createLabel("Root level creation"));
        editor.add(createEditButton(c, () -> createSpecTree(c, creator.canPlace)));

        return editor;
    }

    private static Component createEntityEditor(GameSpecEditorContext c, Creators.EntityCreator entity) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 3));

        panel.add(Swing.createLabel("Entity"));
        panel.add(Swing.createLabel(entity.name));
        panel.add(new JPanel());

        panel.add(Swing.createLabel("Parent"));
        panel.add(Editors.createEntityReferenceSelector(c, entity.parent));
        panel.add(new JPanel());

        boolean hasParent = entity.parent.reference != null;

        for (Interfaces.ValueCreator<?> e : entity.fields) {
            panel.add(Swing.createLabel(e.getFieldName()));
            panel.add(Editors.createEditor(c, e, true));
            if (hasParent)
                panel.add(Swing.createLabel(Interfaces.InheritedValue.toString(entity.parent.reference.locateInheretedValue(c, e, new LinkedList<>()))));
            else
                panel.add(new JPanel());
        }

        panel.add(Swing.createLabel("Production"));
        panel.add(createEditButton(c, () -> createSpecTree(c, entity.canCreate)));
        panel.add(new JPanel());

        panel.add(Swing.createLabel("Crafting"));
        panel.add(createEditButton(c, () -> createSpecTree(c, entity.canCraft)));
        panel.add(new JPanel());

        return panel;
    }

    private static JPanel createResourceEditor(GameSpecEditorContext c, Creators.ResourceCreator resource) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2));

        panel.add(Swing.createLabel("Resource"));
        panel.add(Swing.createLabel(resource.name));

        panel.add(Swing.createLabel("Weight"));
        panel.add(Editors.createEditor(c, resource.weight,false));

        panel.add(Swing.createLabel("Grows into"));
        panel.add(Editors.createEditor(c, resource.growsInto,false));

        return panel;
    }

    private static JPanel createGenEditor(GameSpecEditorContext c, Creators.GenCreator creator, Interfaces.Remover<JPanel> onRemove) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 0));
        if (creator instanceof Creators.ResourceGenCreator) {
            Creators.ResourceGenCreator rg = (Creators.ResourceGenCreator) creator;
            panel.add(createEditor(c, rg.resource, false));
            panel.add(createEditor(c, rg.numPatches, false));
            panel.add(createEditor(c, rg.patchSize, false));
        } else if (creator instanceof Creators.UnitGenCreator) {
            Creators.UnitGenCreator ug = (Creators.UnitGenCreator) creator;
            panel.add(createEditor(c, ug.unit, false));
            panel.add(createEditor(c, ug.numberToGenerate, false));
        }
        panel.add(Swing.createButton("Remove", () -> onRemove.remove(panel)));
        return panel;
    }

    private static <T extends Creators.GenCreator> JPanel createGenEditors(GameSpecEditorContext c, String[] headers, List<T> creators, Interfaces.Creator<T> creator, String title) {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(title));

        ProportionalLayout layout = new ProportionalLayout(panel);
        panel.setLayout(layout);

        JPanel headerPanel = new JPanel(new GridLayout(1, 0));
        for (String header : headers)
            headerPanel.add(Swing.createLabel(header));
        headerPanel.add(new JPanel());

        layout.setPosition(headerPanel, new Rectangle2D.Double(0, 0, 1, 0.1));

        JPanel creatorsPanel = new JPanel();
        creatorsPanel.setLayout(new GridLayout(0, 1));
        for (T ug : creators) {
            addGenEditor(c, creators, creatorsPanel, ug);
        }
        JScrollPane sp = new JScrollPane(creatorsPanel);
        panel.add(sp);
        layout.setPosition(sp, new Rectangle2D.Double(0, 0.1, 1, 0.8));

        JButton btn = Swing.createButton("Add", () -> {
            T g = creator.create(null);
            addGenEditor(c, creators, creatorsPanel, g);
            creators.add(g);
            creatorsPanel.revalidate();
            creatorsPanel.repaint();
        });
        panel.add(btn);
        layout.setPosition(btn, new Rectangle2D.Double(0, 0.9, 1, 0.1));
        return panel;
    }

    private static <T extends Creators.GenCreator> void addGenEditor(GameSpecEditorContext c, List<T> creators, JPanel creatorsPanel, T ug) {
        creatorsPanel.add(createGenEditor(c, ug, p -> {
            creatorsPanel.remove(p);
            creators.remove(ug);
            creatorsPanel.revalidate();
            creatorsPanel.repaint();
        }));
    }

    private static JTabbedPane createGenerationEditor(GameSpecEditorContext c, Creators.GenerationCreator creator) {
        JTabbedPane jTabbedPane = new JTabbedPane();
        JPanel gaia = new JPanel();
        gaia.setLayout(new GridLayout(1, 0));
        gaia.add(createGenEditors(c, new String[] {"Unit", "Number to generate"}, creator.gaiaUnitGens, n -> new Creators.UnitGenCreator(), "Gaia units"));
        gaia.add(createGenEditors(c, new String[] {"Resource", "Number of patches", "Patch size"}, creator.gaiaResGens, n -> new Creators.ResourceGenCreator(), "Gaia resources"));
        jTabbedPane.addTab("Gaia Generation", gaia);

        JPanel perPlayer = new JPanel();
        perPlayer.setLayout(new GridLayout(1, 0));
        perPlayer.add(createGenEditors(c, new String[] {"Unit", "Number to generate"}, creator.byPlayerUnitGens, n -> new Creators.UnitGenCreator(), "Gaia units"));
        perPlayer.add(createGenEditors(c, new String[] {"Resource", "Number of patches", "Patch size"}, creator.byPlayerResGens, n -> new Creators.ResourceGenCreator(), "Gaia resources"));
        jTabbedPane.addTab("Per player Generation", perPlayer);

        return jTabbedPane;
    }

    private static JPanel createWeaponSpecEditor(GameSpecEditorContext c, Creators.WeaponSpecCreator weapon) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2));

        panel.add(Swing.createLabel("Weapon"));
        panel.add(Swing.createLabel(weapon.name));

        for (Interfaces.ValueCreator<?> vc : weapon.valueCreators) {
            panel.add(Swing.createLabel(vc.getFieldName()));
            panel.add(createEditor(c, vc, false));
        }

        return panel;
    }
}
