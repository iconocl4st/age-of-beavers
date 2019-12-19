package client.gui.game.gl;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import common.state.spec.EntitySpec;
import common.state.spec.GameSpec;

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

    private void loadTexture(String path) {
        try {
            Texture texture = TextureIO.newTexture(new File("images/" + path), true);
            textures.put(path, texture);
        } catch (Exception e) {
            System.out.println("Unable to read " + path);
            // return default question mark image or something...
            e.printStackTrace();
            System.exit(-1);
        }
    }

    Texture getTexture(String path) {
        return textures.get(path);
    }

    void loadTextures(GameSpec spec) {
        textures.clear();
        loadTexture("unit/construction.png");
        loadTexture("unit/plant_0.png");
        loadTexture("unit/plant_1.png");
        loadTexture("unit/plant_2.png");
        loadTexture("unit/plant_3.png");
        loadTexture("unit/plant_4.png");
        loadTexture("unit/plant_5.png");

        for (EntitySpec entitySpec : spec.unitSpecs) {
            loadTexture(entitySpec.graphicsImage);
        }
    }
}
