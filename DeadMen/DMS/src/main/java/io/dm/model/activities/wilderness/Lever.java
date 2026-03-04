package io.dm.model.activities.wilderness;

import io.dm.model.activities.pvminstances.InstanceDialogue;
import io.dm.model.activities.pvminstances.InstanceType;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.dialogue.MessageDialogue;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.utils.Option;
import io.dm.model.map.object.GameObject;
import io.dm.model.map.object.actions.ObjectAction;

public class Lever {

    private static void pull(Player player, GameObject lever, int x, int y, String message) {
        player.getMovement().startTeleport(-1, event -> {
            player.animate(2140);
            player.sendMessage("You pull the lever...");
            event.delay(1);
            lever.setId(88);
            event.delay(1);
            player.animate(714);
            player.graphics(111, 110, 0);
            event.delay(3);
            lever.setId(lever.originalId);
            player.getMovement().teleport(x, y);
            player.sendMessage(message);
        });
    }

    static {
        /*
         * Ardougne
         */
        ObjectAction.register(1814, 2561, 3311, 0, "pull", (player, obj) -> pull(player, obj, 3154, 3924, "...and teleport into the wilderness."));


        /*
         * Deserted
         */
        ObjectAction.register(1815, 3153, 3923, 0, 1, (player, obj) -> pull(player, obj, 2562, 3311, "...and teleport out of the wilderness."));
        ObjectAction.register(1815, 3153, 3923, 0, 2, (player, obj) -> pull(player, obj, 3090, 3475, "...and teleport out of the wilderness."));

        /*
         * Magebank
         */
        ObjectAction.register(5959, 3090, 3956, 0, "pull", (player, obj) -> pull(player, obj, 2539, 4712, "...and teleport into the mage's cave."));
        ObjectAction.register(5960, 2539, 4712, 0, "pull", (player, obj) -> pull(player, obj, 3090, 3956, "...and teleport out of the mage's cave."));

        /*
         * KBD
         */
        ObjectAction.register(1816, 3067, 10253, 0, "pull", (player, obj) -> pull(player, obj, 2271, 4680, "...and teleport into the Dragon's Lair."));
        ObjectAction.register(1816, 3067, 10253, 0, "instance", (player, obj) -> {
            if(player.getCombat().checkTb())
                return;
            InstanceDialogue.open(player, InstanceType.KBD);
        });
        ObjectAction.register(1817, "pull", (player, obj) -> pull(player, obj, 3067, 10253, "...and teleport out of the Dragon's Lair."));

    }
}
