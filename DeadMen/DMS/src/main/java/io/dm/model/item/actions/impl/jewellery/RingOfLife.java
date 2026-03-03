package io.dm.model.item.actions.impl.jewellery;

import io.dm.model.World;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.item.containers.Equipment;
import io.dm.model.skills.magic.spells.modern.ModernTeleport;

public class RingOfLife {

    public static void check(Player player) {
        if(player.getDuel().stage >= 4)
            return;
        if (player.getHp() <= player.getMaxHp() * 0.10 && !player.getCombat().isDead()) {
            Item ring = player.getEquipment().get(Equipment.SLOT_RING);
            if (ring == null || ring.getId() != 2570)
                return;
            if(ModernTeleport.teleport(player, World.HOME)) {
                ring.remove();
                player.sendFilteredMessage("Your ring of life crumbles to dust.");
            }
        }
    }
}
