package io.dm.model.inter.handlers;

import io.dm.Server;
import io.dm.api.utils.TimeUtils;
import io.dm.deadman.Deadman;
import io.dm.deadman.content.areas.overworld.OverworldTools;
import io.dm.deadman.content.areas.overworld.combat.CombatTask;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.deadman.tournament.Tournament;
import io.dm.model.World;
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

import static io.dm.cache.Color.*;

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
    private enum Entries {

        SERVER_UPTIME(8, player -> "Server Uptime: " + GREEN.wrap(TimeUtils.fromMs(Server.currentTick() * Server.tickMs(), false))),
        PLAYERS_ONLINE(9, player -> "- Players Online: " + GREEN.wrap(String.valueOf(World.players.count())), (SimpleAction) TabQuest::sendPlayerList),
        STAFF_ONLINE(10, player -> "- Staff Online: " + GREEN.wrap(String.valueOf(getStaffOnlineCount())), (SimpleAction) TabQuest::sendStaffList),
        TOURNAMENT_INFO(11, player -> RED.wrap("----  View Tournament Info  ----"), (SimpleAction) TournamentInformation::send),
        OVERWORLD_POINTS(12, player -> "Overworld Points: " + GREEN.wrap(String.valueOf(player.getOverworldPoints()))),
        GAP_(43, player -> ""), // <-- Leave a gap?
        TOURNAMENT_STATE(43, player -> "Stage: " + GREEN.wrap(Deadman.getStage().stageName().text)), // <-- Leave a gap?
        TOURNAMENT_COUNTDOWN(44, player -> "Next Stage: " + GREEN.wrap(Deadman.timeUntilChange())),
        TOURNAMENT_EVENT_COUNTDOWN(45, player -> "Next Event: " + GREEN.wrap(Deadman.getStage().stageName() != Tournament.StageName.MAIN ? "None" : Deadman.timeUntilEvent())),
        TOURNAMENT_MUTATOR(46, player -> "Mutator: " + GREEN.wrap(Deadman.getConfig().MUTATOR.name())),

        HEADER_OVERWORLD(13, player -> "Overworld Player Info"),
        HEADER_TOURNAMENT(13, player -> "Tournament Player Info"),

        OVERWORLD_AXE_RANK(14, player -> BRONZE.wrap("Axe Grade: ") + GREEN.wrap(player.overworldToolTier[OverworldTools.Tool.AXE.id].name())),
        OVERWORLD_PICKAXE_RANK(15, player -> BRONZE.wrap("Pickaxe Grade: ") + GREEN.wrap(player.overworldToolTier[OverworldTools.Tool.PICKAXE.id].name())),
        OVERWORLD_FISH_TOOL_RANK(16, player -> BRONZE.wrap("Fishing Tool Grade: ") + GREEN.wrap(player.overworldToolTier[OverworldTools.Tool.FISHING.id].name())),
        OVERWORLD_TASK(17, player -> BRONZE.wrap("Combat Task: ") + (player.overworldTaskMonster == CombatTask.TASK_MONSTER.NONE ? RED.wrap("None") : GREEN.wrap(player.overworldTaskRemaining + "x" + player.overworldTaskMonster.name))),
        OVERWORLD_TASK_DIFF(18, player -> BRONZE.wrap("Difficulty: ") + (player.overworldTaskMonster == CombatTask.TASK_MONSTER.NONE ? RED.wrap("None") : GREEN.wrap(player.str_overworldTaskDifficulty))),
        OVERWORLD_TASK_VAULT(49, player -> BRONZE.wrap("Vault Points: ") + (player.overworldPointsVault == 0 ? RED.wrap(String.valueOf(player.overworldPointsVault)) : GREEN.wrap(String.valueOf(player.overworldPointsVault)))),

        TOURNAMENT_KILLS(14, player -> BRONZE.wrap("Kills: ") + GREEN.wrap("0")),
        TOURNAMENT_DEATHS(15, player -> BRONZE.wrap("Deaths: ") + GREEN.wrap("0")),
        TOURNAMENT_POINTS(16, player -> ""),
        GAP__(17, player -> ""),
        GAP___(18, player -> ""),
        GAP____(49, player -> ""),

        SIGIL_HEADER(48, player -> "Sigils"),
        SIGIL_1_HOLDER(50, player -> CYAN.wrap(player.activeSigils[0] == -1 ? "-- Empty Slot" : "" + Sigils.values()[0].name().replace("_", " "))),
        SIGIL_2_HOLDER(51, player -> CYAN.wrap(player.activeSigils[1] == -1 ? "-- Empty Slot" : "" + player.activeSigils[1])),
        SIGIL_3_HOLDER(52, player -> CYAN.wrap(player.activeSigils[2] == -1 ? "-- Empty Slot" : "" + player.activeSigils[2]));

        private int componentId;
        private TextField text;
        private InterfaceAction action;

        //use for components without a click option
        Entries(int componentId, TextField text) {
            this(componentId, text, null);
        }

        //use for components with a click option
        Entries(int componentId, TextField text, InterfaceAction action) {
            this.componentId = componentId;
            this.text = text;
            this.action = action;
        }

        public void send(Player p) {
            p.getPacketSender().sendString(Interface.NOTICEBOARD, getComponentId(), getText().send(p));
        }
    }

    /**
     * Sends all the basic text for the noticeboard components
     * @param p - The player.
     */
    public static void send(Player p) {
        for (Entries component : Entries.values()) {
            p.getPacketSender().sendString(Interface.NOTICEBOARD, component.getComponentId(), component.getText().send(p));
        }

        Entries.SERVER_UPTIME.send(p);
        Entries.PLAYERS_ONLINE.send(p);
        Entries.STAFF_ONLINE.send(p);
        Entries.TOURNAMENT_INFO.send(p);
        Entries.OVERWORLD_POINTS.send(p);
        Entries.GAP_.send(p);
        Entries.TOURNAMENT_STATE.send(p);
        Entries.TOURNAMENT_COUNTDOWN.send(p);
        Entries.TOURNAMENT_EVENT_COUNTDOWN.send(p);

        if (Deadman.getOverworld().contains(p)) {
            Entries.HEADER_OVERWORLD.send(p);
            Entries.OVERWORLD_AXE_RANK.send(p);
            Entries.OVERWORLD_PICKAXE_RANK.send(p);
            Entries.OVERWORLD_FISH_TOOL_RANK.send(p);
            Entries.OVERWORLD_TASK.send(p);
            Entries.OVERWORLD_TASK_DIFF.send(p);
            Entries.OVERWORLD_TASK_VAULT.send(p);
        } else {
            Entries.HEADER_TOURNAMENT.send(p);
            Entries.TOURNAMENT_KILLS.send(p);
            Entries.TOURNAMENT_DEATHS.send(p);
            Entries.GAP__.send(p);
            Entries.GAP___.send(p);
            Entries.GAP____.send(p);
        }

        Entries.SIGIL_HEADER.send(p);
        Entries.SIGIL_1_HOLDER.send(p);
        Entries.SIGIL_2_HOLDER.send(p);
        Entries.SIGIL_3_HOLDER.send(p);



    }

    private static void sendPlayerList(Player p) {
        List<Player> playerList = World.getPlayerStream().toList();
        int interId = 116;
        p.openInterface(InterfaceType.MAIN, interId);
        p.getPacketSender().sendString(interId, 4, "Player List");
        for (int line = 0; line < World.players.count(); line++) {
            p.getPacketSender().sendString(interId, line+6, playerList.get(line).getName());
            line++;
        }
    }

    private static void sendStaffList(Player p) {
        List<Player> staffList = World.getPlayerStream().filter(Player::isStaff).toList();
        int interId = 116;
        p.openInterface(InterfaceType.MAIN, interId);
        p.getPacketSender().sendString(interId, 4, "Staff List");
        for (int line = 0; line < staffList.size(); line++) {
            p.getPacketSender().sendString(interId, line+6, staffList.get(line).getName());
            line++;
        }
    }

    private static int getStaffOnlineCount() {
        List<Player> staffList = World.getPlayerStream().filter(Player::isStaff).collect(Collectors.toList());
        return staffList.size();
    }

    private static String getDoubleDrops() {
        if (World.doubleDrops) {
            return GREEN.wrap("Enabled!");
        } else {
            return RED.wrap("Disabled");
        }
    }
    private static String getDoublePkp() {
        if (World.doublePkp) {
            return GREEN.wrap("Enabled!");
        } else {
            return RED.wrap("Disabled");
        }
    }
    private static String getDoubleSlayerPoints() {
        if (World.doubleSlayer) {
            return GREEN.wrap("Enabled!");
        } else {
            return RED.wrap("Disabled");
        }
    }

    private static String getDoublePcPoints() {
        if (World.doublePest) {
            return GREEN.wrap("Enabled!");
        } else {
            return RED.wrap("Disabled");
        }
    }

    static {
        InterfaceHandler.register(Interface.NOTICEBOARD, (h) -> {
            for (Entries component : Entries.values()) {
                h.actions[component.getComponentId()] = component.getAction();
            }
        });
        LoginListener.register(player -> {
            send(player);
            player.addEvent(event -> {
                while(true) {
                    send(player);
                    event.delay(1);
                }
            });
        });
    }

    private interface TextField {
        String send(Player player);
    }

}
