package io.dm.model.inter;

import io.dm.model.entity.player.Player;

public enum InterfaceType {
    /**
     * Basic types
     * Fixed/resizable/resizable-stacked child slots below are sourced from AlterRSPS's real rev-228
     * {@code InterfaceDestination.kt} (fetched directly from github.com/AlterRSPS/Alter,
     * game-api/src/main/kotlin/org/alter/api/InterfaceDestination.kt, 2026-07-08 — Stage 04), NOT
     * the stale 184-era values this enum previously carried. That file's {@code getDisplayComponentId}
     * confirms parent ids 548 (fixed) / 161 (resizable) / 164 (resizable-list) match this enum's own
     * scheme exactly, so slot numbers map straight across with no translation.
     */
    MAIN( // AlterRSPS MAIN_SCREEN: fixed=9, resize=16, resizeList=16 (fullscreen=13, unused — server
          // only opens the fixed frame today, per Stage 04's scope note)
            new Component(548, 9),
            new Component(161, 16),
            new Component(164, 16),
            false
    ),
    INVENTORY( // AlterRSPS INVENTORY destination: fixed=83, resize=79, resizeList=75 (this is the
               // "swap the inventory tab slot" destination — matches FIXED_FRAME_PANES' own {149,83}
               // entry for the inventory tab itself, confirming the value)
            new Component(548, 83),
            new Component(161, 79),
            new Component(164, 75),
            false
    ),
    INVENTORY_OVERLAY( // same destination as INVENTORY above, just with the overlay flag set
            new Component(548, 83),
            new Component(161, 79),
            new Component(164, 75),
            true
    ),
    SOCIAL_TAB( // AlterRSPS SOCIAL destination: fixed=89, resize=85, resizeList=81
            new Component(548, 89),
            new Component(161, 85),
            new Component(164, 81),
            true
    ),
    CHATBOX( // Stage 11 (2026-07-13): 561 was the 184-era dialogue child. Rev 228's is 566 —
             // AlterRSPS QueueTaskExt.kt's CHATBOX_CHILD = 566 (every chat dialogue opens there),
             // corroborated by the deobbed cs2: chat_onsubchange/script113 only relayout + draw the
             // dialogue background when a sub sits on the chatbox's LAST two children (565/564 in the
             // older-rev dump where 162 had one fewer child; 566/565 in this cache's 567-component
             // 162). A sub on 561 still renders — it's just not a child the chatbox scripts watch,
             // which is why dialogs appeared background-less and never triggered a chatbox relayout.
            new Component(162, 566),
            false
    ),
    /**
     * TODO (Stage 04, 2026-07-08): NOT re-audited against 228 — untouched, left as the original
     * 184-era values. AlterRSPS's InterfaceDestination.kt (see MAIN's javadoc above) has no discrete
     * per-slot equivalent for these six legacy multi-overlay layers; it only exposes a single generic
     * {@code OVERLAY} destination (interfaceId=651, fixed=43, resize=6, resizeList=43). Reassigning
     * six distinct slots to one shared value on a hunch risks turning currently-maybe-correct values
     * into definitely-wrong ones (the same "don't guess" rule HANDOFF.txt invokes repeatedly for
     * Interface.BANK/BANK_INVENTORY). If any of these overlays are reported broken on a live 228
     * client, use the empirical `::modaltest <child>` sweep approach described in
     * stages/STAGE-04-main-interfaces.txt STEP 1c rather than guessing further.
     *   xAlignment = 0
     *   yAlignment = 0
     */
    WILDERNESS_OVERLAY( //unique type required for bounty hunter
            new Component(548, 15),
            new Component(161, 4),
            new Component(164, 4),
            true
    ),
    UNUSED_OVERLAY1(
            new Component(548, 17),
            new Component(161, 7),
            new Component(164, 7),
            true
    ),
    UNUSED_OVERLAY2(
            new Component(548, 18),
            new Component(161, 8),
            new Component(164, 8),
            true
    ),
    /**
     * Not sure how the following values effect the overlays, but they're the only difference in client..
     *   xAlignment = 1
     *   yAlignment = 1
     */
    SECONDARY_OVERLAY( //fading, castle wars game, snow falling
            new Component(548, 14),
            new Component(161, 3),
            new Component(164, 3),
            true
    ),
    PRIMARY_OVERLAY( //castle wars wait lobby, duel challenge area, gnomeball, puro puro imp view, mage training arena, lms lobby, tut
            new Component(548, 15),
            new Component(161, 4),
            new Component(164, 4),
            true
    ),
    TARGET_OVERLAY( //kinda guessing it uses this, doesn't really matter, as long as it has it's own type..
            new Component(548, 16),
            new Component(161, 5),
            new Component(164, 5),
            true
    ),

    POH_LOADING(
            new Component(548, 15),
            new Component(161, 3),
            new Component(164, 3),
            false
    ),

    WORLD_MAP( // AlterRSPS WORLD_MAP destination: fixed=42, resize=18, resizeList=15 (fullscreen=36,
               // unused). A distinct WORLD_MAP_FULL destination also exists (fixed=27) but is marked
               // clickThrough=false + @TODO in the source itself — WORLD_MAP is the one MainFrame's
               // normal world-map-button flow matches, so that's the one used here.
            new Component(548, 42),
            new Component(161, 18),
            new Component(164, 15),
            true
    ),
    ;

    public final Component fixedComponent;

    public final Component resizedComponent;

    public final Component resizedStackedComponent;

    public final int overlaySetting;

    InterfaceType(Component sharedComponent, boolean overlay) {
        this(sharedComponent, sharedComponent, sharedComponent, overlay);
    }

    InterfaceType(Component fixedComponent, Component resizedComponent, Component resizedStackedComponent, boolean overlay) {
        this.fixedComponent = fixedComponent;
        this.resizedComponent = resizedComponent;
        this.resizedStackedComponent = resizedStackedComponent;
        this.overlaySetting = overlay ? 1 : 0;
    }

    public void open(Player player, int id) {
        Component component = getComponent(player);
        player.getPacketSender().sendInterface(id, component.parentId, component.childId, overlaySetting);
    }

    public void close(Player player) {
        Component component = getComponent(player);
        player.getPacketSender().removeInterface(component.parentId, component.childId);
    }

    /**
     * Re-opens [id] into this type's slot with the game-frame pane setting (overlay=1), exactly as
     * {@code RsProt.openFrame}'s FIXED_FRAME_PANES loop does at login — used to restore a permanent
     * tab pane (e.g. the inventory, 149) after a temporary interface that had replaced it closes.
     * On the rev-228 IF3 frame an emptied child stays empty; nothing client-side restores the tab.
     */
    public void openTab(Player player, int id) {
        Component component = getComponent(player);
        player.getPacketSender().sendInterface(id, component.parentId, component.childId, 1);
    }

    private Component getComponent(Player player) {
        if(player.getGameFrameId() == 161)
            return resizedComponent;
        if(player.getGameFrameId() == 164)
            return resizedStackedComponent;
        return fixedComponent;
    }

    private static final class Component {

        final int parentId, childId;

        Component(int parentId, int childId) {
            this.parentId = parentId;
            this.childId = childId;
        }

    }

}