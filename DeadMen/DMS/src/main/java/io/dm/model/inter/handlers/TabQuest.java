package io.dm.model.inter.handlers;

import io.dm.Server;
import io.dm.api.utils.TimeUtils;
import io.dm.cache.Color;
import io.dm.deadman.Deadman;
import io.dm.deadman.tournament.Tournament;
import io.dm.deadman.tournament.stages.Main;
import io.dm.model.World;
import io.dm.model.activities.pvp.PVPInstance;
import io.dm.model.activities.wilderness.Wilderness;
import io.dm.model.entity.player.DoubleDrops;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.shared.listeners.LoginListener;
import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceAction;
import io.dm.model.inter.InterfaceHandler;
import io.dm.model.inter.InterfaceType;
import io.dm.model.inter.actions.SimpleAction;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * Handles the notification board in the quest tab.
 *
 * @author ReverendDread on 5/17/2020
 * https://www.rune-server.ee/members/reverenddread/
 * @project Kronos
 */
public class TabQuest {

    @Getter
    private enum NoticeboardComponent {

        COMPONENT_8(8, player -> "Players Online: " + Color.GREEN.wrap(String.valueOf(World.players.count()))),
        COMPONENT_10(9, player -> "Staff Online: " + getStaffOnlineCount(), (SimpleAction) p -> send(p)),
        COMPONENT_9(10, player -> "View Tournament Info ", (SimpleAction) p -> sendTournamentInfo(p)),
        COMPONENT_11(11, player -> "Players in Tournament: " + Color.GREEN.wrap(String.valueOf(PVPInstance.players.size()))),
        COMPONENT_12(12, player -> "Server Uptime: " + Color.GREEN.wrap(TimeUtils.fromMs(Server.currentTick() * Server.tickMs(), false))),
        COMPONENT_43(43, player -> "XP Bonus: " + Color.GREEN.wrap(String.valueOf(Deadman.getConfig().XP_RATE))),
        COMPONENT_44(44, player -> "Double Drops: " + Color.GREEN.wrap(getDoubleDrops())),
        COMPONENT_45(45, player -> "Double PK Points: " + Color.GREEN.wrap(getDoublePkp())),
        COMPONENT_46(46, player -> "Double Slayer Points: " + Color.GREEN.wrap(getDoubleSlayerPoints())),
        COMPONENT_47(47, player -> "Double Pest Control: " + Color.GREEN.wrap(getDoublePcPoints())),
        COMPONENT_14(14, player -> {
            boolean hasTwoFactor = player.tfa;
            String text = "Two-factor authentication";
            return hasTwoFactor ? Color.GREEN.wrap(text) : Color.RED.wrap(text);
        }, (SimpleAction) player -> player.openUrl("https://community.kronos.rip/index.php?account/security")), //need to hookup tfa to the website somehow
        COMPONENT_15(15, player -> "Time Played: " + Color.GREEN.wrap(TimeUtils.fromMs(player.playTime * Server.tickMs(), false))),
        COMPONENT_16(16, player -> "Total Spent: " + Color.GREEN.wrap( "$" + player.storeAmountSpent)),
        COMPONENT_17(17, player -> "Base XP: "),
        COMPONENT_18(18, player -> "Double Drop Chance: " + Color.GREEN.wrap(DoubleDrops.getChance(player) + "%")),
        COMPONENT_49(49, player -> "PVM Points: " + Color.GREEN.wrap(Integer.toString(player.PvmPoints))),

        COMPONENT_50(50, player -> "Achievements", (SimpleAction) player -> send(player)),
        COMPONENT_51(51, player -> "Drop Tables", (SimpleAction) player -> send(player)),
        COMPONENT_52(52, player -> "Settings", (SimpleAction) player -> send(player)),

        COMPONENT_19(19, player -> "Website", (SimpleAction) player -> player.openUrl("http://kronos.rip/")),
        COMPONENT_20(20, player -> "Community", (SimpleAction) player -> player.openUrl("https://community.kronos.rip/index.php")),
        COMPONENT_21(21, player -> "Discord", (SimpleAction) player -> player.openUrl("https://discord.com/invite/ZyWAmpS")),
        COMPONENT_22(22, player -> "Store", (SimpleAction) player -> player.openUrl("http://kronos.rip/store"));

        private int componentId;
        private TextField text;
        private InterfaceAction action;

        //use for blank components
        NoticeboardComponent(int componentId) {
            this(componentId, player -> "", null);
        }

        //use for components without a click option
        NoticeboardComponent(int componentId, TextField text) {
            this(componentId, text, null);
        }

        //use for components with a click option
        NoticeboardComponent(int componentId, TextField text, InterfaceAction action) {
            this.componentId = componentId;
            this.text = text;
            this.action = action;
        }
    }

