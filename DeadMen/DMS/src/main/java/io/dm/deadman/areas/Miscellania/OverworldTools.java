package io.dm.deadman.areas.Miscellania;

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

            if (maxUpgrade(p, Tool.AXE)) {
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

        ItemAction.registerInventory(4051, "Upgrade", (p, i) -> {
            // === Overworld Axe ===

            if (maxUpgrade(p, Tool.AXE)) {
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
        Infernal(1075000, null),
        Dragon(535000, Infernal),
        Rune(265000, Dragon),
        Adamant(130000, Rune),
        Mithril(65000, Adamant),
        Steel(40000, Mithril),
        Iron(20000, Steel),
        Bronze(5000, Iron);

        public final int cost;
        public final Tier nextTier;

        Tier(int cost, Tier nextTier) {
            this.cost = cost;
            this.nextTier = nextTier;
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


}
