package app;

import app.assign.AiCheck;
import app.assign.AiCheckContext;
import app.assign.Assignments;
import client.ai.ActionRequester;
import client.state.ClientGameState;
import common.DebugGraphics;
import common.state.EntityReader;
import common.state.sst.GameState;

import java.util.List;

class PlayerAiImplementation {
    private final PlayerAiContext context;


    private final Assignments assignments;
    private final DropoffManager dropoffManager;
    private final TickProcessingState tickState;
    private final AiCheckContext checkContext;
    private final AiCheck[] aiChecks = new AiCheck[] {
            AiCheck.TRANSPORT_CHECK,
            AiCheck.CARPENTER_SHOP_CHECK,
            AiCheck.BUILD_CORRALS,
            AiCheck.BUILD_MORE_BROTHELS,
            AiCheck.NO_IDLE_HUMANS,
            AiCheck.BUILD_MORE_STORAGE,
            AiCheck.CHECK_NUM_CONSTRUCTION_WORKERS,
            AiCheck.CHECK_NUM_GARRISONERS,
            AiCheck.CHECK_PRODUCING,
            AiCheck.MINIMUM_NUMBER_OF_ALLOCATIONS,
            AiCheck.CHECK_MISSING_RESOURCES
    };

    PlayerAiImplementation(PlayerAiContext playerAiContext) {
        this.context = playerAiContext;
        assignments = new Assignments();
        dropoffManager = new DropoffManager(playerAiContext);
        tickState = new TickProcessingState(playerAiContext);
        checkContext = new AiCheckContext();

        checkContext.assignments = assignments;
        checkContext.tickState = tickState;
        checkContext.utils = playerAiContext.utils;
        checkContext.goals = new Goals();
        checkContext.dropoffManager = dropoffManager;
        checkContext.clientGameState = clientGameState();
    }


    private GameState state() {
        return clientGameState().gameState;
    }
    private ClientGameState clientGameState() {
        return context.clientGameState;
    }

    // need to addIdle the initial carts...
    synchronized DebugSnapshot updateActions(ActionRequester actionRequester) {
        // get on a horse
        // build a corral
        // build a stable
        // build another brothel
        // build weapons
        // fight
        // build more storage
        // detect when resources run out
        // when the drop off does not need to move, the people should gather resources...

        // why does the brothel have only 100 maximum resources?
        // fresh spawns take a while to gather?

        assignments.verify();
        tickState.reset();

        for (EntityReader entityReader : clientGameState().entityTracker.getTracked()) {
            Object sync = entityReader.getSync();
            if (sync == null)
                continue;
            synchronized (sync) {
                if (entityReader.noLongerExists()) {
                    continue;
                }
                tickState.update(entityReader, checkContext);
            }
        }

        for (AiCheck check : aiChecks) {
            check.check(checkContext);
        }

        dropoffManager.assignDropoffs(checkContext);



        // get the number of assignments with priority less than the reassignment priority (add a priority to the collectResourceAssignments)
        // compare the missing resources to the current people on resource
        // make the necessary changes...
        // add a minimum allocations checker...







//        Map<ResourceType, Integer> requiredShifts = MapUtils.positivePart(MapUtils.subtract(persistentState.getMinimumOnResources(tickState), tickState.peopleOnResource));
//        Map<ResourceType, Integer> shiftableResourceGatherers = MapUtils.positivePart(MapUtils.subtract(tickState.peopleOnResource, persistentState.getMinimumOnResources(tickState)));
//
//        Map<ResourceType, Integer> desiredAllocations = MapUtils.add(
//                new HashMap<>() /* requiredShifts */,
//                MapUtils.getDesired(numShiftableHumans, tickState.getMissingResources())
//        );
//
//        tickState.determineShiftableVillagers(desiredAllocations /* shiftableResourceGatherers */);
//
//        for (Map.Entry<ResourceType, Integer> missingEntry : tickState.determineMissingAllocations(desiredAllocations)) {
//            if (missingEntry.getValue() <= 1)
//                continue;
//            for (int i = 0; i < missingEntry.getValue(); i++) {
//                persistentState.addTo(pull(actionRequester, tickState.peoplePuller.next()), missingEntry.getKey(), this);
//            }
//        }

//        for (Map.Entry<ResourceType, Integer> missingEntry : requiredShifts.entrySet()) {
//            if (missingEntry.getValue() <= 1)
//                continue;
//            for (int i = 0; i < missingEntry.getValue() && tickState.peoplePuller.hasNext(); i++) {
//                persistentState.addTo(pull(actionRequester, tickState.peoplePuller.next()), missingEntry.getKey(), this);
//            }
//        }




        DebugSnapshot snapshot = new DebugSnapshot();
        snapshot.addEntityTracker(clientGameState().entityTracker);
        snapshot.addAssignments(assignments);
        snapshot.addGoals(checkContext.goals);
        snapshot.addTickProcessingState(tickState);
        snapshot.numShiftable = 0;
        snapshot.desiredAllocations.clear();
        snapshot.desiredAllocations.putAll(tickState.desiredResources);
        snapshot.collectedResources.clear();
        snapshot.collectedResources.putAll(tickState.collectedResources);
        return snapshot;
    }

    void setDebugGraphics() {
        List<DebugGraphics> debugGraphics = dropoffManager.getDebugGraphics();
        if (debugGraphics == null || debugGraphics.isEmpty()) return;
        synchronized (DebugGraphics.byPlayer) {
            DebugGraphics.byPlayer.put(context.clientGameState.currentPlayer, debugGraphics);
        }
    }
}
