package client.gui.selected;

import client.app.UiClientContext;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.spec.EntitySpec;
import common.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class MultiSelect extends JPanel {

    private final UiClientContext context;

    private final List<DrawnUnit> locatedIcons = new LinkedList<>();
    private int currentlySelectedIndex = -1;

    private MultiSelect(UiClientContext context) {
        this.context = context;
    }

    EntityReader cycle() {
        synchronized (locatedIcons) {
            ++currentlySelectedIndex;
            if (currentlySelectedIndex >= locatedIcons.size()) {
                currentlySelectedIndex = 0;
            }
            return locatedIcons.get(currentlySelectedIndex).entity;
        }
    }

    public void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        drawNothing(g);

        synchronized (locatedIcons) {
            for (DrawnUnit drawn : locatedIcons) {
                g.drawImage(
                        drawn.image,
                        drawn.rectangle.x,
                        drawn.rectangle.y,
                        drawn.rectangle.width,
                        drawn.rectangle.height,
                        Color.white,
                        this
                );
                g.setColor(Color.black);
                g.drawRect(
                        drawn.rectangle.x,
                        drawn.rectangle.y,
                        drawn.rectangle.width,
                        drawn.rectangle.height
                );
            }

            if (currentlySelectedIndex >= 0) {
                g.setColor(Color.yellow);
                DrawnUnit drawn = locatedIcons.get(currentlySelectedIndex);
                g.drawRect(
                        drawn.rectangle.x,
                        drawn.rectangle.y,
                        drawn.rectangle.width,
                        drawn.rectangle.height
                );
            }
        }
        Toolkit.getDefaultToolkit().sync();
    }

    private void drawNothing(Graphics2D g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    void setSelectedLocations(java.util.List<EntityReader> unitIds) {
        currentlySelectedIndex = -1;
        int numUnits = unitIds.size();
        Point shape = getShape(numUnits);

        int[] dimensions = new int[]{shape.x, shape.y};
        Iterator<int[]> idxIterator = new Util.IndexIterator(dimensions);
        Iterator<EntityReader> iterator = unitIds.iterator();

        int width = getWidth();
        int height = getHeight();
        int slotWidth = width / dimensions[1];
        int slotHeight = height / dimensions[0];

        synchronized (locatedIcons) {
            locatedIcons.clear();
            while (iterator.hasNext() && idxIterator.hasNext()) {
                EntityReader d = iterator.next();
                int[] idx = idxIterator.next();

                EntitySpec type = d.getType();
                if (type == null) continue;

                DrawnUnit drawn = new DrawnUnit();
                drawn.image = context.imageCache.get(type.image);
                drawn.rectangle = new Rectangle(
                        idx[1] * slotWidth,
                        idx[0] * slotHeight,
                        slotWidth,
                        slotHeight
                );
                drawn.entity = d;
                locatedIcons.add(drawn);
            }
        }
    }

    private static final Point[] shapes = {
            new Point(1, 1),
            new Point(1, 2),
            new Point(1, 3),
            new Point(1, 4),
            new Point(1, 5),
            new Point(2, 5),
            new Point(2, 10),
            new Point(3, 10),
            new Point(4, 10),
    };

    private static Point getShape(int size) {
        for (Point shape : shapes)
            if (size <= shape.x * shape.y)
                return shape;
        return shapes[shapes.length - 1];
    }


    static MultiSelect createSelectedUnits(final UiClientContext clientContext) {
        final MultiSelect ret = new MultiSelect(clientContext);
        ret.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if ((mouseEvent.getModifiers() & InputEvent.BUTTON1_MASK) != InputEvent.BUTTON1_MASK) {
                    return;
                }
                for (final DrawnUnit locatedIcon : ret.locatedIcons) {
                    if (!locatedIcon.rectangle.contains(mouseEvent.getPoint()))
                        continue;
                    if (clientContext.uiManager.gameScreen.contextKeyListener.containsKey(KeyEvent.VK_CONTROL)) {
                        Set<EntityReader> remainingEntities = new HashSet<>();
                        for (DrawnUnit drawn : ret.locatedIcons) {
                            if (drawn.entity.equals(locatedIcon.entity))
                                continue;
                            remainingEntities.add(drawn.entity);
                        }
                        clientContext.executorService.submit(() -> clientContext.selectionManager.select(remainingEntities));
                    } else {
                        clientContext.executorService.submit(() -> clientContext.selectionManager.select(locatedIcon.entity));
                    }
                }
            }
        });
        return ret;
    }

    private static class DrawnUnit {
        Rectangle rectangle;
        EntityReader entity;
        BufferedImage image;
    }
}
