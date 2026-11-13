package io.dm.deadman.content.sigils;

import io.dm.api.utils.NumberUtils;
import io.dm.cache.Color;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.InterfaceAction;
import io.dm.model.inter.InterfaceHandler;
import io.dm.model.inter.actions.SimpleAction;
import io.dm.model.inter.dialogue.MessageDialogue;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.utils.Option;
import lombok.Getter;

public enum SigilPurchaseScreen {


    /* TIER 1 */
    FORMIDABLE_FIGHTER(27, (SimpleAction)
            p -> sendStrings(
                    0,
                    p,
                    "Sigil of the Formidable Fighter",
                    "Your melee accuracy is increased by " + Color.RED.wrap("+30 in stab, slash and crush") + ".<br>"
                            + Color.RED.wrap("30% chance") + " for your next hit to deal " + Color.RED.wrap("+10 damage") + ". ",
                    25000)),

    METICULOUS_MAGE(30, (SimpleAction)
            p -> sendStrings(
                    1,
                    p,
                    "Sigil of the Meticulous Mage",
                    "Your magic accuracy is increased by " + Color.RED.wrap("+40") + ".<br>"
                            + Color.RED.wrap("50%") + " chance of " + Color.RED.wrap("saving runes") + " or "
                            + Color.RED.wrap("staff charges") + " when using Magic.",
                    25000)),

    RIGOROUS_RANGER(33, (SimpleAction)
            p -> sendStrings(
                    2,
                    p,
                    "Sigil of the Rigorous Ranger",
                    "Your ranged accuracy is increased by " + Color.RED.wrap("+20") + ".<br>"
                            + Color.RED.wrap("50% chance") + " of " + Color.RED.wrap("saving ammunition")
                            + " using " + Color.RED.wrap("Ranged weapons"),
                    25000)),

    DEFT_STRIKES(36, (SimpleAction)
            p -> sendStrings(
                    3,
                    p,
                    "Sigil of Deft Strikes",
                    "You gain " + Color.RED.wrap("20% more accuracy") + " in all styles against all " + Color.RED.wrap("non-player enemies") + ". ",
                    25000
            )),

    RESILIENCE(39, (SimpleAction)
            p -> sendStrings(
                    4,
                    p,
                    "Sigil of Resilience",
                    "Your hitpoints restore " + Color.RED.wrap("15X faster") + " than normal. ",
                    25000
            )),

    CONSISTENCY(42, (SimpleAction)
            p -> sendStrings(
                    5,
                    p,
                    "Sigil of Consistency",
                    "Every attack does " + Color.RED.wrap("+1 damage") + " to the target. ",
                    25000
            )),

    RESISTANCE(45, (SimpleAction)
            p -> sendStrings(
                    6,
                    p,
                    "Sigil of Resistance",
                    "All attacks from monsters are reduced by " + Color.RED.wrap("25%") + ".<br>" +
                            "Cannot be used with the " + Color.RED.wrap("Sigil of Titanium"),
                    25000
            )),

    AGILE_FORTUNE(52, (SimpleAction)
            p -> sendStrings(
                    7,
                    p,
                    "Sigil of Agile Fortune",
                    Color.RED.wrap("10K") + " Spawns with every " + Color.RED.wrap("Mark of Grace"),
                    25000
            )),

    HOARDING(55, (SimpleAction)
            p -> sendStrings(
                    8,
                    p,
                    "Sigil of Hoarding",
                    Color.RED.wrap("3x") + " the amount of rewards from " + Color.RED.wrap("Wintertodt") + ", " + Color.RED.wrap("Tempoross") + ", and " + Color.RED.wrap("GotR"),
                    25000
            )),

    SUSTENANCE(58, (SimpleAction)
            p -> sendStrings(
                    9,
                    p,
                    "Sigil of Sustenance",
                    "You have a " + Color.RED.wrap("50%") + " chance not to consume<br>" +
                            Color.RED.wrap("secondary ingredients") + " when creating potions. ",
                    25000
            )),

