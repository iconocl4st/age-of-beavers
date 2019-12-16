package client.gui.game.gl;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

class TextureCache {
    private final GLProfile profile;
    private HashMap<String, Texture> textures = new HashMap<>();

    TextureCache(GLProfile profile) {
        this.profile = profile;
    }

    private Texture loadTexture(String path, GL2 gl) {
        try {
//            BufferedImage image = ImageIO.read(new File("./images/" + path));
//            BufferedImage image = ImageIO.read(new File("/home/thallock/Documents/Idea/age-of-beavers/CircleAndShapes/images/res/gold.png"));
//            Texture texture = AWTTextureIO.newTexture(profile, image, true);

            Texture texture = TextureIO.newTexture(new File("/home/thallock/Documents/Idea/age-of-beavers/CircleAndShapes/images/res/gold.png"), true);
            texture.enable(gl);
            return texture;
        } catch (Exception e) {
            System.out.println("Unable to read " + path);
            // return default question mark image or something...
            e.printStackTrace();
            System.exit(-1);
            return null;
        }
    }

    Texture getTexture(String path, GL2 gl) {
        return textures.computeIfAbsent(path, ignore -> loadTexture(path,  gl));
    }

}
