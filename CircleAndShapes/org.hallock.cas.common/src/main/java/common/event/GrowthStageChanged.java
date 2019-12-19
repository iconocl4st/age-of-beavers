package common.event;

import common.state.EntityReader;
import common.state.sst.sub.GrowthInfo;

public class GrowthStageChanged extends AiEvent {
    public final EntityReader currentPlant;
    public final GrowthInfo growthInfo;

    public GrowthStageChanged(
            EntityReader currentPlant,
            GrowthInfo currentStage
    ) {
        super(currentPlant.entityId, AiEventType.GrowthChanged);
        this.currentPlant = currentPlant;
        this.growthInfo = currentStage;
    }

//    public static GrowthStageChanged finishParsing(JsonReaderWrapperSpec reader, ReadOptions options, EntityId entityId) throws IOException {
//        EntityId planter = reader.read("planter", EntityId.Serializer, options);
//        EntityId currentPlant = reader.read("currentPlant", EntityId.Serializer, options);
//        GrowthStage currentStage = reader.b(GrowthStage.values(), reader.readInt32("currentStage"));
//        return new GrowthStageChanged(entityId, planter, currentPlant, currentStage);
//    }
//
//    @Override
//    void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
//        writer.write("planter", planter, EntityId.Serializer, options);
//        writer.write("currentPlant", currentPlant, EntityId.Serializer, options);
//        writer.write("currentStage", currentStage.ordinal());
//    }
}
