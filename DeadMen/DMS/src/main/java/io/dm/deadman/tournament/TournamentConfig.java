package io.dm.deadman.tournament;

import io.dm.deadman.tournament.configs.QuickSolo;

public abstract class TournamentConfig {

    public int XP_RATE = 1;
    public int DROP_RATE = 1;
    public int PET_RATE = 1;

    public Timespan GAME_LENGTH = Timespan.ONE_HOUR;


    public TeamSize TEAM_SIZE_MAX = TeamSize.SOLO;

    public enum TeamSize {
        SOLO(1);
        public int asInt;
        TeamSize(int asInt) {
            this.asInt = asInt;
        }
    }

    public enum Timespan {
        ONE_HOUR("1 Hour", 3600000, 900000),          // 1 Hour   | 15 Mins
        THREE_HOURS("3 Hours", 10800000, 1800000),     // 3 Hours  | 30 Mins
        SIX_HOURS("6 Hours", 21600000, 1800000),       // 6 Hours  | 30 Mins
        TWELVE_HOURS("12 Hours", 43200000, 2700000);    // 12 Hours | 45 Mins

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
        return new QuickSolo();
    }

}
