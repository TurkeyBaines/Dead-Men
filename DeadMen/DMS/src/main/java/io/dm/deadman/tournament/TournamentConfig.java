package io.dm.deadman.tournament;

import io.dm.cache.Color;
import io.dm.deadman.Deadman;
import io.dm.deadman.mutators.Mutator;
import io.dm.deadman.tournament.configs.QuickSolo;
import io.dm.model.World;

public abstract class TournamentConfig {

    public int XP_RATE = 1;
    public int DROP_RATE = 1;
    public int PET_RATE = 1;
    public Mutator MUTATOR = null;

    public Timespan GAME_LENGTH = Timespan.ONE_HOUR;


    public TeamSize TEAM_SIZE_MAX = TeamSize.SOLO;

    public enum TeamSize {
        SOLO(1),
        DUO(2),
        TRIO(3);
        public int asInt;
        TeamSize(int asInt) {
            this.asInt = asInt;
        }
    }

    public enum Timespan {
        ONE_HOUR("1 Hour", 3600000, 900000),          // 1 Hour   | 15 Mins
        THREE_HOURS("3 Hours", 10800000, 1800000),     // 3 Hours  | 30 Mins
        FIVE_HOURS("5 Hours", 18000000, 1800000),     // 5 Hours  | 30 Mins
        EIGHT_HOURS("8 Hours", 28800000, 2700000),       // 8 Hours  | 45 Mins
        TEN_HOURS("10 Hours", 36000000, 2700000),       // 6 Hours  | 45 Mins
        TWELVE_HOURS("12 Hours", 43200000, 3600000);    // 12 Hours | 1 Hour

        public String text;
        public long runtime;
        public long finals;

        Timespan(String text, long runtime, long finals) {
            this.text = text;
            this.runtime = runtime;
            this.finals = finals;
        }
    }

    public static TournamentConfig getRandom() {
        if (!Deadman.canOverrideConfig()) {
            //We have a custom config to apply!

            return Deadman.getNext_config();
        }

        return new QuickSolo();
    }

    public void printConfig() {
        StringBuilder msg1 = new StringBuilder();
        msg1.append(Color.RAID_PURPLE.wrap("[Tournament] "));
        if (!Deadman.canOverrideConfig())
            msg1.append(Color.DARK_RED.wrap("The next Tournament Configuration has been overridden by " + Deadman.getNext_Config_Name() + "!"));
        else if (Deadman.getStage().stageName() == Tournament.StageName.MAIN)
            msg1.append(Color.DARK_RED.wrap("The current Tournament Configuration is:"));
        else if (Deadman.getStage().stageName() == Tournament.StageName.LOBBY)
            msg1.append(Color.DARK_RED.wrap("The next Tournament Configuration has been selected!"));
        else
            msg1.append(Color.DARK_RED.wrap("The current Tournament is just ending... the last Configuration was:"));

        StringBuilder msg2 = new StringBuilder();
        msg2.append(Color.RAID_PURPLE.wrap("[Tournament]"));
        msg2.append(Color.BLUE.wrap("[Config] "));
        msg2.append("Runtime: " + Color.DARK_RED.wrap(GAME_LENGTH.text) + ", Team Size: " + Color.DARK_RED.wrap("" + TEAM_SIZE_MAX) + ", XP Rate: " + Color.DARK_RED.wrap(XP_RATE + "x"));

        StringBuilder msg3 = new StringBuilder();
        msg3.append(Color.RAID_PURPLE.wrap("[Tournament]"));
        msg3.append(Color.BLUE.wrap("[Config] "));
        msg3.append("drop Rate: " + Color.DARK_RED.wrap(DROP_RATE + "x") + ", Pet Drop Rate: " + Color.DARK_RED.wrap(PET_RATE + "x") + ", Mutator: " + Color.DARK_RED.wrap(MUTATOR.name()));

        StringBuilder msg4 = new StringBuilder();
        msg4.append(Color.RAID_PURPLE.wrap("[Tournament]"));
        msg4.append(Color.BLUE.wrap("[Next Config] "));
        if (!Deadman.canOverrideConfig())
            msg4.append(Color.DARK_RED.wrap("The next Config has been pre-selected by " + Deadman.getNext_Config_Name()));
        else
            msg4.append(Color.DARK_RED.wrap("The next Config is available to be overridden"));

        World.players.forEach( p -> {
            p.sendMessage(msg1.toString());
            p.sendMessage(msg2.toString());
            p.sendMessage(msg3.toString());
            p.sendMessage(msg4.toString());
        });
    }

}
