package io.dm.model.entity.shared.listeners;

import io.dm.model.entity.player.Player;
import io.dm.model.skills.prayer.Prayer;

@FunctionalInterface
public interface ActivatePrayerListener {

	boolean allow(Player player, Prayer prayer);
}
