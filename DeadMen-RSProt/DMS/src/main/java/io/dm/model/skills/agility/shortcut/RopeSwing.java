package io.dm.model.skills.agility.shortcut;

import io.dm.model.entity.player.Player;
import io.dm.model.entity.shared.LockType;
import io.dm.model.map.Direction;
import io.dm.model.map.Position;
import io.dm.model.map.object.GameObject;
import io.dm.model.stat.StatType;

/**
 * @author ReverendDread on 3/16/2020
 * https://www.rune-server.ee/members/reverenddread/
 * @project Kronos
 */
public class RopeSwing {

    public static void shortcut(Player player, GameObject object, int level, Position position, Position destination) {
        if (!player.getStats().check(StatType.Agility, level, "swing-on"))
            return;
        player.startEvent(e -> {
            e.path(player, position);
            player.lock(LockType.FULL_DELAY_DAMAGE);
            player.animate(751);
            e.delay(1);
            if (player.getAbsX() > object.x)
                player.getMovement().force(destination.getX() - player.getAbsX(), 0, 0, 0, 0, 50, Direction.WEST);
            else
                player.getMovement().force(destination.getX() - player.getAbsX(), 0, 0, 0, 0, 50, Direction.EAST);
            e.delay(2);
            player.getStats().addXp(StatType.Agility, 0.5, true);
            player.unlock();
        });
    }

}
