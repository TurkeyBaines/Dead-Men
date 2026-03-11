package io.dm.deadman.tournament.configs;

import io.dm.deadman.mutators.impl.EmptyMutator;
import io.dm.deadman.mutators.impl.StaticGasMutator;
import io.dm.deadman.mutators.impl.VampiricRitesMutator;
import io.dm.deadman.tournament.TournamentConfig;

public class QuickSolo extends TournamentConfig {
    public QuickSolo() {
        XP_RATE = 50;
        DROP_RATE = 5;
        PET_RATE = 5;
        GAME_LENGTH = Timespan.ONE_HOUR;
        TEAM_SIZE_MAX = TeamSize.SOLO;
        MUTATOR = new VampiricRitesMutator();
    }
}
