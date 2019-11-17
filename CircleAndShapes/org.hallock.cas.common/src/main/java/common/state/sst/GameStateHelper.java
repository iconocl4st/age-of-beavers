package common.state.sst;

import common.state.spec.EntitySpec;
import common.state.EntityId;
import common.state.Player;
import common.state.sst.manager.ManagerImpl;
import common.util.json.EmptyJsonable;

public class GameStateHelper {
    public static boolean playerCanRide(GameState gs, Player player, EntityId rider, EntityId ridden) {
        if (rider.equals(ridden)) return false;
        EntitySpec riderType = gs.typeManager.get(rider);
        EntitySpec riddenType = gs.typeManager.get(ridden);
        Player riderOwner = gs.playerManager.get(rider);
        Player riddenOwner = gs.playerManager.get(ridden);
        if (riderType == null || riddenType == null || riderOwner == null || riddenOwner == null)
            return false;
        if (!riderType.containsClass("rider") || !riddenType.containsClass("ridable"))
            return false;
//        if (gs.ridingManager.getRiding(rider) != null) return false;
        if (gs.hiddenManager.get(rider) || gs.hiddenManager.get(ridden))
            return false;
        if (player.equals(Player.GOD)) return true;
        if (riddenOwner.equals(Player.GAIA)) {
            return player.equals(riderOwner);
        } else if (riderOwner.equals(Player.GAIA)) {
            return player.equals(riddenOwner);
        } else {
            return player.equals(riddenOwner) && player.equals(riderOwner);
        }
    }

    public static boolean playerCanGarrison(GameState gs, Player player, EntityId goGarrison, EntityId destination) {
        EntitySpec eEntity = gs.typeManager.get(goGarrison);
        if (eEntity == null || !eEntity.containsClass("can-garrison-in-others"))
            return false;
        EntitySpec rEntity = gs.typeManager.get(destination);
        if (rEntity == null || !rEntity.containsClass("can-garrison-others"))
            return false;
        Player eOwner = gs.playerManager.get(goGarrison);
        if (eOwner == null) return false;
        Player dOwner = gs.playerManager.get(destination);
        if (dOwner == null) return false;
        if (player.equals(Player.GOD)) return true;
        if (!player.equals(eOwner)) return false;
        return player.equals(dOwner) || dOwner.equals(Player.GAIA);
    }

    public static Object[] getSynchronizationObjects(ManagerImpl<EmptyJsonable> entityIdManager, EntityId e1, EntityId e2) {
        Object o1 = entityIdManager.get(e1);
        if (o1 == null) return null;
        Object o2 = entityIdManager.get(e2);
        if (o2 == null) return null;
        if (e1.id < e2.id)
            return new Object[]{e1, e2};
        else
            return new Object[]{e2, e1};
    }
}
