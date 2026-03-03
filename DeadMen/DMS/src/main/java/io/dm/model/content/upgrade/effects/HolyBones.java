package io.dm.model.content.upgrade.effects;

import io.dm.api.utils.Random;
import io.dm.model.content.upgrade.ItemUpgrade;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.skills.prayer.Bone;
import io.dm.model.stat.Stat;
import io.dm.model.stat.StatType;

import static io.dm.model.skills.prayer.Bone.*;

/**
 * @author ReverendDread on 6/18/2020
 * https://www.rune-server.ee/members/reverenddread/
 * @project Kronos
 */
public class HolyBones extends ItemUpgrade {

    @Override
    public boolean modifyDroppedItem(Player player, Item item) {
        Bone bone = Bone.get(item.getId());
        if (bone != null && Random.rollDie(6)) {
            player.getStats().addXp(StatType.Prayer, bone.exp, true);
            Stat prayer = player.getStats().get(StatType.Prayer);
            if(bone.id == BIG_BONES.id)
                prayer.restore(2, 0);
            else if(bone.id == BABYDRAGON_BONES.id)
                prayer.restore(3, 0);
            else if(bone.id == DRAGON_BONES.id || bone.id == DAGANNOTH_BONES.id || bone.id == DRAKE_BONES.id)
                prayer.restore(4, 0);
            else if(bone.id == SUPERIOR_DRAGON_BONES.id || bone.id == HYDRA_BONES.id)
                prayer.restore(5, 0);
            else
                prayer.restore(1, 0);
            return false;
        }
        return true;
    }

}
