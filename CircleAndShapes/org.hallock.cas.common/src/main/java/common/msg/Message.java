package common.msg;

import common.AiEvent;
import common.action.Action;
import common.app.LobbyInfo;
import common.state.EntityId;
import common.state.Player;
import common.state.spec.EntitySpec;
import common.state.spec.GameSpec;
import common.state.sst.GameState;
import common.state.sst.sub.*;
import common.state.sst.sub.capacity.CapacitySpec;
import common.util.DPoint;
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

        public Launched(GameSpec spec, Player player) {
            this.spec = spec;
            this.player = player;
        }

        public static Launched finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new Launched(
                reader.read("spec", GameSpec.Serializer, spec),
                reader.read("player", Player.Serializer, spec)
            );
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions verbose) throws IOException {
            writer.write("spec", spec, GameSpec.Serializer, verbose);
            writer.write("player", player, Player.Serializer, verbose);
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
        public CapacitySpec capacity;
        public Double buildSpeed;

        public String debug;

        public static UnitUpdated finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            UnitUpdated unitUpdated = new UnitUpdated();
            unitUpdated.unitId = reader.read("unitId", EntityId.Serializer, spec);
            reader.readBeginArray("changes");
            if (!reader.hasMoreInArray()) return unitUpdated;
            reader.readBeginDocument();
            String currentKey = reader.awkwardlyReadName();

            // TODO:
            if (currentKey.equals("location")) { unitUpdated.location = reader.read(DPoint.Serializer, spec); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("action")) { unitUpdated.action = reader.read(Action.Serializer, spec); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("load")) { unitUpdated.load = reader.read(Load.Serializer, spec); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("initialBaseHealth")) { unitUpdated.health = reader.readDouble(); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("owner")) { unitUpdated.owner = reader.read(Player.Serializer, spec); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("rides")) { unitUpdated.rides = reader.read(EntityId.Serializer, spec); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("isWithin")) { unitUpdated.isWithin = reader.read(EntityId.Serializer, spec); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("buildProgress")) { unitUpdated.buildProgress = reader.readDouble(); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("isNowOfType")) { unitUpdated.isNowOfType = reader.read(EntitySpec.Serializer, spec); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("newMovementSpeed")) { unitUpdated.newMovementSpeed = reader.readDouble(); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("isHidden")) { unitUpdated.isHidden = reader.readBoolean(); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("losPlayer")) { unitUpdated.losPlayer = reader.read(Player.Serializer, spec); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("losDistance")) { unitUpdated.losDistance = reader.readDouble(); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("losOldLocation")) { unitUpdated.losOldLocation = reader.read(DPoint.Serializer, spec); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("losNewLocation")) { unitUpdated.losNewLocation = reader.read(DPoint.Serializer, spec); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("creationTime")) { unitUpdated.creationTime = reader.readDouble(); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("constructionZone")) { unitUpdated.constructionZone = reader.read(ConstructionZone.Serializer, spec); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("constructionProgress")) { unitUpdated.constructionProgress = reader.readDouble(); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("occupancy")) { unitUpdated.occupancy = reader.read(GateInfo.Serializer, spec); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("gatherPoint")) { unitUpdated.gatherPoint = reader.read(DPoint.Serializer, spec); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("attackSpeed")) { unitUpdated.attackSpeed = reader.readDouble(); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("rotationSpeed")) { unitUpdated.rotationSpeed = reader.readDouble(); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("orientation")) { unitUpdated.orientation = reader.readDouble(); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("weapons")) { unitUpdated.weapons = reader.read(WeaponSet.Serializer, spec); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("capacity")) { unitUpdated.capacity = reader.read(CapacitySpec.Serializer, spec); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("buildSpeed")) { unitUpdated.buildSpeed = reader.readDouble(); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            if (currentKey.equals("debug-string")) { unitUpdated.debug = reader.readString(); reader.readEndDocument(); if (!reader.hasMoreInArray()) { reader.readEndArray(); return unitUpdated; } reader.readBeginDocument(); currentKey = reader.awkwardlyReadName(); }
            reader.readEndArray();
            return unitUpdated;
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("unitId", unitId, EntityId.Serializer, options);
            writer.writeBeginArray("changes");
            if (location != null) { writer.writeBeginDocument(); writer.write("location", location, DPoint.Serializer, options); writer.writeEndDocument(); }
            if (action != null) { writer.writeBeginDocument(); writer.write("action", action, Action.Serializer, options); writer.writeEndDocument(); }
            if (load != null) { writer.writeBeginDocument(); writer.write("load", load, Load.Serializer, options); writer.writeEndDocument(); }
            if (health != null) { writer.writeBeginDocument(); writer.write("initialBaseHealth", health); writer.writeEndDocument(); }
            if (owner != null) { writer.writeBeginDocument(); writer.write("owner", owner, Player.Serializer, options); writer.writeEndDocument(); }
            if (rides != null) { writer.writeBeginDocument(); writer.write("rides", rides, EntityId.Serializer, options); writer.writeEndDocument(); }
            if (isWithin != null) { writer.writeBeginDocument(); writer.write("isWithin", isWithin, EntityId.Serializer, options); writer.writeEndDocument(); }
            if (buildProgress != null) { writer.writeBeginDocument(); writer.write("buildProgress", buildProgress); writer.writeEndDocument(); }
            if (isNowOfType != null) { writer.writeBeginDocument(); writer.write("isNowOfType", isNowOfType, EntitySpec.Serializer, options); writer.writeEndDocument(); }
            if (newMovementSpeed != null) { writer.writeBeginDocument(); writer.write("newMovementSpeed", newMovementSpeed); writer.writeEndDocument(); }
            if (isHidden != null) { writer.writeBeginDocument(); writer.write("isHidden", isHidden); writer.writeEndDocument(); }
            if (losPlayer != null) { writer.writeBeginDocument(); writer.write("losPlayer", losPlayer, Player.Serializer, options); writer.writeEndDocument(); }
            if (losDistance != null) { writer.writeBeginDocument(); writer.write("losDistance", losDistance); writer.writeEndDocument(); }
            if (losOldLocation != null) { writer.writeBeginDocument(); writer.write("losOldLocation", losOldLocation, DPoint.Serializer, options); writer.writeEndDocument(); }
            if (losNewLocation != null) { writer.writeBeginDocument(); writer.write("losNewLocation", losNewLocation, DPoint.Serializer, options); writer.writeEndDocument(); }
            if (creationTime != null) { writer.writeBeginDocument(); writer.write("creationTime", creationTime); writer.writeEndDocument(); }
            if (constructionZone != null) { writer.writeBeginDocument(); writer.write("constructionZone", constructionZone, ConstructionZone.Serializer, options); writer.writeEndDocument(); }
            if (constructionProgress != null) { writer.writeBeginDocument(); writer.write("constructionProgress", constructionProgress); writer.writeEndDocument(); }
            if (occupancy != null) { writer.writeBeginDocument(); writer.write("occupancy", occupancy, GateInfo.Serializer, options); writer.writeEndDocument(); }
            if (gatherPoint != null) { writer.writeBeginDocument(); writer.write("gatherPoint", gatherPoint, DPoint.Serializer, options); writer.writeEndDocument(); }
            if (attackSpeed != null) { writer.writeBeginDocument(); writer.write("attackSpeed", attackSpeed); writer.writeEndDocument(); }
            if (rotationSpeed != null) { writer.writeBeginDocument(); writer.write("rotationSpeed", rotationSpeed); writer.writeEndDocument(); }
            if (orientation != null) { writer.writeBeginDocument(); writer.write("orientation", orientation); writer.writeEndDocument(); }
            if (weapons != null) { writer.writeBeginDocument(); writer.write("weapons", weapons, WeaponSet.Serializer, options); writer.writeEndDocument(); }
            if (capacity != null) { writer.writeBeginDocument(); writer.write("capacity", capacity, CapacitySpec.Serializer, options); writer.writeEndDocument(); }
            if (buildSpeed != null) { writer.writeBeginDocument(); writer.write("buildSpeed", buildSpeed); writer.writeEndDocument(); }
            if (debug != null) { writer.writeBeginDocument(); writer.write("debug-string", debug); writer.writeEndDocument(); }
            writer.writeEndArray();
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
    }

    /*
    public static final DataSerializer<Message> Serializer = new DataSerializer.JsonableSerializer<Message>() {
        @Override
        public Message parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            MessageType msgType = reader.b(MessageType.values(), reader.readInt32("type"));
            Message msg = null;
            spec.spec =
            switch (msgType) {
                case AI_EVENT: msg = AiEventMessage.finishParsing(reader, spec); break;
                case OCCUPANCY_UPDATED: msg = OccupancyChanged.finishParsing(reader, spec); break;
                case QUIT_CONNECTION: msg = Quit.finishParsing(reader, spec); break;
                case UNIT_REMOVED: msg = UnitRemoved.finishParsing(reader, spec); break;
                case UNIT_UPDATED: msg = UnitUpdated.finishParsing(reader, spec); break;
                case CHANGE_OCCUPANCY: msg = ChangeOccupancy.finishParsing(reader, spec); break;
                case DIE: msg = Die.finishParsing(reader, spec); break;
                case DROP_ALL: msg = DropAll.finishParsing(reader, spec); break;
                case GAME_OVER: msg = GameOver.finishParsing(reader, spec); break;
                case GARRISON: msg = Garrison.finishParsing(reader, spec); break;
                case JOIN: msg = Join.finishParsing(reader, spec); break;
                case JOINED: msg = Joined.finishParsing(reader, spec); break;
                case LAUNCH: msg = Launch.finishParsing(reader, spec); break;
                case LAUNCHED: msg = Launched.finishParsing(reader, spec); break;
                case LEAVE: msg = Leave.finishParsing(reader, spec); break;
                case LEFT: msg = Left.finishParsing(reader, spec); break;
                case LIST_LOBBIES: msg = ListLobbies.finishParsing(reader, spec); break;
                case LOBBY_LIST: msg = LobbyList.finishParsing(reader, spec); break;
                case PLACE_BUILDING: msg = PlaceBuilding.finishParsing(reader, spec); break;
                case PROJECTILE_LANDED: msg = ProjectileLanded.finishParsing(reader, spec); break;
                case PROJECTILE_LAUNCHED: msg = ProjectileLaunched.finishParsing(reader, spec); break;
                case REQUEST_ACTION: msg = RequestAction.finishParsing(reader, spec); break;
                case RIDE: msg = Ride.finishParsing(reader, spec); break;
                case SET_GATHER_POINT: msg = SetGatherPoint.finishParsing(reader, spec); break;
                case STOP_RIDING: msg = StopRiding.finishParsing(reader, spec); break;
                case TIME_CHANGE: msg = TimeChange.finishParsing(reader, spec); break;
                case UNGARRISON: msg = UnGarrison.finishParsing(reader, spec); break;
//                case UPDATE_ENTIRE_GAME: msg = UpdateEntireGameState.finishParsing(reader, spec); break;
                case ERROR: msg = Error.finishParsing(reader, spec); break;
                case INFORM: msg = Inform.finishParsing(reader, spec); break;
                default:
                    throw new RuntimeException("Unrecognized message type");
            }
            reader.readEndDocument();
            return msg;
        }
    };
    */
}
