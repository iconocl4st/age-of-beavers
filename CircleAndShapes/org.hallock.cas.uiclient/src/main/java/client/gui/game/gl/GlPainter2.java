package client.gui.game.gl;

import client.gui.game.gl.object.Face;
import client.gui.game.gl.object.Obj;
import client.gui.game.gl.object.ObjParser;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.TextureIO;

import java.io.File;
import java.io.IOException;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

public class GlPainter2 implements GLEventListener {

    private GLU glu;  // for the GL Utility

    // Texture
    private Texture texture;
    private String textureFileName = "images/res/gold.png";

    Obj human;

    /**
     * Called back immediately after the OpenGL context is initialized. Can be used
     * to perform one-time initialization. Run only once.
     */
    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        glu = new GLU();
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClearDepth(1.0f);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LEQUAL);
        gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        gl.glShadeModel(GL_SMOOTH);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        try {
            texture = TextureIO.newTexture(new File(textureFileName), true);
        } catch (GLException | IOException e) {
            e.printStackTrace();
        }

        try {
            human = ObjParser.parseObjectFile("objs/human2.obj");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Call-back handler for window re-size event. Also called when the drawable is
     * first set to visible.
     */
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context

        if (height == 0) height = 1;   // prevent divide by zero
        float aspect = (float)width / height;

        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0, aspect, 0.1, 100.0); // fovy, aspect, zNear, zFar

        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    /**
     * Called back by the animator to perform rendering.
     */
    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear color and depth buffers

        // ------ Render a Cube with texture ------
        gl.glLoadIdentity();  // reset the model-view matrix
        gl.glTranslatef(0.0f, 0.0f, -5.0f); // translate into the screen

        drawObj(gl, human);
//        paintImage(gl);
    }

    private void drawObj(GL2 gl, Obj obj) {
        if (obj == null) {
            return;
        }

        for (Face face : obj.getFaces()) {
            gl.glBegin(GL2.GL_POLYGON);
            {
                for (int i = 0; i < face.vertices.length; i++) {
                    gl.glVertex3f(face.vertices[i][0], face.vertices[i][1], face.vertices[i][2]); // bottom-left of the texture and quad
                }
            }
            gl.glEnd();
        }
    }

    private void paintImage(GL2 gl) {
        texture.enable(gl);
        texture.bind(gl);

        gl.glBegin(GL2.GL_POLYGON);

        TextureCoords textureCoords = texture.getImageTexCoords();
        float textureTop = textureCoords.top();
        float textureBottom = textureCoords.bottom();
        float textureLeft = textureCoords.left();
        float textureRight = textureCoords.right();

        // Front Face
        gl.glTexCoord2f(textureLeft, textureBottom);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f); // bottom-left of the texture and quad
        gl.glTexCoord2f(textureRight, textureBottom);
        gl.glVertex3f(1.0f, -1.0f, 1.0f);  // bottom-right of the texture and quad
        gl.glTexCoord2f(textureRight, textureTop);
        gl.glVertex3f(1.0f, 1.0f, 1.0f);   // top-right of the texture and quad
        gl.glTexCoord2f(textureLeft, textureTop);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);  // top-left of the texture and quad

        gl.glEnd();
    }

    /**
     * Called back before the OpenGL context is destroyed. Release resource such as buffers.
     */
    @Override
    public void dispose(GLAutoDrawable drawable) { }
}
