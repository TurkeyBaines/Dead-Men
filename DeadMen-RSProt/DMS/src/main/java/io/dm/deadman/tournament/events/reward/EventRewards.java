package io.dm.deadman.tournament.events.reward;

import io.dm.api.utils.Random;
import io.dm.cache.ItemID;
import io.dm.model.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EventRewards {

    private static List<Reward> rewards;

    static {
        rewards = new ArrayList<>();
        addLowTier();
        addMedTier();
        addHighTier();
        addGodTier();
    }

    public static Reward giveReward(Player player, RewardTier tier) {
        Reward chosen = getRandomReward(tier);

        chosen.spawnReward(player);
        player.sendMessage("You are rewarded with " + chosen.getText());
        return chosen;
    }

    public static List<Reward> filterRewards(
            List<Reward> rewards,
            RewardType type,
            RewardTier tier
    ) {
        return rewards.stream()
                .filter(r -> r.getType() == type)
                .filter(r -> r.getTier() == tier)
                .collect(Collectors.toList());
    }

    public static Reward getRandomReward(RewardTier tier) {
        switch (tier) {
            case LOW:
                return getLowTier().get(Random.get(getLowTier().size()-1));

            case MED:
                return getMedTier().get(Random.get(getMedTier().size()-1));

            case HIGH:
                return getHighTier().get(Random.get(getHighTier().size()-1));

            case GOD:
                return getGodTier().get(Random.get(getGodTier().size()-1));

            default:
                return null;
        }
    }

    private static void addLowTier() {
        rewards.add(new Reward(RewardTier.LOW, RewardType.ARMOUR, 1079, 1)); //      | Rune Platelegs
        rewards.add(new Reward(RewardTier.LOW, RewardType.ARMOUR, 1113, 1)); //      | Rune Chainbody
        rewards.add(new Reward(RewardTier.LOW, RewardType.ARMOUR, 1127, 1)); //      | Rune Platebody
        rewards.add(new Reward(RewardTier.LOW, RewardType.ARMOUR, 1147, 1)); //      | Rune Med Helm
        rewards.add(new Reward(RewardTier.LOW, RewardType.ARMOUR, 1163, 1)); //      | Rune Full Helm
        rewards.add(new Reward(RewardTier.LOW, RewardType.ARMOUR, 1185, 1)); //      | Rune Sq Shield
        rewards.add(new Reward(RewardTier.LOW, RewardType.ARMOUR, 1201, 1)); //      | Rune Kite Shield
        rewards.add(new Reward(RewardTier.LOW, RewardType.ARMOUR, 4089, 1)); //      | Mystic Hat
        rewards.add(new Reward(RewardTier.LOW, RewardType.ARMOUR, 4091, 1)); //      | Mystic Robe Top
        rewards.add(new Reward(RewardTier.LOW, RewardType.ARMOUR, 4093, 1)); //      | Mystic Robe Bottoms

        rewards.add(new Reward(RewardTier.LOW, RewardType.WEAPON, 1319, 1)); //      | Rune 2h Sword
        rewards.add(new Reward(RewardTier.LOW, RewardType.WEAPON, 1333, 1)); //      | Rune Scim
    }

    private static List<Reward> getLowTier() {
        List<Reward> rewards1 = new ArrayList<>();
        for (Reward r : rewards) {
            if (r.getTier() == RewardTier.LOW) rewards1.add(r);
        }
        return rewards1;
    }

    private static void addMedTier() {
        rewards.add(new Reward(RewardTier.MED, RewardType.UPGRADE, 11663, 1)); //    | Void Mage Set
        rewards.add(new Reward(RewardTier.MED, RewardType.UPGRADE, 11664, 1)); //    | Void Range Set
        rewards.add(new Reward(RewardTier.MED, RewardType.UPGRADE, 11665, 1)); //    | Void Melee Set
        rewards.add(new Reward(RewardTier.MED, RewardType.UPGRADE, 11665, 1)); //    | Rune Defender
    }

    private static List<Reward> getMedTier() {
        List<Reward> rewards1 = new ArrayList<>();
        for (Reward r : rewards) {
            if (r.getTier() == RewardTier.MED) rewards1.add(r);
        }
        return rewards1;
    }

    private static void addHighTier() {
        rewards.add(new Reward(RewardTier.HIGH, RewardType.UPGRADE, 11663, 1)); //    | Void Mage Set
        rewards.add(new Reward(RewardTier.HIGH, RewardType.UPGRADE, 11664, 1)); //    | Void Range Set
        rewards.add(new Reward(RewardTier.HIGH, RewardType.UPGRADE, 11665, 1)); //    | Void Melee Set
        rewards.add(new Reward(RewardTier.HIGH, RewardType.UPGRADE, 11665, 1)); //    | Rune Defender
    }

    private static List<Reward> getHighTier() {
        List<Reward> rewards1 = new ArrayList<>();
        for (Reward r : rewards) {
            if (r.getTier() == RewardTier.HIGH) rewards1.add(r);
        }
        return rewards1;
    }

    private static void addGodTier() {
        rewards.add(new Reward(RewardTier.GOD, RewardType.WEAPON, ItemID.DRAGON_CLAWS, 1)); //    | Dragon Claws
        rewards.add(new Reward(RewardTier.GOD, RewardType.WEAPON, ItemID.ARMADYL_GODSWORD, 1)); //    | Armadyl Godsword
        rewards.add(new Reward(RewardTier.GOD, RewardType.WEAPON, ItemID.VESTAS_LONGSWORD, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.WEAPON, ItemID.VESTAS_SPEAR, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.WEAPON, ItemID.STATIUSS_WARHAMMER, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.WEAPON, ItemID.ZURIELS_STAFF, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.WEAPON, ItemID.KODAI_WAND, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.WEAPON, ItemID.TOXIC_STAFF_OF_THE_DEAD, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.WEAPON, ItemID.UNCHARGED_TOXIC_TRIDENT, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.WEAPON, ItemID.SANGUINESTI_STAFF_UNCHARGED, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.WEAPON, ItemID.ARMADYL_CROSSBOW, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.WEAPON, ItemID.DRAGON_HUNTER_CROSSBOW, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.WEAPON, ItemID.MORRIGANS_JAVELIN, 25));
        rewards.add(new Reward(RewardTier.GOD, RewardType.WEAPON, ItemID.MORRIGANS_THROWING_AXE, 25));
        rewards.add(new Reward(RewardTier.GOD, RewardType.WEAPON, ItemID.GHRAZI_RAPIER, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.WEAPON, ItemID.DRAGON_WARHAMMER, 1));

        rewards.add(new Reward(RewardTier.GOD, RewardType.ARMOUR, ItemID.DINHS_BULWARK, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.ARMOUR, ItemID.VESTAS_CHAINBODY, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.ARMOUR, ItemID.VESTAS_PLATESKIRT, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.ARMOUR, ItemID.ZURIELS_ROBE_TOP, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.ARMOUR, ItemID.ZURIELS_ROBE_BOTTOM, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.ARMOUR, ItemID.ZURIELS_HOOD, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.ARMOUR, ItemID.MORRIGANS_COIF, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.ARMOUR, ItemID.MORRIGANS_LEATHER_BODY, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.ARMOUR, ItemID.MORRIGANS_LEATHER_CHAPS, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.ARMOUR, ItemID.STATIUSS_FULL_HELM, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.ARMOUR, ItemID.STATIUSS_PLATEBODY, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.ARMOUR, ItemID.STATIUSS_PLATELEGS, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.ARMOUR, ItemID.DRAGON_DEFENDER, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.ARMOUR, ItemID.VOID_MAGE_HELM, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.ARMOUR, ItemID.VOID_RANGER_HELM, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.ARMOUR, ItemID.VOID_MELEE_HELM, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.ARMOUR, ItemID.VOID_MELEE_HELM, 1));

        rewards.add(new Reward(RewardTier.GOD, RewardType.UPGRADE, ItemID.ARCHERS_RING_I, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.UPGRADE, ItemID.BERSERKER_RING_I, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.UPGRADE, ItemID.SEERS_RING_I, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.UPGRADE, ItemID.SEERS_RING_I, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.UPGRADE, ItemID.BRIMSTONE_RING, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.UPGRADE, ItemID.IMBUED_HEART, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.UPGRADE, ItemID.IMBUED_GUTHIX_CAPE, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.UPGRADE, ItemID.IMBUED_ZAMORAK_CAPE, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.UPGRADE, ItemID.IMBUED_SARADOMIN_CAPE, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.UPGRADE, ItemID.AMULET_OF_TORTURE, 1));
        rewards.add(new Reward(RewardTier.GOD, RewardType.UPGRADE, ItemID.AMULET_OF_THE_DAMNED_FULL, 1));

    }

    private static List<Reward> getGodTier() {
        List<Reward> rewards1 = new ArrayList<>();
        for (Reward r : rewards) {
            if (r.getTier() == RewardTier.GOD) rewards1.add(r);
        }
        return rewards1;
    }
}
