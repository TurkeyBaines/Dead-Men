package io.dm.model.skills.runecrafting;

import io.dm.cache.ItemDef;

public enum Essence {

    REGULAR(1436),
    PURE(7936),
    DARK(7938);

    public final int id;
    public final String name;

    Essence(int id) {
        this.id = id;
        this.name = ItemDef.get(id).name.toLowerCase();
    }

}
