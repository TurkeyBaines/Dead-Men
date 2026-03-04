package io.dm.deadman.tournament.stages;

import io.dm.deadman.Deadman;
import io.dm.deadman.sigils.Sigil;
import io.dm.deadman.tournament.Stage;
import io.dm.deadman.tournament.Tournament;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.item.ItemContainer;

public class Lobby extends Stage {

    @Override
    public void onLoad() {
        startTime = System.currentTimeMillis();
        duration = 900000;
        for (Player p : players()) {
            if (Deadman.getOverworld().contains(p)) {
                //Reset None-Current Stats & Items!!!
                p.skillHolder = new int[][] {
                        {1, 0},
                        {1, 0},
                        {1, 0},
                        {10, 1154},
                        {1, 0},
                        {1, 0},
                        {1, 0},
                        {1, 0},
                        {1, 0},
                        {1, 0},
                        {1, 0},
                        {1, 0},
                        {1, 0},
                        {1, 0},
                        {1, 0},
                        {1, 0},
                        {1, 0},
                        {1, 0},
                        {1, 0},
                        {1, 0},
                        {1, 0},
                        {1, 0},
                        {1, 0}
                };
                p.inventoryHolder = new ItemContainer();
                p.equipmentHolder = new Item[]{
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                };

                p.sendMessage("A Tournament has just finished, we are now in the Lobby Phase.");
                p.sendMessage("To take part in the next Tournament, please head back to the citadel.");
                p.sendMessage("The next Tournament will begin in 30 minutes!");
            } else {
                p.getStats().reset();
                p.getInventory().clear();
                p.getInventory().sendUpdates();
                p.getEquipment().clear();
                p.getEquipment().sendUpdates();
            }
            p.getBank().clear();
            p.getBank().sendUpdates();
            p.getCombat().updateCombatLevel();

            Sigil.reset(p);
            p.dmmNeedsReset = false;

            p.sendMessage("A Tournament has just finished, we are now in the Lobby Phase.");
            p.sendMessage("To take part in the next Tournament, please head to the citadel before the start time.");
            p.sendMessage("The next Tournament will begin in 30 minutes!");
        }
    }

    @Override
    public void onUpdate() {
        if (System.currentTimeMillis() >= (startTime + duration)) {
            progress(Tournament.StageName.MAIN);
            return;
        }

        long currentTime = System.currentTimeMillis();
        long nextTime = startTime + duration;
        int seconds = (int) (nextTime - currentTime) / 1000;

        switch (seconds) {
            case 60:
            case 30:
            case 10:
            case 5:
            case 4:
            case 3:
            case 2:
                players().forEach(p -> p.sendMessage("The Tournament will begin in " + seconds + " seconds."));
                break;
            case 1:
                players().forEach(p -> p.sendMessage("The Tournament will begin in 1 second."));
        }

    }

    @Override
    public void onRemove() {
        startTime = -1;
        duration = -1;
    }

    @Override
    public Tournament.StageName stageName() {
        return Tournament.StageName.LOBBY;
    }
}
