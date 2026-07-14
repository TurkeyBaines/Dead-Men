package io.dm.deadman.content.sigils;

import io.dm.api.utils.Random;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;

public abstract class Sigil {

    public void process() {

    }

    public boolean proc() {
        int rng = Random.get(100);
        return rng < chance();
    }

    public abstract String name();
    protected abstract int chance();
    public abstract int unattuned();
    public abstract int attuned();

    public boolean effect() {return false;}
    public boolean effect(Player player) {return false;}
    public boolean effect(Player player, Object... params) {return false;}
}
