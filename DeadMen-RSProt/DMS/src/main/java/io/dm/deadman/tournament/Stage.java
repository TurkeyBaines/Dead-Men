package io.dm.deadman.tournament;

import io.dm.deadman.Deadman;
import io.dm.deadman.tournament.stages.Final;
import io.dm.deadman.tournament.stages.Lobby;
import io.dm.deadman.tournament.stages.Main;
import io.dm.model.World;
import io.dm.model.entity.EntityList;
import io.dm.model.entity.player.Player;

public abstract class Stage {

    public TournamentConfig config;
    public long startTime;
    public long duration;

    public abstract void onLoad();
    public abstract void onUpdate();
    public abstract void onRemove();

    public abstract Tournament.StageName stageName();

    public EntityList<Player> players() {
        return World.players;
    }

    public void progress(Tournament.StageName stage) {
        Deadman.getStage().onRemove();

        switch (stage) {
            case FINAL:
                Deadman.setStage(new Final());
                break;

            case MAIN:
                Deadman.setStage(new Main());
                break;

            case LOBBY:
                Deadman.setStage(new Lobby());
                config = TournamentConfig.getRandom();
                break;
        }

        Deadman.getStage().onLoad();
    }

}
