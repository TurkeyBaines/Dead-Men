package io.dm.deadman.content.items;

import io.dm.cache.ItemID;
import io.dm.deadman.Deadman;
import io.dm.model.item.actions.ItemAction;

public class BloodEssence {



    static {
        ItemAction.registerInventory(ItemID.BLOOD_ESSENCE, "Redeem", (p, i) -> {
            //Check for save zone
            if (!Deadman.getCitadel().contains(p) && !Deadman.getPurgatory().contains(p)) {
                
            }
        });
    }

}
