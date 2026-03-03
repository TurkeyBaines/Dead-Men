package io.dm.model.activities.summerevent;

import io.dm.api.utils.Random;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.player.Player;
import io.dm.model.map.Position;
import io.dm.model.map.ground.GroundItem;

/*
 * @project Kronos
 * @author Patrity - https://github.com/Patrity
 * Created on - 7/20/2020
 */
public class SummerTokens {
    public static void npcKill (Player player, NPC npc, Position dropLocation) {
        if (SummerShrine.EVENT_ENABLED) {
            int tokens = 0;
            if (npc.getDef().combatLevel > 274 && !(npc.getDef().name.contains("brutal") || npc.getDef().name.contains("Adamant Dragon") || npc.getDef().name.contains("Rune Dragon"))) {
                tokens = Random.get(20, 30);
            } else if (npc.getDef().combatLevel < 50) {
                if (Random.get(1, 7) == 1) {
                    tokens = 2;
                }
            }

            if (0 == tokens) {

                return;
            }

            new GroundItem(SummerShrine.TOKENS, tokens).owner(player).position(dropLocation).spawn();
        }
    }

    public static void xpDrop(Player player) {
        if (SummerShrine.EVENT_ENABLED) {
            if (Random.get(1, 10) == 5) {
                new GroundItem(SummerShrine.TOKENS, 5).owner(player).position(player.getPosition()).spawn();
            }
        }
    }
}
