package io.dm.deadman.content.sigils;

import io.dm.deadman.content.sigils.tier1.*;
import io.dm.deadman.content.sigils.tier2.*;
import io.dm.deadman.content.sigils.tier3.*;

import lombok.Getter;

public class Sigils {

    public static Sigil get(int id) {
        System.out.println("[Sigils] Finding id{" + id + "}");
        for (Tier1 t1 : Tier1.values())
            if (t1.attuned == id || t1.unattuned == id)
                return t1.get();
        for (Tier2 t2 : Tier2.values())
            if (t2.attuned == id || t2.unattuned == id)
                return t2.get();
        for (Tier3 t3 : Tier3.values())
            if (t3.attuned == id || t3.unattuned == id)
                return t3.get();

        return null;
    }

    public static Sigil get(String name) {
        System.out.println("[Sigils] Finding name{" + name + "}");
        for (Tier1 t1 : Tier1.values())
            if (t1.name.equalsIgnoreCase(name))
                return t1.get();

        for (Tier2 t2 : Tier2.values())
            if (t2.name.equalsIgnoreCase(name))
                return t2.get();

        for (Tier3 t3 : Tier3.values())
            if (t3.name.equalsIgnoreCase(name))
                return t3.get();

        return null;
    }

    public enum Tier1 {
        // Combat Sigils
        SIGIL_OF_RESISTANCE(            28490, 28489, "Sigil of resistance",            new SigilOfResistance()),
        SIGIL_OF_RESILIENCE(            25991, 25990, "Sigil of resilience",            new SigilOfResilience()),
        SIGIL_OF_CONSISTENCY(           25994, 25993, "Sigil of consistency",           new SigilOfConsistency()),
        SIGIL_OF_THE_FORMIDABLE_FIGHTER(25997, 25996, "Sigil of the formidable fighter",new SigilOfTheFormidableFighter()),
        SIGIL_OF_THE_RIGOROUS_RANGER(   26000, 25999, "Sigil of the rigorous ranger",   new SigilOfTheRigorousRanger()),
        SIGIL_OF_THE_METICULOUS_MAGE(   26003, 26002, "Sigil of the meticulous mage",   new SigilOfTheMeticulousMage()),
        SIGIL_OF_DEFT_STRIKES(          26012, 26011, "Sigil of deft strikes",          new SigilOfDeftStrikes()),

        // Skilling Sigils
        SIGIL_OF_AGILE_FORTUNE(         28493, 28492, "Sigil of agile fortune",         new SigilOfAgileFortune()),
        SIGIL_OF_HOARDING(              28481, 28480, "Sigil of hoarding",              new SigilOfHoarding()),
        SIGIL_OF_SUSTENANCE(            28478, 28477, "Sigil of sustenance",            new SigilOfSustenance()),
        SIGIL_OF_THE_FORAGER(           26102, 26101, "Sigil of the forager",           new SigilOfTheForager()),
        SIGIL_OF_ENHANCED_HARVEST(      26018, 26017, "Sigil of enhanced harvest",      new SigilOfEnhancedHarvest()),
        SIGIL_OF_STORAGE(               26021, 26020, "Sigil of storage",               new SigilOfStorage()),
        SIGIL_OF_THE_ABYSS(             26039, 26038, "Sigil of the abyss",             new SigilOfTheAbyss()),

        // Utility Sigils
        SIGIL_OF_THE_ALCHEMANIAC(       28484, 28483, "Sigil of the alchemaniac",       new SigilOfTheAlchemaniac()),
        SIGIL_OF_THE_FOOD_MASTER(       28496, 28495, "Sigil of the food master",       new SigilOfTheFoodMaster()),
        SIGIL_OF_THE_WELL_FED(          28499, 28498, "Sigil of the well fed",          new SigilOfTheWellFed()),
        SIGIL_OF_STAMINA(               26042, 26041, "Sigil of stamina",               new SigilOfStamina()),
        SIGIL_OF_THE_POTION_MASTER(     26045, 26044, "Sigil of the potion master",     new SigilOfThePotionMaster()),
        SIGIL_OF_THE_ETERNAL_JEWELLER(  26048, 26047, "Sigil of the eternal jeweller",  new SigilOfTheEternalJeweller()),
        SIGIL_OF_MOBILITY(              26054, 26053, "Sigil of mobility",              new SigilOfMobility()),
        SIGIL_OF_EXAGGERATION(          26057, 26056, "Sigil of exaggeration",          new SigilOfExaggeration());

        @Getter private final int unattuned;
        @Getter private final int attuned;
        @Getter private final String name;
        private final Sigil sigil;
        @Getter private int anim;
        @Getter private int gfx;

        Tier1(int unattuned, int attuned, String name, Sigil sigil) {
            this.unattuned = unattuned;
            this.attuned = attuned;
            this.name = name;
            this.sigil = sigil;
        }
        Tier1(int unattuned, int attuned, String name, int anim, int gfx, Sigil sigil) {
            this.unattuned = unattuned;
            this.attuned = attuned;
            this.name = name;
            this.anim = anim;
            this.gfx = gfx;
            this.sigil = sigil;
        }

        public Sigil get() {
            return sigil;
        }
    }

