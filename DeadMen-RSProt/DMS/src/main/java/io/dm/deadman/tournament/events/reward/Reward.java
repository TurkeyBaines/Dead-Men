package io.dm.deadman.tournament.events.reward;

import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;

public class Reward {
    private final RewardTier tier;
    private final RewardType type;
    private final Item item;

    public Reward(RewardTier tier, RewardType type, int id, int amount) {
        this.tier = tier;
        this.type = type;
        this.item = new Item(id, amount);
    }

    public RewardTier getTier() { return tier; }
    public RewardType getType() { return type; }
    public Item getItem() { return item; }

    public void spawnReward(Player player) {
        if (item.getId() == VOID_MAGE || item.getId() == VOID_RANGE || item.getId() == VOID_MELEE) {
            if (player.getInventory().contains(item.getId()) || player.getBank().contains(item.getId())) {
                EventRewards.giveReward(player, tier); //re-roll the reward if the player already owns the helm
                return;
            }
            player.getInventory().addOrDrop(item.getId(), 1, null);

            if (!player.getInventory().contains(8839) && !player.getBank().contains(8839)) //prevent duplicate void
                player.getInventory().addOrDrop(8839, 1, null);
            if (!player.getInventory().contains(8840) && !player.getBank().contains(8840)) //items if already owned
                player.getInventory().addOrDrop(8840, 1, null);
            if (!player.getInventory().contains(8842) && !player.getBank().contains(8842))
                player.getInventory().addOrDrop(8842, 1, null);
        } else {
            player.getInventory().addOrDrop(item.getId(), item.getAmount(), null);
        }
    }

    public String getText() {
        switch (item.getId()) {
            case VOID_MAGE:
                return "Void Mage Set";

            case VOID_RANGE:
                return "Void Range Set";

            case VOID_MELEE:
                return "Void Melee Set";

            default:
                return getItem().getDef().name;
        }
    }

    private final int VOID_MAGE = 11663;
    private final int VOID_RANGE = 11664;
    private final int VOID_MELEE = 11665;
}