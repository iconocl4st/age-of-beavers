package client.gui.game.gl;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;

class MapToScreenContext {
    private int viewport[] = new int[4];
    private double mvmatrix[] = new double[16];
    private double projmatrix[] = new double[16];
    // Could combine these into a single array...
    private double wcoord1[] = new double[3];
    private double wcoord2[] = new double[3];

    double gameLocationX;
    double gameLocationY;

    void init(GL2 gl) {
        gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
        gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, mvmatrix, 0);
        gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, projmatrix, 0);
    }

    void map(GLU glu, double x, double y) {
        map(glu, x, y, 0);
    }

    void map(GLU glu, double x, double y, double desiredZ) {
        double realy = viewport[3] - y - 1;
        glu.gluUnProject(x, realy, 0d,
                mvmatrix, 0,
                projmatrix, 0,
                viewport, 0,
                wcoord1, 0
        );
        glu.gluUnProject(x, realy, 1d,
                mvmatrix, 0,
                projmatrix, 0,
                viewport, 0,
                wcoord2, 0
        );
        double t = (desiredZ - wcoord1[2]) / (wcoord2[2] - wcoord1[2]);
        gameLocationX = wcoord1[0] + (wcoord2[0] - wcoord1[0]) * t;
        gameLocationY = wcoord1[1] + (wcoord2[1] - wcoord1[1]) * t;
    }
}
