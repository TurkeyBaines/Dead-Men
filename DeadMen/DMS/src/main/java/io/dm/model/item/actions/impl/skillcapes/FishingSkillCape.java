package io.dm.model.item.actions.impl.skillcapes;

import io.dm.model.entity.player.Player;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.utils.Option;
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
public class FishingSkillCape {
    private static final int CAPE = StatType.Fishing.regularCapeId;
    private static final int TRIMMED_CAPE = StatType.Fishing.trimmedCapeId;

    static {
        ItemAction.registerInventory(CAPE, "Teleport", FishingSkillCape::selectTeleport);
        ItemAction.registerEquipment(CAPE, "Teleport", FishingSkillCape::selectTeleport);


        ItemAction.registerInventory(TRIMMED_CAPE, "Teleport", FishingSkillCape::selectTeleport);
        ItemAction.registerEquipment(TRIMMED_CAPE, "Teleport", FishingSkillCape::selectTeleport);

    }

    private static void selectTeleport(Player player, Item item) {
        player.dialogue(new OptionsDialogue("Choose Location:",
                new Option("Fishing Guild", () -> ModernTeleport.teleport(player, new Bounds(2493,3414,2595,3416,0))),
                new Option("Otto's Grotto", () -> ModernTeleport.teleport(player, new Bounds(2501,3493,2502,3495,0)))
        ));
    }
}
