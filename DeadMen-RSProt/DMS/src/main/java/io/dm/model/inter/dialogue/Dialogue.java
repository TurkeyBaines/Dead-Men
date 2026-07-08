package io.dm.model.inter.dialogue;

import io.dm.model.entity.player.Player;

public interface Dialogue {

    void open(Player player);

    default void closed(Player player) {}

}