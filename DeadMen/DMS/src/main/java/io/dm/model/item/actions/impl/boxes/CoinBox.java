package io.dm.model.item.actions.impl.boxes;

import io.dm.api.utils.NumberUtils;
import io.dm.api.utils.Random;
import io.dm.cache.Color;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.item.actions.ItemAction;

import static io.dm.cache.ItemID.COINS_995;


/**
 * @author Adam Ali ("Kal-El") https://www.rune-server.ee/members/kal+el/
 */
public class CoinBox {

    static {
            ItemAction.registerInventory(9477, "Open",  CoinBox::open
        );
    }

    private static void open(Player player, Item item) {
        player.lock();
        item.remove();
        int amount = Random.get(150) == 5 ? Random.get(2000000, 100000000) : Random.get(2000000, 4000000);
        player.getInventory().add(COINS_995, amount);
        player.sendMessage("You have received " + Color.GOLD.wrap(NumberUtils.formatNumber(amount) + "") + " coins from the Coinbox");
        player.unlock();
    }
}
