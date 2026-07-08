package io.dm.model.item.actions.impl.scrolls.boosts;

import io.dm.model.entity.shared.listeners.LoginListener;
import io.dm.model.inter.dialogue.ItemDialogue;
import io.dm.model.inter.dialogue.MessageDialogue;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.utils.Option;
import io.dm.model.item.actions.ItemAction;

public class RareDropScroll {

    private static final int RARE_DROP_SCROLL = 607;

    static {
        ItemAction.registerInventory(RARE_DROP_SCROLL, "activate", (player, item) -> {
            if(player.rareDropBonus.isDelayed()) {
                player.dialogue(new MessageDialogue("You already have a rare drop boost activated!"));
                return;
            }
            player.dialogue(new OptionsDialogue("Are you sure you want to active your scroll?",
                    new Option("Yes, active my rare drop boost scroll!", () -> {
                        item.remove();
                        player.rareDropBonus.delaySeconds(30 * 60);
                        player.dialogue(new ItemDialogue().one(item.getId(), "You have activated your rare drop boost scroll. Your drops " +
                                "will be boosted by 15% for the next 30 minutes."));
                    }),
                    new Option("No, I'm not ready yet!")));
        });
        LoginListener.register(player -> {
            if (player.rareDropBonusTimeLeft > 0) {
                player.rareDropBonus.delay(player.rareDropBonusTimeLeft);
                player.rareDropBonusTimeLeft = 0;
            }
        });
    }
}