    public enum Tier2 {
        // Combat Sigils
        SIGIL_OF_THE_AUGMENTED_THRALL(  28517, 28516, "Sigil of the augmented thrall",  new SigilOfTheAugmentedThrall()),
        SIGIL_OF_THE_LIGHTBEARER(       28508, 28507, "Sigil of the lightbearer",       new SigilOfTheLightbearer()),
        SIGIL_OF_PRECISION(             28514, 28513, "Sigil of precision",             new SigilOfPrecision()),
        SIGIL_OF_SPECIALISED_STRIKES(   26060, 26059, "Sigil of specialised strikes",   new SigilOfSpecialisedStrikes()),
        SIGIL_OF_THE_PORCUPINE(         26063, 26062, "Sigil of the porcupine",         new SigilOfThePorcupine()),
        SIGIL_OF_BINDING(               26066, 26065, "Sigil of binding",               new SigilOfBinding()),
        SIGIL_OF_FORTIFICATION(         26006, 26005, "Sigil of fortification",         new SigilOfFortification()),
        SIGIL_OF_THE_RUTHLESS_RANGER(   26072, 26071, "Sigil of the ruthless ranger",   new SigilOfTheRuthlessRanger()),
        SIGIL_OF_THE_FERAL_FIGHTER(     26075, 26074, "Sigil of the feral fighter",     new SigilOfTheFeralFighter()),
        SIGIL_OF_THE_MENACING_MAGE(     26078, 26077, "Sigil of the menacing mage", 10655, 1977, new SigilOfTheMenacingMage()),

        // Skilling Sigils
        SIGIL_OF_THE_HUNTER(            28487, 28486, "Sigil of the hunter",            new SigilOfTheHunter()),
        SIGIL_OF_THE_INFERNAL_CHEF(     28502, 28501, "Sigil of the infernal chef",     new SigilOfTheInfernalChef()),
        SIGIL_OF_THE_INFERNAL_SMITH(    28505, 28504, "Sigil of the infernal smith",    new SigilOfTheInfernalSmith()),
        SIGIL_OF_NATURE(                26096, 26095, "Sigil of nature",                new SigilOfNature()),
        SIGIL_OF_DEVOTION(              26099, 26098, "Sigil of devotion",              new SigilOfDevotion()),

        // Utility Sigils
        SIGIL_OF_FAITH(                 28520, 28519, "Sigil of faith",                 new SigilOfFaith()),
        SIGIL_OF_PROSPERITY(            26081, 26080, "Sigil of prosperity",            new SigilOfProsperity()),
        SIGIL_OF_SLAUGHTER(             26108, 26107, "Sigil of slaughter",             new SigilOfSlaughter()),
        SIGIL_OF_VERSATILITY(           26114, 26113, "Sigil of versatility",           new SigilOfVersatility()),
        SIGIL_OF_THE_SERPENT(           26117, 26116, "Sigil of the serpent",           new SigilOfTheSerpent()),
        SIGIL_OF_SUPREME_STAMINA(       26120, 26119, "Sigil of supreme stamina",       new SigilOfSupremeStamina()),
        SIGIL_OF_PRESERVATION(          26123, 26122, "Sigil of preservation",          new SigilOfPreservation());

        @Getter private final int unattuned;
        @Getter private final int attuned;
        @Getter private final String name;
        private final Sigil sigil;
        @Getter private int anim;
        @Getter private int gfx;

        Tier2(int unattuned, int attuned, String name, Sigil sigil) {
            this.unattuned = unattuned;
            this.attuned = attuned;
            this.name = name;
            this.sigil = sigil;
        }
        Tier2(int unattuned, int attuned, String name, int anim, int gfx, Sigil sigil) {
            this.unattuned = unattuned;
            this.attuned = attuned;
            this.name = name;
            this.anim = anim;
            this.gfx = gfx;
            this.sigil = sigil;
        }

        public Sigil get() {
            return sigil;
        }
    }

    public enum Tier3 {
        // Combat Sigils
        SIGIL_OF_TITANIUM(              28523, 28522, "Sigil of titanium",              new SigilOfTitanium()),
        SIGIL_OF_THE_NINJA(             28526, 28525, "Sigil of the ninja",             new SigilOfTheNinja()),
        SIGIL_OF_FINALITY(              26126, 26125, "Sigil of finality",              new SigilOfFinality()),
        SIGIL_OF_PIOUS_PROTECTION(      26129, 26128, "Sigil of pious protection",      new SigilOfPiousProtection()),
        SIGIL_OF_AGGRESSION(            26132, 26131, "Sigil of aggression",            new SigilOfAggression()),
        SIGIL_OF_RAMPAGE(               26135, 26134, "Sigil of rampage",               new SigilOfRampage()),

        // Skilling Sigils
        SIGIL_OF_WOODCRAFT(             28529, 28528, "Sigil of woodcraft",             new SigilOfWoodcraft()),
        SIGIL_OF_REMOTE_STORAGE(        26141, 26140, "Sigil of remote storage",        new SigilOfRemoteStorage()),

        // Utility Sigils
        SIGIL_OF_LAST_RECALL(           26144, 26143, "Sigil of last recall",           new SigilOfLastRecall()),
        SIGIL_OF_THE_GUARDIAN_ANGEL(    26147, 26146, "Sigil of the guardian angel",    new SigilOfTheGuardianAngel());

        @Getter private final int unattuned;
        @Getter private final int attuned;
        @Getter private final String name;
        private final Sigil sigil;
        @Getter private int anim;
        @Getter private int gfx;

        Tier3(int unattuned, int attuned, String name, Sigil sigil) {
            this.unattuned = unattuned;
            this.attuned = attuned;
            this.name = name;
            this.sigil = sigil;
        }
        Tier3(int unattuned, int attuned, String name, int anim, int gfx, Sigil sigil) {
            this.unattuned = unattuned;
            this.attuned = attuned;
            this.name = name;
            this.anim = anim;
            this.gfx = gfx;
            this.sigil = sigil;
        }

        public Sigil get() {
            return sigil;
        }
    }

}
