//package common.state.sst.sub.capacity;
//
//import common.state.spec.GameSpec;
//import common.state.spec.ResourceType;
//import common.state.sst.sub.Load;
//import common.util.json.JsonReaderWrapperSpec;
//import common.util.json.JsonWriterWrapperSpec;
//import common.util.json.ReadOptions;
//import common.util.json.WriteOptions;
//
//import java.io.IOException;
//
//public class InCapableCapacitySpec extends CapacitySpec {
//
//    public InCapableCapacitySpec() {
//        super(CapacitySpecType.Inacapable);
//    }
//
//    @Override
//    public boolean hasRoomFor(Load load, ResourceType resource) {
//        return false;
//    }
//
//    @Override
//    public int amountPossibleToAccept(Load load, ResourceType resource) {
//        return 0;
//    }
//
//    public String toString() {
//        return "0";
//    }
//
//
//    @Override
//    public void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {}
//
//    public static CapacitySpec finishReading(JsonReaderWrapperSpec reader, ReadOptions spec) {
//        return new InCapableCapacitySpec();
//    }
//}