    FORAGER(61, (SimpleAction)
            p -> sendStrings(
                    10,
                    p,
                    "Sigil of the Forager",
                    Color.RED.wrap("2x") + " resources from " + Color.RED.wrap("Mining") + ", " + Color.RED.wrap("Fishing") + ", " + Color.RED.wrap("Woodcutting") + " and " + Color.RED.wrap("Farming") + ".<br>" +
                            "Cannot be used with the " + Color.RED.wrap("Sigil of the Enhanced Harvest"),
                    25000
            )),

    ENHANCED_HARVEST(64, (SimpleAction)
            p -> sendStrings(
                    11,
                    p,
                    "Sigil of Enhanced Harvest",
                    Color.RED.wrap("3x") + " resources from " + Color.RED.wrap("Mining") + ", " + Color.RED.wrap("Fishing") + ", " + Color.RED.wrap("Woodcutting") + " or " + Color.RED.wrap("Farming") + ".<br>" +
                            "Cannot be used with the " + Color.RED.wrap("Sigil of the Forager"),
                    25000
            )),

    STORAGE(67, (SimpleAction)
            p -> sendStrings(
                    12,
                    p,
                    "Sigil of Storage",
                    Color.RED.wrap("90%") + " chance to send your newly gathered resources to the Bank when gathering with " + Color.RED.wrap("Mining") + ", " + Color.RED.wrap("Fishing") + ", " + Color.RED.wrap("Woodcutting") + " or " + Color.RED.wrap("Farming") + ".",
                    25000
            )),

    ABYSS(70, (SimpleAction)
            p -> sendStrings(
                    13,
                    p,
                    "Sigil of the Abyss",
                    "When Runecrafting, you gain " + Color.RED.wrap("10X") + " as many runes as usual",
                    25000
            )),

    ALCHEMANIAC(77, (SimpleAction)
            p -> sendStrings(
                    14,
                    p,
                    "Sigil of the Alchemaniac",
                    " High Level Alchemy will provide " + Color.RED.wrap("30% extra points") + ".",
                    25000
            )),

    FOOD_MASTER(80, (SimpleAction)
            p -> sendStrings(
                    15,
                    p,
                    "Sigil of the Food Master",
                    Color.RED.wrap("20%") + " chance not to " + Color.RED.wrap("consume food") + " when used.<br>" +
                            "This effect has a " + Color.RED.wrap("6 second cooldown") + ".",
                    25000
            )),

    WELL_FED(83, (SimpleAction)
            p -> sendStrings(
                    16,
                    p,
                    "Sigil of the Well Fed",
                    Color.RED.wrap("20% chance") + " to gain twice the number of " + Color.RED.wrap("hitpoints") + " from any " + Color.RED.wrap("food") + " or " + Color.RED.wrap("potions") + " consumed.",
                    25000
            )),

    STAMINA(86, (SimpleAction)
            p -> sendStrings(
                    17,
                    p,
                    "Sigil of Stamina",
                    "You're permanently under the effects of a Stamina Potion. ",
                    25000
            )),

    POTION_MASTER(89, (SimpleAction)
            p -> sendStrings(
                    18,
                    p,
                    "Sigil of the Potion Master",
                    Color.RED.wrap("20% chance") + " not to consume " + Color.RED.wrap("potions") + " when using them.<br>" +
                            "This effect has a " + Color.RED.wrap("6 second cooldown") + ".",
                    25000
            )),

    ETERNAL_JEWELLER(92, (SimpleAction)
            p -> sendStrings(
                    19,
                    p,
                    "Sigil of the Eternal Jeweller",
                    "Do not consume charges on " + Color.RED.wrap("teleportation items") + "/" + Color.RED.wrap("equipment") + ".",
                    25000
            )),

    MOBILITY(95, (SimpleAction)
            p -> sendStrings(
                    20,
                    p,
                    "Sigil of Mobility",
                    "Teleports do not " + Color.RED.wrap("consume runes") + " and do not grant any " + Color.RED.wrap("XP") + ".",
                    25000
            )),

