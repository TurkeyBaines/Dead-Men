package io.dm.deadman.content.sigils;

import io.dm.deadman.content.sigils.combat.*;
import io.dm.deadman.content.sigils.skilling.*;
import io.dm.deadman.content.sigils.utility.*;

public enum Sigils {

    /* === COMBAT === */
    Formidable_Fighter(0),
    Ruthless_Ranger(1),
    Menacing_Mage(2),
    Resistance(3),
    Deft_Strikes(4),
    Onslaught(5),
    Restoration(6),
    Meticulousness(7),
    Titanium(8),
    Conclusion(9),

    /* === SKILLING === */
    Internal_Chef(20),
    Deception(21),
    Hoarding(22),
    Litheness(23),
    Agile_Fortune(24),
    Efficiency(25),

    /* === UTILITY === */
    Faith(30),
    Food_Master(31),
    Potion_Master(32),
    Well_Fed(33),
    Revoked_Limitation(34),
    Eternal_Belief(35);

    public int id;

    Sigils(int id) {
        this.id = id;
    }

    Sigil create() {
        switch (id) {
            case 0:
                return new FormidableFighter();
            case 1:
                return new RuthlessRanger();
            case 2:
                return new MenacingMage();
            case 3:
                return new Resistance();
            case 4:
                return new DeftStrikes();
            case 5:
                return new Onslaught();
            case 6:
                return new Restoration();
            case 7:
                return new Meticulousness();
            case 8:
                return new Titanium();
            case 9:
                return new Conclusion();


            case 20:
                return new InternalChef();
            case 21:
                return new Deception();
            case 22:
                return new Hoarding();
            case 23:
                return new Litheness();
            case 24:
                return new AgileFortune();
            case 25:
                return new Efficiency();


            case 30:
                return new Faith();
            case 31:
                return new FoodMaster();
            case 32:
                return new PotionMaster();
            case 33:
                return new WellFed();
            case 34:
                return new RevokedLimitation();
            case 35:
                return new EternalBelief();
        }
        return null;
    }
}