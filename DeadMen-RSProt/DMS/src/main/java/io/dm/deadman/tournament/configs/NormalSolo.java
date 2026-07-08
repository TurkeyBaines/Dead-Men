package io.dm.deadman.tournament.configs;

import io.dm.deadman.tournament.TournamentConfig;
import io.dm.deadman.tournament.mutators.Mutators;

public class NormalSolo extends TournamentConfig {
    public NormalSolo() {
        XP_RATE = 50;
        DROP_RATE = 3;
        PET_RATE = 1;
        GAME_LENGTH = Timespan.THREE_HOURS;
        TEAM_SIZE_MAX = TeamSize.SOLO;
        MUTATOR = Mutators.getRandom();
    }
}
