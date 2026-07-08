package io.dm.model.item.actions.impl.skillcapes;

/*
 * @project Kronos
 * @author Patrity - https://github.com/Patrity
 * Created on - 7/31/2020
 */

import io.dm.model.entity.player.Player;
import io.dm.model.item.containers.Equipment;
import io.dm.model.stat.StatType;

public class HitpointsSkillCape {

    private static final int CAPE = StatType.Hitpoints.regularCapeId;
    private static final int TRIMMED_CAPE = StatType.Hitpoints.trimmedCapeId;
    private static final int MASTER_CAPE = StatType.Hitpoints.masterCapeId;

    public static boolean wearsHitpointsCape(Player player) {
        int cape = player.getEquipment().getId(Equipment.SLOT_CAPE);
        return cape == CAPE || cape == TRIMMED_CAPE || cape == 13280 || cape == MASTER_CAPE;
    }
}
