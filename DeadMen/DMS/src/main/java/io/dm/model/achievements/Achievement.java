package io.dm.model.achievements;

import io.dm.cache.Color;
import io.dm.model.achievements.listeners.experienced.*;
import io.dm.model.achievements.listeners.intro.CommenceSlaughter;
import io.dm.model.achievements.listeners.intro.TheBestiary;
import io.dm.model.achievements.listeners.master.ExpertRunecrafter;
import io.dm.model.achievements.listeners.novice.ImplingHunter;
import io.dm.model.achievements.listeners.novice.IntoTheAbyss;
import io.dm.model.achievements.listeners.novice.Lightness;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.shared.listeners.LoginListener;

public enum Achievement {

    /**
     * Intro
     */
    THE_BESTIARY(new TheBestiary(), AchievementCategory.Introductory),
    COMMENCE_SLAUGHTER(new CommenceSlaughter(), AchievementCategory.Introductory),
    //PRESETS(new PresetsIntro(), AchievementCategory.Introductory),

    /**
     * Novice
     */
    INTO_THE_ABYSS(new IntoTheAbyss(), AchievementCategory.Novice),
    //MORYTANIA_FARMING(new MorytaniaFarming(), AchievementCategory.Novice),
    LIGHTNESS(new Lightness(), AchievementCategory.Novice),
    IMPLING_HUNTER(new ImplingHunter(), AchievementCategory.Novice),

    /**
     * Experienced
     */
    DOWN_IN_THE_DIRT(new DownInTheDirt(), AchievementCategory.Experienced),
    ESSENCE_EXTRACTOR(new EssenceExtractor(), AchievementCategory.Experienced),
    GOLDEN_TOUCH(new GoldenTouch(), AchievementCategory.Experienced),
    NATURES_TOUCH(new NaturesTouch(), AchievementCategory.Experienced),
    ABYSSAL_DISTURBANCE(new AbyssalDisturbance(), AchievementCategory.Experienced),
    PRACTICE_MAKES_PERFECT(new PracticeMakesPerfect(), AchievementCategory.Experienced),
    QUICK_HANDS(new QuickHands(), AchievementCategory.Experienced),
    MY_ARMS_PATCH(new MyArmsPatch(), AchievementCategory.Experienced),
    WELCOME_TO_THE_JUNGLE(new WelcomeToTheJungle(), AchievementCategory.Experienced),
    DEMON_SLAYER(new DemonSlayer(), AchievementCategory.Experienced),

    /**
     * Master
     */
    EXPERT_RUNECRAFTER(new ExpertRunecrafter(), AchievementCategory.Master);

    private final AchievementListener listener;
    private final AchievementCategory category;

    Achievement(AchievementListener listener, AchievementCategory category) {
        this.listener = listener;
        this.category = category;
    }

    public void update(Player player) {
        AchievementStage oldStage = player.achievementStages[ordinal()];
        AchievementStage newStage = player.achievementStages[ordinal()];
        if(newStage != oldStage) {
            if(newStage == AchievementStage.STARTED) {
                player.sendMessage("<col=000080>You have started the achievement: <col=800000>" + getListener().name());
                getListener().started(player);
            } else if(newStage == AchievementStage.FINISHED) {
                player.sendMessage("<col=000080>You have completed the achievement: <col=800000>" + getListener().name());
                getListener().finished(player);
            }
        }
    }

    public boolean isStarted(Player player) {
        return player.achievementStages[ordinal()] != AchievementStage.NOT_STARTED;
    }

    public boolean isFinished(Player player) {
        return player.achievementStages[ordinal()] == AchievementStage.FINISHED;
    }

    /**
     * Misc
     */

    public static String slashIf(String string, boolean slash) {
        return slash ? ("<str>" + string + "</str>") : string;
    }

    public AchievementListener getListener() {
        return listener;
    }

    public AchievementCategory getCategory() {
        return category;
    }

    public static AchievementStage counterStage(int current, int start, int finish) {
        if (current >= finish)
            return AchievementStage.FINISHED;
        else if (current <= start)
            return AchievementStage.NOT_STARTED;
        return AchievementStage.STARTED;
    }

    public static void staticInit(){
        for (Achievement achievement : values()) {
            if(achievement.listener != null) achievement.listener.init();
        }
    }
    static {
        LoginListener.register(player -> {
            for (Achievement achievement : values()) {
                player.achievementStages[achievement.ordinal()] = achievement.getListener().stage(player); // forces achievements to have the correct state on login
            }
        });
    }
}