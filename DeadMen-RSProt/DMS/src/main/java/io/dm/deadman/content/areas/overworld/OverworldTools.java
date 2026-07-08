package io.dm.deadman.content.areas.overworld;

import io.dm.cache.ItemID;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.dialogue.MessageDialogue;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.utils.Option;
import io.dm.model.item.actions.ItemAction;

public class OverworldTools {

    public static void register() {

        ItemAction.registerInventory(10491, "Upgrade", (p, i) -> {
            // === Overworld Axe ===

            if (maxUpgrade(p, Tool.AXE)) {
                p.dialogue(new MessageDialogue("This item is already at the maximum upgrade state (Infernal)"));
                return;
            }

            String name = p.overworldToolTier[Tool.AXE.id].name();
            Tier nextTier = p.overworldToolTier[Tool.AXE.id].nextTier;
            int nextPoints = nextTier.cost;
            p.dialogue(
                    new OptionsDialogue(
                            "Overworld Axe | " + name + " Tier",
                            new Option("Upgrade - Cost: " + nextPoints + " Overworld Points", () -> {

                                if (!hasEnoughPoints(p, Tool.AXE)) {
                                    int req = p.overworldToolTier[Tool.AXE.id].nextTier.cost - p.overworldPoints;

                                    p.dialogue(new MessageDialogue("You don't have enough points to do this. You will need " + req + " more points!"));
                                    return;
                                }

                                p.overworldPoints -= nextPoints;
                                p.overworldToolTier[Tool.AXE.id] = nextTier;
                                p.sendMessage("You spend " + nextPoints + " upgrading your Overworld Axe, and it is now " + nextTier.name() + " Tier!");
                            }),
                            new Option("Cancel")
                    )
            );
        });

        ItemAction.registerInventory(11719, "Upgrade", (p, i) -> {
            // === Overworld Axe ===

            if (maxUpgrade(p, Tool.PICKAXE)) {
                p.dialogue(new MessageDialogue("This item is already at the maximum upgrade state (Infernal)"));
                return;
            }

            String name = p.overworldToolTier[Tool.PICKAXE.id].name();
            Tier nextTier = p.overworldToolTier[Tool.PICKAXE.id].nextTier;
            int nextPoints = nextTier.cost;
            p.dialogue(
                    new OptionsDialogue(
                            "Overworld Pickaxe | " + name + " Tier",
                            new Option("Upgrade (" + nextPoints + " Overworld Points)", () -> {

                                if (!hasEnoughPoints(p, Tool.PICKAXE)) {
                                    int req = p.overworldToolTier[Tool.PICKAXE.id].nextTier.cost - p.overworldPoints;

                                    p.dialogue(new MessageDialogue("You don't have enough points to do this. You will need " + req + " more points!"));
                                    return;
                                }

                                p.overworldPoints -= nextPoints;
                                p.overworldToolTier[Tool.PICKAXE.id] = nextTier;
                                p.sendMessage("You spend " + nextPoints + " upgrading your Overworld Pickaxe, and it is now " + nextTier.name() + " Tier!");
                            }),
                            new Option("Cancel")
                    )
            );
        });

        ItemAction.registerInventory(4051, "Info", (p, i) -> {
            StringBuilder unlocked = new StringBuilder();
            StringBuilder remaining = new StringBuilder();
            unlocked.append("Small Net/Bait");
            if (Tier.hasTier(p, Tool.FISHING, Tier.Iron)) { unlocked.append(", Lure/Bait");} else {remaining.append("Lure/Bait (Iron)");}
            if (Tier.hasTier(p, Tool.FISHING, Tier.Steel)) {unlocked.append(", Cage/Harpoon");} else {remaining.append(", Cage/Harpoon (Steel)");}
            if (Tier.hasTier(p, Tool.FISHING, Tier.Adamant)) {unlocked.append(", Big Net/Harpoon");} else {remaining.append(", Big Net/Harpoon (Adamant)");}
            if (Tier.hasTier(p, Tool.FISHING, Tier.Rune)) {unlocked.append(", Minnows");} else {remaining.append(", Minnows (Rune)");}
            if (Tier.hasTier(p, Tool.FISHING, Tier.Dragon)) {unlocked.append(", Small Net/Harpoon");} else {remaining.append(", Small Net/Harpoon (Dragon)");}

            p.dialogue(
                    new MessageDialogue("Current Spots: " + unlocked),
                    new MessageDialogue(
                            Tier.hasTier(p, Tool.FISHING, Tier.Dragon) ?
                                    "You have unlocked all the Fishing Tools!" :
                                    "You still need to unlock: " + remaining
                    )
            );
        });

        ItemAction.registerInventory(4051, "Upgrade", (p, i) -> {
            // === Overworld Fishing Tool ===

            if (maxUpgrade(p, Tool.FISHING)) {
                p.dialogue(new MessageDialogue("This item is already at the maximum upgrade state (All Tools)"));
                return;
            }

            String name = p.overworldToolTier[Tool.FISHING.id].name();
            Tier nextTier = p.overworldToolTier[Tool.FISHING.id].nextTier;
            int nextPoints = nextTier.cost;
            p.dialogue(
                    new OptionsDialogue(
                            "Overworld Fishing Tool | " + name + " Tier",
                            new Option("Upgrade (" + nextPoints + " Overworld Points)", () -> {

                                if (!hasEnoughPoints(p, Tool.FISHING)) {
                                    int req = p.overworldToolTier[Tool.FISHING.id].nextTier.cost - p.overworldPoints;

                                    p.dialogue(new MessageDialogue("You don't have enough points to do this. You will need " + req + " more points!"));
                                    return;
                                }

                                p.overworldPoints -= nextPoints;
                                p.overworldToolTier[Tool.FISHING.id] = nextTier;
                                p.sendMessage("You spend " + nextPoints + " upgrading your Overworld Pickaxe, and it is now " + nextTier.name() + " Tier!");
                            }),
                            new Option("Cancel")
                    )
            );
        });

    }

