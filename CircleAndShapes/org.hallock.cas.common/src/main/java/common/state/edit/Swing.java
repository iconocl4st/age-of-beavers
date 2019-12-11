package common.state.edit;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class Swing {
    static JButton createButton(String label, Runnable listener) {
        JButton btn = new JButton(label);
        btn.addActionListener(e -> listener.run());
        return btn;
    }

    static JLabel createLabel(String label) {
        JLabel visibility = new JLabel(label);
        visibility.setBorder(new BevelBorder(BevelBorder.RAISED));
        visibility.setHorizontalAlignment(JLabel.CENTER);
        visibility.setVerticalAlignment(JLabel.CENTER);
        return visibility;
    }


    static JTextComponent createTextField(String initialValue, Interfaces.PropertyListener<String> listener) {
        JTextComponent visibility = new JTextArea(initialValue == null ? "" : initialValue);
        visibility.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                listener.propertySet(visibility.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                listener.propertySet(visibility.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                listener.propertySet(visibility.getText());
            }
        });
        visibility.setPreferredSize(new Dimension(1, 1));
        visibility.setMinimumSize(new Dimension(1, 1));
        return visibility;
    }
}
