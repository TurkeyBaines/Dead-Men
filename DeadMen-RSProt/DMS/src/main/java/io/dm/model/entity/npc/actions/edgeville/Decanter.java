package io.dm.model.entity.npc.actions.edgeville;

import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.npc.NPCAction;
import io.dm.model.entity.player.Player;
import io.dm.model.inter.Interface;
import io.dm.model.inter.InterfaceHandler;
import io.dm.model.inter.InterfaceType;
import io.dm.model.inter.actions.SimpleAction;
import io.dm.model.skills.herblore.Potion;

/*
 * @project Kronos
 * @author Patrity - https://github.com/Patrity
 * Created on - 7/20/2020
 */
public class Decanter {
    private static void decantPotions(Player player, NPC npc) {
        player.openInterface(InterfaceType.CHATBOX, Interface.POTION_DECANTING);
    }
    static {
        NPCAction.register(5449, "decant", Decanter::decantPotions);
        InterfaceHandler.register(Interface.POTION_DECANTING, h -> {
            h.actions[3] = (SimpleAction) p -> Potion.decant(p, 1);
            h.actions[4] = (SimpleAction) p -> Potion.decant(p, 2);
            h.actions[5] = (SimpleAction) p -> Potion.decant(p, 3);
            h.actions[6] = (SimpleAction) p -> Potion.decant(p, 4);
        });
    }
}
