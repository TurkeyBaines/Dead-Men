package io.dm.model.item.actions.impl;

import io.dm.cache.ItemDef;
import io.dm.model.inter.dialogue.YesNoDialogue;
import io.dm.model.item.actions.ItemAction;

public class DestroyAction {

    static {
        ItemDef.forEach(def -> ItemAction.registerInventory(def.id, "destroy", (player, item) -> {
            player.dialogue(
                    new YesNoDialogue(
                            "Are you sure you want to destroy this item?",
                            "Warning: This action cannot be undone.",
                            item, item::remove
                    )
            );
        }));
    }

}
