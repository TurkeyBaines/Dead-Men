package io.dm.model.item.actions.impl.skillcapes;

import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.item.actions.ItemAction;
import io.dm.model.map.Bounds;
import io.dm.model.skills.magic.spells.modern.ModernTeleport;
import io.dm.model.stat.StatType;

/*
 * @project Kronos
 * @author Patrity - https://github.com/Patrity
 * Created on - 7/23/2020
 */

public class StrengthSkillCape {

    private static final int CAPE = StatType.Strength.regularCapeId;
    private static final int TRIMMED_CAPE = StatType.Strength.trimmedCapeId;


    static {
        ItemAction.registerInventory(CAPE, "Teleport", StrengthSkillCape::strengthTeleport);
        ItemAction.registerEquipment(CAPE, "Warriors' Guild", StrengthSkillCape::strengthTeleport);

        ItemAction.registerInventory(TRIMMED_CAPE, "Teleport", StrengthSkillCape::strengthTeleport);
        ItemAction.registerEquipment(TRIMMED_CAPE, "Warriors' Guild", StrengthSkillCape::strengthTeleport);
    }

    private static void strengthTeleport(Player player, Item item) {
        ModernTeleport.teleport(player, new Bounds(2850, 3547, 2852, 3549, 0));
    }
}
