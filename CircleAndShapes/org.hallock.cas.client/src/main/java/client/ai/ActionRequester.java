package client.ai;

import common.algo.AStar;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.spec.CreationSpec;
import common.state.spec.ResourceType;
import common.state.spec.attack.Weapon;
import common.util.DPoint;

public interface ActionRequester {
    void setUnitActionToMount(EntityReader entity, EntityReader ridden);

    void setUnitActionToDismount(EntityReader entityId);

    void setUnitActionToAttack(EntityReader entityId, EntityReader currentPrey, Weapon weapon);

    void setUnitActionToEnter(EntityReader entityId, EntityReader garrisonLocation);

    void setUnitActionToExit(EntityReader entityId);

    void setUnitActionToCollect(EntityReader entityId, EntityReader collected, ResourceType resourceType);

    void setUnitActionToCollect(EntityReader entityId, EntityReader collected, ResourceType resource, int amountToRetreive);

    void setUnitActionToBuild(EntityReader entityId, EntityReader constructionId);

    void setUnitActionToDeposit(EntityReader entityId, EntityReader entity);

    void setUnitActionToDeposit(EntityReader entityId, EntityReader constructionId, ResourceType key, Integer aDouble);

    Ai.AiAttemptResult setUnitActionToWait(EntityReader entityId, double v);

    void setUnitActionToMove(EntityReader unit, AStar.Path path);

    boolean setUnitActionToMove(EntityReader unit, DPoint destination);

    Ai.AiAttemptResult setUnitActionToMove(EntityReader unit, EntityReader destination);

    void setUnitActionToSuicide(EntityReader unit);

    void setUnitActionToDropAll(EntityReader unit);

    void setUnitActionToCreate(EntityReader unit, CreationSpec spec);

    void setUnitActionToIdle(EntityReader entity);
}
