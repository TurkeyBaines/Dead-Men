package io.dm.model.activities.tasks;

import io.dm.model.activities.wilderness.Hotspot;
import io.dm.model.activities.wilderness.Wilderness;
import io.dm.model.combat.Killer;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.player.Player;
import io.dm.model.stat.StatType;

import java.util.function.BiPredicate;

public enum PlayerKillTaskListener implements TaskListener  {
    EDGEVILLE((p, k) -> p.getPosition().inBounds(Killer.EDGEVILLE_FARM_SKIP_BOUNDS)),
    MAIN((p, k) -> p.getCombat().getLevel() == 126),
    PURE((p, k) -> p.getStats().get(StatType.Defence).fixedLevel == 1),
    HOTSPOT((p, k) -> p.getPosition().inBounds(Hotspot.ACTIVE.bounds)),
    DEEP_WILD((p, k) -> p.wildernessLevel >= 50),
    REV_CAVE((p, k) -> p.getPosition().inBounds(Wilderness.REVENANT_CAVE)),

    ;

    private BiPredicate<Player, Player> predicate;

    PlayerKillTaskListener(BiPredicate<Player, Player> predicate) {
        this.predicate = predicate;
    }

    @Override
    public int onPlayerKill(Player player, Player killed) {
        return predicate.test(player, killed) ? 1 : 0;
    }

    @Override
    public int onNPCKill(Player player, NPC killed) {
        return 0;
    }
}