    EXAGGERATION(228, (SimpleAction)
            p -> sendStrings(
                    21,
                    p,
                    "Sigil of Exaggeration",
                    "All " + Color.RED.wrap("non-combat skills") + " are permanently " + Color.RED.wrap("boosted by 5") + ".",
                    25000
            )),

    /* TIER 2 */
    AUGMENTED_THRALL(102, (SimpleAction)
            p -> sendStrings(
                    22,
                    p,
                    "Sigil of the Augmented Thrall",
                    "The max hits of your " + Color.RED.wrap("thralls") + " are " + Color.RED.wrap("tripled") + ".",
                    50000
            )),

    LIGHTBEARER(105, (SimpleAction)
            p -> sendStrings(
                    23,
                    p,
                    "Sigil of the Lightbearer",
                    Color.RED.wrap("Special attack regen") + " becomes " + Color.RED.wrap("15 seconds per 10%") + ".<br>" +
                    "If " + Color.RED.wrap("Lightbearer ring") + " equipped, becomes " + Color.RED.wrap("10 seconds per 10%") + ".",
                    50000
            )),

    PRECISION(108, (SimpleAction)
            p -> sendStrings(
                    24,
                    p,
                    "Sigil of Precision",
                    "You gain " + Color.RED.wrap("50% more accuracy") + " in all styles against " + Color.RED.wrap("all monsters") + ".",
                    50000
            )),

    SPECIALISED_STRIKES(111, (SimpleAction)
            p -> sendStrings(
                    25,
                    p,
                    "Sigil of Specialised Strikes",
                    "Reduce " + Color.RED.wrap("Special Attack") + " costs by " + Color.RED.wrap("10% for 12 seconds") + ".<br>" +
                    "This effect has a " + Color.RED.wrap("30 second cooldown"),
                    50000
            )),

    PORCUPINE(114, (SimpleAction)
            p -> sendStrings(
                    26,
                    p,
                    "Sigil of the Porcupine",
                    Color.RED.wrap("Recoil 4 damage") + " to your attacker for every hit you take for " + Color.RED.wrap("45 seconds") + ".<br>" +
                    "This effect has a " + Color.RED.wrap("2 minute cooldown"),
                    50000
            )),

    BINDING(117, (SimpleAction)
            p -> sendStrings(
                    27,
                    p,
                    "Sigil of Binding",
                    Color.RED.wrap("75% magic accuracy") + " buff on next " + Color.RED.wrap("binding spell") + " within " + Color.RED.wrap("12 seconds") + ".<br>" +
                    "This effect has a " + Color.RED.wrap("30 second cooldown"),
                    50000
            )),

    FORTIFICATION(120, (SimpleAction)
            p -> sendStrings(
                    28,
                    p,
                    "Sigil of Fortification",
                    "Your " + Color.RED.wrap("defence") + " is increased by " + Color.RED.wrap("+50") + " for<br>" +
                    Color.RED.wrap("stab") + ", " + Color.RED.wrap("slash") + ", " + Color.RED.wrap("crush") + " and " + Color.RED.wrap("ranged") + ".",
                    50000
            )),

    RUTHLESS_RANGER(123, (SimpleAction)
            p -> sendStrings(
                    29,
                    p,
                    "Sigil of the Ruthless Ranger",
                    Color.RED.wrap( "10% chance") + " to deal " + Color.RED.wrap("5 damage") + " over " + Color.RED.wrap("6 seconds") + " & drain " + Color.RED.wrap("30 energy") + ".<br>" +
                    " Damage is " + Color.RED.wrap("doubled") + " against targets with " + Color.RED.wrap("no run energy") + ".",
                    50000
            )),

    FERAL_FIGHTER(127, (SimpleAction)
            p -> sendStrings(
                    30,
                    p,
                    "Sigil of the Feral Fighter",
                    Color.RED.wrap("20% chance") + " to set your " + Color.RED.wrap("attack speed") + " to " + Color.RED.wrap("1.2 seconds") + " for<br>" +
                            "" + Color.RED.wrap("1 attack vs player") + "," + Color.RED.wrap("2 attacks vs monsters") + ". within the next " + Color.RED.wrap("12 seconds") + ".",
                    50000)),

