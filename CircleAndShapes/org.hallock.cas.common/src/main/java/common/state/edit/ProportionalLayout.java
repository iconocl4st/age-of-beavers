package common.state.edit;


import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;


// MOVE
public class ProportionalLayout implements ComponentListener, LayoutManager {

    private List<Child> children = new LinkedList<>();
    private Container parent;
    int w;
    int h;

    public ProportionalLayout(Container parent) {
        parent.setLayout(null);
        parent.addComponentListener(this);
        w = parent.getWidth();
        h = parent.getHeight();
        this.parent = parent;
    }

    public void setPosition(Container c, Rectangle2D position) {
        removeLayoutComponent(c);
        Child child = new Child();
        child.container = c;
        child.position = position;
        children.add(child);
    }

    public void setPositions() {
        for (Child child : children) {
            layoutContainer(child);
        }
        parent.revalidate();
        parent.repaint();
    }

    @Override
    public void componentResized(ComponentEvent componentEvent) {
        w = componentEvent.getComponent().getWidth();
        h = componentEvent.getComponent().getHeight();
        setPositions();
    }

    @Override
    public void componentMoved(ComponentEvent componentEvent) {
        w = componentEvent.getComponent().getWidth();
        h = componentEvent.getComponent().getHeight();
        setPositions();
    }

    @Override
    public void componentShown(ComponentEvent componentEvent) {
        w = componentEvent.getComponent().getWidth();
        h = componentEvent.getComponent().getHeight();
        setPositions();
    }

    @Override
    public void componentHidden(ComponentEvent componentEvent) {
        w = componentEvent.getComponent().getWidth();
        h = componentEvent.getComponent().getHeight();
        setPositions();
    }

    @Override
    public void addLayoutComponent(String s, Component component) {
        setPositions();
    }

    @Override
    public void removeLayoutComponent(Component component) {
        for (Child child : children) {
            if (child.container.equals((component))) {
                children.remove(child);
                return;
            }
        }
    }

    @Override
    public Dimension preferredLayoutSize(Container container) {
        return PREFERRED;
    }

    @Override
    public Dimension minimumLayoutSize(Container container) {
        return PREFERRED;
    }

    @Override
    public void layoutContainer(Container container) {
        layoutContainer(get(container));
    }

    private void layoutContainer(Child child) {
        if (child == null) return;
        child.container.setBounds(
                (int)(w * child.position.getX()),
                (int)(h * child.position.getY()),
                (int)(w * child.position.getWidth()),
                (int)(h * child.position.getHeight())
        );
    }

    public Child get(Container container) {
        for (Child child : children) {
            if (child.container.equals(container)) {
                return child;
            }
        }
        return null;
    }

    static final class Child {
        Container container;
        Rectangle2D position;
    }

    private static final Dimension PREFERRED = new Dimension(1, 1);


//    private interface Specifier {
//        int get(int total);
//    }
//
//    private static class Proportional implements Specifier {
//        double proportion;
//
//        Proportional()
//
//        @Override
//        public int get(int total) {
//            return 0;
//        }
//    }

}
