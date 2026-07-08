package io.dm.model.entity.shared.listeners;

import io.dm.model.entity.player.Player;

public interface AttackPlayerListener {

    boolean allow(Player player, Player pTarget, boolean message);

}