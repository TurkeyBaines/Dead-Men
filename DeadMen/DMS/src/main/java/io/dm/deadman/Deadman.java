package io.dm.deadman;

import io.dm.cache.Color;
import io.dm.deadman.areas.Citadel;
import io.dm.deadman.areas.overworld.Overworld;
import io.dm.deadman.areas.MultiZone;
import io.dm.deadman.areas.SafeZone;
import io.dm.deadman.sigils.Sigil;
import io.dm.deadman.sigils.Sigils;
import io.dm.deadman.tournament.Stage;
import io.dm.deadman.tournament.Tournament;
import io.dm.deadman.tournament.TournamentConfig;
import io.dm.deadman.tournament.stages.Lobby;
import io.dm.deadman.tournament.stages.Main;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.shared.listeners.LoginListener;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.utils.Option;
import io.dm.model.item.Item;
import io.dm.model.item.ItemContainer;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

public class Deadman {

    private long currentTime;

    @Getter private static Citadel citadel;
    @Getter private static MultiZone multiZone;
    @Getter private static SafeZone safeZone;
    @Getter private static Overworld overworld;

    @Getter @Setter private static Stage stage;
    @Getter @Setter private static TournamentConfig config;


    public void update() {
        currentTime = System.currentTimeMillis();

        stage.onUpdate();
    }

    public Deadman() {
        currentTime = System.currentTimeMillis();

        citadel = new Citadel();
        multiZone = new MultiZone();
        safeZone = new SafeZone();
        overworld = new Overworld();

        config = TournamentConfig.getRandom();
        stage = new Lobby();
        stage.onLoad();

        LoginListener.register(player -> {
            if (stage.stageName() == Tournament.StageName.LOBBY) {
                player.sendMessage(Color.GOLD.wrap("[Tournament] ") + "A new tournament is due to start shortly.");

                teleToCitadel(player);

                if (player.dmmNeedsReset) {
                    resetPlayer(player);

                    player.sendMessage("Your Stats, Equipment, Bank, and Items have been reset from a previous tournament!");
                    player.dmmNeedsReset = false;
                }

            } else if (stage.stageName() == Tournament.StageName.MAIN) {
                player.sendMessage(Color.GOLD.wrap("[Tournament] ") + "A Tournament is underway!");
                if (!Sigil.playerUnlocked(player, Sigils.Ruthless_Ranger) && !Sigil.playerUnlocked(player, Sigils.Menacing_Mage) && !Sigil.playerUnlocked(player, Sigils.Formidable_Fighter)) {
                    player.dialogue(new OptionsDialogue(
                            "Please select an starting Sigil to unlock",
                            new Option("Sigil of the Ruthless Ranger", () -> { Sigil.unlock(player, Sigils.Ruthless_Ranger); }),
                            new Option("Sigil of the Formidable Fighter", () -> { Sigil.unlock(player, Sigils.Formidable_Fighter); }),
                            new Option("Sigil of the Menacing Mage", () -> { Sigil.unlock(player, Sigils.Menacing_Mage); })
                    ));
                    player.dmmNeedsReset = true;
                }
            } else if (stage.stageName() == Tournament.StageName.FINAL) {
                player.sendMessage(Color.RED.wrap("A Tournament Finals is underway! You can spectate it using the orb, or visit the Overworld while you wait."));
                teleToCitadel(player);
                player.dmmNeedsReset = true;
            }
        });
    }

    private void resetPlayer(Player p) {
        if (overworld.contains(p)) {
            p.skillHolder = new int[][] {
                    {1, 0},
                    {1, 0},
                    {1, 0},
                    {10, 1154},
                    {1, 0},
                    {1, 0},
                    {1, 0},
                    {1, 0},
                    {1, 0},
                    {1, 0},
                    {1, 0},
                    {1, 0},
                    {1, 0},
                    {1, 0},
                    {1, 0},
                    {1, 0},
                    {1, 0},
                    {1, 0},
                    {1, 0},
                    {1, 0},
                    {1, 0},
                    {1, 0},
                    {1, 0}
            };
            p.inventoryHolder = new ItemContainer();
            p.equipmentHolder = new Item[]{
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
            };
        } else {
            p.getInventory().clear();
            p.getInventory().sendUpdates();
            p.getEquipment().clear();
            p.getEquipment().sendUpdates();
            p.getBank().clear();
            p.getBank().sendUpdates();
            p.getStats().reset();
        }
    }

    private void teleToCitadel(Player p) {
        if (!getCitadel().contains(p) && !getOverworld().contains(p)) {
            p.getMovement().teleport(2970, 3343, 0);
        }
    }

    public static String timeUntilChange() {
        long millis = 0;
        if (stage.stageName() == Tournament.StageName.LOBBY) {
            Lobby lobby = (Lobby) stage;
            millis = (lobby.startTime + lobby.duration) - System.currentTimeMillis();
        } else if (stage.stageName() == Tournament.StageName.MAIN) {
            Main main = (Main) stage;
            millis = (main.startTime + main.duration) - System.currentTimeMillis();
        }

        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static String timeUntilEvent() {
        Main main = (Main) stage;
        long millis = main.nextEvent - System.currentTimeMillis();

        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

}