    /**
     * Sends all the basic text for the noticeboard components
     * @param p - The player.
     */
    public static void send(Player p) {
        for (NoticeboardComponent component : NoticeboardComponent.values()) {
            p.getPacketSender().sendString(Interface.NOTICEBOARD, component.getComponentId(), component.getText().send(p));
        }
        p.getPacketSender().sendString(116, 7, Color.ORANGE.wrap("\t Current Tournament Stage: ") + Color.BLUE.wrap(Deadman.getStage().stageName().name()));
        p.getPacketSender().sendString(116, 8, Color.ORANGE.wrap("\t Time Until Next Stage: ") + Color.BLUE.wrap(Deadman.timeUntilChange()));
        if (Deadman.getStage().stageName() == Tournament.StageName.MAIN)
            p.getPacketSender().sendString(116, 20, Color.ORANGE.wrap("\t # Time Until Next Event: ") + Color.BLUE.wrap(Deadman.timeUntilEvent()));
    }

    private static void sendTournamentInfo(Player p) {
        int interId = 116;
        p.openInterface(InterfaceType.MAIN, interId);
        p.getPacketSender().sendString(interId, 4, "Tournament Information Board");
        p.getPacketSender().sendString(interId, 6, Color.DARK_RED.wrap("-- Server Info --"));
        p.getPacketSender().sendString(interId, 7, Color.ORANGE.wrap("\t Current Tournament Stage: ") + Color.BLUE.wrap(Deadman.getStage().stageName().name()));
        p.getPacketSender().sendString(interId, 8, Color.ORANGE.wrap("\t Time Until Next Stage: ") + Color.BLUE.wrap(Deadman.timeUntilChange()));

        switch (Deadman.getStage().stageName()) {
            case LOBBY:
                p.getPacketSender().sendString(interId, 10, Color.DARK_RED.wrap("--- Lobby Info ---"));
                p.getPacketSender().sendString(interId, 11, Color.DARK_RED.wrap("- Next Tournament -"));
                p.getPacketSender().sendString(interId, 12, Color.ORANGE.wrap("\t # Length: ") + Color.BLUE.wrap(Deadman.getConfig().GAME_LENGTH.text));
                p.getPacketSender().sendString(interId, 13, Color.ORANGE.wrap("\t # XP Rate: ") + Color.BLUE.wrap("" + Deadman.getConfig().XP_RATE));
                p.getPacketSender().sendString(interId, 14, Color.ORANGE.wrap("\t # Drop Rate: ") + Color.BLUE.wrap("" + Deadman.getConfig().DROP_RATE));
                p.getPacketSender().sendString(interId, 15, Color.ORANGE.wrap("\t # Pet Rate: ") + Color.BLUE.wrap("" + Deadman.getConfig().PET_RATE));
                p.getPacketSender().sendString(interId, 16, Color.ORANGE.wrap("\t # Team Size: ") + Color.BLUE.wrap("" + Deadman.getConfig().TEAM_SIZE_MAX.asInt));



                p.getPacketSender().sendString(interId, 18,     Color.DARK_RED.wrap("- Mutator Info -"));
                p.getPacketSender().sendString(interId, 19, Color.DARK_GREEN.wrap(  "----------------------------------------------"));
                p.getPacketSender().sendString(interId, 20, getCenteredMenuString("Vampiric Rites", Color.DARK_RED));
                p.getPacketSender().sendString(interId, 21, getCenteredMenuString("No natural HP regen, heal by", Color.ORANGE));
                p.getPacketSender().sendString(interId, 22, getCenteredMenuString("dealing damage or with food.", Color.ORANGE));
                p.getPacketSender().sendString(interId, 23, Color.DARK_GREEN.wrap(  "----------------------------------------------"));
                p.getPacketSender().sendString(interId, 24, Color.DARK_GREEN.wrap(  "----------------------------------------------"));
                p.getPacketSender().sendString(interId, 25, getCenteredMenuString("Static Gas", Color.DARK_RED));
                p.getPacketSender().sendString(interId, 26, getCenteredMenuString("Gas will prevent access to", Color.ORANGE));
                p.getPacketSender().sendString(interId, 27, getCenteredMenuString("areas of the game for the", Color.ORANGE));
                p.getPacketSender().sendString(interId, 28, getCenteredMenuString("duration of the tournament", Color.ORANGE));
                p.getPacketSender().sendString(interId, 29, Color.DARK_GREEN.wrap(  "----------------------------------------------"));

                p.getPacketSender().sendString(interId, 31, "Beginner Sigils are now available for purchase");
                p.getPacketSender().sendString(interId, 32, "in the Citadel, and in the Overworld!");
                break;

            case MAIN:
                p.getPacketSender().sendString(interId, 10,     Color.DARK_RED.wrap("--- Tournament Info ---"));
                p.getPacketSender().sendString(interId, 11,     Color.DARK_RED.wrap("- Current Tournament -"));
                p.getPacketSender().sendString(interId, 12, Color.ORANGE.wrap("\t # Length: ") + Color.BLUE.wrap(Deadman.getConfig().GAME_LENGTH.text));
                p.getPacketSender().sendString(interId, 13, Color.ORANGE.wrap("\t # XP Rate: ") + Color.BLUE.wrap("" + Deadman.getConfig().XP_RATE));
                p.getPacketSender().sendString(interId, 14, Color.ORANGE.wrap("\t # Drop Rate: ") + Color.BLUE.wrap("" + Deadman.getConfig().DROP_RATE));
                p.getPacketSender().sendString(interId, 15, Color.ORANGE.wrap("\t # Pet Rate: ") + Color.BLUE.wrap("" + Deadman.getConfig().PET_RATE));
                p.getPacketSender().sendString(interId, 16, Color.ORANGE.wrap("\t # Team Size: ") + Color.BLUE.wrap("" + Deadman.getConfig().TEAM_SIZE_MAX.asInt));

                p.getPacketSender().sendString(interId, 18,     Color.DARK_RED.wrap("- Event Info -"));
                p.getPacketSender().sendString(interId, 19, Color.ORANGE.wrap("\t # Last Event Type: ") + Color.BLUE.wrap("/TODO"));
                p.getPacketSender().sendString(interId, 20, Color.ORANGE.wrap("\t # Time Until Next Event: ") + Color.BLUE.wrap(Deadman.timeUntilEvent()));
                p.getPacketSender().sendString(interId, 21, Color.ORANGE.wrap("\t # Next Event Tier: ") + Color.BLUE.wrap("/TODO"));

                p.getPacketSender().sendString(interId, 23,     Color.DARK_RED.wrap("- Mutator Info -"));
                p.getPacketSender().sendString(interId, 24, Color.DARK_GREEN.wrap(  "----------------------------------------------"));
                p.getPacketSender().sendString(interId, 25, getCenteredMenuString("Vampiric Rites", Color.DARK_RED));
                p.getPacketSender().sendString(interId, 26, getCenteredMenuString("No natural HP regen, heal by", Color.ORANGE));
                p.getPacketSender().sendString(interId, 27, getCenteredMenuString("dealing damage or with food.", Color.ORANGE));
                p.getPacketSender().sendString(interId, 28, Color.DARK_GREEN.wrap(  "----------------------------------------------"));
                p.getPacketSender().sendString(interId, 29, Color.DARK_GREEN.wrap(  "----------------------------------------------"));
                p.getPacketSender().sendString(interId, 30, getCenteredMenuString("Static Gas", Color.DARK_RED));
                p.getPacketSender().sendString(interId, 31, getCenteredMenuString("Gas will prevent access to", Color.ORANGE));
                p.getPacketSender().sendString(interId, 32, getCenteredMenuString("areas of the game for the", Color.ORANGE));
                p.getPacketSender().sendString(interId, 33, getCenteredMenuString("duration of the tournament", Color.ORANGE));
                p.getPacketSender().sendString(interId, 34, Color.DARK_GREEN.wrap(  "----------------------------------------------"));

                break;

            case FINAL:
                break;
        }
    }

