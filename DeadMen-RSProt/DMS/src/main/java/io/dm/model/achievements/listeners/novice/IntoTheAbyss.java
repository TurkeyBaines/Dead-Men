package io.dm.model.achievements.listeners.novice;

import io.dm.model.achievements.Achievement;
import io.dm.model.achievements.AchievementListener;
import io.dm.model.achievements.AchievementStage;
import io.dm.model.entity.player.Player;

public class IntoTheAbyss implements AchievementListener {
    @Override
    public String name() {
        return "Abyssal Crafter";
    }

    @Override
    public AchievementStage stage(Player player) {
        return player.enteredAbyss ? AchievementStage.FINISHED : AchievementStage.NOT_STARTED;
    }

    @Override
    public String[] lines(Player player, boolean finished) {
        return new String[] {
                Achievement.slashIf("Enter the depths, enter the danger. Many threats may be", finished),
                Achievement.slashIf("found here; only those with the will to survive make it", finished),
                Achievement.slashIf("there and back alive.", finished),
                "",
                Achievement.slashIf("<col=000080>Assignment</col>: Enter the Abyss.", finished),
                Achievement.slashIf("<col=000080>Reward</col>: Unlocks the small essence pouch.", finished),
        };
    }

    @Override
    public void started(Player player) {

    }

    @Override
    public void finished(Player player) {

    }

    public static void entered(Player player) {
        if (!Achievement.INTO_THE_ABYSS.isFinished(player)) {
            player.enteredAbyss = true;
            Achievement.INTO_THE_ABYSS.update(player);
        }
    }
}
