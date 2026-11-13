package io.dm.deadman.content.areas.lobby;

import io.dm.deadman.Deadman;
import io.dm.deadman.tournament.stages.Final;
import io.dm.deadman.tournament.stages.Lobby;
import io.dm.deadman.tournament.stages.Main;
import io.dm.model.World;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.npc.NPCAction;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.shared.LockType;
import io.dm.model.inter.InterfaceType;
import io.dm.model.inter.dialogue.MessageDialogue;
import io.dm.model.inter.dialogue.NPCDialogue;
import io.dm.model.inter.dialogue.PlayerDialogue;
import io.dm.model.map.Bounds;
import io.dm.model.map.Direction;
import io.dm.model.map.Position;
import io.dm.model.map.Tile;
import io.dm.model.map.object.GameObject;
import io.dm.model.map.object.actions.ObjectAction;

import java.util.List;

public class Purgatory {

    private List<Player> players;

    private Bounds bounds = new Bounds(3776, 9728, 1, 64);
    private Bounds spawns = new Bounds(3802, 9754, 1, 13);
    private Position centre = new Position(3808, 9759, 1);

    private GameObject portal;
    private final  int RED_PORTAL_ID = 31947;
    private final  int WHITE_PORTAL_ID = 31946;

    private NPC sam;
    private NPC death;

    public Purgatory() {
        registerPortal();

        registerSam();
    }

    public boolean contains(Player player) {
        return bounds.inBounds(player.getPosition());
    }

    private void registerPortal() {
        ObjectAction.register(31947, "Enter", (p, o) -> {
            if (!(Deadman.getStage() instanceof Main)) {
                Lobby lobby = (Lobby) Deadman.getStage();
                p.dialogue(
                        new NPCDialogue(9855, "You must wait until i tell you to start.. There will be time for death, don't worry!"),
                        new MessageDialogue("The next tournament begins in " + lobby.formatTimeLeft(lobby.startTime, lobby.duration))
                );
            }

            p.startEvent((e) -> {
                p.lock(LockType.MOVEMENT);
                p.getMovement().forceWalkTo(centre);
                e.delay(5);
                p.animate(11876);
                e.delay(2);
                p.animate(11877);
                e.delay(4);
                p.getMovement().teleport(Deadman.getCitadel().getRandomSpawnPoint());
                p.animate(11274);
                e.delay(8);
                p.unlock();
            });
        });
        portal = Tile.getObject(31947, 3806, 9757, 1);
        portal.walkTo = new Position(3808, 9759, 1);

        World.startEvent(event -> {
            while (true) {

                if (portal == null)
                    return;

                if (portal.id == WHITE_PORTAL_ID) {
                    if (Deadman.getStage() instanceof Main) {
                        portal.remove();
                        Position pos = portal.getPosition();
                        portal = new GameObject(RED_PORTAL_ID, pos.getX(), pos.getY(), pos.getZ(), 10, 0);
                        portal.spawn();
                    }
                }

                if (portal.id == RED_PORTAL_ID) {
                    if (Deadman.getStage() instanceof io.dm.deadman.tournament.stages.Lobby || Deadman.getStage() instanceof Final) {
                        portal.remove();
                        Position pos = portal.getPosition();
                        portal = new GameObject(WHITE_PORTAL_ID, pos.getX(), pos.getY(), pos.getZ(), 10, 0);
                        portal.spawn();
                    }
                }

                event.delay(5);
            }
        });
    }

    private void registerSam() {
        sam = new NPC(14172);
        sam.spawn(3799, 9757, 1, 15);

        NPCAction.register(14172, "Talk-to", (p, n) -> {
            p.dialogue(
                    new NPCDialogue(14172, "Rocks... Rocks for sale. Get your Magical Rocks!"),
                    new PlayerDialogue("What on earth are you talking about?"),
                    new NPCDialogue(14172, "These rocks are full of power! They're all shiny and glowing, energy galore. I'll show ye!"),
                    new MessageDialogue("It appears Sam is under some form of magical spell")
            );
        });

        NPCAction.register(14172, "Check Points", (p, n) -> {
            p.dialogue(
                    new NPCDialogue(14172, "You have " + p.getDeadMenPoints() + " points")
            );
        });

        NPCAction.register(14172, "Exchange", (p, n) -> {
            p.openInterface(InterfaceType.MAIN, 912);
        });
    }

    private void registerDeath() {

    }

}
