package io.dm.deadman.tournament.stages;

import io.dm.deadman.Deadman;
import io.dm.deadman.tournament.events.DMMEvent;
import io.dm.deadman.content.sigils.Sigil;
import io.dm.deadman.content.sigils.Sigils;
import io.dm.deadman.tournament.Stage;
import io.dm.deadman.tournament.Tournament;
import io.dm.model.inter.dialogue.OptionsDialogue;
import io.dm.model.inter.utils.Option;

public class Main extends Stage {
    public long nextEvent;
    public DMMEvent currentEvent;
    public Main() {
        super.config = Deadman.getConfig();
    }

    @Override
    public void onLoad() {
        players().forEach(p -> {
            p.dmmNeedsReset = true;
            p.dialogue(new OptionsDialogue(
                    "Please select an starting Sigil to unlock",
                    new Option("Sigil of the Ruthless Ranger", () -> { Sigil.unlock(p, Sigils.Ruthless_Ranger); }),
                    new Option("Sigil of the Formidable Fighter", () -> { Sigil.unlock(p, Sigils.Formidable_Fighter); }),
                    new Option("Sigil of the Menacing Mage", () -> { Sigil.unlock(p, Sigils.Menacing_Mage); })
            ));
        });
        startTime = System.currentTimeMillis();
        duration = Deadman.getConfig().GAME_LENGTH.runtime;
        nextEvent = startTime + 900000;
    }

    @Override
    public void onUpdate() {
        if (currentEvent != null) {
            if (currentEvent.eventFinished) { currentEvent = null; nextEvent = System.currentTimeMillis() + (60000 * 15); }
            else currentEvent.update();
        } else {
            if (System.currentTimeMillis() >= nextEvent) {
                currentEvent = DMMEvent.getRandom();
                currentEvent.spawn();
            }
        }

        if (System.currentTimeMillis() >= (startTime + duration)) {
            progress(Tournament.StageName.FINAL);
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
                players().forEach(p -> p.sendMessage("The Finals will begin in " + seconds + " seconds."));
                break;
            case 1:
                players().forEach(p -> p.sendMessage("The Finals will begin in 1 second."));
        }
    }

    @Override
    public void onRemove() {
        currentEvent = null;
        duration = -1;
        startTime = -1;
        nextEvent = -1;
    }

    @Override
    public Tournament.StageName stageName() {
        return Tournament.StageName.MAIN;
    }
}
