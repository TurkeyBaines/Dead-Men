package io.dm.deadman.content.items;

import io.dm.cache.ItemID;
import io.dm.deadman.Deadman;
import io.dm.deadman.tournament.mutators.Mutator;
import io.dm.deadman.tournament.mutators.impl.EmptyMutator;
import io.dm.deadman.tournament.mutators.impl.StaticGasMutator;
import io.dm.deadman.tournament.mutators.impl.VampiricRitesMutator;
import io.dm.deadman.tournament.TournamentConfig;
import io.dm.deadman.tournament.configs.CustomConfig;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.dialogue.MessageDialogue;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.dialogue.YesNoDialogue;
import io.dm.model.inter.utils.Option;
import io.dm.model.item.Item;
import io.dm.model.item.actions.ItemAction;

public class TournamentTicket {

    static TournamentConfig.Timespan runtime;
    static String str_runtime;
    static TournamentConfig.TeamSize teamSize;
    static String str_teamSize;
    static int xpRate;
    static String str_xpRate;
    static int dropRate;
    static String str_dropRate;
    static int dropPetRate;
    static String str_dropPetRate;
    static Mutator mutator;
    static String str_mutator;

    static {

        ItemAction.registerInventory(ItemID.TOURNAMENT_TICKET, "Info", (p, i) -> {
            p.dialogue(
                    new MessageDialogue("This ticket allows you to control the settings for the next tournament!"),
                    new YesNoDialogue("Would you like to begin?", "", new Item(ItemID.TOURNAMENT_TICKET), () -> setupTournament(p))
            );
        });

        ItemAction.registerInventory(ItemID.TOURNAMENT_TICKET, "Claim", (p, i) -> {
            p.dialogue(new YesNoDialogue("Would you like to begin?", "", new Item(ItemID.TOURNAMENT_TICKET), () -> setupTournament(p)));
        });

    }

    private static void setupTournament(Player p) {
        if (!p.getInventory().contains(ItemID.TOURNAMENT_TICKET)) {
            p.sendMessage("Nice try... this has been recorded.");
            return;
        }
        if (!Deadman.getCitadel().contains(p) && !Deadman.getOverworld().contains(p)) {
            p.sendMessage("You must be in the Overworld or the Citadel to start this process");
            return;
        }


        p.lock();

        showDialogue(p, 0);
    }

