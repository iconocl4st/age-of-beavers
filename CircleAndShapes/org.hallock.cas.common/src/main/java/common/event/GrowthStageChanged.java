package common.event;

import common.state.EntityId;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;

import java.io.IOException;

public class GrowthStageChanged extends NetworkAiEvent {
    public final EntityId planter;
    public final EntityId currentPlant;
    public final GrowthStage currentStage;
    public final int x;
    public final int y;

    public GrowthStageChanged(
            EntityId entity,
            EntityId planter,
            EntityId currentPlant,
            GrowthStage currentStage,
            int x,
            int y
    ) {
        super(entity, AiEventType.GrowthChanged);
        this.planter = planter;
        this.currentPlant = currentPlant;
        this.currentStage = currentStage;
        this.x = x;
        this.y = y;
    }

    public static GrowthStageChanged finishParsing(JsonReaderWrapperSpec reader, ReadOptions options, EntityId entityId) throws IOException {
        reader.readBeginDocument();
        EntityId planter = reader.read("planter", EntityId.Serializer, options);
        EntityId currentPlant = reader.read("currentPlant", EntityId.Serializer, options);
        GrowthStage currentStage = reader.b(GrowthStage.values(), reader.readInt32("currentStage"));
        int x = reader.readInt32("x");
        int y = reader.readInt32("y");
        reader.readEndDocument();
        return new GrowthStageChanged(entityId, planter, currentPlant, currentStage, x, y);
    }

    @Override
    void writeInnards(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.write("planter", planter, EntityId.Serializer, options);
        writer.write("currentPlant", currentPlant, EntityId.Serializer, options);
        writer.write("currentStage", currentStage.ordinal());
        writer.write("x", x);
        writer.write("y", y);
    }

    public enum GrowthStage {
        ToBePlanted,
        Growing,
        NeedsTending,
        Ripe,
        Expired,
    }
}
