package client.ai.ai2;

import common.AiAttemptResult;
import common.event.AiEvent;
import common.state.EntityReader;
import common.state.spec.EntitySpec;
import common.state.spec.ResourceType;
import common.state.spec.attack.Weapon;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class Hunt extends DefaultAiTask {

    // TODO Extract out a fight ai

    private EntitySpec preyType;
    private EntityReader currentPrey;
    private final Set<EntityReader> currentResources = new HashSet<>();

    private enum HuntState {
        CollectingWeapons,
        CollectingAmmunition,
        Hunting,
        Collecting,
        Delivering
    }

    private HuntState state = HuntState.Delivering;

    public Hunt(EntityReader entity, EntitySpec preyType) {
        this(entity, null, preyType);
    }

    public Hunt(EntityReader entity, EntityReader currentPrey, EntitySpec preyType) {
        super(entity);
        this.preyType = preyType;
        this.currentPrey = currentPrey;
    }

    @Override
    public String toString() {
        return "hunt " + preyType.name;
    }

    @Override
    protected AiAttemptResult initialize(AiContext aiContext) {
        setCurrentPrey(aiContext, currentPrey);
        return requestActions(aiContext);
    }


    protected AiAttemptResult targetWithinRange(AiContext context, EntityReader target) {
        return requestActions(context);
    }

    @Override
    protected AiAttemptResult targetKilled(AiContext aiContext, EntityReader target, Set<EntityReader> readers) {
        setCurrentPrey(aiContext, null);
        currentResources.clear();
        currentResources.addAll(readers);
        state = HuntState.Collecting;
        return requestActions(aiContext);
    }

    @Override
    protected void removeExtraListeners(AiContext aiContext) {
        setCurrentPrey(aiContext, null);
    }

    @Override
    public AiAttemptResult requestActions(AiContext aiContext) {
        aiContext = aiContext.controlling(entity);

        Weapon weapon = entity.getPreferredWeaponWithAmmunition(WEAPON_COMPARATOR);
        if (weapon == null) {
            return  AiAttemptResult.Unsuccessful;
        }

        while (true) {
            switch (state) {
                case Hunting: {
                    if (currentPrey == null)
                        setCurrentPrey(aiContext, aiContext.locator.locateNearestPrey(aiContext, entity, preyType));
                    if (currentPrey == null)
                        return AiAttemptResult.Completed;

                    double range = weapon.weaponType.rangeCanStartAttacking;
                    if (entity.getLocation().distanceTo(currentPrey.getLocation()) > range) {
                        AiAttemptResult result = aiContext.stack.push(aiContext, new Chase.RangeChase(entity, currentPrey, range));
                        return result;
//                        if (result.didSomething()) return result;
                    }

                    aiContext.requester.setUnitActionToAttack(entity, currentPrey, weapon);
                    return AiAttemptResult.RequestedAction;
                }
                case Collecting: {
                    for (EntityReader collectionResource : currentResources) {
                        if (collectionResource.noLongerExists())
                            continue;
                        AiAttemptResult result = OneTripTransport.pickupAllResources(aiContext, collectionResource);
                        switch (result) {
                            case RequestedAction: return result;
                            case Completed: break;
                            case NothingDone: break;
                            default: throw new IllegalStateException();
                        }
                    }
                    state = HuntState.Delivering;
                } // fall through
                case Delivering: { // might should remember the current drop off location...
                    Set<ResourceType> ammunitionResources = weapon.weaponType.getAmunitionResources();
                    AiAttemptResult result = OneTripTransport.deliverOtherResources(aiContext, ammunitionResources);
                    if (result.didSomething())
                        return result;
                    int resourcesLeft = sumResourcesStillInCarcass();
                    if (resourcesLeft == 0)
                        state = HuntState.Hunting;
                    else
                        state = HuntState.Collecting;
                }
            }
        }
    }

    private int sumResourcesStillInCarcass() {
        int sum = 0;
        for (EntityReader reader : currentResources) {
            if (reader.noLongerExists()) continue;
            sum += reader.getCarrying().getWeight();
        }
        return sum;
    }

    private void setCurrentPrey(AiContext aiContext, EntityReader prey) {
        if (currentPrey != null) {
            aiContext.clientGameState.eventManager.stopListeningTo(aiContext.stack, currentPrey.entityId);
        }
        currentPrey = prey;
        if (currentPrey != null) {
            aiContext.clientGameState.eventManager.listenForEventsFrom(aiContext.stack, currentPrey.entityId);
        }
    }


    // seems to be backward...
    private static int getDesirability(Weapon weapon) {
        switch (weapon.weaponType.name) {
            case "fist": return 0;
            case "sword": return 1;
            case "bow": return 2;
            case "rifle": return 3;
            case "laser gun": return 4;
            default:
                return -1;
        }
    }
    private static final Comparator<Weapon> WEAPON_COMPARATOR = Comparator.comparingInt(Hunt::getDesirability);
}
