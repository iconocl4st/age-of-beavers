package common.state.edit;

import common.state.spec.GameSpec;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Paths;

public class GameSpecManager {

    public static class GameSpecCreator {
        private Creators.GameSpecCreator delegate;

        private GameSpecCreator(Creators.GameSpecCreator delegate) {
            this.delegate = delegate;
        }

        public void reset() {

        }

        public String serialize() {
            return CreatorParser.save(delegate);
        }

        public GameSpec getGameSpec() {
            return delegate.create();
        }

        public JPanel createEditor() {
            return null;
        }
    }

    public static GameSpec deserialize(String json) {
        return CreatorParser.parse(json).create();
    }

    public static GameSpecCreator createDefaultCreator() throws IOException {
        return new GameSpecCreator(CreatorParser.parseCreator(Paths.get("./specification")));
    }
}








//    private static final HashSet<String> NATURAL_RESOURCE_CLASSES = new HashSet<>(Arrays.asList("natural-resource", "visible-in-fog", "occupies"));
//    private static void parseNaturalResources(Path location, GameSpec spec) throws IOException, ParseException {
//        JSONArray resourceTypes = (JSONArray) getJson(location.resolve("natural_resources.json")).get("natural-resources");
//        LinkedList<EntitySpec> parsedResources = new LinkedList<>();
//        for (int i = 0; i < resourceTypes.size(); i++) {
//            JSONObject resourceType = (JSONObject) resourceTypes.get(i);
//            EntitySpec eSpec = new EntitySpec(
//                    (String) resourceType.get("name"),
//                    (String) resourceType.get("image")
//            );
//            eSpec.size = new Dimension(1, 1);
//            eSpec.carrying = Collections.singleton(new CarrySpec(
//                    spec.getResourceType((String) resourceType.get("resource-type")),
//                    (int) ((long) resourceType.get("quantity"))
//            ));
//            eSpec.dropOnDeath = Collections.emptyList();
//            HashMap<ResourceType, Integer> carryLimits = new HashMap<>();
//            for (CarrySpec carrySpec : eSpec.carrying) {
//                carryLimits.put(carrySpec.type, carrySpec.startingQuantity);
//            }
//            String seedName = (String) resourceType.get("grows-from");
//            if (seedName != null) {
//                ResourceType seedType = spec.getResourceType(seedName);
//                if (seedType.growsInto != null) throw new IllegalStateException("Seed grows into multiple things...");
//                seedType.growsInto = eSpec;
//            }
//
//            eSpec.carryCapacity = PrioritizedCapacitySpec.createCapacitySpec(Arrays.asList(spec.resourceTypes), carryLimits, false);
//            eSpec.classes = NATURAL_RESOURCE_CLASSES;
//            eSpec.requiredResources = Collections.emptyMap();
//            eSpec.canCreate = Collections.emptySet();
//            eSpec.aiArgs = Collections.emptyMap();
//            parsedResources.add(eSpec);
//        }
//        spec.naturalResources = parsedResources.toArray(new EntitySpec[0]);
//    }