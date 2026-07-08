package io.dm.deadman.tournament.mutators;

import io.dm.api.utils.Random;
import io.dm.deadman.tournament.mutators.impl.*;

public class Mutators {

    private static Mutator[] mutators = {
            new BloodFrenzyMutator(),
            new BountyHunterMutator(),
            new FamineMutator(),
            //new StaticGasMutator(),
            new VampiricRitesMutator()
    };

    public static Mutator getRandom() {

        return mutators[Random.get(mutators.length-1)];
    }

}
