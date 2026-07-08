package io.dm.model.item.actions.impl.skillcapes;

import io.dm.model.entity.player.Player;
import io.dm.model.item.containers.Equipment;
import io.dm.model.stat.StatType;

/*
 * @project Kronos
 * @author Patrity - https://github.com/Patrity
 * Created on - 7/31/2020
 */
public class ThievingSkillCape {

    private static final int CAPE = StatType.Thieving.regularCapeId;
    private static final int TRIMMED_CAPE = StatType.Thieving.trimmedCapeId;
    private static final int MASTER_CAPE = StatType.Thieving.masterCapeId;

    public static boolean wearsThievingCape(Player player) {
        int cape = player.getEquipment().getId(Equipment.SLOT_CAPE);
        return cape == CAPE || cape == TRIMMED_CAPE || cape == 13280 || cape == MASTER_CAPE;
    }
}
