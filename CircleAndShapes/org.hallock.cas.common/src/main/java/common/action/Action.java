package common.action;

import common.state.spec.CreationSpec;
import common.state.spec.ResourceType;
import common.state.EntityId;
import common.state.Player;
import common.algo.AStar;
import common.util.DPoint;
import common.util.json.*;

import java.io.IOException;

public abstract class Action implements Jsonable {
    public final ActionType type;

    public Player requestingPlayer;

    protected Action(ActionType type) {
        this.type = type;
    }

    public abstract double getProgressIndicator();

    // TODO: ignore several of the fields when coming from the client

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("type", type.ordinal());
//        writer.write("requesting-player", requestingPlayer, Player.Serializer, options);
        writeInnards(writer, options);
        writer.writeEndDocument();
    }

    protected abstract void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException;

    public enum ActionType {
        Attack,
        Collect,
        Create,
        Deposit,
        Idle,
        Move,
        Build,
        Wait,
        Chase,
        ;

        public static final DataSerializer<ActionType> Serializer = new DataSerializer<ActionType>() {
            @Override
            public void write(ActionType value, JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
                writer.write(value.ordinal());
            }

            @Override
            public ActionType parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
                return reader.b(values(), reader.readInt32());
            }
        };
    }

    public String toString() {
        return type.name();
    }

    public static class Attack extends Action {
        public final EntityId target;
        public final String weaponType;
        public boolean isOnCooldown;
        public double progress;


        public Attack(EntityId target, String weaponType) {
            super(ActionType.Attack);
            this.weaponType = weaponType;
            this.target = target;
        }

        public static Action finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            EntityId target = reader.read("target", EntityId.Serializer, spec);
            String weaponType = reader.readString("weapon-type");
            Attack ret = new  Attack(target, weaponType);
            ret.isOnCooldown = reader.readBoolean("is-on-cooldown");
            ret.progress = reader.readDouble("progress");
            return ret;
        }

        public String toString() {
            return "attacking " + target;
        }

        public double getProgressIndicator() {
            return progress;
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("target", target, EntityId.Serializer, options);
            writer.write("weapon-type", weaponType);
            writer.write("is-on-cooldown", isOnCooldown);
            writer.write("progress", progress);
        }
    }


    public static class Build extends Action {
        public final EntityId constructionId;

        public Build(EntityId constructionUnit) {
            super(ActionType.Build);
            this.constructionId = constructionUnit;
        }

        public static Action finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new Build(reader.read("construction-id", EntityId.Serializer, spec));
        }

        @Override
        public double getProgressIndicator() {
            return -1.0;
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("construction-id", constructionId, EntityId.Serializer, options);
        }
    }


    public static class Collect extends Action {
        public final ResourceType resource;
        public final EntityId resourceCarrier;
        public final int maximumAmountToCollect;
        public double progress;

//        public Collect(EntityId resource) {
//            this(resource, Integer.MAX_VALUE);
//        }

//        public Collect(EntityId resource, int maxAmount) {
//            this(resource, maxAmount, null);
//        }

        public Collect(EntityId carrier, ResourceType resourceToCollect, int maxAmount) {
            super(ActionType.Collect);
            this.resourceCarrier = carrier;
            this.maximumAmountToCollect = maxAmount;
            this.resource = resourceToCollect;
        }

        static Action finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            Collect collect = new Collect(
                    reader.read("resource-entity", EntityId.Serializer, spec),
                    reader.read("resource", ResourceType.Serializer, spec),
                    reader.readInt32("maximum-to-collect")
            );
            collect.progress = reader.readDouble("progress");
            return collect;
        }

        public String toString() {
            return "collecting (" + String.format("0.2f", 100 * progress) + "%)";
        }

        @Override
        public double getProgressIndicator() {
            return progress;
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("resource-entity", resourceCarrier, EntityId.Serializer, options);
            writer.write("resource", resource, ResourceType.Serializer, options);
            writer.write("maximum-to-collect", maximumAmountToCollect);
            writer.write("progress", progress);
        }
    }


    public static class Deposit extends Action {
        public final EntityId location;
        public final ResourceType resource;
        public final int maxAmount;
        public double progress;

        public Deposit(EntityId location, ResourceType resource, int maximumAmountToDeposit) {
            super(ActionType.Deposit);
            this.location = location;
            this.resource = resource;
            this.maxAmount = maximumAmountToDeposit;
        }

        public static Action finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            Deposit collect = new Deposit(
                    reader.read("deposit-entity", EntityId.Serializer, spec),
                    reader.read("resource", ResourceType.Serializer, spec),
                    reader.readInt32("maximum-to-collect")
            );
            collect.progress = reader.readDouble("progress");
            return collect;
        }

        @Override
        public double getProgressIndicator() {
            return progress;
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("deposit-entity", location, EntityId.Serializer, options);
            writer.write("resource", resource, ResourceType.Serializer, options);
            writer.write("maximum-to-collect", maxAmount);
            writer.write("progress", progress);
        }
    }


    public static class Create extends Action {
        public final CreationSpec spec;
        public double timeRemaining;
        public int numberOfContributingUnits;

        public Create(CreationSpec spec) {
            super(ActionType.Create);
            this.spec = spec;
            this.timeRemaining = spec.createdType.creationTime;
        }

        public static Action finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            Create create = new Create(reader.read("creation-spec", CreationSpec.Serializer, spec));
            create.timeRemaining = reader.readDouble("time-remaining");
            create.numberOfContributingUnits = reader.readInt32("number-of-contributors");
            return create;
        }

        public String toString() {
            return "Creating a " + spec.createdType.name + ": (" + String.format("%.2f", 100 * (spec.createdType.creationTime - timeRemaining) / spec.createdType.creationTime) + "%) (from " + numberOfContributingUnits + ")";
        }

        @Override
        public double getProgressIndicator() {
            return (spec.createdType.creationTime - timeRemaining) / spec.createdType.creationTime;
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("creation-spec", spec, CreationSpec.Serializer, options);
            writer.write("time-remaining", timeRemaining);
            writer.write("number-of-contributors", numberOfContributingUnits);
        }
    }


    public static class Idle extends Action {
        public Idle() {
            super(ActionType.Idle);
        }

        public static Action finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) {
            return new Idle();
        }

        @Override
        public double getProgressIndicator() {
            return -1.0;
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) {}
    }

    public static class MoveSeq extends Action {
        public int progress;
        public final AStar.Path path;
        public int blockedCount;

        public MoveSeq(AStar.Path path) {
            super(ActionType.Move);
            this.path = path;
        }

        public static Action finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            MoveSeq move = new MoveSeq(reader.read("path", AStar.Path.Serializer, spec));
            move.progress = reader.readInt32("progress");
            return move;
        }

        @Override
        public double getProgressIndicator() {
            return progress / (double) path.points.size();
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("path", path, AStar.Path.Serializer, options);
            writer.write("progress", progress);
        }
    }

    public static class Wait extends Action {
        public double remainingTime;
        public Wait(double time) {
            super(ActionType.Wait);
            this.remainingTime = time;
        }

        public static Action finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new Wait(reader.readDouble("remaining-time"));
        }

        @Override
        public double getProgressIndicator() {
            return -1.0;
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.write("remaining-time", remainingTime);
        }
    }

    public static class Chase extends Action {
        public final EntityId chased;
        public DPoint lastKnownLocation;
        public final double desiredProximity;

        public Chase(EntityId chased, double desiredProximity) {
            super(ActionType.Chase);
            this.chased = chased;
            this.desiredProximity = desiredProximity;

        }

        public static Action finishParsing(JsonReaderWrapperSpec reader, ReadOptions spec) {return null;}

        @Override
        public double getProgressIndicator() {
            return 0;
        }

        @Override
        protected void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) {}
    }


//    public class Research extends Action {
//        @Override
//        public ActionType getType() {
//            return ActionType.Research;
//        }
//    }


    public static final DataSerializer<Action> Serializer = new DataSerializer.JsonableSerializer<Action>() {
        @Override
        public Action parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            ActionType type = reader.b(ActionType.values(), reader.readInt32("type"));
//            Player player = reader.read("requesting-player", spec, Player.Serializer);
            Action action = null;
            switch (type) {
                case Attack: action = Attack.finishParsing(reader, spec); break;
                case Build: action = Build.finishParsing(reader, spec); break;
                case Collect: action = Collect.finishParsing(reader, spec); break;
                case Deposit: action = Deposit.finishParsing(reader, spec); break;
                case Move: action = MoveSeq.finishParsing(reader, spec); break;
                case Create: action = Create.finishParsing(reader, spec); break;
                case Idle: action = Idle.finishParsing(reader, spec); break;
                case Wait: action = Wait.finishParsing(reader, spec); break;
                case Chase: action = Chase.finishParsing(reader, spec); break;
            }
            reader.readEndDocument();
            return action;
        }
    };
}
