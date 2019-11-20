package common.msg;

import common.AiEvent;
import common.action.Action;
import common.app.LobbyInfo;
import common.state.EntityId;
import common.state.Player;
import common.state.spec.EntitySpec;
import common.state.spec.GameSpec;
import common.state.spec.ResourceType;
import common.state.sst.GameState;
import common.state.sst.sub.*;
import common.state.sst.sub.capacity.PrioritizedCapacitySpec;
import common.util.DPoint;
import common.util.EvolutionSpec;
import common.util.json.*;

import java.awt.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public abstract class Message implements Jsonable {

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("type", getMessageType().ordinal());
        writeInnards(writer, options);
        writer.writeEndDocument();
    }

    public void writeFromStart(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.flush();

        writer.write("type", getMessageType().ordinal());
        writeInnards(writer, options);
        writer.writeEndDocument();
    }

    protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {}

    // TODO: make part of the constructor
    public abstract MessageType getMessageType();



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class Inform extends Message {
        public static Inform finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) {
            return new Inform();
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.INFORM;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class Quit extends Message {
        public static Quit finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) {
            return new Quit();
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.QUIT_CONNECTION;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class ListLobbies extends Message {
        public static ListLobbies finishParsing(JsonReaderWrapperSpec reader, ReadOptions ReadOptions) {
            return new ListLobbies();
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.LIST_LOBBIES;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class LobbyList extends Message {
        public final List<LobbyInfo> infos;

        public LobbyList(List<LobbyInfo> status) {
            this.infos = status;
        }

        public static LobbyList finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new LobbyList((List<LobbyInfo>) reader.read("infos", spec, new LinkedList<>(), LobbyInfo.Serializer));
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("infos", infos, LobbyInfo.Serializer, options);
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.LOBBY_LIST;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class Join extends Message {
        public final LobbyInfo lobby;

        public Join(LobbyInfo lobby) {
            this.lobby = lobby;
        }

        public static Join finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new Join(reader.read("info", LobbyInfo.Serializer, spec));
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("info", lobby, LobbyInfo.Serializer, options);
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.JOIN;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class Spectate extends Message {
        public final boolean spectate;

        public Spectate(boolean spectate) {
            this.spectate = spectate;
        }

        public static Spectate finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new Spectate(reader.readBoolean("spectate"));
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("spectate", spectate);
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.SPECTATE;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class IsSpectating extends Message {
        public final boolean isSpectating;

        public IsSpectating(boolean isSpectating) {
            this.isSpectating = isSpectating;
        }

        public static IsSpectating finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new IsSpectating(reader.readBoolean("spectate"));
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("spectate", isSpectating);
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.SPECTATING;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class Joined extends Message {
        public final LobbyInfo lobby;

        public Joined(LobbyInfo lobby) {
            this.lobby = lobby;
        }

        public static Joined finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new Joined(reader.read("info", LobbyInfo.Serializer, spec));
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions verbose) throws IOException {
            writer.write("info", lobby, LobbyInfo.Serializer, verbose);
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.JOINED;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class Leave extends Message {
        public static Leave finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) {
            return new Leave();
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.LEAVE;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class Left extends Message {
        public static Left finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) {
            return new Left();
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.LEFT;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class Launch extends Message {
        public static Launch finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) {
            return new Launch();
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.LAUNCH;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class Launched extends Message {
        public final GameSpec spec;
        public final Player player;
        public final Point playerStart;

        public Launched(GameSpec spec, Player player, Point playerStart) {
            this.spec = spec;
            this.player = player;
            this.playerStart = playerStart;
        }

        public static Launched finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new Launched(
                reader.read("spec", GameSpec.Serializer, spec),
                reader.read("player", Player.Serializer, spec),
                reader.read("location", DataSerializer.PointSerializer, spec)
            );
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions verbose) throws IOException {
            writer.write("spec", spec, GameSpec.Serializer, verbose);
            writer.write("player", player, Player.Serializer, verbose);
            writer.write("location", playerStart, DataSerializer.PointSerializer, verbose);
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.LAUNCHED;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class GameOver extends Message {
        public static GameOver finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) {
            return new GameOver();
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.GAME_OVER;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class Error extends Message {
        public final String errorMsg;

        Error(String errorMsg) {
            this.errorMsg = errorMsg;
        }

        public static Error finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new Error(reader.readString("error-message"));
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions verbose) throws IOException {
            writer.write("error-message", errorMsg);
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.ERROR;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class UpdateEntireGameState extends Message {
        public final GameState gameState;

        public UpdateEntireGameState(GameState gameState) {
            this.gameState = gameState;
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions verbose) throws IOException {
            writer.writeName("state");
            gameState.writeTo(writer, verbose);
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.UPDATE_ENTIRE_GAME;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class ProjectileLaunched extends Message {
        public final EntityId entityId;
        public final ProjectileLaunch launch;

        public ProjectileLaunched(EntityId entityId, ProjectileLaunch id) {
            this.entityId = entityId;
            this.launch = id;
        }

        public static ProjectileLaunched finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            EntityId entity = reader.read("entity", EntityId.Serializer, spec);
            ProjectileLaunch projectile = reader.read("projectile", ProjectileLaunch.Serializer, spec);
            return new ProjectileLaunched(entity, projectile);
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("entity", entityId, EntityId.Serializer, options);
            writer.write("projectile", launch, ProjectileLaunch.Serializer, options);
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.PROJECTILE_LAUNCHED;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class ProjectileLanded extends Message {
        public final EntityId entityId;

        public ProjectileLanded(EntityId id) {
            this.entityId = id;
        }

        public static ProjectileLanded finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new ProjectileLanded(reader.read("entity", EntityId.Serializer, spec));
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("entity", entityId, EntityId.Serializer, options);
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.PROJECTILE_LANDED;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class UnitRemoved extends Message {
        public final EntityId unitId;

        public UnitRemoved(EntityId id) {
            this.unitId = id;
        }

        public static UnitRemoved finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new UnitRemoved(reader.read("entity", EntityId.Serializer, spec));
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("entity", unitId, EntityId.Serializer, options);
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.UNIT_REMOVED;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class UnitUpdated extends Message {
        public EntityId unitId;
        public DPoint location;
        public Action action;
        public Load load;
        public Double health;
        public Double baseHealth;
        public Player owner;
        public EntityId rides;
        public EntityId isWithin;
        public Double buildProgress;
        public EntitySpec isNowOfType;
        public Double newMovementSpeed;
        public Boolean isHidden;
        public Player losPlayer;
        public Double losDistance;
        public DPoint losOldLocation;
        public DPoint losNewLocation;
        public Double creationTime;
        public ConstructionZone constructionZone;
        public Double constructionProgress;
        public GateInfo occupancy;
        public DPoint gatherPoint;
        public Double attackSpeed;
        public Double rotationSpeed;
        public Double orientation;
        public WeaponSet weapons;
        public PrioritizedCapacitySpec capacity;
        public Double buildSpeed;
        public Double collectSpeed;
        public Double depositSpeed;
        public EvolutionSpec evolutionWeights;

        public String debug;

        public static UnitUpdated finishParsing(JsonReaderWrapperSpec reader, ReadOptions options) throws IOException {
            UnitUpdated unitUpdated = new UnitUpdated();
            unitUpdated.unitId = reader.read("unitId", EntityId.Serializer, options);
            long flags = reader.readLong("flags");
            if ((flags & 0b0000_0000_0000_0000_0000_0000_0000_0001) != 0) unitUpdated.location = reader.read("location", DPoint.Serializer, options);
            if ((flags & 0b0000_0000_0000_0000_0000_0000_0000_0010) != 0) unitUpdated.action = reader.read("action", Action.Serializer, options);
            if ((flags & 0b0000_0000_0000_0000_0000_0000_0000_0100) != 0) unitUpdated.load = reader.read("load", Load.Serializer, options);
            if ((flags & 0b0000_0000_0000_0000_0000_0000_0000_1000) != 0) unitUpdated.health = reader.readDouble("current-health");
            if ((flags & 0b0000_0000_0000_0000_0000_0000_0001_0000) != 0) unitUpdated.owner = reader.read("owner", Player.Serializer, options);
            if ((flags & 0b0000_0000_0000_0000_0000_0000_0010_0000) != 0) unitUpdated.rides = reader.read("rides", EntityId.Serializer, options);
            if ((flags & 0b0000_0000_0000_0000_0000_0000_0100_0000) != 0) unitUpdated.isWithin = reader.read("isWithin", EntityId.Serializer, options);
            if ((flags & 0b0000_0000_0000_0000_0000_0000_1000_0000) != 0) unitUpdated.buildProgress = reader.readDouble("buildProgress");
            if ((flags & 0b0000_0000_0000_0000_0000_0001_0000_0000) != 0) unitUpdated.isNowOfType = reader.read("isNowOfType", EntitySpec.Serializer, options);
            if ((flags & 0b0000_0000_0000_0000_0000_0010_0000_0000) != 0) unitUpdated.newMovementSpeed = reader.readDouble("newMovementSpeed");
            if ((flags & 0b0000_0000_0000_0000_0000_0100_0000_0000) != 0) unitUpdated.isHidden = reader.readBoolean("isHidden");
            if ((flags & 0b0000_0000_0000_0000_0000_1000_0000_0000) != 0) unitUpdated.losPlayer = reader.read("losPlayer", Player.Serializer, options);
            if ((flags & 0b0000_0000_0000_0000_0001_0000_0000_0000) != 0) unitUpdated.losDistance = reader.readDouble("losDistance");
            if ((flags & 0b0000_0000_0000_0000_0010_0000_0000_0000) != 0) unitUpdated.losOldLocation = reader.read("losOldLocation", DPoint.Serializer, options);
            if ((flags & 0b0000_0000_0000_0000_0100_0000_0000_0000) != 0) unitUpdated.losNewLocation = reader.read("losNewLocation", DPoint.Serializer, options);
            if ((flags & 0b0000_0000_0000_0000_1000_0000_0000_0000) != 0) unitUpdated.creationTime = reader.readDouble("creationTime");
            if ((flags & 0b0000_0000_0000_0001_0000_0000_0000_0000) != 0) unitUpdated.constructionZone = reader.read("constructionZone", ConstructionZone.Serializer, options);
            if ((flags & 0b0000_0000_0000_0010_0000_0000_0000_0000) != 0) unitUpdated.constructionProgress = reader.readDouble("constructionProgress");
            if ((flags & 0b0000_0000_0000_0100_0000_0000_0000_0000) != 0) unitUpdated.occupancy = reader.read("occupancy", GateInfo.Serializer, options);
            if ((flags & 0b0000_0000_0000_1000_0000_0000_0000_0000) != 0) unitUpdated.gatherPoint = reader.read("gatherPoint", DPoint.Serializer, options);
            if ((flags & 0b0000_0000_0001_0000_0000_0000_0000_0000) != 0) unitUpdated.attackSpeed = reader.readDouble("attackSpeed");
            if ((flags & 0b0000_0000_0010_0000_0000_0000_0000_0000) != 0) unitUpdated.rotationSpeed = reader.readDouble("rotationSpeed");
            if ((flags & 0b0000_0000_0100_0000_0000_0000_0000_0000) != 0) unitUpdated.orientation = reader.readDouble("orientation");
            if ((flags & 0b0000_0000_1000_0000_0000_0000_0000_0000) != 0) unitUpdated.weapons = reader.read("weapons", WeaponSet.Serializer, options);
            if ((flags & 0b0000_0001_0000_0000_0000_0000_0000_0000) != 0) unitUpdated.capacity = reader.read("capacity", PrioritizedCapacitySpec.Serializer, options);
            if ((flags & 0b0000_0010_0000_0000_0000_0000_0000_0000) != 0) unitUpdated.buildSpeed = reader.readDouble("buildSpeed");
            if ((flags & 0b0000_0100_0000_0000_0000_0000_0000_0000) != 0) unitUpdated.depositSpeed = reader.readDouble("deposit-speed");
            if ((flags & 0b0000_1000_0000_0000_0000_0000_0000_0000) != 0) unitUpdated.collectSpeed = reader.readDouble("collect-speed");
            if ((flags & 0b0001_0000_0000_0000_0000_0000_0000_0000) != 0) unitUpdated.evolutionWeights = reader.read("evolution-weights", EvolutionSpec.Serializer, options);
            if ((flags & 0b0010_0000_0000_0000_0000_0000_0000_0000) != 0) unitUpdated.baseHealth = reader.readDouble("base-health");
            if ((flags & 0b1000_0000_0000_0000_0000_0000_0000_0000) != 0) unitUpdated.debug = reader.readString("debug");
            return unitUpdated;
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("unitId", unitId, EntityId.Serializer, options);

            long flags = 0;
            if (location != null)               flags |= 0b0000_0000_0000_0000_0000_0000_0000_0001;
            if (action != null)                 flags |= 0b0000_0000_0000_0000_0000_0000_0000_0010;
            if (load != null)                   flags |= 0b0000_0000_0000_0000_0000_0000_0000_0100;
            if (health != null)                 flags |= 0b0000_0000_0000_0000_0000_0000_0000_1000;
            if (owner != null)                  flags |= 0b0000_0000_0000_0000_0000_0000_0001_0000;
            if (rides != null)                  flags |= 0b0000_0000_0000_0000_0000_0000_0010_0000;
            if (isWithin != null)               flags |= 0b0000_0000_0000_0000_0000_0000_0100_0000;
            if (buildProgress != null)          flags |= 0b0000_0000_0000_0000_0000_0000_1000_0000;
            if (isNowOfType != null)            flags |= 0b0000_0000_0000_0000_0000_0001_0000_0000;
            if (newMovementSpeed != null)       flags |= 0b0000_0000_0000_0000_0000_0010_0000_0000;
            if (isHidden != null)               flags |= 0b0000_0000_0000_0000_0000_0100_0000_0000;
            if (losPlayer != null)              flags |= 0b0000_0000_0000_0000_0000_1000_0000_0000;
            if (losDistance != null)            flags |= 0b0000_0000_0000_0000_0001_0000_0000_0000;
            if (losOldLocation != null)         flags |= 0b0000_0000_0000_0000_0010_0000_0000_0000;
            if (losNewLocation != null)         flags |= 0b0000_0000_0000_0000_0100_0000_0000_0000;
            if (creationTime != null)           flags |= 0b0000_0000_0000_0000_1000_0000_0000_0000;
            if (constructionZone != null)       flags |= 0b0000_0000_0000_0001_0000_0000_0000_0000;
            if (constructionProgress != null)   flags |= 0b0000_0000_0000_0010_0000_0000_0000_0000;
            if (occupancy != null)              flags |= 0b0000_0000_0000_0100_0000_0000_0000_0000;
            if (gatherPoint != null)            flags |= 0b0000_0000_0000_1000_0000_0000_0000_0000;
            if (attackSpeed != null)            flags |= 0b0000_0000_0001_0000_0000_0000_0000_0000;
            if (rotationSpeed != null)          flags |= 0b0000_0000_0010_0000_0000_0000_0000_0000;
            if (orientation != null)            flags |= 0b0000_0000_0100_0000_0000_0000_0000_0000;
            if (weapons != null)                flags |= 0b0000_0000_1000_0000_0000_0000_0000_0000;
            if (capacity != null)               flags |= 0b0000_0001_0000_0000_0000_0000_0000_0000;
            if (buildSpeed != null)             flags |= 0b0000_0010_0000_0000_0000_0000_0000_0000;
            if (depositSpeed != null)           flags |= 0b0000_0100_0000_0000_0000_0000_0000_0000;
            if (collectSpeed != null)           flags |= 0b0000_1000_0000_0000_0000_0000_0000_0000;
            if (evolutionWeights != null)       flags |= 0b0001_0000_0000_0000_0000_0000_0000_0000;
            if (baseHealth != null)             flags |= 0b0010_0000_0000_0000_0000_0000_0000_0000;
            if (debug != null)                  flags |= 0b1000_0000_0000_0000_0000_0000_0000_0000;

            writer.write("flags", flags);

            if ((flags & 0b0000_0000_0000_0000_0000_0000_0000_0001) != 0) writer.write("location", location, DPoint.Serializer, options);
            if ((flags & 0b0000_0000_0000_0000_0000_0000_0000_0010) != 0) writer.write("action", action, Action.Serializer, options);
            if ((flags & 0b0000_0000_0000_0000_0000_0000_0000_0100) != 0) writer.write("load", load, Load.Serializer, options);
            if ((flags & 0b0000_0000_0000_0000_0000_0000_0000_1000) != 0) writer.write("current-health", health);
            if ((flags & 0b0000_0000_0000_0000_0000_0000_0001_0000) != 0) writer.write("owner", owner, Player.Serializer, options);
            if ((flags & 0b0000_0000_0000_0000_0000_0000_0010_0000) != 0) writer.write("rides", rides, EntityId.Serializer, options);
            if ((flags & 0b0000_0000_0000_0000_0000_0000_0100_0000) != 0) writer.write("isWithin", isWithin, EntityId.Serializer, options);
            if ((flags & 0b0000_0000_0000_0000_0000_0000_1000_0000) != 0) writer.write("buildProgress", buildProgress);
            if ((flags & 0b0000_0000_0000_0000_0000_0001_0000_0000) != 0) writer.write("isNowOfType", isNowOfType, EntitySpec.Serializer, options);
            if ((flags & 0b0000_0000_0000_0000_0000_0010_0000_0000) != 0) writer.write("newMovementSpeed", newMovementSpeed);
            if ((flags & 0b0000_0000_0000_0000_0000_0100_0000_0000) != 0) writer.write("isHidden", isHidden);
            if ((flags & 0b0000_0000_0000_0000_0000_1000_0000_0000) != 0) writer.write("losPlayer", losPlayer, Player.Serializer, options);
            if ((flags & 0b0000_0000_0000_0000_0001_0000_0000_0000) != 0) writer.write("losDistance", losDistance);
            if ((flags & 0b0000_0000_0000_0000_0010_0000_0000_0000) != 0) writer.write("losOldLocation", losOldLocation, DPoint.Serializer, options);
            if ((flags & 0b0000_0000_0000_0000_0100_0000_0000_0000) != 0) writer.write("losNewLocation", losNewLocation, DPoint.Serializer, options);
            if ((flags & 0b0000_0000_0000_0000_1000_0000_0000_0000) != 0) writer.write("creationTime", creationTime);
            if ((flags & 0b0000_0000_0000_0001_0000_0000_0000_0000) != 0) writer.write("constructionZone", constructionZone, ConstructionZone.Serializer, options);
            if ((flags & 0b0000_0000_0000_0010_0000_0000_0000_0000) != 0) writer.write("constructionProgress", constructionProgress);
            if ((flags & 0b0000_0000_0000_0100_0000_0000_0000_0000) != 0) writer.write("occupancy", occupancy, GateInfo.Serializer, options);
            if ((flags & 0b0000_0000_0000_1000_0000_0000_0000_0000) != 0) writer.write("gatherPoint", gatherPoint, DPoint.Serializer, options);
            if ((flags & 0b0000_0000_0001_0000_0000_0000_0000_0000) != 0) writer.write("attackSpeed", attackSpeed);
            if ((flags & 0b0000_0000_0010_0000_0000_0000_0000_0000) != 0) writer.write("rotationSpeed", rotationSpeed);
            if ((flags & 0b0000_0000_0100_0000_0000_0000_0000_0000) != 0) writer.write("orientation", orientation);
            if ((flags & 0b0000_0000_1000_0000_0000_0000_0000_0000) != 0) writer.write("weapons", weapons, WeaponSet.Serializer, options);
            if ((flags & 0b0000_0001_0000_0000_0000_0000_0000_0000) != 0) writer.write("capacity", capacity, PrioritizedCapacitySpec.Serializer, options);
            if ((flags & 0b0000_0010_0000_0000_0000_0000_0000_0000) != 0) writer.write("buildSpeed", buildSpeed);
            if ((flags & 0b0000_0100_0000_0000_0000_0000_0000_0000) != 0) writer.write("deposit-speed", depositSpeed);
            if ((flags & 0b0000_1000_0000_0000_0000_0000_0000_0000) != 0) writer.write("collect-speed", collectSpeed);
            if ((flags & 0b0001_0000_0000_0000_0000_0000_0000_0000) != 0) writer.write("evolution-weights", evolutionWeights, EvolutionSpec.Serializer, options);
            if ((flags & 0b0010_0000_0000_0000_0000_0000_0000_0000) != 0) writer.write("base-health", baseHealth);
            if ((flags & 0b1000_0000_0000_0000_0000_0000_0000_0000) != 0) writer.write("debug", debug);
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.UNIT_UPDATED;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class PlaceBuilding extends Message {
        public final EntitySpec spec;
        public final Point location;

        public PlaceBuilding(EntitySpec building, int buildingLocX, int buildingLocY) {
            this.spec = building;
            this.location = new Point(buildingLocX, buildingLocY);
        }

        public static PlaceBuilding finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            EntitySpec entity = reader.read("entity", EntitySpec.Serializer, spec);
            Point location = reader.read("location", DataSerializer.PointSerializer, spec);
            return new PlaceBuilding(entity, location.x, location.y);
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("entity", spec, EntitySpec.Serializer, options);
            writer.write("location", location, DataSerializer.PointSerializer, options);
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.PLACE_BUILDING;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class ChangeOccupancy extends Message {
        public final EntityId entity;
        public final GateInfo.GateState newState;

        public ChangeOccupancy(EntityId entity, GateInfo.GateState newState) {
            this.entity = entity;
            this.newState = newState;
        }

        public static ChangeOccupancy finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            EntityId entity = reader.read("entity", EntityId.Serializer, spec);
            GateInfo.GateState b = reader.b(GateInfo.GateState.values(), reader.readInt32("gate-state"));
//            GateInfo b = reader.read("gate-state", spec, GateInfo.Serializer);
            return new ChangeOccupancy(entity, b);
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("entity", entity, EntityId.Serializer, options);
            writer.write("gate-state", newState.ordinal());
//            writer.write("gate-state", newState, GateInfo.Serializer, options);
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.CHANGE_OCCUPANCY;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class SetEvolutionSelection extends Message {
        public final EntityId entity;
        public final EvolutionSpec weights;

        public SetEvolutionSelection(EntityId entity, EvolutionSpec newState) {
            this.entity = entity;
            this.weights = newState;
        }

        public static SetEvolutionSelection finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new SetEvolutionSelection(
                reader.read("entity", EntityId.Serializer, spec),
                reader.read("new-weights", EvolutionSpec.Serializer, spec)
            );
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("entity", entity, EntityId.Serializer, options);
            writer.write("new-weights", weights, EvolutionSpec.Serializer, options);
//            writer.write("gate-state", newState, GateInfo.Serializer, options);
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.SET_EVOLUTION_SELECTION;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class SetDesiredCapacity extends Message {
        public final EntityId entity;
        public final ResourceType resourceType;
        public final int priority;
        public final int desiredMinimum;
        public final int desiredMaximum;

        public SetDesiredCapacity(EntityId entity, ResourceType resourceType, int priority, int desiredMinimum, int desiredMaximum) {
            this.entity = entity;
            this.resourceType = resourceType;
            this.priority = priority;
            this.desiredMinimum = desiredMinimum;
            this.desiredMaximum = desiredMaximum;
        }

        public static SetDesiredCapacity finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            EntityId entity = reader.read("entity", EntityId.Serializer, spec);
            ResourceType resource = reader.read("resource", ResourceType.Serializer, spec);
            Integer priority = reader.readInt32("priority");
            Integer minimum = reader.readInt32("minimum");
            Integer maximum = reader.readInt32("maximum");
            return new SetDesiredCapacity(entity, resource, priority, minimum, maximum);
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("entity", entity, EntityId.Serializer, options);
            writer.write("resource", resourceType, ResourceType.Serializer, options);
            writer.write("priority", priority);
            writer.write("minimum", desiredMinimum);
            writer.write("maximum", desiredMaximum);
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.SET_DESIRED_CAPACITY;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class AiEventMessage extends Message {
        public final AiEvent event;

        public AiEventMessage(AiEvent event) {
            this.event = event;
        }

        public static AiEventMessage finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new AiEventMessage(reader.read("event", AiEvent.Serializer, spec));
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("event", event, AiEvent.Serializer, options);
        }

        public MessageType getMessageType() { return MessageType.AI_EVENT; }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class RequestAction extends Message {
        public final EntityId performer;
        public final Action action;

        public RequestAction(EntityId entityId, Action action) {
            this.performer = entityId;
            this.action = action;
        }

        public static RequestAction finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new RequestAction(
                reader.read("performer", EntityId.Serializer, spec),
                reader.read("action", Action.Serializer, spec)
            );
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("performer", performer, EntityId.Serializer, options);
            writer.write("action", action, Action.Serializer, options);
        }

        public MessageType getMessageType() {
            return MessageType.REQUEST_ACTION;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class OccupancyChanged extends Message {
        public final Point location;
        public final Dimension size;
        public final boolean occupied;

        public OccupancyChanged(Point remLoc, Dimension size, boolean b) {
            this.location = remLoc;
            this.size = size;
            this.occupied = b;
        }

        public static OccupancyChanged finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new OccupancyChanged(
                reader.read("location", DataSerializer.PointSerializer, spec),
                reader.read("size", DataSerializer.DimensionSerializer, spec),
                reader.readBoolean("occupied")
            );
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("location", location, DataSerializer.PointSerializer, options);
            writer.write("size", size, DataSerializer.DimensionSerializer, options);
            writer.write("occupied", occupied);
        }

        public MessageType getMessageType() {
            return MessageType.OCCUPANCY_UPDATED;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class SetGatherPoint extends Message {
        public final EntityId entityId;
        public final DPoint location;

        public SetGatherPoint(EntityId d, DPoint destination) {
            this.entityId = d;
            this.location = destination;
        }

        public static SetGatherPoint finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new SetGatherPoint(
                    reader.read("entity", EntityId.Serializer, spec),
                    reader.read("location", DPoint.Serializer, spec)
            );
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("entity", entityId, EntityId.Serializer, options);
            writer.write("location", location, DPoint.Serializer, options);
        }

        public MessageType getMessageType() {
            return MessageType.SET_GATHER_POINT;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class TimeChange extends Message {

        public final double currentTime;

        public TimeChange(double currentTime) {
            this.currentTime = currentTime;
        }

        public static TimeChange finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new TimeChange(reader.readDouble("current-time"));
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("current-time", currentTime);
        }

        public MessageType getMessageType() {
            return MessageType.TIME_CHANGE;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class Garrison extends Message {
        public final EntityId entity;
        public final EntityId within;

        public Garrison(EntityId entity, EntityId within) {
            this.entity = entity;
            this.within = within;
        }

        public static Garrison finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new Garrison(
                reader.read("entity", EntityId.Serializer, spec),
                reader.read("within", EntityId.Serializer, spec)
            );
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("entity", entity, EntityId.Serializer, options);
            writer.write("within", within, EntityId.Serializer, options);
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.GARRISON;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class UnGarrison extends Message {
        public final EntityId entity;

        public UnGarrison(EntityId entity) {
            this.entity = entity;
        }

        public static UnGarrison finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new UnGarrison(reader.read("entity", EntityId.Serializer, spec));
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("entity", entity, EntityId.Serializer, options);
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.UNGARRISON;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class Ride extends Message {
        public final EntityId rider;
        public final EntityId ridden;

        public Ride(EntityId rider, EntityId ridden) {
            this.rider = rider;
            this.ridden = ridden;
        }

        public static Ride finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new Ride(
                reader.read("rider", EntityId.Serializer, spec),
                reader.read("ridden", EntityId.Serializer, spec)
            );
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("rider", rider, EntityId.Serializer, options);
            writer.write("ridden", ridden, EntityId.Serializer, options);
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.RIDE;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class StopRiding extends Message {
        public final EntityId rider;

        public StopRiding(EntityId currentlySelected) {
            this.rider = currentlySelected;
        }

        public static StopRiding finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new StopRiding(reader.read("entity", EntityId.Serializer, spec));
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("entity", rider, EntityId.Serializer, options);
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.STOP_RIDING;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class Die extends Message {
        public final EntityId entityId;

        public Die(EntityId currentlySelected) {
            this.entityId = currentlySelected;
        }

        public static Die finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new Die(reader.read("entity", EntityId.Serializer, spec));
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("entity", entityId, EntityId.Serializer, options);
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.DIE;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class DropAll extends Message {
        public final EntityId entityId;

        public DropAll(EntityId currentlySelected) {
            this.entityId = currentlySelected;
        }

        public static DropAll finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new DropAll(reader.read("entity", EntityId.Serializer, spec));
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("entity", entityId, EntityId.Serializer, options);
        }

        @Override
        public MessageType getMessageType() {
            return MessageType.DROP_ALL;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public enum MessageType {
        ERROR,
        INFORM,
        QUIT_CONNECTION,
        LIST_LOBBIES,
        LOBBY_LIST,
        LAUNCHED,
        LAUNCH,
        JOINED,
        JOIN,
        GAME_OVER,
        LEAVE,
        LEFT,
        UPDATE_ENTIRE_GAME,
        REQUEST_ACTION,
        UNIT_REMOVED,
        UNIT_UPDATED,
        OCCUPANCY_UPDATED,
        PLACE_BUILDING,
        SET_GATHER_POINT,
        TIME_CHANGE,
        AI_EVENT,
        CHANGE_OCCUPANCY,
        PROJECTILE_LAUNCHED,
        PROJECTILE_LANDED,
        UNGARRISON,
        GARRISON,
        RIDE,
        STOP_RIDING,
        DIE,
        DROP_ALL,
        SET_EVOLUTION_SELECTION,
        SET_DESIRED_CAPACITY,
        SPECTATING,
        SPECTATE,
    }
}
