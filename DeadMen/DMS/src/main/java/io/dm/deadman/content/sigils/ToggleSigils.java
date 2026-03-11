package io.dm.deadman.content.sigils;

import io.dm.deadman.content.sigils.toggleable.Devotion;
import io.dm.deadman.content.sigils.toggleable.EnhancedHarvest;
import io.dm.deadman.content.sigils.toggleable.RemoteStorage;
import io.dm.deadman.content.sigils.toggleable.Slaughter;

public enum ToggleSigils {
    Devotion(0),
    Enhanced_Harvest(1),
    Remote_Storage(2),
    Slaughter(3);

    int id;

    ToggleSigils(int id) { this.id = id; }

    Sigil create() {
        switch (id) {
            case 0:
                return new Devotion();
            case 1:
                return new EnhancedHarvest();
            case 2:
                return new RemoteStorage();
            case 3:
                return new Slaughter();
        }
        return null;
    }
}