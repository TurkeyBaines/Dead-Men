package io.dm.model.entity.shared.listeners;

import io.dm.model.combat.Hit;
import io.dm.model.combat.Killer;
import io.dm.model.entity.Entity;
import io.dm.model.entity.player.Player;

public interface DeathListener {

    void handle(Entity entity, Killer killer, Hit killHit);

    interface Simple extends DeathListener {
        default void handle(Entity entity, Killer killer, Hit killHit) {
            handle();
        }
        void handle();
    }

    interface SimpleKiller extends DeathListener {
        //Warning killer can be null on this!
        default void handle(Entity entity, Killer killer, Hit killHit) {
            handle(killer);
        }
        void handle(Killer killer);
    }

    interface SimplePlayer extends DeathListener {
        //only calls if killer is player
        default void handle(Entity entity, Killer killer, Hit killHit) {
            if(killer != null)
                handle(killer.player);
        }
        void handle(Player pKiller);
    }

}