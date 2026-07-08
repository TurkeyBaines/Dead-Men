package io.dm.deadman.tournament.configs;

import io.dm.deadman.tournament.mutators.Mutator;
import io.dm.deadman.tournament.TournamentConfig;

public class CustomConfig extends TournamentConfig {

    public CustomConfig(Timespan runtime, TeamSize maxTeamSize, int xpRate, int dropRate, int petDropRate, Mutator mutator) {
        GAME_LENGTH = runtime;
        TEAM_SIZE_MAX = maxTeamSize;
        XP_RATE = xpRate;
        DROP_RATE = dropRate;
        PET_RATE = petDropRate;
        MUTATOR = mutator;
    }

}
