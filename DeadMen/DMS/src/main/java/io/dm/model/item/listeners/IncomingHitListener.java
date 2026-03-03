package io.dm.model.item.listeners;

import io.dm.model.combat.Hit;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;

@FunctionalInterface
public interface IncomingHitListener {
    /**
     * @param player player that triggered this event
     * @param item   item that triggered this event
     * @param hit    hit that triggered this event
     */
    void accept(Player player, Item item, Hit hit);

    default IncomingHitListener andThen(IncomingHitListener other) {
        return (p, i, h) -> {
            accept(p, i, h);
            other.accept(p, i, h);
        };
    }
}
