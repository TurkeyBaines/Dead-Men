package io.dm.deadman.content.areas.citadel;

import io.dm.cache.Color;
import io.dm.cache.ItemID;
import io.dm.deadman.Deadman;
import io.dm.deadman.content.areas.overworld.OverworldHelper;
import io.dm.deadman.content.items.TournamentTicket;
import io.dm.deadman.tournament.stages.Final;
import io.dm.deadman.tournament.stages.Lobby;
import io.dm.deadman.tournament.stages.Main;
import io.dm.deadman.tournament.team.Group;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.shared.LockType;
import io.dm.model.inter.dialogue.MessageDialogue;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.dialogue.YesNoDialogue;
import io.dm.model.inter.handlers.TournamentInformation;
import io.dm.model.inter.utils.Option;
import io.dm.model.item.Item;
import io.dm.model.map.Direction;
import io.dm.model.map.Position;
import io.dm.model.map.object.actions.ObjectAction;
import io.dm.model.skills.magic.SpellBook;

import java.util.ArrayList;

public class CitadelObjects {

    public static void register() {
        registerAltars();
        registerPortals();
        registerTournamentBoard();
    }

    private static void registerAltars() {
        ObjectAction.register(6552, 1, (p, o) -> {
            p.dialogue(
                    new OptionsDialogue(
                            "Pick a spellbook to swap to",
                            new Option("normal", () -> SpellBook.MODERN.setActive(p)),
                            new Option("ancients", () -> SpellBook.ANCIENT.setActive(p)),
                            new Option("lunar", () -> SpellBook.LUNAR.setActive(p)),
                            new Option("arceuus", () -> SpellBook.ARCEUUS.setActive(p))
                    )
            );
        });
    }

    private static void registerPortals() {
        ObjectAction.register(10251, 1, ((player, obj) -> {
            player.startEvent(e -> {
                player.lock(LockType.FULL);
                player.face(Direction.EAST);
                OverworldHelper.process(player);
                if (!player.getPosition().isWithinDistance(new Position(2963, 3346, 0)))
                    player.getMovement().teleport(2963, 3346, 0);
                else
                    player.getMovement().teleport(2540, 3872, 0);
                player.unlock();
            });
        }));

        ObjectAction.register(33181, "Menu", (p, o) -> sendTeleportMenu(p, 0));
    }

    private static void registerTournamentBoard() {
        ObjectAction.register(32658, "Read", (p, o) -> {
            TournamentInformation.send(p);
        });

        ObjectAction.register(32658, "Override", (p, o) -> {
            if (!p.getInventory().hasItem(ItemID.TOURNAMENT_TICKET, 1)) {
                TournamentTicket.process(p);
            }
        });

        ObjectAction.register(32658, "Group Settings", (p, o) -> {
            showGroupOptions(p);
        });
    }

    private static void showKickMenu(Player p) {
        if (p.getGroup().size() == 1)
            p.dialogue(new MessageDialogue("You are the only person in your group"));
        else if (p.getGroup().size() == 2)
            p.dialogue(new OptionsDialogue(
                    "Select a Member",
                    new Option(p.getGroup().getMemberStrings().get(1).toString(), () -> {
                        String name = p.getGroup().getMemberStrings().get(1).toString();
                        Group group = p.getGroup();
                        p.dialogue(
                                new YesNoDialogue("Kick: " + name, "Are you sure you want to kick " + Color.GREEN.wrap(name) + "?", new Item(7668, 1), () -> { group.leave(group.getMembers().get(1)); }),
                                new MessageDialogue("You have kicked " + Color.GREEN.wrap(name) + " from the group")
                        );
                    })
            ));
        else
            p.dialogue(new OptionsDialogue(
                    "Select a Member",
                    new Option(p.getGroup().getMemberStrings().get(1).toString(), () -> {
                        String name = p.getGroup().getMemberStrings().get(1).toString();
                        Group group = p.getGroup();
                        p.dialogue(
                                new YesNoDialogue("Kick: " + name, "Are you sure you want to kick " + Color.GREEN.wrap(name) + "?", new Item(7668, 1), () -> { group.leave(group.getMembers().get(1)); }),
                                new MessageDialogue("You have kicked " + Color.GREEN.wrap(name) + " from the group")

                        );
                    }),
                    new Option(p.getGroup().getMemberStrings().get(2).toString(), () -> {
                        String name = p.getGroup().getMemberStrings().get(2).toString();
                        Group group = p.getGroup();
                        p.dialogue(
                                new YesNoDialogue("Kick: " + name, "Are you sure you want to kick " + Color.GREEN.wrap(name) + "?", new Item(7668, 1), () -> { group.leave(group.getMembers().get(2)); }),
                                new MessageDialogue("You have kicked " + Color.GREEN.wrap(name) + " from the group")
                        );
                    })
            ));
    }