    MENACING_MAGE(130, (SimpleAction)
            p -> sendStrings(
                    31,
                    p,
                    "Sigil of the Menacing Mage",
                    Color.RED.wrap( "10% chance") + " to deal " + Color.RED.wrap("12 damage") + " over " + Color.RED.wrap("6 seconds") + ".<br>" +
                            "Heal for " + Color.RED.wrap("100%") + " of the  " + Color.RED.wrap("damage dealt") + ".",
                    50000)),

    HUNTER(152, (SimpleAction)
            p -> sendStrings(
                    32,
                    p,
                    "Sigil of the Hunter",
                    "You gain " + Color.RED.wrap("three Chinchompas") + " per " + Color.RED.wrap("successful Chinchompa capture") + ".",
                    50000)),

    INFERNAL_CHEF(155, (SimpleAction)
            p -> sendStrings(
                    33,
                    p,
                    "Sigil of the Infernal Chef",
                    "Instantly "+ Color.RED.wrap("cook fish") + " that are caught " + Color.RED.wrap("while Fishing") + ".<br>" +
                            "Gain " + Color.RED.wrap("Cooking XP") + " for fish cooked.",
                    50000)),

    INFERNAL_SMITH(158, (SimpleAction)
            p -> sendStrings(
                    34,
                    p,
                    "Sigil of the Infernal Smith",
                    "Instantly "+ Color.RED.wrap("forge bars") + " from the " + Color.RED.wrap("ore you Mine") + ".<br>" +
                            "Gain " + Color.RED.wrap("Smithing XP") + " for bars created.",
                    50000)),

    NATURE(161, (SimpleAction)
            p -> sendStrings(
                    35,
                    p,
                    "Sigil of Nature",
                    "Choose your own " + Color.RED.wrap("Farming Contracts") + ".<br>" +
                            "Crops grow " + Color.RED.wrap("5x quicker") + ".",
                    50000)),

    DEVOTION(164, (SimpleAction)
            p -> sendStrings(
                    36,
                    p,
                    "Sigil of Devotion",
                    Color.RED.wrap("Bones") + " and " + Color.RED.wrap("Ashes") + " are automatically " + Color.RED.wrap("buried/scattered") + ".<br>" +
                            Color.RED.wrap("Prayer XP") + " is increased by " + Color.RED.wrap("200%") + ".",
                    50000)),

    FAITH(232, (SimpleAction)
            p -> sendStrings(
                    37,
                    p,
                    "Sigil of Faith",
                    Color.RED.wrap("20% chance") + " to gain " + Color.RED.wrap("2x Prayer points") + " when consuming <br>" +
                            Color.RED.wrap("Prayer-restoring potions") + ".",
                    50000)),

    PROSPERITY(235, (SimpleAction)
            p -> sendStrings(
                    38,
                    p,
                    "Sigil of Prosperity",
                    Color.RED.wrap("20% increased chance") + " to receive loot from the <br>" +
                            Color.RED.wrap("Global Deadman Mode loot table") + ".",
                    50000)),

    SLAUGHTER(238, (SimpleAction)
            p -> sendStrings(
                    39,
                    p,
                    "Sigil of Slaughter",
                    "Pay " + Color.RED.wrap("30 Slayer Points") + " to choose your own " + Color.RED.wrap("Slayer Task") + ".<br>" +
                            Color.RED.wrap("50% chance") + " for slayer kill to " + Color.RED.wrap("provide XP") + " but not count towards task",
                    50000)),

    VERSATILITY(241, (SimpleAction)
            p -> sendStrings(
                    40,
                    p,
                    "Sigil of Versatility",
                    "Instantly " + Color.RED.wrap("swap your Spellbook") + " to any unlocked Spellbook<br>" +
                            "while " + Color.RED.wrap("outside of combat") + ".",
                    50000)),

