package io.dm.deadman;

import io.dm.cache.Color;
import io.dm.deadman.content.areas.citadel.Citadel;
import io.dm.deadman.content.areas.MultiZone;
import io.dm.deadman.content.areas.SafeZone;
import io.dm.deadman.content.areas.lobby.Purgatory;
import io.dm.deadman.tournament.Stage;
import io.dm.deadman.tournament.Tournament;
import io.dm.deadman.tournament.TournamentConfig;
import io.dm.deadman.tournament.stages.Lobby;
import io.dm.deadman.tournament.stages.Main;
import io.dm.deadman.tournament.team.Groups;
import io.dm.model.World;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.shared.listeners.LoginListener;
import io.dm.model.stat.StatType;
import lombok.Getter;
import lombok.Setter;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Deadman {


    @Getter private static Citadel citadel;
    @Getter private static Purgatory purgatory;
    @Getter private static MultiZone multiZone;
    @Getter private static SafeZone safeZone;

    @Getter @Setter private static String utc;
    @Getter @Setter private static Stage stage;
    @Getter @Setter private static TournamentConfig config;
    @Getter @Setter private static TournamentConfig next_config;
    @Getter @Setter private static String next_Config_Name;

    @Getter private static Groups groups;

    static {
        stage = new Lobby();
        stage.onLoad();
        config = TournamentConfig.getRandom();

        citadel = new Citadel();
        purgatory = new Purgatory();
        multiZone = new MultiZone();
        safeZone = new SafeZone();

        groups = new Groups();

        World.startEvent(e -> {
            while (true) {
                update();
                e.delay(3);
            }

        });
    }

    private static void update() {
        stage.onUpdate();
    }

    public Deadman() {
        generateUTC();
        LoginListener.register(player -> {
            if (stage.stageName() == Tournament.StageName.LOBBY) {
                player.sendMessage(Color.GOLD.wrap("[Tournament] ") + "A new tournament is due to start shortly.");
                teleToLobby(player);

                if (!player.utc.equals(utc)) {
                    resetPlayer(player);

                    player.sendMessage("Your Stats, Equipment, Bank, and Items have been reset from a previous tournament!");
                    player.utc = utc;
                }

            } else if (stage.stageName() == Tournament.StageName.MAIN) {
                player.sendMessage(Color.GOLD.wrap("[Tournament] ") + "A Tournament is underway!");
                if (!player.utc.equals(utc)) {
                    resetPlayer(player);

                    player.sendMessage("Your Stats, Equipment, Bank, and Items have been reset from a previous tournament!");
                }
                player.utc = getUtc();
            } else if (stage.stageName() == Tournament.StageName.FINAL) {
                player.sendMessage(Color.RED.wrap("The Tournament Finals are underway! You can spectate it using the orb, or visit the Overworld while you wait."));
                teleToLobby(player);
                resetPlayer(player);
                player.utc = getUtc();
            }
            getConfig().printConfig();
        });
    }

    private void resetPlayer(Player p) {
            p.getInventory().clear();
            p.getInventory().sendUpdates();
            p.getEquipment().clear();
            p.getEquipment().sendUpdates();
            p.getBank().clear();
            p.getBank().sendUpdates();
            for (StatType stat : StatType.values()) {
                if (stat == StatType.Hitpoints) {
                    p.getStats().get(stat).currentLevel = 10;
                    p.getStats().get(stat).fixedLevel = 10;
                    p.getStats().get(stat).experience = 1154;
                    p.getStats().get(stat).updated = true;
                    continue;
                }

                p.getStats().get(stat).resetTo1();
            }
    }

    private void teleToLobby(Player p) {
        if (!getCitadel().contains(p)) {
            p.getMovement().teleport(3808, 9754, 1);
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

    public static boolean canOverrideConfig() {
        return next_config == null;
    }

    public static void overrideConfig(TournamentConfig config) {
        next_config = config;
    }

    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "abcdefghijklmnopqrstuvwxyz" +
            "0123456789";

    public static void generateUTC() {
        SecureRandom RANDOM = new SecureRandom();

        setUtc(RANDOM.ints(20, 0, CHAR_POOL.length())
                .mapToObj(CHAR_POOL::charAt)
                .map(Object::toString)
                .collect(Collectors.joining()));
    }

}
