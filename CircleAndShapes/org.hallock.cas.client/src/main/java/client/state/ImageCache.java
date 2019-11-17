package client.state;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class ImageCache {
    private HashMap<String, BufferedImage> cache = new HashMap<>();
    private static final BufferedImage DEFAULT_IMAGE = null; // new BufferedImage(); // should fill it in...

    // sync?
    public BufferedImage get(String path) {
        BufferedImage image = cache.get(path);
        if (image != null) {
            return image;
        }

        try {
            image = ImageIO.read(new File("images/" + path));
        } catch (IOException e) {
            e.printStackTrace();
            // return DEFAULT_IMAGE
            throw new RuntimeException("Unable to find the image at path " + path);
        }

        cache.put(path, image);

        return image;
    }
}
