package common.algo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Tes {

    public static void main(String[] args) {
        JFrame frame = new JFrame();


        JPanel panel = new JPanel();
        panel.setBackground(Color.red);

        panel.setFocusTraversalKeysEnabled(false);

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                switch (keyEvent.getKeyCode()) {
                    case KeyEvent.VK_TAB:
                        System.out.println("here.");
                }
            }
        });


        frame.setContentPane(panel);
        frame.setBounds(50,50,500,500);
        frame.setTitle("Testing");
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel.grabFocus();
    }
}
