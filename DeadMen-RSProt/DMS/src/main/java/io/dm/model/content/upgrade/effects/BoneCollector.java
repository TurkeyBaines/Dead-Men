package io.dm.model.content.upgrade.effects;

import io.dm.api.utils.Random;
import io.dm.model.content.upgrade.ItemUpgrade;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.skills.prayer.Bone;

/**
 * @author ReverendDread on 6/18/2020
 * https://www.rune-server.ee/members/reverenddread/
 * @project Kronos
 */
public class BoneCollector extends ItemUpgrade {

    @Override
    public boolean modifyDroppedItem(Player player, Item item) {
        Bone bone = Bone.get(item.getId());
        if (bone != null && Random.rollDie(6)) {
            player.getBank().add(item.getId(), item.getAmount(), item.copyOfAttributes());
            return false;
        }
        return true;
    }
}
