package io.dm.model.skills.prayer;

import io.dm.model.activities.duelarena.DuelRule;
import io.dm.model.entity.player.Player;
import io.dm.model.stat.StatType;

public class Redemption {

    public static void check(Player player) {
        if(DuelRule.NO_PRAYER.isToggled(player)) //pointless, won't be able to turn on if no prayer is on lol
            return;
        if (player.getHp() <= player.getMaxHp() * 0.10 && !player.getCombat().isDead() && player.getPrayer().isActive(Prayer.REDEMPTION)) {
            player.getPrayer().drain(99);
            player.getPrayer().deactivateAll();
            player.graphics(436, 0, 0);
            player.incrementHp((int) (player.getStats().get(StatType.Prayer).fixedLevel * 0.25));
        }
    }
}
