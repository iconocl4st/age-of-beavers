package common.state.sst.sub.capacity;

import common.util.json.*;

import java.io.IOException;

public class Prioritization implements Jsonable {
    public int desiredAmount = 0;
    public int desiredMaximum = Integer.MAX_VALUE;
    public int maximumAmount = Integer.MAX_VALUE;
    public int priority = 0;

    private static String f(Integer i) {
        if (i == Integer.MAX_VALUE) return "inf";
        return String.valueOf(i);
    }
    public String toString() {
        if (maximumAmount == 0) return "None ";
        return "(" + priority + ") [" + desiredAmount + "," + f(desiredMaximum) + "," + f(maximumAmount) + "]";
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("desired-minimum", desiredAmount);
        writer.write("desired-maximum", desiredMaximum);
        writer.write("absolute-maximum", maximumAmount);
        writer.write("priority", priority);
        writer.writeEndDocument();
    }

    public static final DataSerializer<Prioritization> Serializer = new DataSerializer.JsonableSerializer<Prioritization>() {
        @Override
        public Prioritization parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            Prioritization prioritization = new Prioritization();
            reader.readBeginDocument();
            prioritization.desiredAmount = reader.readInt32("desired-minimum");
            prioritization.desiredMaximum = reader.readInt32("desired-maximum");
            prioritization.maximumAmount = reader.readInt32("absolute-maximum");
            prioritization.priority = reader.readInt32("priority");
            reader.readEndDocument();
            return prioritization;
        }
    };

    public static Prioritization MISSING = new Prioritization();

    public static Prioritization createNotAccepted() {
        Prioritization ret = new Prioritization();
        ret.desiredAmount = ret.desiredMaximum = ret.maximumAmount = 0;
        return ret;
    }
}
