package io.dm.model.skills.agility.shortcut;

import io.dm.model.entity.player.Player;
import io.dm.model.entity.shared.LockType;
import io.dm.model.map.Direction;
import io.dm.model.map.object.GameObject;
import io.dm.model.stat.StatType;

public class Stile {

    public static void shortcut(Player p, GameObject stile, int levelReq) {
        if (!p.getStats().check(StatType.Agility, levelReq, "attempt this"))
            return;
        p.startEvent(e -> {
            p.lock(LockType.FULL_DELAY_DAMAGE);
            p.animate(839);
            if(p.getAbsX() > stile.x)
                p.getMovement().force(-2, 0, 0, 0, 0, 60, Direction.WEST);
            else
                p.getMovement().force(2, 0, 0, 0, 0, 60, Direction.EAST);
            e.delay(2);
            p.getStats().addXp(StatType.Agility, 0.5, true);
            p.unlock();
        });
    }
    public static void shortcutN(Player p, GameObject stile, int levelReq) {
        if (!p.getStats().check(StatType.Agility, levelReq, "attempt this"))
            return;
        p.startEvent(e -> {
            p.lock(LockType.FULL_DELAY_DAMAGE);
            p.animate(839);
            if(p.getAbsY() > stile.y)
                p.getMovement().force(0, -2, 0, 0, 0, 60, Direction.SOUTH);
            else
                p.getMovement().force(0, 2, 0, 0, 0, 60, Direction.NORTH);
            e.delay(2);
            p.getStats().addXp(StatType.Agility, 0.5, true);
            p.unlock();
        });
    }

}
