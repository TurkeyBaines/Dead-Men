package io.dm.network.incoming.handlers.commands;

import io.dm.api.utils.Random;
import io.dm.model.entity.player.Player;
import io.dm.model.item.actions.impl.Pet;

public class PetCommands {

        public void process(Player player, String... args) {
            switch (args[0]) {
                case "unlock":
                    unlock(player, args);
                    break;

                case "add":
                    add(player, args);
                    break;
            }
        }


        private void unlock(Player p, String... args) {
            if (args.length == 1) {
                for (Pet pet : Pet.values()) {
                    pet.unlock(p);
                    System.out.println("You have a funny feeling like you've cheated your way to the " + pet.name());
                }
            }

            if (args.length == 2) {
                int id = Integer.parseInt(args[1]);
                for (Pet pet : Pet.values()) {
                    if (pet.itemId == id || pet.npcId == id) {
                        pet.unlock(p);
                        System.out.println("You have a funny feeling like you've cheated your way to the " + pet.name());
                    }
                }
            }
        }


        private void add(Player player, String... args) {

            if (player.pet != null)
                return;

            if (args.length == 1) {
                Pet selected;
                selected = Pet.values()[Random.get(Pet.values().length)-1];

                selected.spawn(player);
            }
        }

}
