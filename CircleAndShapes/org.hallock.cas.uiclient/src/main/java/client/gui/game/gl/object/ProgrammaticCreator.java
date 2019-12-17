package client.gui.game.gl.object;

import java.util.HashMap;

class ProgrammaticCreator {

    ObjCreator creator = new ObjCreator();
    private HashMap<String, Integer> map = new HashMap<>();

    ProgrammaticCreator(String name) {
        creator.name = name;
    }

    private int addVertex(double[] vertex) {
        String h = hash(vertex);
        Integer integer = map.get(h);
        if (integer == null) {
            int index = creator.vertices.size() + 1;
            map.put(h, index);
            creator.vertices.addLast(vertex);
            return index;
        }
        return integer;
    }

    ProgrammaticCreator addCircle(double x, double y, double r, int N) {
        int[][] face = new int[N][3];
        for (int i = 0; i < N; i++) {
            face[i][0] = addVertex(new double[]{
                    x + r * Math.cos(2 * Math.PI * i / N),
                    y + r * Math.sin(2 * Math.PI * i / N),
                    0
            });
        }
        creator.faces.add(face);
        return this;
    }

    ProgrammaticCreator addPolygon(double[][] locations) {
        int[][] face = new int[locations.length][3];
        for (int i = 0; i < locations.length; i++) {
            face[i][0] = addVertex(new double[]{
                locations[i][0],
                locations[i][1],
                0
            });
        }
        creator.faces.add(face);
        return this;
    }

    private static String hash(double[] ds) {
        StringBuilder builder = new StringBuilder();
        for (double d : ds) builder.append(d).append(":");
        return builder.toString();
    }
}
