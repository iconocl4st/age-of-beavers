package client.gui.game.gl;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;

import java.awt.*;

class GlPainter implements GLEventListener {

    private final GlRectangleListener recListener;
    private final GlPressListener pressListener;

    private final MapToScreenContext mapContext;

    private GLU glu;
    private TextureCache textureCache;
    private GlZoom glZoom;

    GlPainter(
            TextureCache textureCache,
            GlZoom glZoom,
            GlRectangleListener recclistener,
            GlPressListener pressListener
    ) {
        this.textureCache = textureCache;
        this.glZoom = glZoom;
        this.mapContext = new MapToScreenContext();
        this.recListener = recclistener;
        this.pressListener = pressListener;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        glu = new GLU();
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClearDepth(1.0f);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST); // best perspective correction
        gl.glShadeModel(GL2.GL_SMOOTH); // blends colors nicely, and smoothes out lighting


        // ----- Your OpenGL initialization code here -----
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        synchronized (glZoom.sync) {
            GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context
            if (height == 0) height = 1;

            glZoom.aspect = width / (double) height;
            glZoom.screenWidth = width;
            glZoom.screenHeight = height;

            gl.glViewport(0, 0, width, height);

            // Setup perspective projection, with aspect ratio matches viewport
            gl.glMatrixMode(GL2.GL_PROJECTION);  // choose projection matrix
            gl.glLoadIdentity();             // reset projection matrix
            glu.gluPerspective(GlConstants.FOV_Y, glZoom.aspect, 0.1, 1000.0); // fovy, aspect, zNear, zFar

            // Enable the model-view transform
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity(); // reset
        }
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();


        gl.glTranslated(glZoom.locationX, glZoom.locationY, glZoom.locationZ);

        String path = "/home/thallock/Documents/Idea/age-of-beavers/CircleAndShapes/images/res/gold.png";
        paintImage(gl, path, 0, 0, 1, 1);

        // unbind?

//            gl.glFlush();


        mapContext.init(gl);
        pressListener.update(mapContext, glu);
        updateSelecting(gl);
        updateScreenLocation();
    }

    private void updateSelecting(GL2 gl) {
        synchronized (recListener.sync) {
            if (!recListener.update(mapContext, glu))
                return;
            drawRectangle(gl, Color.red, recListener.gameXBegin, recListener.gameYBegin, recListener.gameXCurrent, recListener.gameYCurrent);
        }
    }

    private void updateScreenLocation() {
        synchronized (glZoom.sync) {
            mapContext.map(glu, 0d, 0d);
            glZoom.screenLowerX = mapContext.gameLocationX;
            glZoom.screenLowerY = mapContext.gameLocationY;

            mapContext.map(glu, glZoom.screenWidth, glZoom.screenHeight);
            glZoom.screenUpperX = mapContext.gameLocationX;
            glZoom.screenUpperY = mapContext.gameLocationY;
        }
    }

    /**
     * Called back before the OpenGL context is destroyed. Release resource such as buffers.
     */
    @Override
    public void dispose(GLAutoDrawable drawable) {

    }




















    private void paintImage(GL2 gl, String path, double x, double y, double w, double h) {
        Texture texture = textureCache.getTexture(path, gl);

        TextureCoords textureCoords = texture.getImageTexCoords();
        float textureTop = textureCoords.top();
        float textureBottom = textureCoords.bottom();
        float textureLeft = textureCoords.left();
        float textureRight = textureCoords.right();

        texture.enable(gl);
        texture.bind(gl);
        gl.glBegin(GL2.GL_QUADS);
        gl.glColor4f(1f, 1f, 1f, 1f);
        {
            gl.glTexCoord2f(textureLeft, textureBottom);
            gl.glVertex3d(x, y, 0.0);
            gl.glTexCoord2f(textureRight, textureBottom);
            gl.glVertex3d(x + w, y, 0.0);
            gl.glTexCoord2f(textureRight, textureTop);
            gl.glVertex3d(x + w, y + h, 0.0);
            gl.glTexCoord2f(textureLeft, textureTop);
            gl.glVertex3d(x, y + h, 0.0);
        }
        gl.glEnd();
        texture.disable(gl);
    }

    private void drawRectangle(GL2 gl, Color color, double x1, double y1, double x2, double y2) {
        // could use shorts...
        gl.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1f); // color.getAlpha());

        gl.glBegin(GL2.GL_LINES);
        {
            gl.glVertex3d(x1, y1, 0d);
            gl.glVertex3d(x1, y2, 0d);
        }
        gl.glEnd();

        gl.glBegin(GL2.GL_LINES);
        {
            gl.glVertex3d(x1, y2, 0d);
            gl.glVertex3d(x2, y2, 0d);
        }
        gl.glEnd();

        gl.glBegin(GL2.GL_LINES);
        {
            gl.glVertex3d(x2, y2, 0d);
            gl.glVertex3d(x2, y1, 0d);
        }
        gl.glEnd();

        gl.glBegin(GL2.GL_LINES);
        {
            gl.glVertex3d(x2, y1, 0d);
            gl.glVertex3d(x1, y1, 0d);
        }
        gl.glEnd();
    }

        // fill rectangle
        // fill circle
        // draw line
        // draw string
        // draw progress bar


//
//        private static double getAngle(double dx, double dy) {
//            if (Math.abs(dx) > 14-3)
//                return 180 * Math.atan(dy / dx) / Math.PI;
//            if (dy > 0)
//                return 90d;
//            else
//                return -90d;
//        }
//
//        private static void drawProjectile(Graphics2D g, ProjectileLaunch launch, double prevTime, double curTime) {
//            g.setColor(Color.yellow);
//            double theta = getAngle(launch.directionX, launch.directionY);
//            double spread = 15;
//            double innerRadius = launch.projectile.speed * (prevTime - launch.launchTime);
//            double outerRadius = launch.projectile.speed * (curTime - launch.launchTime);
//
//            Area inner = new Area(new Arc2D.Double(launch.launchLocation.x - innerRadius, launch.launchLocation.y - innerRadius, 2 * innerRadius, 2 * innerRadius, theta - spread, 2 * spread, Arc2D.PIE));
//            Area outer = new Area(new Arc2D.Double(launch.launchLocation.x - outerRadius, launch.launchLocation.y - outerRadius, 2 * outerRadius, 2 * outerRadius, theta - spread, 2 * spread, Arc2D.PIE));
//            outer.subtract(inner);
//            g.fill(outer);
//        }
//    }
}