    public static String getCenteredMenuString(String text, Color middleColour) {
        int contentWidth = 36 - 2; // Subtracting 2 for the "|" characters
        int spacesNeeded = contentWidth - text.length();

        // Ensure we don't have negative spaces if the text is too long
        int leftPadding = Math.max(0, spacesNeeded / 2);
        int rightPadding = Math.max(0, spacesNeeded - leftPadding);

        String leftSpaces = " ".repeat(leftPadding);
        String rightSpaces = " ".repeat(rightPadding);

        return Color.DARK_GREEN.wrap("|" + leftSpaces) +
                middleColour.wrap(text) +
                Color.DARK_GREEN.wrap(rightSpaces + "|");
    }

    private static String getDoubleDrops() {
        if (World.doubleDrops) {
            return Color.GREEN.wrap("Enabled!");
        } else {
            return Color.RED.wrap("Disabled");
        }
    }
    private static String getDoublePkp() {
        if (World.doublePkp) {
            return Color.GREEN.wrap("Enabled!");
        } else {
            return Color.RED.wrap("Disabled");
        }
    }
    private static String getDoubleSlayerPoints() {
        if (World.doubleSlayer) {
            return Color.GREEN.wrap("Enabled!");
        } else {
            return Color.RED.wrap("Disabled");
        }
    }
    private static String getDoublePcPoints() {
        if (World.doublePest) {
            return Color.GREEN.wrap("Enabled!");
        } else {
            return Color.RED.wrap("Disabled");
        }
    }

    private static int getStaffOnlineCount() {
        List<Player> staffList = World.getPlayerStream().filter(Player::isStaff).collect(Collectors.toList());
        return staffList.size();
    }

    static {
        InterfaceHandler.register(Interface.NOTICEBOARD, (h) -> {
            for (NoticeboardComponent component : NoticeboardComponent.values()) {
                h.actions[component.getComponentId()] = component.getAction();
            }
        });
        LoginListener.register(player -> {
            send(player);
            player.addEvent(event -> {
                while(true) {
                    send(player);
                    event.delay(2);
                }
            });
        });
    }

    private interface TextField {
        String send(Player player);
    }

}
