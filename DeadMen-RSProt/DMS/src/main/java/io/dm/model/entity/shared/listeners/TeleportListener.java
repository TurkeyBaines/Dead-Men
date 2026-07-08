package io.dm.model.entity.shared.listeners;

import io.dm.model.entity.player.Player;

public interface TeleportListener {

    boolean allow(Player player);

}