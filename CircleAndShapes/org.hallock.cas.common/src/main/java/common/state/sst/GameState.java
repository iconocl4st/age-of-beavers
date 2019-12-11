package common.state.sst;

import common.action.Action;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.Player;
import common.state.spec.EntitySpec;
import common.state.spec.GameSpec;
import common.state.sst.manager.*;
import common.state.sst.sub.*;
import common.state.sst.sub.capacity.PrioritizedCapacitySpec;
import common.util.BitArray;
import common.util.DPoint;
import common.util.EvolutionSpec;
import common.util.json.*;

import java.awt.*;
import java.io.IOException;

public class GameState implements Jsonable {

    public long timeOfGameTime;
    public double currentTime;
    public GameSpec gameSpec;
    public int numPlayers;

    // todo: rename to syncs
    public LocationManager locationManager;

    public EntityManager entityManager;
    public ReversableManagerImpl<EntityId, EntityId> garrisonManager;
    public ReversableManagerImpl<EntityId, EntityId> ridingManager;
    public ReversableManagerImpl<GateInfo, Point> gateStateManager;
    public ManagerImpl<ConstructionZone> constructionManager;
    public ManagerImpl<Double> ageManager;
    public ReversableManagerImpl<Action, Action.ActionType> actionManager;
    public ManagerImpl<Load> carryingManager;
    public ReversableManagerImpl<Player, Player> playerManager;
    public ReversableManagerImpl<EntitySpec, EntitySpec> typeManager;
    public BooleanManager hiddenManager;
    public ManagerImpl<Double> healthManager;
    public ManagerImpl<DPoint> gatherPointManager;
    public ManagerImpl<Double> movementSpeedManager;
    public ManagerImpl<Double> attackSpeedManager;
    public ManagerImpl<Double> baseHealthManager;
    public ManagerImpl<Double> rotationSpeedManager;
    public ManagerImpl<Double> orientationManager;
    public ManagerImpl<Double> lineOfSightManager;
    public ManagerImpl<Double> collectSpeedManager;
    public ManagerImpl<Double> depositSpeedManager;
    public ManagerImpl<Double> buildSpeedManager;
    public ManagerImpl<PrioritizedCapacitySpec> capacityManager;
    public ManagerImpl<EvolutionSpec> evolutionManager;
    public ManagerImpl<WeaponSet> weaponsManager;
    public ManagerImpl<ProjectileLaunch> projectileManager;
    public ManagerImpl<String> graphicsManager;
    public ManagerImpl<Double> gardenSpeed;
    public ManagerImpl<Double> burySpeed;
    public ManagerImpl<GrowthInfo> crops;
//    public ManagerImpl<Armor> armorManager;

    // for natural resources and buildings...
    // rename...
    public BitArray staticOccupancy;
    public BitArray buildingOccupancy;
    public Textures textures;

    public static GameState createGameState(GameSpec spec, int numPlayers) {
        GameState gs = new GameState();
        gs.numPlayers = numPlayers;
        gs.actionManager = new ReversableManagerImpl<>(action -> action.type, Action.Serializer);
        gs.carryingManager = new ManagerImpl<>(Load.Serializer);
        gs.entityManager = new EntityManager();
        gs.healthManager = new ManagerImpl<>(DataSerializer.DoubleSerializer);
        gs.locationManager = new LocationManager(spec);
        gs.playerManager = new ReversableManagerImpl<>(p -> p, Player.Serializer);
        gs.typeManager = new ReversableManagerImpl<>(e -> e, EntitySpec.Serializer);
        gs.constructionManager = new ManagerImpl<>(ConstructionZone.Serializer);
        gs.garrisonManager = new ReversableManagerImpl<>(e -> e, EntityId.Serializer);
        gs.ridingManager = new ReversableManagerImpl<>(e -> e, EntityId.Serializer);
        gs.movementSpeedManager = new ManagerImpl<>(DataSerializer.DoubleSerializer);
        gs.hiddenManager = new BooleanManager();
        gs.ageManager = new ManagerImpl<>(DataSerializer.DoubleSerializer);
        gs.gateStateManager = new ReversableManagerImpl<>(i -> i.location, GateInfo.Serializer);
        gs.gatherPointManager = new ManagerImpl<>(DPoint.Serializer);
        gs.attackSpeedManager = new ManagerImpl<>(DataSerializer.DoubleSerializer);
        gs.baseHealthManager = new ManagerImpl<>(DataSerializer.DoubleSerializer);
        gs.rotationSpeedManager = new ManagerImpl<>(DataSerializer.DoubleSerializer);
        gs.orientationManager = new ManagerImpl<>(DataSerializer.DoubleSerializer);
        gs.weaponsManager = new ManagerImpl<>(WeaponSet.Serializer);
        gs.projectileManager = new ManagerImpl<>(ProjectileLaunch.Serializer);
        gs.capacityManager = new ManagerImpl<>(PrioritizedCapacitySpec.Serializer);
        gs.buildSpeedManager = new ManagerImpl<>(DataSerializer.DoubleSerializer);
        gs.lineOfSightManager = new ManagerImpl<>(DataSerializer.DoubleSerializer);
        gs.collectSpeedManager = new ManagerImpl<>(DataSerializer.DoubleSerializer);
        gs.depositSpeedManager = new ManagerImpl<>(DataSerializer.DoubleSerializer);
        gs.evolutionManager = new ManagerImpl<>(EvolutionSpec.Serializer);
        gs.staticOccupancy = new BitArray(spec.width, spec.height);
        gs.buildingOccupancy = new BitArray(spec.width, spec.height);
        gs.textures = new Textures();
        gs.graphicsManager = new ManagerImpl<>(DataSerializer.StringSerializer);
        gs.crops = new ManagerImpl<>(GrowthInfo.Serializer);
        gs.gardenSpeed = new ManagerImpl<>(DataSerializer.DoubleSerializer);
        gs.burySpeed = new ManagerImpl<>(DataSerializer.DoubleSerializer);
        gs.gameSpec = spec;
        return gs;
    }

