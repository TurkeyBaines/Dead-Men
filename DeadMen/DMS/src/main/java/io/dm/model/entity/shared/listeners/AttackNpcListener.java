package io.dm.model.entity.shared.listeners;

import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.player.Player;

public interface AttackNpcListener {

    boolean allow(Player player, NPC npc, boolean message);

}