    private static void sendTeleportMenu(Player p, int page) {
        if (Deadman.getStage() instanceof Final) {
            p.dialogue(new MessageDialogue("The Teleporter is disabled during the finals."));
            return;
        }
        if (Deadman.getStage() instanceof Lobby) {
            p.dialogue(new MessageDialogue("You must remain in the Citadel/Overworld until the tournament begins."));
            return;
        }

        ArrayList<Option> initialOption = new ArrayList<>();
        ArrayList<Option> subOption = new ArrayList<>();
        switch (page) {
            case 0:
            //Initial        -> Page 1
            //City Hubs      -> Page 10
            //Dangerous Hubs -> Page 20 @ 25% runtime
            //Boss Hubs      -> Page 30 @ 50% runtime
                initialOption.add(new Option("Citadel", () -> p.getMovement().teleport(2968, 3338)));
                initialOption.add(new Option("City Hubs", () -> sendTeleportMenu(p, 10)));
                if (((Main) Deadman.getStage()).getRuntimePercentage() > 10)
                    initialOption.add(new Option("Dangerous Hubs", () -> sendTeleportMenu(p, 20)));

                if (((Main) Deadman.getStage()).getRuntimePercentage() > 50)
                    initialOption.add(new Option("Boss Hubs", () -> sendTeleportMenu(p, 30)));

                p.dialogue(new OptionsDialogue("Please select an option...", initialOption.toArray(new Option[0])));
            break;

            case 10:
                subOption.add(new Option("Varrock", () -> p.getMovement().teleport(3182, 3440)));
                subOption.add(new Option("Falador", () -> p.getMovement().teleport(3016, 3357)));
                subOption.add(new Option("Lumbridge", () -> p.getMovement().teleport(3224, 3226)));
                subOption.add(new Option("Tree Gnome Village", () -> p.getMovement().teleport(2729, 3492)));
                subOption.add(new Option("next...", () -> sendTeleportMenu(p, 11)));
                p.dialogue(new OptionsDialogue("Select a location... [1/2]", subOption.toArray(new Option[0])));
                break;

            case 11:
                subOption.add(new Option("Ardougne", () -> p.getMovement().teleport(2653, 3281)));
                subOption.add(new Option("Rellekka", () -> p.getMovement().teleport(2658, 3679)));
                subOption.add(new Option("Port Phasmatys", () -> p.getMovement().teleport(0, 0)));
                subOption.add(new Option("Yanille", () -> p.getMovement().teleport(0, 0)));
                subOption.add(new Option("back...", () -> sendTeleportMenu(p, 10)));
                p.dialogue(new OptionsDialogue("Select a location... [2/2]", subOption.toArray(new Option[0])));
                break;


            case 20:
                subOption.add(new Option("Ferox Enclave", () -> p.getMovement().teleport(0, 0)));
                subOption.add(new Option("Al-Kharid", () -> p.getMovement().teleport(0, 0)));
                subOption.add(new Option("Burthorpe", () -> p.getMovement().teleport(0, 0)));
                subOption.add(new Option("Brimhaven", () -> p.getMovement().teleport(0, 0)));
                subOption.add(new Option("next...", () -> sendTeleportMenu(p, 21)));
                break;

            case 21:
                subOption.add(new Option("Canifis", () -> p.getMovement().teleport(0, 0)));
                subOption.add(new Option("Canifis", () -> p.getMovement().teleport(0, 0)));
                subOption.add(new Option("Nardah", () -> p.getMovement().teleport(0, 0)));
                subOption.add(new Option("back...", () -> sendTeleportMenu(p, 20)));
                break;

            case 30:
                subOption.add(new Option("Barrows (500k fee)", () -> p.getMovement().teleport(0, 0)));
                subOption.add(new Option("Kraken (500k fee)", () -> p.getMovement().teleport(0, 0)));
                subOption.add(new Option("Godwars (1M fee)", () -> p.getMovement().teleport(0, 0)));
                subOption.add(new Option("Dagannoth Kings (1M fee)", () -> p.getMovement().teleport(0, 0)));
                subOption.add(new Option("next...", () -> sendTeleportMenu(p, 31)));
                break;

            case 31:
                subOption.add(new Option("Zulrah (2M fee)", () -> p.getMovement().teleport(0, 0)));
                subOption.add(new Option("Cerberus (2M fee)", () -> p.getMovement().teleport(0, 0)));
                subOption.add(new Option("Calvar'ion (2M fee)", () -> p.getMovement().teleport(0, 0)));
                subOption.add(new Option("Spindel (2M fee)", () -> p.getMovement().teleport(0, 0)));
                subOption.add(new Option("Artio (2M fee)", () -> p.getMovement().teleport(0, 0)));
                break;
        }
        initialOption.clear(); initialOption = null;
        subOption.clear(); subOption = null;
    }


    private static void showGroupOptions(Player p) {
        if (p.groupID == null) {
            p.dialogue(new OptionsDialogue(
                    "Select an option",
                    new Option("Start a Group", () -> { p.stringInput("Enter a name", (name) -> { Deadman.getGroups().newTeam(p, name); }); }),
                    new Option("Join a Group", () -> { p.stringInput("Enter Group ID", (id) -> { Deadman.getGroups().Request(p, id); }); })
            ));
        } else if (!p.getGroup().isOwner(p)) {
            p.dialogue(new OptionsDialogue(
                    new Option("Leave Group", () -> p.dialogue(
                            new YesNoDialogue("Leave Group", "Are you sure you want to leave?", null, () -> p.getGroup().leave(p))
                    ))
            ));
        } else {
            p.dialogue(new OptionsDialogue(
                    "Select an option",
                    new Option("View Group ID", () -> p.dialogue(new MessageDialogue("Group ID: " + Color.DARK_RED.wrap(p.groupID)))),
                    new Option("Kick Members", CitadelObjects::showKickMenu),
                    new Option("Leave Group", () -> p.dialogue(
                            new YesNoDialogue("Leave Group", "Are you sure you want to leave?", null, () -> p.getGroup().leave(p))
                    ))
            ));
        }
    }

}
