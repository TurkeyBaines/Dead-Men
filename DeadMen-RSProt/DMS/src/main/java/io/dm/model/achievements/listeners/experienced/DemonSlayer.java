package io.dm.model.achievements.listeners.experienced;

import io.dm.api.utils.NumberUtils;
import io.dm.model.achievements.Achievement;
import io.dm.model.achievements.AchievementListener;
import io.dm.model.achievements.AchievementStage;
import io.dm.model.entity.npc.NPC;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.player.PlayerCounter;

public class DemonSlayer implements AchievementListener {
    @Override
    public String name() {
        return "Demon Slayer";
    }

    @Override
    public AchievementStage stage(Player player) {
        int amount = PlayerCounter.DEMON_KILLS.get(player);
        if (amount == 0)
            return AchievementStage.NOT_STARTED;
        else if (amount >= 300)
            return AchievementStage.FINISHED;
        else
            return AchievementStage.STARTED;
    }

    @Override
    public String[] lines(Player player, boolean finished) {
        return new String[]{
                Achievement.slashIf("Prove yourself worthy of the Darklight sword by slaying demons. ", finished),
                "",
                Achievement.slashIf("<col=000080>Assignment</col>: Kill 300 demons of any kind.", finished),
                Achievement.slashIf("<col=000080>Reward</col>: Ability to purchase and equip Darklight.", finished),
                "",
                Achievement.slashIf("<col=000080>Demons slain</col>: " + NumberUtils.formatNumber(PlayerCounter.DEMON_KILLS.get(player)), finished),
        };
    }

    @Override
    public void started(Player player) {

    }

    @Override
    public void finished(Player player) {

    }

    public static void check(Player player, NPC killed) {
        if (killed.getDef().demon)
            PlayerCounter.DEMON_KILLS.increment(player, 1);
    }
}
