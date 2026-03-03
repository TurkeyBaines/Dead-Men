package io.dm.model.skills.crafting;

import io.dm.model.entity.player.Player;
import io.dm.model.inter.dialogue.skill.SkillDialogue;
import io.dm.model.inter.dialogue.skill.SkillItem;
import io.dm.model.item.Item;
import io.dm.model.item.actions.ItemItemAction;
import io.dm.model.skills.Tool;
import io.dm.model.stat.StatType;

public enum Amethyst {

    AMETHYST_BOLT_TIPS(21338, 83, 60.0, 15),
    AMETHYST_ARROW_TIPS(21350, 85, 60.0, 15),
    AMETHYST_JAVELIN_HEADS(21352, 87, 60.0, 5);

    public final int itemID, levelReq, amount;
    public final double exp;

    Amethyst(int itemID, int levelReq, double exp, int amount) {
        this.itemID = itemID;
        this.levelReq = levelReq;
        this.exp = exp;
        this.amount = amount;
    }

    private static final int AMETHYST = 21347;

    private static void craft(Player player, Amethyst crystal, int amount) {
        player.closeInterfaces();
        if (!player.getStats().check(StatType.Crafting, crystal.levelReq, "make this"))
            return;

        int maxAmount = player.getInventory().getAmount(AMETHYST);
        if (maxAmount < amount)
            amount = maxAmount;

        final int amt = amount;
        player.startEvent(event -> {
            int made = 0;
            while (made++ < amt) {
                Item item = player.getInventory().findItem(AMETHYST);
                if(item == null)
                    return;
                item.remove();
                player.getInventory().add(crystal.itemID, crystal.amount);
                player.animate(6295);
                player.getStats().addXp(StatType.Crafting, crystal.exp, true);
                event.delay(2);
            }
        });
    }

    static {
        ItemItemAction.register(AMETHYST, Tool.CHISEL, (player, primary, secondary) -> {
            SkillDialogue.make(player,
                    new SkillItem(AMETHYST_BOLT_TIPS.itemID).addAction((p, amount, event) -> craft(p, AMETHYST_BOLT_TIPS, amount)),
                    new SkillItem(AMETHYST_ARROW_TIPS.itemID).addAction((p, amount, event) -> craft(p, AMETHYST_ARROW_TIPS, amount)),
                    new SkillItem(AMETHYST_JAVELIN_HEADS.itemID).addAction((p, amount, event) -> craft(p, AMETHYST_JAVELIN_HEADS, amount)));
        });
    }
}
