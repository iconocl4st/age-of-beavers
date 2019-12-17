package client.gui.game.gl.object;

import java.util.LinkedList;

final class ObjCreator {
    String name;
    // inferred 1 at the end
    LinkedList<double[]> vertices = new LinkedList<>();
    // inferred 0 at the end
    LinkedList<double[]> vertexTextures = new LinkedList<>();
    // normalize these
    LinkedList<double[]> vertexNormals = new LinkedList<>();
    LinkedList<int[][]> faces = new LinkedList<>();


    Obj create() {
        return new Obj(
            name,
            vertices.toArray(ParseUtils.DOUBLE_2_DUMMY),
            vertexTextures.toArray(ParseUtils.DOUBLE_2_DUMMY),
            vertexNormals.toArray(ParseUtils.DOUBLE_2_DUMMY),
            faces.toArray(ParseUtils.INT_3_DUMMY)
        );
    }
}
