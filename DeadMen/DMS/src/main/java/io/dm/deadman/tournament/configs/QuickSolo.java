package io.dm.deadman.tournament.configs;

import io.dm.deadman.tournament.mutators.Mutators;
import io.dm.deadman.tournament.mutators.impl.VampiricRitesMutator;
import io.dm.deadman.tournament.TournamentConfig;

public class QuickSolo extends TournamentConfig {
    public QuickSolo() {
        XP_RATE = 50;
        DROP_RATE = 3;
        PET_RATE = 1;
        GAME_LENGTH = Timespan.ONE_HOUR;
        TEAM_SIZE_MAX = TeamSize.SOLO;
        MUTATOR = Mutators.getRandom();
    }
}
