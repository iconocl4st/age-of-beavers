package client.gui.game.gl.object;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ObjectCreators {

    private static final double UPPER = 0.5;
    private static final double LOWER = -0.5;

    private static ProgrammaticCreator createHuman() {
        double neckWidth = 0.05;
        double neckHeight = 0.25;
        double footWidth = 0.25;
        double waistWidth = 0.1;
        return new ProgrammaticCreator("human")
        // head...
        .addCircle(0, .45, .13, 20)
        // left leg
        .addPolygon(new double[][]{
                {-waistWidth, 0.000000},
                {waistWidth, 0.000000},
                {-footWidth, LOWER},
        })
        // right leg
        .addPolygon(new double[][]{
                {-waistWidth, 0.000000},
                {waistWidth, 0.000000},
                {footWidth, LOWER},
        })
        // body
        .addPolygon(new double[][]{
                {-waistWidth, 0.000000},
                {waistWidth, 0.000000},
                {neckWidth, neckHeight},
                {-neckWidth, neckHeight},
        })
        // arms
        .addPolygon(new double[][]{
                {LOWER, neckHeight},
                {UPPER, neckHeight},
                {0, 0.1},
        });
    }

    private static void write(ProgrammaticCreator pc, String path) throws IOException {
        System.out.println("Saving to " + path);
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(path));) {
            bufferedWriter.write(pc.creator.create().toString());
        }
    }

    public static void main(String[] args) throws IOException {
        write(createHuman(), "objs/human2.obj");
    }
}
