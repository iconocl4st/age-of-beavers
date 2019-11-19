package client.ai;

import common.AiAttemptResult;
import common.action.Action;
import common.algo.AStar;
import common.msg.Message;
import common.msg.NoExceptionsConnectionWriter;
import common.state.EntityReader;
import common.state.spec.CreationSpec;
import common.state.spec.ResourceType;
import common.state.spec.attack.Weapon;
import common.util.DPoint;
import common.util.GridLocationQuerier;

public class ActionRequester {
    private final NoExceptionsConnectionWriter writer;

    public ActionRequester(NoExceptionsConnectionWriter writer) {
        this.writer = writer;
    }

    public void setUnitActionToMount(EntityReader entity, EntityReader ridden) {
        writer.send(new Message.Ride(entity.entityId, ridden.entityId));
    }

    public void setUnitActionToDismount(EntityReader entityId) {
        writer.send(new Message.StopRiding(entityId.entityId));
    }

    public void setUnitActionToAttack(EntityReader entityId, EntityReader currentPrey, Weapon weapon) {
        writer.send(new Message.RequestAction(entityId.entityId, new Action.Attack(currentPrey.entityId, weapon.weaponType.name)));
    }

    public void setUnitActionToEnter(EntityReader entityId, EntityReader garrisonLocation) {
        writer.send(new Message.Garrison(entityId.entityId, garrisonLocation.entityId));
    }

    public void setUnitActionToExit(EntityReader entityId) {
        writer.send(new Message.UnGarrison(entityId.entityId));
    }

    public void setUnitActionToCollect(EntityReader entityId, EntityReader collected, ResourceType resourceType) {
        setUnitActionToCollect(entityId, collected, resourceType, Integer.MAX_VALUE);
    }

    public void setUnitActionToCollect(EntityReader entityId, EntityReader collected, ResourceType resource, int amountToRetreive) {
        writer.send(new Message.RequestAction(entityId.entityId, new Action.Collect(collected.entityId, resource, amountToRetreive)));
    }

    public void setUnitActionToBuild(EntityReader entityId, EntityReader constructionId) {
        writer.send(new Message.RequestAction(entityId.entityId, new Action.Build(constructionId.entityId)));
    }

    public void setUnitActionToDeposit(EntityReader entityId, EntityReader entity) {
        setUnitActionToDeposit(entityId, entity, null, Integer.MAX_VALUE);
    }

    public void setUnitActionToDeposit(EntityReader entityId, EntityReader constructionId, ResourceType key, Integer aDouble) {
        writer.send(new Message.RequestAction(entityId.entityId, new Action.Deposit(constructionId.entityId, key, aDouble)));
    }

    public void setUnitActionToWait(EntityReader entityId, double v) {
        writer.send(new Message.RequestAction(entityId.entityId, new Action.Wait(v)));
    }


    public void setUnitActionToMove(EntityReader unit, AStar.Path path) {
        writer.send(new Message.RequestAction(unit.entityId, new Action.MoveSeq(path)));
    }

    public AiAttemptResult setUnitActionToMove(EntityReader unit, DPoint destination) {
        DPoint location = unit.getLocation();
        if (location == null || unit.getMovementSpeed() <= 0.0) return AiAttemptResult.Unsuccessful;

        AStar.PathSearch path = GridLocationQuerier.findPath(
                unit.getState(),
                location,
                destination,
                unit.getOwner()
        );
        if (path == null) return AiAttemptResult.Unsuccessful;
        setUnitActionToMove(unit, path.path);
        return AiAttemptResult.Successful;
    }

    public AiAttemptResult setUnitActionToMove(EntityReader unit, EntityReader destination) {
        AStar.PathSearch path = GridLocationQuerier.findPath(
                unit.getState(),
                unit.getLocation(),
                destination.entityId,
                unit.getOwner()
        );
        if (path == null) return AiAttemptResult.Unsuccessful;
        setUnitActionToMove(unit, path.path);
        return AiAttemptResult.Successful;
    }

    public void setUnitActionToSuicide(EntityReader unit) {
        writer.send(new Message.Die(unit.entityId));
    }

    public void setUnitActionToDropAll(EntityReader unit) {
        writer.send(new Message.DropAll(unit.entityId));
    }

    public void setUnitActionToCreate(EntityReader unit, CreationSpec spec) {
        writer.send(new Message.RequestAction(unit.entityId, new Action.Create(spec)));
    }

    public void setUnitActionToIdle(EntityReader entity) {
        writer.send(new Message.RequestAction(entity.entityId, new Action.Idle()));
    }
}
