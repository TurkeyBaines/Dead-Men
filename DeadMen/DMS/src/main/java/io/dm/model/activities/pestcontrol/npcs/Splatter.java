package io.dm.model.activities.pestcontrol.npcs;

import io.dm.model.combat.Hit;
import io.dm.model.entity.Entity;
import io.dm.model.entity.npc.NPCCombat;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.shared.listeners.HitListener;
import io.dm.utility.Misc;

/**
 * The combat script for the Splatter pest.
 * @author Heaven
 */
public class Splatter extends NPCCombat {

	@Override
	public void init() {
		npc.deathStartListener = (entity, killer, hit) -> splat();
		npc.hitListener = new HitListener().postDamage((hit)-> {
			Entity attacker = hit.attacker;
			if (attacker != null && attacker.isPlayer()) {
				Player player = attacker.player;
				if (player.pestGame != null && hit.damage > 0) {
					player.pestActivityScore += hit.damage / 2;
				}
			}
		});
	}

	@Override
	public void follow() {
		follow(10);
	}

	@Override
	public boolean attack() {
		if (withinDistance(1)) {
			basicAttack();
			return true;
		}
		return false;
	}

	private void splat() {
		npc.graphics(650);
		npc.localPlayers().forEach(p -> {
			if (Misc.getEffectiveDistance(npc, p) <= 1) {
				p.hit(new Hit(npc).fixedDamage(20));
			}
		});
	}
}
