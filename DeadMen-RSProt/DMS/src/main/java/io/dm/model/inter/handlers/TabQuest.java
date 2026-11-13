package io.dm.model.inter.handlers;

import io.dm.api.utils.StringUtils;
import io.dm.api.utils.TimeUtils;
import io.dm.deadman.Deadman;
import io.dm.deadman.tournament.Stage;
import io.dm.deadman.tournament.stages.Final;
import io.dm.deadman.tournament.stages.Lobby;
import io.dm.deadman.tournament.stages.Main;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.shared.listeners.LoginListener;

public class TabQuest {

    static {
        LoginListener.register(p -> {
            send(p);
        });
    }

    public static void send(Player p) {
        final int parent = 629;

        //Player
        final int playtime = 41;
        final int tournamentCount = 42;
        final int topFinish = 43;
        final int dmPoints = 44;
        final int overallKD = 53;

        //Tournament
        final int state = 48;
        final int nextState = 49;
        final int eventState = 50;
        final int tournInfo1 = 54;
        final int tournInfo2 = 55;
        final int tournInfo3 = 52;

        final int sigil_1 = 58;
        final int sigil_2 = 56;
        final int sigil_3 = 57;

        p.startEvent(e -> {
            while (true) {
                p.getPacketSender().sendString(parent, playtime, "Playtime: " + TimeUtils.fromMs((System.currentTimeMillis() - p.firstAccountStartTime), false));
                p.getPacketSender().sendString(parent, tournamentCount, "Tournaments: " + p.getTotalTournamentCount());
                p.getPacketSender().sendString(parent, topFinish, "Top Finish: " + formatFinishPosition(p.getTopFinishPosition()));
                p.getPacketSender().sendString(parent, dmPoints, "DeadMen Points: " + p.getDeadMenPoints());
                p.getPacketSender().sendString(parent, overallKD, "Overall K/D: " + p.getOverallKills() + "/" + p.getOverallDeaths() + "(" + formatKD(p.getOverallKills(), p.getOverallDeaths()) + ")");


                p.getPacketSender().sendString(parent, state, "Stage: " + Deadman.getStage().stageName());
                p.getPacketSender().sendString(parent, nextState, "Next: " + nextStage(Deadman.getStage()) + " in " + Deadman.timeUntilChange());
                p.getPacketSender().sendString(parent, eventState, "Event: " + eventString());


                if (p.getCombat().getLevel() < 90) {
                    p.getPacketSender().sendSprite(parent, sigil_3, 6391);
                } else if (p.getSigils()[2] != -1) {
                    p.getPacketSender().sendSprite(parent, sigil_3, 6391); //Get Sigil Graphic ID
                } else {
                    p.getPacketSender().sendSprite(parent, sigil_3, -1);
                }

                if (p.getCombat().getLevel() < 50) {
                    p.getPacketSender().sendSprite(parent, sigil_2, 6391);
                } else if (p.getSigils()[1] != -1) {
                    p.getPacketSender().sendSprite(parent, sigil_2, 6391); //Get Sigil Graphic ID
                } else {
                    p.getPacketSender().sendSprite(parent, sigil_2, -1);
                }

                if (p.getSigils()[0] != -1) {
                    p.getPacketSender().sendSprite(parent, sigil_1, 6391); //Get Sigil Graphic ID
                } else {
                    p.getPacketSender().sendSprite(parent, sigil_1, -1);
                }

                e.delay(1);
            }
        });

    }

    private static String eventString() {
        if (Deadman.getStage() instanceof Main) {
            Main stage = (Main) Deadman.getStage();

            if (stage.currentEvent == null) {
                return Deadman.timeUntilEvent();
            }

            return "! Event Active !";
        }

        return "None";
    }

    private static String formatFinishPosition(int position) {
        if (position == 0 || position == -1) return "No Finish";

        if (position < 10) {
            switch (position) {
                case 1: return "1st";
                case 2: return "2nd";
                case 3: return "3rd";
                default: return position + "th";
            }
        }

        int remain = position % 10;
        switch (position) {
            case 1: return "1st";
            case 2: return "2nd";
            case 3: return "3rd";
            default: return position + "th";
        }
    }

    private static String formatKD(int kills, int deaths) {
        if (deaths == 0) {
            return String.format("%.2f", (double) kills);
        }
        return String.format("%.2f", (double) kills / deaths);
    }

    private static String nextStage(Stage stage) {
        if (stage instanceof Lobby) return "Game";
        if (stage instanceof Main) return "Finals";
        if (stage instanceof Final) return "Lobby";
        return null;
    }
}
