package io.dm.model.activities.raids.xeric;

import io.dm.cache.EnumMap;
import io.dm.cache.ItemDef;

public class CoxItem {

    static {
        EnumMap.get(1666).ints().forEach((k, v) -> {
            mark(v);
        });
    }

    private static void mark(int id) {
        ItemDef def = ItemDef.get(id);
        if (def != null)
            mark(def);
    }

    private static void mark(ItemDef def) {
        def.coxItem = true;
        def.tradeable = true;
    }

}
