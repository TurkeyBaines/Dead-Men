package io.dm.model.skills.agility.courses;

import io.dm.api.utils.Random;
import io.dm.model.entity.player.Player;
import io.dm.model.item.actions.impl.Pet;

public class AgilityPet {

    public static void rollForPet(Player player, int chance) {
        if (Random.rollDie(chance))
            Pet.GIANT_SQUIRREL.unlock(player);
    }

}
