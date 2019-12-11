package common.state.edit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CreatorParser {

    static String save(Creators.GameSpecCreator creator) {
        JSONObject theWholeKitAndCadoodle = new JSONObject();

        JSONObject topLevel = new JSONObject();
        creator.save(topLevel);
        theWholeKitAndCadoodle.put("top-level", topLevel);

        theWholeKitAndCadoodle.put("resources", Creators.toArray(creator.resources));
        theWholeKitAndCadoodle.put("units", Creators.toArray(creator.entities));
        theWholeKitAndCadoodle.put("weapons", Creators.toArray(creator.weapons));

        JSONObject generation = new JSONObject();
        creator.generation.save(generation);
        theWholeKitAndCadoodle.put("generation", generation);

        JSONObject placeable = new JSONObject();
        creator.canPlace.save(placeable);
        theWholeKitAndCadoodle.put("placeable", placeable);

        return theWholeKitAndCadoodle.toString(2);
    }

    static Creators.GameSpecCreator parse(String json) {
        JSONObject theWholeKitAndCadoodle = new JSONObject(json);

        Creators.GameSpecCreator spec = new Creators.GameSpecCreator();
        spec.parse(theWholeKitAndCadoodle.getJSONObject("top-level"));

        JSONArray r = theWholeKitAndCadoodle.getJSONArray("resources");
        for (int i = 0; i < r.length(); i++)
            spec.resources.add(Creators.ResourceCreator.parseResource(r.getJSONObject(i)));

        JSONArray u = theWholeKitAndCadoodle.getJSONArray("units");
        for (int i = 0; i < u.length(); i++)
            spec.entities.add(Creators.EntityCreator.parseEntity(u.getJSONObject(i)));

        JSONArray w = theWholeKitAndCadoodle.getJSONArray("weapons");
        for (int i = 0; i < w.length(); i++)
            spec.weapons.add(Creators.WeaponSpecCreator.parseW(w.getJSONObject(i)));

        spec.resources.sort(Comparator.comparing(rt -> rt.name));
        spec.entities.sort(Comparator.comparing(rt -> rt.name));
        spec.weapons.sort(Comparator.comparing(rt -> rt.name));

        spec.generation.parse(theWholeKitAndCadoodle.getJSONObject("generation"));
        spec.canPlace.parse(theWholeKitAndCadoodle.getJSONObject("placeable"));

        return spec;
    }

    static Creators.GameSpecCreator parseCreator(Path location) throws IOException {
        Creators.GameSpecCreator spec = new Creators.GameSpecCreator();
        spec.parse(getJson(location.resolve("top_lvl.json")));

        JSONArray o = getJson(location.resolve("resource_types.json")).getJSONArray("resource-types");
        for (int i = 0; i < o.length(); i++)
            spec.resources.add(Creators.ResourceCreator.parseResource(o.getJSONObject(i)));

        JSONArray w = getJson(location.resolve("weapons.json")).getJSONArray("weapons");
        for (int i = 0; i < w.length(); i++)
            spec.weapons.add(Creators.WeaponSpecCreator.parseW(w.getJSONObject(i)));

        spec.generation.parse(getJson(location.resolve("generation.json")).getJSONObject("generation"));

        spec.canPlace.parse(getJson(location.resolve("placeable.json")));

        try (Stream<Path> paths = Files.walk(location.resolve("units")).filter(Files::isRegularFile)) {
            for (Path p : paths.collect(Collectors.toList()))
                try {
                    spec.entities.add(Creators.EntityCreator.parseEntity(getJson(p)));
                } catch (Throwable t) {
                    System.out.println("Error parsing file " + p.toString());
                    throw t;
                }
        }

        spec.resources.sort(Comparator.comparing(rt -> rt.name));
        spec.entities.sort(Comparator.comparing(rt -> rt.name));
        spec.weapons.sort(Comparator.comparing(rt -> rt.name));

        spec.compile();
        return  spec;
    }

    /*

  "can-create": [{
    "creation-method": "Garrison",
    "created-unit": "human"
  }]

  "can-create": [
    {
      "creation-method": "Aura",
      "created-unit": "horse"
    },
    {
      "creation-method": "Aura",
      "created-unit": "cow"
    }
  ]



  "can-create": [{
    "creation-method": "Garrison",
    "created-unit": "wagon"
  }]

     */

    static void save(Creators.GameSpecCreator creator, Path location) throws IOException {
        creator.resources.sort(Comparator.comparing(rt -> rt.name));
        creator.entities.sort(Comparator.comparing(rt -> rt.name));
        creator.weapons.sort(Comparator.comparing(rt -> rt.name));

        JSONObject topLevel = new JSONObject();
        creator.save(topLevel);
        writeJson(topLevel, location.resolve("top_lvl.json"));

        JSONObject resources = new JSONObject();
        resources.put("resource-types", Creators.toArray(creator.resources));
        writeJson(resources, location.resolve("resource_types.json"));

        JSONObject weapons = new JSONObject();
        weapons.put("weapons", Creators.toArray(creator.weapons));
        writeJson(weapons, location.resolve("weapons.json"));

        JSONObject generation = new JSONObject();
        JSONObject generationObj;
        generation.put("generation", generationObj = new JSONObject());
        creator.generation.save(generationObj);
        writeJson(generation, location.resolve("generation.json"));

        JSONObject placeable = new JSONObject();
        creator.canPlace.save(placeable);
        writeJson(placeable, location.resolve("placeable.json"));

        Path unitsDirectory = location.resolve("units");
        if (!Files.exists(unitsDirectory)) {
            Files.createDirectory(unitsDirectory);
        }
        if (!Files.isDirectory(unitsDirectory))
            throw new IllegalStateException("Expected directory: " + unitsDirectory);

        HashMap<String, Integer> pathsAlreadyUsed = new HashMap<>();
        for (Creators.EntityCreator entity : creator.entities) {
            JSONObject obj = new JSONObject();
            entity.save(obj);
            writeJson(obj, getPath(pathsAlreadyUsed, unitsDirectory, entity.name));
        }
    }






    private static Path getPath(HashMap<String, Integer> pathsAlreadyUsed, Path location, String name) {
        String sanitized = name.replace('-', '_').replaceAll("[^a-zA-Z0-9_]", "");
        int numUsed = pathsAlreadyUsed.getOrDefault(sanitized, 0);
        pathsAlreadyUsed.put(sanitized, numUsed + 1);
        if (numUsed == 0) {
            return location.resolve(sanitized + ".json");
        }
        return location.resolve(sanitized + "-" + numUsed + ".json");
    }

    private static JSONObject getJson(Path path) throws IOException {
        try (FileReader fileReader = new FileReader(path.toFile())) {
            return new JSONObject(new JSONTokener(fileReader));
        }
    }
    private static void writeJson(JSONObject object, Path path) throws IOException {
        try (FileWriter fileWriter = new FileWriter(path.toFile())) {
            object.write(fileWriter, 2, 0);
        }
    }
}
