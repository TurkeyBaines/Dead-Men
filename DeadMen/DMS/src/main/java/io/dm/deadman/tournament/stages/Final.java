package io.dm.deadman.tournament.stages;

import io.dm.deadman.Deadman;
import io.dm.deadman.events.DMMEvent;
import io.dm.deadman.tournament.FinalEvent;
import io.dm.deadman.tournament.Stage;
import io.dm.deadman.tournament.Tournament;
import io.dm.deadman.tournament.TournamentConfig;
import io.dm.model.entity.player.Player;
import lombok.Setter;

import java.util.ArrayList;

public class Final extends Stage {

    public FinalEvent event;

    public Final() {
        super.config = Deadman.getConfig();
        event = FinalEvent.getRandom();
    }
    @Override
    public void onLoad() {
        System.out.println("onLoad FINAL Running!");
        startTime = System.currentTimeMillis();
        duration = config.GAME_LENGTH.finals;
        players().forEach(p -> {
            p.dmmNeedsReset = true;
        });
        event.init();

    }

    @Override
    public void onUpdate() {
        System.out.println("onUpdate FINAL Running!");
        event.update();

        if (event.finalFinished) {
            onRemove();
            progress(Tournament.StageName.LOBBY);
        }
    }

    @Override
    public void onRemove() {
        System.out.println("onRemove FINAL Running!");
        event = null;
    }

    @Override
    public Tournament.StageName stageName() {
        return Tournament.StageName.FINAL;
    }

    public FinalEvent getEvent() {
        return event;
    }

}
