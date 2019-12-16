package client.gui.game.gl;

import client.gui.game.Colors;
import client.gui.game.GamePainter;
import client.gui.game.Renderer;
import client.gui.game.ZLevels;
import client.gui.mouse.BuildingPlacer;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import common.util.Profiler;
import common.util.TicksPerSecondTracker;

import java.awt.*;

class GlPainter implements GLEventListener {

    private final GlRectangleListener recListener;
    private final GlPressListener pressListener;
    private final GlZoomListener zoomListener;
    private final MapToScreenContext mapContext;
    private final Profiler profiler;
    private final BuildingPlacer placer;

    private GLU glu;
    private TextRenderer textRenderer;
    private TextureCache textureCache;
    private GlZoom glZoom;
    private GlRenderer renderer;

    GamePainter.RenderContext renderContext;
    private TicksPerSecondTracker tracker = new TicksPerSecondTracker(2);

    private int mouseX;
    private int mouseY;

    GlPainter(
            TextureCache textureCache,
            GlZoom glZoom,
            GlRectangleListener reclistener,
            GlPressListener pressListener,
            GlZoomListener zoomListener,
            Profiler profiler,
            BuildingPlacer placer
    ) {
        this.textureCache = textureCache;
        this.glZoom = glZoom;
        this.mapContext = new MapToScreenContext();
        this.recListener = reclistener;
        this.pressListener = pressListener;
        this.zoomListener = zoomListener;
        this.profiler = profiler;
        this.placer = placer;
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

        textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 36));
        textRenderer.setColor(Color.pink);

        renderer = new GlRenderer();

        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);

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
        renderer.gl = gl;

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        gl.glTranslated(-glZoom.locationX, -glZoom.locationY, glZoom.locationZ);

        mapContext.init(gl);
        updateScreenLocation();

        if (renderer != null && renderContext != null && renderContext.getState() != null) {
            renderContext.xmin = glZoom.screenLowerX;
            renderContext.ymin = glZoom.screenLowerY;
            renderContext.xmax = glZoom.screenUpperX;
            renderContext.ymax = glZoom.screenUpperY;
            renderContext.currentTime = renderContext.getState().getCurrentGameTime();

            GamePainter.s_renderGame(renderer, renderContext, profiler);
        }

        pressListener.update(mapContext, glu);
        zoomListener.update(mapContext, glu);
        updateSelecting(gl);
        checkPlacing();

        tracker.receiveTick();
        if (renderer != null && renderContext != null && renderContext.getState() != null) {
//            textRenderer.beginRendering(glZoom.screenWidth, glZoom.screenHeight);
//        , ZLevels.Z_FPS
//            textRenderer.draw(String.valueOf(renderContext.currentTime) + " fps: " + tracker.lastAverage, 0, 10);
//            textRenderer.endRendering();
//            textRenderer.flush();
        }
    }

    private void checkPlacing() {
        if (placer.isNotPlacing()) return;
        mapContext.map(glu, mouseX, mouseY);
        placer.setPosition((int) mapContext.gameLocationX, (int) mapContext.gameLocationY);
        Rectangle r = placer.getBuildingLocation();
        if (r == null)
            return;

        Color c;
        if (placer.canBuild()) {
            c = Colors.CAN_PLACE;
        } else {
            c = Colors.CANNOT_PLACE;
        }
        renderer.fillRectangle(c, r.x, r.y, r.width, r.height, ZLevels.Z_BUILDING_PLACEMENT);
    }

    private void updateSelecting(GL2 gl) {
        synchronized (recListener.sync) {
            if (renderer == null)
                return;
            if (!recListener.update(mapContext, glu))
                return;
            renderer.drawRectangleEndPoints(Color.red, recListener.gameXBegin, recListener.gameYBegin, recListener.gameXCurrent, recListener.gameYCurrent, 0.0);
        }
    }

    private void updateScreenLocation() {
        synchronized (glZoom.sync) {
            mapContext.map(glu, 0d, 0d);
            glZoom.screenLowerX = mapContext.gameLocationX;
            glZoom.screenUpperY = mapContext.gameLocationY;

            mapContext.map(glu, glZoom.screenWidth, glZoom.screenHeight);
            glZoom.screenUpperX = mapContext.gameLocationX;
            glZoom.screenLowerY = mapContext.gameLocationY;
        }
    }

    /**
     * Called back before the OpenGL context is destroyed. Release resource such as buffers.
     */
    @Override
    public void dispose(GLAutoDrawable drawable) {

    }


    void paintBuckShot(GL2 gl) {
//
//        double counter = 0.3;
//        double prev = counter;
//        counter += 0.3;
//        fillArc(gl, Color.yellow, -2, -2, prev, counter, 0, Math.PI / 10);
//        if (counter > 4) {
//            counter = 0;
//        }
    }

    void setCurrentMousePosition(int x, int y) {
        this.mouseX = x;
        this.mouseY = y;
    }


    class GlRenderer implements Renderer {
        GL2 gl;

        @Override
        public void fillEverything(Color color, double z) {}

        @Override
        public void fillRectangle(Color color, double x, double y, double w, double h, double z) {
            fillRectangleEndPoints(color, x, y, x + w, y + h, z);
        }

        @Override
        public void fillRectangleEndPoints(Color color, double x1, double y1, double x2, double y2, double z) {
            if (noNeedToDraw(x1, y1, x2, y2))
                return;
            gl.glBegin(GL2.GL_QUADS);
            {
                setColor(gl, color);
                gl.glVertex3d(x1, y1, z);
                gl.glVertex3d(x2, y1, z);
                gl.glVertex3d(x2, y2, z);
                gl.glVertex3d(x1, y2, z);
            }
            gl.glEnd();
        }

        private void drawRectangleEndPoints(Color color, double x1, double y1, double x2, double y2, double z) {
            if (noNeedToDraw(x1, y1, x2, y2))
                return;

            // could use shorts...
            setColor(gl, color);
            gl.glBegin(GL2.GL_LINES);
            {
                gl.glVertex3d(x1, y1, z);
                gl.glVertex3d(x1, y2, z);
            }
            gl.glEnd();

            gl.glBegin(GL2.GL_LINES);
            {
                gl.glVertex3d(x1, y2, z);
                gl.glVertex3d(x2, y2, z);
            }
            gl.glEnd();

            gl.glBegin(GL2.GL_LINES);
            {
                gl.glVertex3d(x2, y2, z);
                gl.glVertex3d(x2, y1, z);
            }
            gl.glEnd();

            gl.glBegin(GL2.GL_LINES);
            {
                gl.glVertex3d(x2, y1, z);
                gl.glVertex3d(x1, y1, z);
            }
            gl.glEnd();
        }

        @Override
        public void drawRectangle(Color color, double x, double y, double w, double h, double z) {
            drawRectangleEndPoints(color, x, y, x + w, y + h, z);
        }

        @Override
        public void drawLine(Color color, double x1, double y1, double x2, double y2, double z) {
            if (noNeedToDraw(x1, y1, x2, y2))
                return;
            setColor(gl, color);
            gl.glBegin(GL2.GL_LINES);
            {
                gl.glVertex3d(x1, y1, z);
                gl.glVertex3d(x2, y2, z);
            }
        }

        @Override
        public void fillArc(Color color, double centerX, double centerY, double r1, double r2, double beginAngle, double endAngle, double z) {

        }

        @Override
        public void fillCircle(Color color, double x, double y, double r, double z) {

        }

        @Override
        public void drawProgress(double progress, Color outerColor, Color innerColor, double xCenter, double yCenter, double innerR, double outerR, double zAction) {

        }

        @Override
        public void drawGameString(Color color, String str, double x, double y, double z) {

        }

        @Override
        public void drawScreenString(Color color, String str, int i, int i1, double z) {

        }

        @Override
        public void paintImage(String imagePath, double x, double y, double w, double h, double z) {
            if (noNeedToDraw(x, y, x + w, y + h))
                return;
            if (true) return;

            Texture texture = textureCache.getTexture(imagePath, gl);

            TextureCoords textureCoords = texture.getImageTexCoords();
            float textureTop = textureCoords.top();
            float textureBottom = textureCoords.bottom();
            float textureLeft = textureCoords.left();
            float textureRight = textureCoords.right();

//            texture.enable(gl);
            texture.bind(gl);
            gl.glBegin(GL2.GL_QUADS);
            setColor(gl, Color.white);
            {
                gl.glTexCoord2f(textureLeft, textureBottom);
                gl.glVertex3d(x, y, z);
                gl.glTexCoord2f(textureRight, textureBottom);
                gl.glVertex3d(x + w, y, z);
                gl.glTexCoord2f(textureRight, textureTop);
                gl.glVertex3d(x + w, y + h, z);
                gl.glTexCoord2f(textureLeft, textureTop);
                gl.glVertex3d(x, y + h, z);
            }
            gl.glEnd();
            texture.disable(gl);
        }
    }

    private boolean noNeedToDraw(double x1, double y1, double x2, double y2) {
        return (
            Math.min(x1, x2) > glZoom.screenUpperX ||
            Math.max(x1, x2) < glZoom.screenLowerX ||
            Math.min(y1, y2) > glZoom.screenUpperY || // TODO
            Math.max(y1, y2) < glZoom.screenLowerY ||
            glZoom.locationZ >= 0
        );
    }

    private void fillOval(GL2 gl, Color color, double x1, double y1, double x2, double y2, double z) {
        fillOval(gl, color, x1, y1, x2, y2, GlConstants.SegmentsPerCircle.ceilingEntry(glZoom.locationZ).getValue(), z);
    }

    private void fillOval(GL2 gl, Color color, double x1, double y1, double x2, double y2, int numSegments, double z) {
        if (noNeedToDraw(x1, y1, x2, y2))
            return;
        double centerX = (x2 + x1) / 2;
        double centerY = (y2 + y1) / 2;
        double radiusX = (x2 - x1) / 2;
        double radiusY = (y2 - y1) / 2;
        gl.glBegin(GL2.GL_POLYGON);
        gl.glColor4f(1f, 1f, 1f, 1f);
        {
            setColor(gl, color);
            for (int i = 0; i < numSegments; i++) {
                double angle = 2 * Math.PI * i / numSegments;
                double x = centerX + radiusX * Math.cos(angle);
                double y = centerY + radiusY * Math.sin(angle);
                gl.glVertex3d(x, y, z);
            }
        }
        gl.glEnd();
    }

    private void fillArc(GL2 gl, Color color, double centerX, double centerY, double r1, double r2, double beginAngle, double endAngle, double z) {
        fillArc(gl, color, centerX, centerY, r1, r2, beginAngle, endAngle, GlConstants.SegmentsPerCircle.ceilingEntry(glZoom.locationZ).getValue(), z);
    }
    private void fillArc(GL2 gl, Color color, double centerX, double centerY, double r1, double r2, double beginAngle, double endAngle, int numSegments, double z) {
        if (noNeedToDraw(centerX - r2, centerY - r2, centerX + r2, centerY + r2))
            return;

        for (int i = 0; i < numSegments; i++) {
            gl.glBegin(GL2.GL_POLYGON);
            setColor(gl, color);
            double angle1 = beginAngle + (endAngle - beginAngle) * (i + 0) / (double) numSegments;
            double angle2 = beginAngle + (endAngle - beginAngle) * (i + 1) / (double) numSegments;
            gl.glVertex3d(centerX + r1 * Math.cos(angle1), centerY + r1 * Math.sin(angle1), z);
            gl.glVertex3d(centerX + r2 * Math.cos(angle1), centerY + r2 * Math.sin(angle1), z);
            gl.glVertex3d(centerX + r2 * Math.cos(angle2), centerY + r2 * Math.sin(angle2), z);
            gl.glVertex3d(centerX + r1 * Math.cos(angle2), centerY + r1 * Math.sin(angle2), z);
            gl.glEnd();
        }
    }

    private void setColor(GL2 gl, Color color) {
        gl.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1f); // color.getAlpha());
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