    SERPENT(244, (SimpleAction)
            p -> sendStrings(
                    41,
                    p,
                    "Sigil of the Serpent",
                    Color.RED.wrap("Cure Venom and Poison") + " and " + Color.RED.wrap("gain immunity") + " from both for " + Color.RED.wrap("1 minute") + ".<br>" +
                            "This effect has a " + Color.RED.wrap("1 minute cooldown"),
                    50000)),

    SUPREME_STAMINA(247, (SimpleAction)
            p -> sendStrings(
                    42,
                    p,
                    "Sigil of Supreme Stamina",
                    "Set your " + Color.RED.wrap("Run Energy") + " to " + Color.RED.wrap("100%") + " and stop it from draining for " + Color.RED.wrap("1 minute") + ".<br>" +
                            "This effect has a " + Color.RED.wrap("2 minute cooldown"),
                    50000)),

    PRESERVATION(250, (SimpleAction)
            p -> sendStrings(
                    43,
                    p,
                    "Sigil of Preservation",
                    Color.RED.wrap("Stats boosted") + " above your base level will " + Color.RED.wrap("not drain over time") + ".",
                    50000)),

    /* TIER 3 */
    TITANIUM(180, (SimpleAction)
            p -> sendStrings(
                    44,
                    p,
                    "Sigil of Titanium",
                    Color.RED.wrap("All attacks") + " from " + Color.RED.wrap("monsters") + " are reduced by " + Color.RED.wrap("60%") + ".<br>" +
                            "Does not stack with " + Color.RED.wrap("the Sigil of resistance"),
                    100000)),

    NINJA(183, (SimpleAction)
            p -> sendStrings(
                    45,
                    p,
                    "Sigil of the Ninja",
                    Color.RED.wrap("All weapons") + " with a " + Color.RED.wrap("base attack speed above 4") + " will be " + Color.RED.wrap("reduced by 1") + ".<br>" +
                            "Excludes the " + Color.RED.wrap("crystal bow") + " and " + Color.RED.wrap("Bow of Faerdhinen") + ".",
                    100000)),

    FINALITY(186, (SimpleAction)
            p -> sendStrings(
                    46,
                    p,
                    "Sigil of Finality",
                    Color.RED.wrap("100% accuracy") + " on next " + Color.RED.wrap("special attack") + " within " + Color.RED.wrap("12 seconds") + ".<br>" +
                            "This effect has a " + Color.RED.wrap("1 minute cooldown") + ".",
                    100000)),

    PIOUS_PROTECTION(189, (SimpleAction)
            p -> sendStrings(
                    47,
                    p,
                    "Sigil of Pious Protection",
                    Color.RED.wrap("25% damage reduction") + " when " + Color.RED.wrap("praying corrrectly") + " against attacks.<br>" +
                            Color.RED.wrap("5% damage increase") + " when " + Color.RED.wrap("praying incorrectly") + " against attacks.",
                    100000)),

    AGGRESSION(192, (SimpleAction)
            p -> sendStrings(
                    48,
                    p,
                    "Sigil of Aggression",
                    "Deal " + Color.RED.wrap("10% more damage") + " but take " + Color.RED.wrap("5% more damage") + ".<br>" +
                            "Does not stack with " + Color.RED.wrap("the Sigil of Rampage")+ ".",
                    100000)),

    RAMPAGE(195, (SimpleAction)
            p -> sendStrings(
                    49,
                    p,
                    "Sigil of Rampage",
                    "Alternate " + Color.RED.wrap("combat styles") + " to " + Color.RED.wrap("deal bonus damage") + ", max 5 stacks.<br>" +
                            "Does not stack with " + Color.RED.wrap("the Sigil of Aggression")+ ", resets after " + Color.RED.wrap("12 seconds") + ".",
                    100000)),

    WOODCRAFT(257, (SimpleAction)
            p -> sendStrings(
                    50,
                    p,
                    "Sigil of Woodcraft",
                    "Receive " + Color.RED.wrap("planks") + " when " + Color.RED.wrap("chopping trees") + ", requires points to work.<br>" +
                            "50gp Normal, 125gp Oak, 250gp Teak, 700gp Mahogany.",
                    100000)),

