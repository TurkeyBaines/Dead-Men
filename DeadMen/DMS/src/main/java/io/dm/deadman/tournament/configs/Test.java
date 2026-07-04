package io.dm.deadman.tournament.configs;

import io.dm.deadman.tournament.TournamentConfig;
import io.dm.deadman.tournament.mutators.Mutators;

public class Test extends TournamentConfig {
    public Test() {
        XP_RATE = 200;
        DROP_RATE = 10;
        PET_RATE = 10;
        GAME_LENGTH = Timespan.TEST;
        TEAM_SIZE_MAX = TeamSize.SOLO;
        MUTATOR = Mutators.getRandom();
    }
}
