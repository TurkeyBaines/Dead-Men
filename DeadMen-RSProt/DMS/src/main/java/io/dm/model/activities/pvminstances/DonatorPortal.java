package io.dm.model.activities.pvminstances;

import io.dm.model.entity.player.Player;
import io.dm.model.inter.dialogue.NPCDialogue;
import io.dm.model.inter.dialogue.PlayerDialogue;
import io.dm.model.map.object.GameObject;
import io.dm.model.map.object.actions.ObjectAction;

public class DonatorPortal {

	private static void enter(Player player, GameObject object) {
		if (!player.isSapphire() && !player.isAdmin()) {
			player.dialogue(new NPCDialogue(2108, "Not so fast there, that portal is exclusive to donators!"),
					new PlayerDialogue("I can't enter?"),
					new NPCDialogue(2108, "No."),
					new PlayerDialogue("How can I enter?"),
					new NPCDialogue(2108, "Once you've purchased something in the ::store, this portal will be all yours!"));
			return;
		}

		// Regular donator teleports to lower area, higher ends up in the bank area
		player.getMovement().teleport(3810, 2844, 1);

	}

	static {
		ObjectAction.register(4390, "Exit", DonatorPortal::enter);
	}

}
