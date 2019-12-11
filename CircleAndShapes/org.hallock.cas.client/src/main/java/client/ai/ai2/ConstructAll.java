package client.ai.ai2;

import common.AiAttemptResult;
import common.state.EntityReader;
import common.state.spec.ResourceType;
import common.util.MapUtils;

import java.sql.ResultSet;
import java.util.Map;

public class ConstructAll extends DefaultAiTask {

    private EntityReader constructionZone;

    public ConstructAll(EntityReader constructor)  {
        this(constructor, null);
    }

    public ConstructAll(EntityReader constructor, EntityReader constructionZone) {
        super(constructor);
        this.constructionZone = constructionZone;
    }

    @Override
    public String toString() {
        return "Building";
    }

    @Override
    protected AiAttemptResult requestActions(AiContext aiContext) {
        if (constructionZone != null && constructionZone.noLongerExists())
            constructionZone = null;
        if (constructionZone == null)
            constructionZone = aiContext.locator.locateNearestConstructionZone(entity);
        if (constructionZone == null)
            return AiAttemptResult.Completed;
        return construct(aiContext.controlling(entity), constructionZone);
    }

    public static AiAttemptResult construct(AiContext aiContext, EntityReader constructionZone) {
        Map<ResourceType, Integer> missingResources = constructionZone.getMissingConstructionResources();
        if (MapUtils.isEmpty(missingResources))
            return build(aiContext, constructionZone);
        for (Map.Entry<ResourceType, Integer> entry : missingResources.entrySet()) {
            EntityReader storage = aiContext.locator.locateNearestPickupSite(aiContext.controlling, entry.getKey(), aiContext.controlling);
            if (storage == null)
                continue;
            return aiContext.stack.push(aiContext, new OneTripTransportFromTo(aiContext.controlling, storage, constructionZone, missingResources));
        }
        return AiAttemptResult.Unsuccessful;
    }

    public static AiAttemptResult build(AiContext aiContext, EntityReader constructionZone) {
        AiAttemptResult result = AiUtils.moveToProximity(aiContext, constructionZone);
        if (result.didSomething()) return result;

        aiContext.requester.setUnitActionToBuild(aiContext.controlling, constructionZone);
        return AiAttemptResult.RequestedAction;
    }
}