    public void updateAll(JsonReaderWrapperSpec reader, ReadOptions options) throws IOException {
        options.state = this;
        reader.readBeginDocument();
        reader.readName("actionManager"); actionManager.updateAll(reader, options);
        reader.readName("carryingManager"); carryingManager.updateAll(reader, options);
        reader.readName("entityManager"); entityManager.updateAll(reader, options);
        reader.readName("healthManager"); healthManager.updateAll(reader, options);
        reader.readName("locationManager"); locationManager.updateAll(reader, options);
        reader.readName("typeManager"); typeManager.updateAll(reader, options);
        reader.readName("playerManager"); playerManager.updateAll(reader, options);
        reader.readName("garrisonManager"); garrisonManager.updateAll(reader, options);
        reader.readName("ridingManager"); ridingManager.updateAll(reader, options);
        reader.readName("movementSpeedManager"); movementSpeedManager.updateAll(reader, options);
        reader.readName("hiddenManager"); hiddenManager.updateAll(reader, options);
        reader.readName("ageManager"); ageManager.updateAll(reader, options);
        reader.readName("gateStateManager"); gateStateManager.updateAll(reader, options);
        reader.readName("gatherPointManager"); gatherPointManager.updateAll(reader, options);
        reader.readName("attackSpeedManager"); attackSpeedManager.updateAll(reader, options);
        reader.readName("baseHealthManager"); baseHealthManager.updateAll(reader, options);
        reader.readName("rotationSpeedManager"); rotationSpeedManager.updateAll(reader, options);
        reader.readName("orientationManager"); orientationManager.updateAll(reader, options);
        reader.readName("weaponsManager"); weaponsManager.updateAll(reader, options);
        reader.readName("capacityManager"); capacityManager.updateAll(reader, options);
        reader.readName("buildSpeedManager"); buildSpeedManager.updateAll(reader, options);
        reader.readName("projectileManager"); projectileManager.updateAll(reader, options);
        reader.readName("constructionManager"); constructionManager.updateAll(reader, options);
        reader.readName("line-of-sight-manager"); lineOfSightManager.updateAll(reader, options);
        reader.readName("collect-speed-manager"); collectSpeedManager.updateAll(reader, options);
        reader.readName("deposit-speed-manager"); depositSpeedManager.updateAll(reader, options);
        reader.readName("evolution-manager"); evolutionManager.updateAll(reader, options);
        reader.readName("static-occupancy"); staticOccupancy.updateAll(reader, options);
        reader.readName("building-occupancy"); buildingOccupancy.updateAll(reader, options);
        reader.readName("textures"); textures.updateAll(reader, options);
        reader.readName("graphics"); graphicsManager.updateAll(reader, options);
        reader.readName("crops"); crops.updateAll(reader, options);
        reader.readName("gardening-speeds"); gardenSpeed.updateAll(reader, options);
        reader.readName("bury-speeds"); burySpeed.updateAll(reader, options);
        reader.readEndDocument();
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        // could save a lot of space by treating trees differently...
        writer.writeBeginDocument();
        writer.writeName("actionManager"); actionManager.writeTo(writer, options);
        writer.writeName("carryingManager"); carryingManager.writeTo(writer, options);
        writer.writeName("entityManager"); entityManager.writeTo(writer, options);
        writer.writeName("healthManager"); healthManager.writeTo(writer, options);
        writer.writeName("locationManager"); locationManager.writeTo(writer, options);
        writer.writeName("typeManager"); typeManager.writeTo(writer, options);
        writer.writeName("playerManager"); playerManager.writeTo(writer, options);
        writer.writeName("garrisonManager"); garrisonManager.writeTo(writer, options);
        writer.writeName("ridingManager"); ridingManager.writeTo(writer, options);
        writer.writeName("movementSpeedManager"); movementSpeedManager.writeTo(writer, options);
        writer.writeName("hiddenManager"); hiddenManager.writeTo(writer, options);
        writer.writeName("ageManager"); ageManager.writeTo(writer, options);
        writer.writeName("gateStateManager"); gateStateManager.writeTo(writer, options);
        writer.writeName("gatherPointManager"); gatherPointManager.writeTo(writer, options);
        writer.writeName("attackSpeedManager"); attackSpeedManager.writeTo(writer, options);
        writer.writeName("baseHealthManager"); baseHealthManager.writeTo(writer, options);
        writer.writeName("rotationSpeedManager"); rotationSpeedManager.writeTo(writer, options);
        writer.writeName("orientationManager"); orientationManager.writeTo(writer, options);
        writer.writeName("weaponsManager"); weaponsManager.writeTo(writer, options);
        writer.writeName("capacityManager"); capacityManager.writeTo(writer, options);
        writer.writeName("buildSpeedManager"); buildSpeedManager.writeTo(writer, options);
        writer.writeName("projectileManager"); projectileManager.writeTo(writer, options);
        writer.writeName("constructionManager"); constructionManager.writeTo(writer, options);
        writer.writeName("line-of-sight-manager"); lineOfSightManager.writeTo(writer, options);
        writer.writeName("collect-speed-manager"); collectSpeedManager.writeTo(writer, options);
        writer.writeName("deposit-speed-manager"); depositSpeedManager.writeTo(writer, options);
        writer.writeName("evolution-manager"); evolutionManager.writeTo(writer, options);
        writer.writeName("static-occupancy"); staticOccupancy.writeTo(writer, options);
        writer.writeName("building-occupancy"); buildingOccupancy.writeTo(writer, options);
        writer.writeName("textures"); textures.writeTo(writer, options);
        writer.writeName("graphics"); graphicsManager.writeTo(writer, options);
        writer.writeName("crops"); crops.writeTo(writer, options);
        writer.writeName("gardening-speeds"); gardenSpeed.writeTo(writer, options);
        writer.writeName("bury-speeds"); burySpeed.writeTo(writer, options);
        writer.writeEndDocument();
    }

