package io.dm.model.achievements.listeners.novice;

import io.dm.api.utils.NumberUtils;
import io.dm.model.achievements.Achievement;
import io.dm.model.achievements.AchievementListener;
import io.dm.model.achievements.AchievementStage;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.player.PlayerCounter;

public class ImplingHunter implements AchievementListener {

    @Override public String name() {
        return "Impling Catcher";
    }

    @Override
    public AchievementStage stage(Player player) {
        int amount = PlayerCounter.IMPLINGS_CAUGHT.get(player);
        if (amount == 0)
            return AchievementStage.NOT_STARTED;
        else if (amount >= 30)
            return AchievementStage.FINISHED;
        else
            return AchievementStage.STARTED;
    }

    public String[] lines(Player player, boolean finished) {
        return new String[] {
                Achievement.slashIf("<col=000080>Assignment</col>: Catch 30 implings.", finished),
                Achievement.slashIf("<col=000080>Reward</col>: Unlocks the magic butterfly net.", finished),
                "",
                Achievement.slashIf("<col=000080>Implings caught</col>: " + NumberUtils.formatNumber(PlayerCounter.IMPLINGS_CAUGHT.get(player)), finished),
        };
    }

    @Override
    public void started(Player player) {

    }

    @Override
    public void finished(Player player) {

    }

}
