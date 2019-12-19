package common.state.spec;

public interface EntityClasses {
    String OCCUPIES = "occupies";
    String PLAYER_OCCUPIES = "player-occupies";
    String GARRISONS_OTHERS = "can-garrison-others";
    String STORAGE = "storage";
    String NATURAL_RESOURCE = "natural-resource";
    String HUNTER = "hunter";
    String CARRIER = "carrier";
    String CONSTRUCTOR = "constructor";
    String GATHERER = "gatherer";
    String CAN_GARRISON = "can-garrison-in-others";
    String RIDER = "rider";
    String RIDABLE = "ridable";
    String CONSTRUCTION_ZONE = "construction-zone";
    String UNIT = "unit";
    String OWNED = "owned";
    String EVOLVES = "evolves";
    String VISIBLE_IN_FOG = "visible-in-fog";
    String PREY = "prey";
    String CONSTRUCTED = "constructed";
    String FARM = "crop";
    String FARMER = "farmer";

    String[] ALL_CLASSES = new String[] {
        OCCUPIES,
        PLAYER_OCCUPIES,
        GARRISONS_OTHERS,
        STORAGE,
        NATURAL_RESOURCE,
        HUNTER,
        CARRIER,
        CONSTRUCTOR,
        GATHERER,
        CAN_GARRISON,
        RIDER,
        RIDABLE,
        CONSTRUCTION_ZONE,
        UNIT,
        OWNED,
        EVOLVES,
        VISIBLE_IN_FOG,
        PREY,
        CONSTRUCTED,
        FARM,
        FARMER,
    };


    static boolean isValidClass(String str) {
        if (str == null) return false;
        for (String validClass : ALL_CLASSES) {
            if (validClass.equals(str))
                return true;
        }
        return false;
    }
}
