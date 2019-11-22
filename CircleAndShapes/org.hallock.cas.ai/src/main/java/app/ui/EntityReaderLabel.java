package app.ui;

import common.state.EntityReader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class EntityReaderLabel extends JLabel implements MouseListener {
    private final Highlighter highlighter;
    private EntityReader reader;
    private final String name;

    EntityReaderLabel(String name, Highlighter highlighter) {
        this.name = name;
        this.highlighter = highlighter;
        addMouseListener(this);
        setOpaque(true);
    }

    void setReader(EntityReader reader, EntityReader highlighted) {
        this.reader = reader;
        boolean isHighlighted = reader != null && reader.equals(highlighted);
        setBackground(isHighlighted ? Color.yellow : Color.white);
        setForeground(isHighlighted ? Color.black : Color.black);
        if (reader != null)
            setText(name + reader.entityId.id);
        else
            setText("None");
    }


    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        if (reader == null) return;
        highlighter.setHighlight(reader);
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }
}
