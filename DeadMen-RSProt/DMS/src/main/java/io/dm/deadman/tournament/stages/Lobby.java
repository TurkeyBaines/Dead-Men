package io.dm.deadman.tournament.stages;

import io.dm.deadman.Deadman;
import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.tournament.Stage;
import io.dm.deadman.tournament.Tournament;
import io.dm.model.World;
import io.dm.model.entity.player.Player;
import io.dm.model.item.Item;
import io.dm.model.item.ItemContainer;

public class Lobby extends Stage {

    @Override
    public void onLoad() {
        Deadman.generateUTC();

        startTime = System.currentTimeMillis();
        duration = 60000;
        for (Player p : players()) {
            p.getStats().reset();
            p.getInventory().clear();
            p.getInventory().sendUpdates();
            p.getEquipment().clear();
            p.getEquipment().sendUpdates();
            p.getBank().clear();
            p.getBank().sendUpdates();
            p.getCombat().updateCombatLevel();

            p.utc = Deadman.getUtc();

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

    public String formatTimeLeft(long startTime, long duration) {
        long targetTime = startTime + duration;
        long timeLeftMs = targetTime - System.currentTimeMillis();

        // If the duration has already passed
        if (timeLeftMs <= 0) {
            return "Expired";
        }

        // Convert milliseconds to total units
        long totalSeconds = timeLeftMs / 1000;
        long totalMinutes = totalSeconds / 60;
        long totalHours = totalMinutes / 60;
        long days = totalHours / 24;

        // Calculate remainder portions for display
        long seconds = totalSeconds % 60;
        long minutes = totalMinutes % 60;
        long hours = totalHours % 24;

        StringBuilder sb = new StringBuilder();

        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            sb.append(minutes).append("m ");
        }
        sb.append(seconds).append("s");

        return sb.toString().trim() + " left";
    }
}
