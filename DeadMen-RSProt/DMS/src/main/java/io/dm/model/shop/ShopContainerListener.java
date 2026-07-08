package io.dm.model.shop;

import io.dm.model.entity.player.Player;
import io.dm.model.item.containers.ShopItemContainer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class ShopContainerListener {

    private final Player player;
    private ShopItemContainer shopContainer;


    public void open(){
        shopContainer.send(player, 300 << 16 | 16, 2);
    }


    public void update(){
        shopContainer.sendUpdates(player, 300 << 16 | 16, 2);
    }


}
