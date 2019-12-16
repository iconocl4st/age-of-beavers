package client.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImagePanel extends JPanel {
    private BufferedImage image;

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        if (image == null) {
            g.setColor(Color.black);
            g.fillRect(0, 0, width, height);
            return;
        }

        int h = height;
        int w = (int) ((image.getWidth() / (double) image.getHeight()) * h);

        g.drawImage(image, 0, 0, w, h, Color.white, this);
    }
}