    public void removeEntity(EntityId entity) {
        Object sync = entityManager.get(entity);
        if (sync == null) return;
        synchronized (sync) {
            actionManager.remove(entity);
            carryingManager.remove(entity);
            entityManager.remove(entity);
            healthManager.remove(entity);
            locationManager.remove(new EntityReader(this, entity));
            typeManager.remove(entity);
            playerManager.remove(entity);
            garrisonManager.remove(entity);
            ridingManager.remove(entity);
            movementSpeedManager.remove(entity);
            hiddenManager.remove(entity);
            ageManager.remove(entity);
            gateStateManager.remove(entity);
            gatherPointManager.remove(entity);
            attackSpeedManager.remove(entity);
            baseHealthManager.remove(entity);
            rotationSpeedManager.remove(entity);
            orientationManager.remove(entity);
            weaponsManager.remove(entity);
            capacityManager.remove(entity);
            lineOfSightManager.remove(entity);
            collectSpeedManager.remove(entity);
            depositSpeedManager.remove(entity);
            evolutionManager.remove(entity);
            graphicsManager.remove(entity);
            crops.remove(entity);
            gardenSpeed.remove(entity);
            burySpeed.remove(entity);
        }
    }

    public void updateTime(double currentGameTime, long timeOfGameTime) {
        currentTime = currentGameTime;
        this.timeOfGameTime = timeOfGameTime;
        locationManager.setTime(getCurrentGameTime());
    }

    public double getCurrentGameTime() {
        return currentTime + gameSpec.gameSpeed * (System.nanoTime() - timeOfGameTime) / 1e9d;
    }
}
