package io.dm.model.combat;

public enum HitType {

    // rsprotSelfSplat/rsprotOtherSplat are real OSRS HitsplatID cache config ids (see RuneLite's
    // HitsplatID.java) — a totally separate numbering scheme from this enum's own ordinal, which is
    // only meaningful to the legacy 184 OutBuffer wire format (HitsUpdate.send()). Passing the raw
    // ordinal (0-6) straight into RSProt's addHitMark/addHeadBar landed on unrelated/undefined real
    // hitsplat ids (ordinal 1 = DAMAGE, the most common hit, has no real hitsplat id at all — nothing
    // rendered; ordinal 0 = BLOCKED landed on id 0 = CORRUPTION, an unrelated splat) — this table is
    // the fix, used only by the RSProt bridge (RsProt.pushMaskUpdates/pushNpcMaskUpdates), never by
    // the legacy send() path below.
    BLOCKED(true, 12, 13),
    DAMAGE(true, 16, 17),
    POISON(false, 65, 65),
    DISEASE1(false, 4, 4),
    DISEASE2(false, 3, 3),
    VENOM(false, 5, 5),
    HEAL(false, 6, 6);

    public final boolean resetActions;
    public final int rsprotSelfSplat;
    public final int rsprotOtherSplat;

    HitType(boolean resetActions, int rsprotSelfSplat, int rsprotOtherSplat) {
        this.resetActions = resetActions;
        this.rsprotSelfSplat = rsprotSelfSplat;
        this.rsprotOtherSplat = rsprotOtherSplat;
    }

}