    public enum Tool {
        AXE(0),
        PICKAXE(1),
        FISHING(2);

        public int id;

        Tool(int id) {
            this.id = id;
        }
    }

    public enum Tier {
        Infernal(1075000, null, 99),
        Dragon(535000, Infernal, 98),
        Rune(265000, Dragon, 97),
        Adamant(130000, Rune, 96),
        Mithril(65000, Adamant, 95),
        Steel(40000, Mithril, 94),
        Iron(20000, Steel, 93),
        Bronze(5000, Iron, 92);

        public final int cost;
        public final Tier nextTier;
        public final int power;

        Tier(int cost, Tier nextTier, int power) {
            this.cost = cost;
            this.nextTier = nextTier;
            this.power = power;
        }

        public static boolean hasTier(Player p, Tool tool, Tier tier) {
            return p.overworldToolTier[tool.id].power >= tier.power;
        }

        public static boolean hasFishingTier(Player p, Tool tool, NPC tier) {
            switch (tier.getId()) {
                case 1518: return true;

                case 1506: return hasTier(p, tool, Tier.Iron);

                case 1519: return hasTier(p, tool, Tier.Steel);

                case 1520: return hasTier(p, tool, Tier.Adamant);

                case 4316: return hasTier(p, tool, Tier.Rune);

                case 7731: return hasTier(p, tool, Tier.Dragon);

                default: return false;
            }
        }
    }

    private static boolean hasEnoughPoints(Player p, Tool tool) {
        Tier currentTier = p.overworldToolTier[tool.id];

        return p.overworldPoints >= currentTier.nextTier.cost;
    }

    private static boolean maxUpgrade(Player p, Tool tool) {
        return p.overworldToolTier[tool.id] == Tier.Infernal;
    }

    private static void upgrade(Player p, Tool tool) {
        Tier currentTier = p.overworldToolTier[tool.id];

        p.overworldToolTier[tool.id] = currentTier.nextTier;
    }

    public static boolean hasTool(Player p, Tool tool) {
        int id = tool == Tool.AXE ? ItemID.OVERWORLD_AXE :
                tool == Tool.PICKAXE ? ItemID.OVERWORLD_PICKAXE :
                        tool == Tool.FISHING ? ItemID.OVERWORLD_PICKAXE : -1;
        return (p.getInventory().contains(id) || p.getEquipment().contains(id));
    }


}
