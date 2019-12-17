package common.state.edit;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

class WindowStack {
    private final JPanel parent = new JPanel();
    private final JTextArea errorsView = new JTextArea();
    private final JTextArea preview = new JTextArea();
    private final JPanel top = new JPanel();
    private final LinkedList<Component> stack = new LinkedList<>();
    private final JButton back = Swing.createButton("Back", this::pop);

    WindowStack(GameSpecEditorContext context) {
        ProportionalLayout pLayout = new ProportionalLayout(parent);
        parent.setLayout(pLayout);

        top.setLayout(new GridLayout(0, 1));

        JTabbedPane tPane = new JTabbedPane();
        tPane.addTab("Editor",  top);

        JPanel p;
        {
            p = new JPanel();
            p.setLayout(new GridLayout(0, 1));
            p.add(new JScrollPane(preview));
            tPane.addTab("Preview", p);
        }

        {
            p = new JPanel();
            p.setLayout(new GridLayout(0, 1));
            p.add(new JScrollPane(errorsView));
            tPane.addTab("Errors", p);
        }

        parent.add(tPane);
        pLayout.setPosition(tPane, new Rectangle2D.Double(0, 0.1, 1d, 0.9));

        JPanel options = new JPanel();
        options.setLayout(new GridLayout(1, 0));
        parent.add(options);
        pLayout.setPosition(options, new Rectangle2D.Double(0, 0, 1d, 0.1));

        options.add(back);
        options.add(Swing.createButton("Save", context::save));
        options.add(Swing.createButton("Load", context::load));
        options.add(Swing.createButton("Check for errors", () -> {
            Interfaces.Errors errors = context.checkForErrors();
            StringBuilder builder = new StringBuilder();
            for (Interfaces.ExportError error : errors.errors)
                error.append(builder).append('\n');
            errorsView.setText(builder.toString());
        }));
        options.add(Swing.createButton("Export", context::export));
        options.add(Swing.createButton("Preview", () -> preview.setText(CreatorParser.save(context.spec))));
    }

    void push(Component panel) {
        top.removeAll();
        top.add(panel);
        stack.addLast(panel);
        parent.validate();
        parent.repaint();
        back.setEnabled(stack.size() > 1);
    }

    private void pop() {
        top.removeAll();
        stack.removeLast();
        top.add(stack.getLast());
        parent.validate();
        parent.repaint();
        back.setEnabled(stack.size() > 1);
    }

    void clear() {
        stack.clear();
        top.removeAll();
    }

    Container getParent() {
        return parent;
    }
}