    REMOTE_STORAGE(260, (SimpleAction)
            p -> sendStrings(
                    51,
                    p,
                    "Sigil of Remote Storage",
                    Color.RED.wrap("Resources gathered") + " from " + Color.RED.wrap("Mining") + ", " + Color.RED.wrap("Fishing") + ", " + Color.RED.wrap("Woodcutting") + " and " + Color.RED.wrap("Farming") + "<br>will be automatically " + Color.RED.wrap("sent to your Bank") + ".",
                    100000)),

    LAST_RECALL(282, (SimpleAction)
            p -> sendStrings(
                    52,
                    p,
                    "Sigil of Last Recall",
                    Color.RED.wrap("teleport back") + " to the location from which your " + Color.RED.wrap("most recent teleport") + " occurred.<br>" +
                            "This effect has a " + Color.RED.wrap("3 minute cooldown"),
                    100000)),

    GUARDIAN_ANGEL(285, (SimpleAction)
            p -> sendStrings(
                    53,
                    p,
                    "Sigil of the Guardian Angel",
                    Color.RED.wrap("Revive") + " when " + Color.RED.wrap("dying from PvM combat") + ", does " + Color.RED.wrap("not work in PvP") + ".<br>" +
                            "This sigil will be " + Color.RED.wrap("consumed upon activation"),
                    100000)),

    CLOSE_BUTTON(9, (SimpleAction) p -> {
        p.selectedSigilShopIndex = -1;
        p.selectedSigilShopName = null;
        p.selectedSigilShopPrice = -1;
    }),

    PURCHASE_BUTTON(221, (SimpleAction) SigilPurchaseScreen::buySigil);
    ;

    @Getter private final int component;
    @Getter InterfaceAction action;

    SigilPurchaseScreen() {
        component = -1;
    }

    SigilPurchaseScreen(int component, InterfaceAction action) {
        this.component = component;
        this.action = action;
    }

    static {

        InterfaceHandler.register(912, (h) -> {
            for (SigilPurchaseScreen element : SigilPurchaseScreen.values()) {
                if (element.component < 0)
                    continue;
                h.actions[element.component] = element.getAction();
            }
        });

    }

    private static void sendStrings(int selection, Player p, String name, String info, int price) {
        p.selectedSigilShopIndex = selection;
        p.selectedSigilShopName = name;
        p.selectedSigilShopPrice = price;

        p.getPacketSender().sendString(912, 223, name);
        p.getPacketSender().sendString(912, 224, info);
        p.getPacketSender().sendString(912, 225, NumberUtils.formatNumber(price) + " points");
    }

    private static void buySigil(Player p) {
        String name = p.selectedSigilShopName;
        int price = p.selectedSigilShopPrice;
        Sigil sigil = Sigils.get(name);

        if (sigil == null) {
            p.sendMessage("Something catastrophic has gone wrong, please take a screenshot and raise a ticket on discord.");
            p.closeInterfaces();
            return;
        }

        p.unsafeDialogue(
                new OptionsDialogue(
                        "Purchase the " + name + " for " + NumberUtils.formatNumber(price) + " points?",
                        new Option("Yes", () -> {
                            if (!p.getInventory().contains(995, price)) {
                                showMessage(p, "You don't have enough points to purchase this sigil.");
                                return;
                            }

                            if (!p.getInventory().hasFreeSlots(1) || (p.getInventory().getFreeSlots() == 0 && p.getInventory().count(995) != price)) {
                                showMessage(p, "You don't have enough free space to complete this.");
                                return;
                                }

                            p.getInventory().remove(995, price);
                            p.getInventory().add(sigil.unattuned(), 1);
                            p.closeDialogue();
                        }),
                        new Option("No", p::closeDialogue)
                )
        );
    }

    private static void showMessage(Player p, String message) {
        p.unsafeDialogue(
                new MessageDialogue(message)
        );
    }

}
