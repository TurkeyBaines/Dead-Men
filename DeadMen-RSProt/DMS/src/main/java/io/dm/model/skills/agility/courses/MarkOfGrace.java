package io.dm.model.skills.agility.courses;

import io.dm.api.utils.Random;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.player.PlayerGroup;
import io.dm.model.item.Item;
import io.dm.model.map.Position;
import io.dm.model.map.ground.GroundItem;

import java.util.List;

public class MarkOfGrace {

    public static void rollMark(Player player, int levelReq, List<Position> spawns) {
        if (spawns == null)
            return;
        double chance = levelReq / 2 / 100.0;
        if (Random.get() <= chance) {
            Position spawn = Random.get(spawns);
            new GroundItem(new Item(11849, (Random.get(1, 4) + markOfGraceDonatorIncrease(player)))).owner(player).position(spawn).spawn(2);
        }
    }

    private static int markOfGraceDonatorIncrease(Player player) {
        if (player.isGroup(PlayerGroup.ZENYTE)) {
            return 7;
        } else if (player.isGroup(PlayerGroup.ONYX)) {
            return 6;
        } else if (player.isGroup(PlayerGroup.DRAGONSTONE)) {
            return 5;
        } else if (player.isGroup(PlayerGroup.DIAMOND)) {
            return 4;
        } else if (player.isGroup(PlayerGroup.RUBY)) {
            return 3;
        } else if (player.isGroup(PlayerGroup.EMERALD)) {
            return 2;
        } else if (player.isGroup(PlayerGroup.SAPPHIRE)) {
            return 1;
        } else {
            return 0;
        }
    }
}
