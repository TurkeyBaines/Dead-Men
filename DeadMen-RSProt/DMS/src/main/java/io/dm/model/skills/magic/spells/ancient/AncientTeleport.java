package io.dm.model.skills.magic.spells.ancient;

import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.map.Bounds;
import io.dm.model.skills.magic.Spell;

public class AncientTeleport extends Spell {

    public AncientTeleport(int lvlReq, double xp, Bounds bounds, Item... runeItems) {
        registerClick(lvlReq, xp, true, runeItems, (p, i) -> teleport(p, bounds));
    }

    public AncientTeleport(int lvlReq, double xp, int x, int y, int z, Item... runeItems) {
        registerClick(lvlReq, xp, true, runeItems, (p, i) -> teleport(p, x, y, z));
    }

    public static boolean teleport(Player player, Bounds bounds) {
        return teleport(player, bounds.randomX(), bounds.randomY(), bounds.z);
    }

    public static boolean teleport(Player player, int x, int y, int z) {
        return player.getMovement().startTeleport(e -> {
            player.animate(1979);
            player.graphics(392);
            player.publicSound(195);
            e.delay(4);
            player.getMovement().teleport(x, y, z);
        });
    }

}
