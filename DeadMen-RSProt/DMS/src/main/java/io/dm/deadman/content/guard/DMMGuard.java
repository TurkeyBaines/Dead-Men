package io.dm.deadman.content.guard;

import io.dm.model.entity.npc.NPC;

/**
 * A safe zone guard. Attacks players that are skulled inside a safe zone (see {@link DMMGuardCombat}) and can't
 * be attacked itself. Spawn through {@link DMMGuards}.
 */
public class DMMGuard extends NPC {

    public DMMGuard() {
        this(DMMGuardConfig.NPC_ID);
    }

    public DMMGuard(int id) {
        super(id);
        useCombat(new DMMGuardCombat(), DMMGuardConfig.combatInfo());
        attackNpcListener = (player, npc, message) -> {
            if (message)
                player.sendMessage("The guards are far too strong to attack.");
            return false;
        };
    }

}
