package io.dm.model.entity.shared.listeners;

import io.dm.model.combat.Killer;
import io.dm.model.item.Item;

public interface DropListener {

    void dropping(Killer killer, Item item);

}