package client.ai;

import client.app.UiClientContext;
import common.action.Action;
import common.algo.AStar;
import common.msg.Message;
import common.state.EntityReader;
import common.state.spec.CreationSpec;
import common.state.spec.ResourceType;
import common.state.spec.attack.Weapon;
import common.util.DPoint;
import common.util.GridLocationQuerier;

import java.io.IOException;

public class NetworkActionRequester implements ActionRequester {

    private final UiClientContext context;

    public NetworkActionRequester(UiClientContext context) {
        this.context = context;
    }

    @Override
    public void setUnitActionToMount(EntityReader entity, EntityReader ridden) {
        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.Ride(entity.entityId, ridden.entityId));
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
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


    @Override
    public void setUnitActionToAttack(EntityReader entityId, EntityReader currentPrey, Weapon weapon) {
        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.RequestAction(entityId.entityId, new Action.Attack(currentPrey.entityId, weapon.weaponType.name)));
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    @Override
    public void setUnitActionToEnter(EntityReader entityId, EntityReader garrisonLocation) {
        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.Garrison(entityId.entityId, garrisonLocation.entityId));
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
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

    @Override
    public void setUnitActionToCollect(EntityReader entityId, EntityReader collected, ResourceType resourceType) {
        setUnitActionToCollect(entityId, collected, resourceType, Integer.MAX_VALUE);
    }

    @Override
    public void setUnitActionToCollect(EntityReader entityId, EntityReader collected, ResourceType resource, int amountToRetreive) {
        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.RequestAction(entityId.entityId, new Action.Collect(collected.entityId, resource, amountToRetreive)));
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void setUnitActionToBuild(EntityReader entityId, EntityReader constructionId) {
        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.RequestAction(entityId.entityId, new Action.Build(constructionId.entityId)));
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void setUnitActionToDeposit(EntityReader entityId, EntityReader entity) {
        setUnitActionToDeposit(entityId, entity, null, Integer.MAX_VALUE);
    }

    @Override
    public void setUnitActionToDeposit(EntityReader entityId, EntityReader constructionId, ResourceType key, Integer aDouble) {
        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.RequestAction(entityId.entityId, new Action.Deposit(constructionId.entityId, key, aDouble)));
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
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


    @Override
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

    @Override
    public boolean setUnitActionToMove(EntityReader unit, DPoint destination) {
        DPoint location = unit.getLocation();
        if (location == null || unit.getMovementSpeed() <= 0.0) return false;

        AStar.PathSearch path = GridLocationQuerier.findPath(
                context.clientGameState.gameState,
                location,
                destination,
                context.clientGameState.currentPlayer
        );
        if (path == null) return false;
        setUnitActionToMove(unit, path.path);
        return true;
    }

    @Override
    public Ai.AiAttemptResult setUnitActionToMove(EntityReader unit, EntityReader destination) {
        AStar.PathSearch path = GridLocationQuerier.findPath(
                context.clientGameState.gameState,
                context.clientGameState.gameState.locationManager.getLocation(unit.entityId),
                destination.entityId,
                context.clientGameState.currentPlayer
        );
        if (path == null) return Ai.AiAttemptResult.Unsuccessful;
        setUnitActionToMove(unit, path.path);
        return Ai.AiAttemptResult.Successful;
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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
