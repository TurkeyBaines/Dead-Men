package io.dm.model.activities.tasks;

import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.player.Player;

public interface TaskListener {

    int onPlayerKill(Player player, Player killed);

    int onNPCKill(Player player, NPC killed);
}