    private static void showDialogue(Player p, int id) {

        switch (id) {
            case 0:
                p.dialogue(new MessageDialogue("Welcome to the Tournament Setup Interface!"));

                if (Deadman.canOverrideConfig()) {
                    showDialogue(p, 1);
                } else {
                    showDialogue(p, -1);
                }
                break;

            case -1:
                p.dialogue(new MessageDialogue("I'm afraid the next Tournament is already preset for someone else... Please try again once the next Tournament begins."));
                p.unlock();
                break;

            case 1:
                p.dialogue(new MessageDialogue("First we're going to select Runtime, this is how long the Tournament will go on for..."),
                        new OptionsDialogue(
                                "How long would you like to set?",
                                new Option("3 Hours", () -> { runtime = TournamentConfig.Timespan.THREE_HOURS; str_runtime = "3 Hours"; showDialogue(p, 2);}),
                                new Option("5 Hours", () -> { runtime = TournamentConfig.Timespan.FIVE_HOURS; str_runtime = "5 Hours"; showDialogue(p, 2);}),
                                new Option("8 Hours", () -> { runtime = TournamentConfig.Timespan.EIGHT_HOURS; str_runtime = "8 Hours"; showDialogue(p, 2);}),
                                new Option("10 Hours", () -> { runtime = TournamentConfig.Timespan.TEN_HOURS; str_runtime = "10 Hours"; showDialogue(p, 2);}),
                                new Option("12 Hours", () -> { runtime = TournamentConfig.Timespan.TWELVE_HOURS; str_runtime = "12 Hours"; showDialogue(p, 2);})
                        ));
                break;

            case 2:
                p.dialogue(new MessageDialogue("How many people should we have in a team, should it be a Solo Tournament, or Groups?"),
                        new OptionsDialogue(
                                "How many people to a team?",
                                new Option("Solo", () -> { teamSize = TournamentConfig.TeamSize.SOLO; str_teamSize = "Solo"; showDialogue(p, 3);}),
                                new Option("Duos", () -> { teamSize = TournamentConfig.TeamSize.DUO; str_teamSize = "Duos"; showDialogue(p, 3);}),
                                new Option("Trios", () -> { teamSize = TournamentConfig.TeamSize.TRIO; str_teamSize = "Trios"; showDialogue(p, 3);})
                        ));
                break;

            case 3:
                p.dialogue(new MessageDialogue("Great choice! We're now going to select XP Rate, this will control XP Gain..."),
                        new OptionsDialogue(
                                "What XP Multiplier would you like to set?",
                                new Option("5x", () -> { xpRate = 5; str_xpRate = "5x"; showDialogue(p, 4);}),
                                new Option("10x", () -> { xpRate = 10; str_xpRate = "10x"; showDialogue(p, 4);}),
                                new Option("25x", () -> { xpRate = 25; str_xpRate = "25x"; showDialogue(p, 4);}),
                                new Option("50x", () -> { xpRate = 50; str_xpRate = "50x"; showDialogue(p, 4);}),
                                new Option("100x", () -> { xpRate = 100; str_xpRate = "100x"; showDialogue(p, 4);})
                        ));
                break;

            case 4:
                p.dialogue(new MessageDialogue("Nice! Next up is the Drop Rate, it controls how often rare items drop..."),
                        new OptionsDialogue(
                                "What Drop Rate Multiplier would you like to set?",
                                new Option("1x", () -> { dropRate = 1; str_dropRate = "1x"; showDialogue(p, 5);}),
                                new Option("2x", () -> { dropRate = 2; str_dropRate = "2x"; showDialogue(p, 5);}),
                                new Option("5x", () -> { dropRate = 5; str_dropRate = "5x"; showDialogue(p, 5);}),
                                new Option("10x", () -> { dropRate = 10; str_dropRate = "10x"; showDialogue(p, 5);})
                        ));
                break;

            case 5:
                p.dialogue(new MessageDialogue("Ok, we're almost done. Let's look at Pet Drop Rate, these are kept across tournaments..."),
                        new OptionsDialogue(
                                "What Pet Drop Rate Multiplier would you like to set?",
                                new Option("1x", () -> { dropPetRate = 1; str_dropPetRate = "1x"; showDialogue(p, 6);}),
                                new Option("2x", () -> { dropPetRate = 2; str_dropPetRate = "2x"; showDialogue(p, 6);})
                        ));
                break;

            case 6:
                p.dialogue(new MessageDialogue("Finally, we need to set a Mutator. These will mix up the fun drastically!"),
                        new OptionsDialogue(
                                "Which Mutator would you like to enable, hit Random for a surprise!",
                                new Option("None", () -> { mutator = new EmptyMutator(); str_mutator = "None"; showDialogue(p, 7);}),
                                new Option("Static Gas Zone", () -> { mutator = new StaticGasMutator(); str_mutator = "Static Gas"; showDialogue(p, 7);}),
                                new Option("Vampiric Rites", () -> { mutator = new VampiricRitesMutator(); str_mutator = "Vampiric Rites"; showDialogue(p, 7);})
                        )
                );
                break;

            case 7:
                p.dialogue(new MessageDialogue("You selected: Runtime=" + str_runtime +", Team Size=" + str_teamSize + ", XP Rate=" + str_xpRate + ", Drop Rate=" + str_dropRate + ", Pet Rate=" + str_dropPetRate + ", Mutator=" + str_mutator),
                        new OptionsDialogue(
                                "Is this correct?",
                                new Option("Yes", () -> applyTournamentConfig(p, runtime, teamSize, xpRate, dropRate, dropPetRate, mutator)),
                                new Option("No - Restart", () -> showDialogue(p, 0)),
                                new Option("No - Cancel", () -> p.unlock())
                        ));
                break;
        }

    }

    private static void applyTournamentConfig(Player p, TournamentConfig.Timespan Runtime, TournamentConfig.TeamSize TeamSize, int XPRate, int DropRate, int PetDropRate, Mutator Mutator) {
        if (!Deadman.canOverrideConfig()) {
            showDialogue(p, 7);
            return;
        }

        p.getInventory().remove(ItemID.TOURNAMENT_TICKET, 1);
        Deadman.setNext_Config_Name(p.getName());
        Deadman.setNext_config(new CustomConfig(Runtime, TeamSize, XPRate, DropRate, PetDropRate, Mutator));
        p.unlock();

        Deadman.getNext_config().printConfig();
    }

    public static void process(Player p) {
        showDialogue(p, 0);
    }
}
