package driver;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Images {


    public static BufferedImage createStorageYardImage() {
        int w = 512;
        int h = 512;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Color color = new Color(0, 0, 0, 0);
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                image.setRGB(i, j, color.getRGB());
            }
        }
        Graphics2D graphics = image.createGraphics();

        int ceil = h / 3;
        int wwidth = w / 8;
        graphics.setColor(Color.DARK_GRAY);
        graphics.fillRect(0, ceil, wwidth, h - ceil);
        graphics.fillRect(w - wwidth, ceil, wwidth, h - ceil);
        graphics.fillRect(0, ceil, w, wwidth /  2);
        graphics.fillPolygon(new int[]{0, w, w / 2}, new int[]{ceil, ceil, 0}, 3);

        int rx = w / 20;
        int ry = h / 20;
        int[] numboxes = new int[]{3, 3, 2, 1};
        for (int i = 0; i < numboxes.length; i++) {
            int x = wwidth + (w - 2 * wwidth) * (i + 1) / (numboxes.length + 1);
            for (int j = 0; j < numboxes[i]; j++) {
                graphics.fillRect(x - rx, h - 2 * (j + 1) * ry - (j + 1) * (ry / 2), 2*rx, 2*ry);
            }
        }

        return image;
    }

    public static BufferedImage createBrothelImage() {
        int w = 512;
        int h = 512;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Color color = new Color(0, 0, 0, 0);
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                image.setRGB(i, j, color.getRGB());
            }
        }
        Graphics2D graphics = image.createGraphics();

        int ceil = h / 3;
        int wwidth = w / 8;
        graphics.setColor(Color.DARK_GRAY);
        graphics.fillRect(0, ceil, wwidth, h - ceil);
        graphics.fillRect(w - wwidth, ceil, wwidth, h - ceil);
        graphics.fillRect(0, ceil, w, wwidth /  2);
        graphics.fillPolygon(new int[]{0, w, w / 2}, new int[]{ceil, ceil, 0}, 3);

        int r = (w - wwidth) / 8;
        graphics.fillRect(w / 2 - w / 10, h / 2 + r, 2 * w / 10, h / 10);

        graphics.fillOval(w / 2 - 2 * r, h / 2, 2 * r, 2 * r);
        graphics.fillOval(w / 2, h / 2, 2 * r, 2 * r);
        graphics.fillPolygon(
                new int[]{w / 2 - 2 * r + w / 40, w / 2 + 2 * r - w / 40, w / 2},
                new int[]{h / 2 + r + h / 15, h / 2 + r + h / 15, h - h / 10},
                3);

        return image;
    }

    public static void main(String[] args) throws IOException {
        ImageIO.write(createStorageYardImage(), "png", new File("images/unit/storage_yard.png"));
        ImageIO.write(createBrothelImage(), "png", new File("images/unit/brothel.png"));
    }
}
