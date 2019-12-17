package client.gui.game.gl.object;

import java.util.LinkedList;

public class Obj {
    private final String name;
    private final double[][] vertices;
    private final double[][] vertexTextures;
    private final double[][] vertexNormals;
    private final int[][][] faces;

    Obj(String name, double[][] ts, double[][] ts1, double[][] ts2, int[][][] ts3) {
        this.name = name;
        this.vertices = ts;
        this.vertexTextures = ts1;
        this.vertexNormals = ts2;
        this.faces = ts3;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("o ").append(name).append('\n');
        ParseUtils.toString(builder, "v ", vertices);
        ParseUtils.toString(builder, "vt ", vertexTextures);
        ParseUtils.toString(builder, "vn ", vertexNormals);

        for (int[][] face : faces) {
            builder.append("f ");
            for (int j = 0; j < face.length; j++) {
                for (int k = 0; k < face[j].length; k++) {
                    builder.append(face[j][k]);
                    if (k != face[j].length - 1) {
                        builder.append('/');
                    }
                }
                if (j != face.length - 1) {
                    builder.append(' ');
                }
            }
            builder.append('\n');
        }
        return builder.toString();
    }

    private double[] getVertex(int i) {
        if (i < 0) {
            throw new UnsupportedOperationException("Not implemented");
        } else {
            return vertices[i - 1];
        }
    }

    public Face[] getFaces() {
        LinkedList<Face> ret = new LinkedList<>();
        for (int[][] f : faces) {
            float[][] vertexList = new float[f.length][3];
            for (int i = 0; i < vertexList.length; i++) {
                double[] vertices = getVertex(f[i][0]);
                for (int j = 0; j < 3; j++) {
                    vertexList[i][j] = (float) vertices[j];
                }
            }
            ret.add(new Face(vertexList));
        }

        return ret.toArray(new Face[0]);
    }
}
