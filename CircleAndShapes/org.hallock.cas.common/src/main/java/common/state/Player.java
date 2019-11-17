package common.state;

import common.util.json.*;

import java.io.IOException;

public class Player implements Jsonable {

    public static final Player GAIA = new Player(0);
    public static final Player NO_PLAYER = new Player(-1);
    public static final Player GOD = new Player(-2);


    public int number;

    public Player(int number) {
        this.number = number;
    }

    public String toString() {
        if (number < -2) {
            return "Unknown player";
        }
        if (number == -2) {
            return "God";
        }
        if (number == -1) {
            return "No one";
        }
        if (number == 0) {
            return "Gaia";
        }
        return "Player " + number;
    }

    public int hashCode() {
        return Integer.hashCode(number);
    }

    public boolean equals(Object other) {
        if (!(other instanceof Player)) {
            return false;
        }
        return ((Player) other).number == number;
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("number", number);
        writer.writeEndDocument();
    }

    public static final DataSerializer<Player> Serializer = new DataSerializer.JsonableSerializer<Player>() {
        @Override
        public Player parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            Player player = new Player(reader.readInt32("number"));
            reader.readEndDocument();
            return player;
        }
    };
}
