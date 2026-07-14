package io.dm.deadman.content.sigils;

import io.dm.deadman.content.sigils.tier1.SigilOfResistance;
import io.dm.deadman.content.sigils.tier2.SigilOfTheMenacingMage;
import io.dm.model.entity.player.Player;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class Sigils {

    private static HashMap<Integer, Sigil> sigils = new HashMap<>();

    static {
        sigils.put(28490, new SigilOfTheMenacingMage());
    }

    public static Sigil get(int unattunedId) {
        if (sigils.containsKey(unattunedId))
            return sigils.get(unattunedId);
        return null;
    }

    public enum Tier1 {
        // Combat Sigils
        SIGIL_OF_RESISTANCE(28490, 28489, "Sigil of resistance", null),
        SIGIL_OF_RESILIENCE(25991, 25990, "Sigil of resilience", null),
        SIGIL_OF_CONSISTENCY(25994, 25993, "Sigil of consistency", null),
        SIGIL_OF_THE_RIGOROUS_RANGER(26000, 25999, "Sigil of the rigorous ranger", null),
        SIGIL_OF_THE_METICULOUS_MAGE(26003, 26002, "Sigil of the meticulous mage", null),
        SIGIL_OF_DEFT_STRIKES(26012, 26011, "Sigil of deft strikes", null),

        // Skilling Sigils
        SIGIL_OF_AGILE_FORTUNE(28493, 28492, "Sigil of agile fortune", null),
        SIGIL_OF_HOARDING(28481, 28480, "Sigil of hoarding", null),
        SIGIL_OF_SUSTENANCE(28478, 28477, "Sigil of sustenance", null),
        SIGIL_OF_THE_FORAGER(26102, 26101, "Sigil of the forager", null),
        SIGIL_OF_ENHANCED_HARVEST(26018, 26017, "Sigil of enhanced harvest", null),
        SIGIL_OF_STORAGE(26021, 26020, "Sigil of storage", null),
        SIGIL_OF_THE_ABYSS(26039, 26038, "Sigil of the abyss", null),
        SIGIL_OF_DECEPTION(29658, 29657, "Sigil of deception", null),
        SIGIL_OF_LITHENESS(29661, 29660, "Sigil of litheness", null),

        // Utility Sigils
        SIGIL_OF_THE_ALCHEMANIAC(28484, 28483, "Sigil of the alchemaniac", null),
        SIGIL_OF_THE_FOOD_MASTER(28496, 28495, "Sigil of the food master", null),
        SIGIL_OF_THE_WELL_FED(28499, 28498, "Sigil of the well fed", null),
        SIGIL_OF_THE_POTION_MASTER(26045, 26044, "Sigil of the potion master", null),
        SIGIL_OF_THE_ETERNAL_JEWELLER(26048, 26047, "Sigil of the eternal jeweller", null),
        SIGIL_OF_THE_TREASURE_HUNTER(26051, 26050, "Sigil of the treasure hunter", null),
        SIGIL_OF_MOBILITY(26054, 26053, "Sigil of mobility", null),
        SIGIL_OF_EXAGGERATION(26057, 26056, "Sigil of exaggeration", null);

        @Getter private final int unattuned;
        @Getter private final int attuned;
        @Getter private final String name;
        @Getter private final Sigil sigil;

        Tier1(int unattuned, int attuned, String name, Sigil sigil) {
            this.unattuned = unattuned;
            this.attuned = attuned;
            this.name = name;
            this.sigil = sigil;
        }
    }

    public enum Tier2 {
        // Combat Sigils
        SIGIL_OF_THE_AUGMENTED_THRALL(28517, 28516, "Sigil of the augmented thrall", null),
        SIGIL_OF_THE_LIGHTBEARER(28508, 28507, "Sigil of the lightbearer", null),
        SIGIL_OF_PRECISION(28514, 28513, "Sigil of precision", null),
        SIGIL_OF_SPECIALISED_STRIKES(26060, 26059, "Sigil of specialised strikes", null),
        SIGIL_OF_THE_PORCUPINE(26063, 26062, "Sigil of the porcupine", null),
        SIGIL_OF_BINDING(26066, 26065, "Sigil of binding", null),
        SIGIL_OF_FORTIFICATION(26006, 26005, "Sigil of fortification", null),
        SIGIL_OF_THE_RUTHLESS_RANGER(26072, 26071, "Sigil of the ruthless ranger", null),
        SIGIL_OF_THE_FORMIDABLE_FIGHTER(25997, 25996, "Sigil of the formidable fighter", null),
        SIGIL_OF_THE_MENACING_MAGE(26078, 26077, "Sigil of the menacing mage", Sigils.get(26078)),
        SIGIL_OF_THE_SWASHBUCKLER(29673, 29672, "Sigil of the swashbuckler", null),
        SIGIL_OF_THE_GUNSLINGER(29676, 29675, "Sigil of the gunslinger", null),
        SIGIL_OF_ARCANE_SWIFTNESS(29679, 29678, "Sigil of arcane swiftness", null),
        SIGIL_OF_THE_ADROIT(29664, 29663, "Sigil of the adroit", null),
        SIGIL_OF_ONSLAUGHT(29667, 29666, "Sigil of onslaught", null),
        SIGIL_OF_RESTORATION(29670, 29669, "Sigil of restoration", null),
        SIGIL_OF_BARROWS(26009, 26008, "Sigil of barrows", null),

        // Skilling Sigils
        SIGIL_OF_THE_HUNTER(28487, 28486, "Sigil of the hunter", null),
        SIGIL_OF_THE_INFERNAL_CHEF(28502, 28501, "Sigil of the infernal chef", null),
        SIGIL_OF_THE_INFERNAL_SMITH(28505, 28504, "Sigil of the infernal smith", null),
        SIGIL_OF_NATURE(26096, 26095, "Sigil of nature", null),
        SIGIL_OF_DEVOTION(26099, 26098, "Sigil of devotion", null),

        // Utility Sigils
        SIGIL_OF_REVOKED_LIMITATION(29652, 29651, "Sigil of revoked limitation", null),
        SIGIL_OF_LAST_RECALL(26144, 26143, "Sigil of last recall", null),
        SIGIL_OF_THE_BLOODHOUND(28511, 28510, "Sigil of the bloodhound", null),
        SIGIL_OF_FAITH(28520, 28519, "Sigil of faith", null),
        SIGIL_OF_PROSPERITY(26081, 26080, "Sigil of prosperity", null),
        SIGIL_OF_SLAUGHTER(26108, 26107, "Sigil of slaughter", null),
        SIGIL_OF_THE_FORTUNE_FARMER(26111, 26110, "Sigil of the fortune farmer", null),
        SIGIL_OF_VERSATILITY(26114, 26113, "Sigil of versatility", null),
        SIGIL_OF_THE_SERPENT(26117, 26116, "Sigil of the serpent", null),
        SIGIL_OF_PRESERVATION(26123, 26122, "Sigil of preservation", null);

        @Getter private final int unattuned;
        @Getter private final int attuned;
        @Getter private final String name;
        @Getter private final Sigil sigil;

        Tier2(int unattuned, int attuned, String name, Sigil sigil) {
            this.unattuned = unattuned;
            this.attuned = attuned;
            this.name = name;
            this.sigil = sigil;
        }
    }

}
