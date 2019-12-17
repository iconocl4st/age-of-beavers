package client.gui.game.gl.object;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ObjParser {

    public static void main(String[] args) throws IOException {
        Obj obj = parseObjectFile("/home/thallock/untitled.obj");
        System.out.println(obj);
    }

    private static Mtl parseMaterialFile(String file) throws IOException {
        MtlCreator creator = new MtlCreator();
        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(file));) {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                String[] components = line.split(" ");
                switch (components[0]) {
                    case "#":
                        continue;
                    case "newmtl":
                    case "Ns":
                    case "Ka":
                    case "Kd":
                    case "Ks":
                    case "Ke":
                    case "Ni":
                    case "g":
                    case "d":
                    case "illum":
                    default:
                        System.out.println("Ignoring line " + components[0]);
                }
            }
        }
        return creator.create();
    }
    
    public static Obj parseObjectFile(String file) throws IOException {
        ObjCreator creator = new ObjCreator();
        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(file));) {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                String[] components = line.split(" ");
                switch (components[0]) {
                    case "#":
                        continue;
                    case "o":
                        creator.name = components[1];
                        break;
                    case "v":
                        // inferred 1 at the end
                        creator.vertices.addLast(ParseUtils.parseDoubleArray(components, 1, 4));
                    break;
                    case "vt":
                        creator.vertexTextures.addLast(ParseUtils.parseDoubleArray(components, 1, 3));
                        break;
                    case "vn":
                        creator.vertexNormals.addLast(ParseUtils.parseDoubleArray(components, 1, 4));
                        break;
                    case "f":
                        creator.faces.addLast(ParseUtils.parseFace(components, 1, components.length));
                        break;
                    case "l": // line
                    case "g": // group name
                    case "mtllib": // material library
                    case "s": // smoothing off or on
                    case "usemtl":
                    default:
                        System.out.println("Ignoring line " + components[0]);
                }
            }
        }
        return creator.create();
    }
}
