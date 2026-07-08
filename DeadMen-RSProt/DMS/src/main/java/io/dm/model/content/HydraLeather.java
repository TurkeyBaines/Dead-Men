package io.dm.model.content;


import io.dm.model.entity.player.Player;
import io.dm.model.inter.dialogue.ItemDialogue;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.dialogue.PlayerDialogue;
import io.dm.model.inter.utils.Option;
import io.dm.model.item.Item;
import io.dm.model.item.actions.ItemObjectAction;
import io.dm.model.map.object.GameObject;
import io.dm.model.map.object.actions.ObjectAction;

/*
 * @project Kronos
 * @author Patrity - https://github.com/Patrity
 * Created on - 7/8/2020
 */
public class HydraLeather {

    private static void inspectMachine(Player player, GameObject machine) {
        player.dialogue(
                new PlayerDialogue("I wonder if I can use Hydra Leather<br>" +
                        "with this machine...")
        );
    }
    private static void craftGloves(Player player, Item item, GameObject object) {
        player.dialogue(new ItemDialogue().one(22983, "You are about to turn your<br>" +
                "Hydra Leather into Ferocious<br>" +
                "gloves"),
            new OptionsDialogue("Would you like to continue?",
                new Option("No thanks."),
                    new Option("Yes!", () -> {
                        player.getInventory().remove(22983, 1);
                        player.getInventory().add(22981, 1);
                        player.dialogue(
                                new ItemDialogue().one(22981, "You have successfully created<br>" +
                                        "Ferocious Gloves!"));
                    })));
    }

    private static void revertGloves(Player player, Item item, GameObject object) {
        player.dialogue(new ItemDialogue().one(22981, "You are about to rever your<br>" +
                        "Ferocious Gloves into<br>" +
                        "Hydra Leather"),
                new OptionsDialogue("Would you like to continue?",
                        new Option("No thanks."),
                        new Option("Yes!", () -> {
                            player.getInventory().remove(22981, 1);
                            player.getInventory().add(22983, 1);
                            player.dialogue(
                                    new ItemDialogue().one(22983, "You have successfully reclaimed<br>" +
                                            "your Hydra Leather!"));
                        })));
    }

    static {
        ObjectAction.register(32161, "inspect", HydraLeather::inspectMachine);
        ItemObjectAction.register(22983, 32161, HydraLeather::craftGloves);
        ItemObjectAction.register(22981, 32161, HydraLeather::revertGloves);
    }
}
