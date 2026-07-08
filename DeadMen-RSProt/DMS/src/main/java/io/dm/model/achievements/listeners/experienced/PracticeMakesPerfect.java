package io.dm.model.achievements.listeners.experienced;

import io.dm.api.utils.NumberUtils;
import io.dm.model.achievements.Achievement;
import io.dm.model.achievements.AchievementListener;
import io.dm.model.achievements.AchievementStage;
import io.dm.model.entity.player.Player;

public class PracticeMakesPerfect implements AchievementListener {

    @Override
    public String name() {
        return "Gordon Ramsay";
    }

    @Override
    public AchievementStage stage(Player player) {
        if(player.cookedFood == 0)
            return AchievementStage.NOT_STARTED;
        if(player.cookedFood < 1000)
            return AchievementStage.STARTED;
        return AchievementStage.FINISHED;
    }

    @Override
    public String[] lines(Player player, boolean finished) {
        return new String[]{
                Achievement.slashIf("A growing mastery for the skill, a lesson is learned by grabbing", finished),
                Achievement.slashIf("one too many hot pans with uncovered hands.", finished),
                "",
                Achievement.slashIf("<col=000080>Assignment</col>: Cook 1,000 pieces of food", finished),
                Achievement.slashIf("<col=000080>Reward</col>: Cooking Gauntlets", finished),
                "",
                "<col=000080>Cooked Food: <col=800000>" + NumberUtils.formatNumber(player.cookedFood)
        };
    }

    @Override
    public void started(Player player) {
    }

    @Override
    public void finished(Player player) {

    }

}