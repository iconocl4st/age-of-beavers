package app;

public class AiConstants {
    public static final int MULTIPLY_CREATION_PRODUCE_DEMAND = 4;
    public static final int SS = 30;
    public static final int FENCED_WIDTH = 3 + 2 * 4;

    public static final int PRODUCE_DEMAND_PRIORITY = 3;
    public static final int CONSTRUCTION_DEMAND_PRIORITY = 2;
    public static final int NUM_KMEANS_UPDATES = 20;
    public static final double PERCENTAGE_TO_TRANSPORT = 0.1;
    public static final double MAXIMUM_STORAGE_PERCENTAGE = 0.5;
    public static final int MAXIMUM_FOOD_BEFORE_MORE_BROTHELS = 500;
    public static final double DISTANCE_DELTA_TO_CREATE_WAGONS = 10;
    public static final int MINIMUM_POPULATION_PER_BROTHEL_FOR_NEW_BROTHEL = 5;
    public static final int BUILDING_PLACEMENT_BUFFER = 5;


    private static int current_priority = -2;
    public static final int IDLE_PRIORITY = ++current_priority;
    public static final int STAY_BUSY_PRIORITY = ++current_priority;
    public static final int COLLECT_MISSING_RESOURCES_PRIORITY = ++current_priority;
    public static final int MINIMUM_NUMBER_OF_ALLOCATIONS_PRIORITY = ++current_priority;
    public static final int CONSTRUCTION_PRIORITY = ++current_priority;
    public static final int GARRISON_PRIORITY = ++current_priority;
    public static final int DROPOFF_CART_PRIORITY = ++current_priority;
    public static final int TRANSPORTATION_PRIORITY = ++current_priority;
    public static final int PRODUCE_PRIORITY = ++current_priority;
}
