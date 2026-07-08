package io.dm.model.activities.pvminstances;

import io.dm.model.entity.player.Player;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.utils.Option;
import io.dm.model.map.object.GameObject;
import io.dm.model.map.object.actions.ObjectAction;

public class PortalOfHeroes {

	private static void enter(Player player, GameObject object) {
			player.dialogue(new OptionsDialogue("Where do you want to go?",
					new Option("Wilderness Resource Area", PortalOfHeroes::teleportToResourceArea), new Option("Revenant Cave", PortalOfHeroes::teleportToRevs)));
	}

	private static void teleportToResourceArea(Player player) {
		player.getMovement().startTeleport(e -> {
			player.animate(714);
			player.graphics(111, 92, 0);
			player.publicSound(200);
			e.delay(3);
			player.getMovement().teleport(3187, 3937, 0);
		});
	}

	private static void teleportToRevs(Player player) {
		player.getMovement().startTeleport(e -> {
			player.animate(714);
			player.graphics(111, 92, 0);
			player.publicSound(200);
			e.delay(3);
			player.getMovement().teleport(3127, 3832, 0);
		});
	}

	static {
		ObjectAction.register("Wilderness Portal", "Enter", PortalOfHeroes::enter);
	}

}
