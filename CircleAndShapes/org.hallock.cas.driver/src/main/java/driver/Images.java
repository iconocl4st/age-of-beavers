package driver;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Images {
    private static BufferedImage createStorageYardImage() {
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

    private static BufferedImage createBrothelImage() {
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

    private static BufferedImage createBlackImage() {
        int w = 4;
        int h = 4;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Color color = new Color(0, 0, 0, 255);
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                image.setRGB(i, j, color.getRGB());
            }
        }
        return image;
    }



    private static BufferedImage createPlant(int numleaves) {
        int w = 512;
        int h = 512;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Color color = new Color(97, 97, 54, 255);
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                image.setRGB(i, j, color.getRGB());
            }
        }
        Graphics2D graphics = image.createGraphics();

        int stalkWidth = w / 30;
        int stalkHeight = 3 * h / 4;
        int spacing = h / 30;

        if (numleaves > 0) {
            graphics.setColor(Color.GREEN);
            graphics.fillRect(w / 2 - stalkWidth, h - stalkHeight, 2 * stalkWidth, stalkHeight);
        }

        for (int i = 0; i < numleaves; i++) {
            int leafHeight = h / 20;
            int stemLocation = h - (i + 1) * (2 * leafHeight + spacing);
            graphics.fillPolygon(new int[]{w / 2, 3 * w / 4, 3 * w / 4}, new int[]{stemLocation, stemLocation + leafHeight, stemLocation - leafHeight}, 3);
            graphics.fillPolygon(new int[]{w, 3 * w / 4, 3 * w / 4}, new int[]{stemLocation, stemLocation + leafHeight, stemLocation - leafHeight}, 3);
            graphics.fillPolygon(new int[]{w / 2, w / 4, w / 4}, new int[]{stemLocation, stemLocation + leafHeight, stemLocation - leafHeight}, 3);
            graphics.fillPolygon(new int[]{0, w / 4, w / 4}, new int[]{stemLocation, stemLocation + leafHeight, stemLocation - leafHeight}, 3);
        }
        return image;
    }

    private static BufferedImage createLinearRailRoad() {
        int w = 32;
        int h = 10 * w;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Color color = new Color(132, 143, 145, 255);
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                image.setRGB(i, j, color.getRGB());
            }
        }

        Graphics2D graphics = image.createGraphics();

        int woodHeight = h / 80;
        int numWoods = 15;

        Color wood = new Color(150, 111, 51);
        graphics.setColor(wood);
        for (int i = 0; i < numWoods; i++) {
            graphics.fillRect(w / 8, (int)((i + 0.5) * h / (double) numWoods), w - 2 * w / 8, woodHeight);
        }


        Color metal = new Color(59, 47, 30);
        int railOffset = w / 4;
        int railWidth = w / 16;

        graphics.setColor(metal);
        graphics.fillRect(railOffset - railWidth, 0, 2 * railWidth, h);
        graphics.fillRect(w - railOffset - railWidth, 0, 2 * railWidth, h);

        return image;
    }

    private static BufferedImage createSplitRailRoad() {
        int w = 2 * 32;
        int h = 2 * 10 * w;
        BufferedImage image = new BufferedImage(3 * w, h, BufferedImage.TYPE_INT_ARGB);
        Color color = new Color(132, 143, 145, 255);
        for (int i = 0; i < 3 * w; i++) {
            for (int j = 0; j < h; j++) {
                image.setRGB(i, j, color.getRGB());
            }
        }

        Graphics2D graphics = image.createGraphics();

        int woodHeight = h / 80;
        int numWoods = 15;

        int b1 = numWoods / 3;
        int b2 = 2 * numWoods / 3;

        Color wood = new Color(150, 111, 51);
        graphics.setColor(wood);
        for (int i = 0; i < b1; i++) {
            graphics.fillRect(w + w / 8, (int)((i + 0.5) * h / (double) numWoods), w - 2 * w / 8, woodHeight);
        }
        for (int i = b1; i < b2; i++) {
            double t = (b2 - (i + 0.5)) / (double) (b2 - b1);
            graphics.fillRect((int)(t * (w + w / 8) + (1 - t) * (2 * w + w / 8)), (int)((i + 0.5) * h / (double) numWoods), w - 2 * w / 8, woodHeight);
            graphics.fillRect((int)(t * (w + w / 8) + (1 - t) * (w / 8)), (int)((i + 0.5) * h / (double) numWoods), w - 2 * w / 8, woodHeight);
        }
        for (int i = b2; i < numWoods; i++) {
            graphics.fillRect(w / 8, (int)((i + 0.5) * h / (double) numWoods), w - 2 * w / 8, woodHeight);
            graphics.fillRect(2* w + w / 8, (int)((i + 0.5) * h / (double) numWoods), w - 2 * w / 8, woodHeight);
        }


        Color metal = new Color(59, 47, 30);
        int railOffset = w / 4;
        int railWidth = w / 16;

        graphics.setColor(metal);
        graphics.fillRect(w + railOffset - railWidth, 0, 2 * railWidth, h / 3);
        graphics.fillRect(w + w - railOffset - railWidth, 0, 2 * railWidth, h / 3);

        BasicStroke stroke = new BasicStroke(2 * railWidth);
        graphics.setStroke(stroke);
        graphics.drawLine(w + railOffset,h / 3, railOffset, 2 * h / 3);
        graphics.drawLine(w + railOffset,h / 3, 2 * w + railOffset, 2 * h / 3);
        graphics.drawLine(w + w - railOffset,h / 3, w - railOffset, 2 * h / 3);
        graphics.drawLine(w + w - railOffset,h / 3, 2 * w + w - railOffset, 2 * h / 3);


        graphics.fillRect(railOffset - railWidth, 2 * h / 3, 2 * railWidth, h / 3);
        graphics.fillRect(w - railOffset - railWidth, 2 * h / 3, 2 * railWidth, h / 3);
        graphics.fillRect(2 * w + railOffset - railWidth, 2 * h / 3, 2 * railWidth, h / 3);
        graphics.fillRect(2 * w + w - railOffset - railWidth, 2 * h / 3, 2 * railWidth, h / 3);

        return image;
    }


    private static BufferedImage createRailroadTurn() {
        int w = 5 * 32;
        int h = 5 * 10 * w;

//        h = 2 * Math.PI * r / 4;
        int turnRadius = (int)(2 * h / Math.PI);
        turnRadius = 10 * w;
        BufferedImage image = new BufferedImage(turnRadius, turnRadius, BufferedImage.TYPE_INT_ARGB);
        Color color = new Color(132, 143, 145, 255);
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                image.setRGB(i, j, color.getRGB());
            }
        }

        Graphics2D graphics = image.createGraphics();

        int numWoods = (int) (Math.PI / 2 * 15);

        Color wood = new Color(150, 111, 51);
        graphics.setColor(wood);
        for (int i = 0; i < numWoods; i++) {
            double r2 = turnRadius - w / 8;
            double r1 = turnRadius - w + w / 8;


            double angle = 90 * (i + 0.5) / (double) numWoods;
            Area a2 = new Area(new Arc2D.Double(-r2, turnRadius - r2, 2 * r2, 2 * r2, angle, 2 * 90d / (80 * Math.PI), Arc2D.PIE));
            Area a1 = new Area(new Arc2D.Double(-r1, turnRadius - r1, 2 * r1, 2 * r1, angle, 2 * 90d / (80 * Math.PI), Arc2D.PIE));
            a2.subtract(a1);
            graphics.fill(a2);
        }


        Color metal = new Color(59, 47, 30);
        int railOffset = w / 4;
        int railWidth = w / 16;

        graphics.setColor(metal);

        BasicStroke stroke = new BasicStroke(2 * railWidth);
        graphics.setStroke(stroke);

        double r4 = turnRadius - railOffset + railWidth;
        double r3 = turnRadius - railOffset - railWidth;
        double r2 = turnRadius - w + railOffset + railWidth;
        double r1 = turnRadius - w + railOffset - railWidth;
        Area a4 = new Area(new Arc2D.Double(-r4, turnRadius - r4, 2 * r4, 2 * r4, 0, 90, Arc2D.PIE));
        Area a3 = new Area(new Arc2D.Double(-r3, turnRadius - r3, 2 * r3, 2 * r3, 0, 90, Arc2D.PIE));
        Area a2 = new Area(new Arc2D.Double(-r2, turnRadius - r2, 2 * r2, 2 * r2, 0, 90, Arc2D.PIE));
        Area a1 = new Area(new Arc2D.Double(-r1, turnRadius - r1, 2 * r1, 2 * r1, 0, 90, Arc2D.PIE));
        a4.subtract(a3);
        a2.subtract(a1);
        graphics.fill(a4);
        graphics.fill(a2);
        return image;
    }





    public static void main(String[] args) throws IOException {
//        ImageIO.write(createStorageYardImage(), "png", new File("images/unit/storage_yard.png"));
//        ImageIO.write(createBrothelImage(), "png", new File("images/unit/brothel.png"));
//        ImageIO.write(createBlackImage(), "png", new File("images/unit/black.png"));
        ImageIO.write(createLinearRailRoad(), "png", new File("images/unit/railroad.png"));
        ImageIO.write(createSplitRailRoad(), "png", new File("images/unit/railroad_split.png"));
        ImageIO.write(createRailroadTurn(), "png", new File("images/unit/railroad_turn.png"));
    }





    private static BufferedImage transpose(BufferedImage image) {
        BufferedImage ret = new BufferedImage(image.getHeight(), image.getWidth(), BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < ret.getWidth(); i++) {
            for (int j = 0; j < ret.getHeight(); j++) {
                ret.setRGB(i, j, image.getRGB(j, i));
            }
        }
        return ret;
    }

    private static BufferedImage flipY(BufferedImage image) {
        BufferedImage ret = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < ret.getWidth(); i++) {
            for (int j = 0; j < ret.getHeight(); j++) {
                ret.setRGB(i, j, image.getRGB(i, image.getHeight() - j - 1));
            }
        }
        return ret;
    }
    private static BufferedImage flipX(BufferedImage image) {
        BufferedImage ret = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < ret.getWidth(); i++) {
            for (int j = 0; j < ret.getHeight(); j++) {
                ret.setRGB(i, j, image.getRGB(image.getWidth() - i - 1, j));
            }
        }
        return ret;
    }
}
