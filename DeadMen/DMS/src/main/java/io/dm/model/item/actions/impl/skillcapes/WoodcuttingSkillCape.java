package io.dm.model.item.actions.impl.skillcapes;

import io.dm.model.entity.player.Player;
import io.dm.model.item.containers.Equipment;
import io.dm.model.stat.StatType;

/*
 * @project Kronos
 * @author Patrity - https://github.com/Patrity
 * Created on - 7/31/2020
 */
public class WoodcuttingSkillCape {

    private static final int CAPE = StatType.Woodcutting.regularCapeId;
    private static final int TRIMMED_CAPE = StatType.Woodcutting.trimmedCapeId;
    private static final int MASTER_CAPE = StatType.Woodcutting.masterCapeId;

    public static boolean wearsWoodcuttingCape(Player player) {
        int cape = player.getEquipment().getId(Equipment.SLOT_CAPE);
        return cape == CAPE || cape == TRIMMED_CAPE || cape == 13280 || cape == MASTER_CAPE;
    }
}


