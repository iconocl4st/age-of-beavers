package client.ai;

import client.app.ClientContext;
import common.action.Action;
import common.algo.AStar;
import common.msg.Message;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.spec.CreationSpec;
import common.state.spec.ResourceType;
import common.state.spec.attack.Weapon;
import common.util.DPoint;
import common.util.GridLocationQuerier;

import java.io.IOException;

public class ActionRequester {

    private final ClientContext context;

    public ActionRequester(ClientContext context) {
        this.context = context;
    }

    public void setUnitActionToMount(EntityReader entity, EntityId ridden) {
        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.Ride(entity.entityId, ridden));
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void setUnitActionToDismount(EntityReader entityId) {
        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.StopRiding(entityId.entityId));
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    public void setUnitActionToAttack(EntityReader entityId, EntityId currentPrey, Weapon weapon) {
        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.RequestAction(entityId.entityId, new Action.Attack(currentPrey, weapon.weaponType.name)));
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    public void setUnitActionToEnter(EntityReader entityId, EntityId garrisonLocation) {
        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.Garrison(entityId.entityId, garrisonLocation));
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void setUnitActionToExit(EntityReader entityId) {
        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.UnGarrison(entityId.entityId));
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void setUnitActionToCollect(EntityReader entityId, EntityId collected) {
        setUnitActionToCollect(entityId, collected, null, Integer.MAX_VALUE);
    }

    public void setUnitActionToCollect(EntityReader entityId, EntityId collected, ResourceType resource, int amountToRetreive) {
        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.RequestAction(entityId.entityId, new Action.Collect(collected, resource, amountToRetreive)));
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void setUnitActionToBuild(EntityReader entityId, EntityId constructionId) {
        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.RequestAction(entityId.entityId, new Action.Build(constructionId)));
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void setUnitActionToDeposit(EntityReader entityId, EntityId entity) {
        setUnitActionToDeposit(entityId, entity, null, Integer.MAX_VALUE);
    }

    public void setUnitActionToDeposit(EntityReader entityId, EntityId constructionId, ResourceType key, Integer aDouble) {
        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.RequestAction(entityId.entityId, new Action.Deposit(constructionId, key, aDouble)));
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public Ai.AiAttemptResult setUnitActionToWait(EntityReader entityId, double v) {
        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.RequestAction(entityId.entityId, new Action.Wait(v)));
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return Ai.AiAttemptResult.Successful;
    }


    public void setUnitActionToMove(EntityReader unit, AStar.Path path) {
        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.RequestAction(unit.entityId, new Action.MoveSeq(path)));
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean setUnitActionToMove(EntityReader unit, DPoint destination) {
        DPoint location = unit.getLocation();
        if (location == null || unit.getMovementSpeed() <= 0.0) return false;

        AStar.PathSearch path = GridLocationQuerier.findPath(
                context.gameState,
                location,
                destination,
                context.currentPlayer
        );
        if (path == null) return false;
        setUnitActionToMove(unit, path.path);
        return true;
    }

    public Ai.AiAttemptResult setUnitActionToMove(EntityReader unit, EntityId destination) {
        AStar.PathSearch path = GridLocationQuerier.findPath(
                context.gameState,
                context.gameState.locationManager.getLocation(unit.entityId),
                destination,
                context.currentPlayer
        );
        if (path == null) return Ai.AiAttemptResult.Unsuccessful;
        setUnitActionToMove(unit, path.path);
        return Ai.AiAttemptResult.Successful;
    }

    public void setUnitActionToSuicide(EntityReader unit) {
        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.Die(unit.entityId));
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void setUnitActionToDropAll(EntityReader unit) {
        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.DropAll(unit.entityId));
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void setUnitActionToCreate(EntityReader unit, CreationSpec spec) {
        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.RequestAction(unit.entityId, new Action.Create(spec)));
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void setUnitActionToIdle(EntityReader entity) {
        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.RequestAction(entity.entityId, new Action.Idle()));
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
