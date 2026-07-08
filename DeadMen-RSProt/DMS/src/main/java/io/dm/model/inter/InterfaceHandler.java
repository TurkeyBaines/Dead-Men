package io.dm.model.inter;

import io.dm.cache.InterfaceDef;
import io.dm.model.entity.player.Player;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class InterfaceHandler {

    public static InterfaceHandler[] HANDLERS = new InterfaceHandler[InterfaceDef.COUNTS == null ? 712 : InterfaceDef.COUNTS.length];

    public static final InterfaceHandler EMPTY_HANDLER = new InterfaceHandler();

    public final int id;

    public final InterfaceAction[] actions;

    public BiConsumer<Player, Integer> closedAction;

    public InterfaceHandler() {
        this.id = -1;
        this.actions = new InterfaceAction[0];
    }

    /**
     * Sizes {@link #actions} from {@code Math.max(InterfaceDef.COUNTS[id], id's real child count)} is
     * not knowable here, so this grows defensively instead: same legacy-vs-228 staleness class as the
     * bug already found and fixed in {@code Player.setVisibleInterface}/{@code ensureVisibleCapacity}
     * (interface 548's legacy child count of 80 undershooting the real 228 client's 93+). This
     * constructor is only ever invoked with a literal, hardcoded {@code id} from a {@code static}
     * registration block elsewhere, so a too-small {@link InterfaceDef#COUNTS} entry for some
     * interface would previously either crash the class's static init (if that class's own registered
     * child indices exceed it) or — more insidiously — silently cap {@link #actions}' length below the
     * real 228 client's actual child range, making {@link #getAction} return null (indistinguishable
     * from "no game action") for any legitimately-clicked child at or beyond that count. Falls back to
     * a minimum size of {@code id + 1} slot so construction itself can never throw even if
     * {@code InterfaceDef.COUNTS[id]} is 0.
     */
    protected InterfaceHandler(int id) {
        this.id = id;
        this.actions = new InterfaceAction[Math.max(InterfaceDef.COUNTS[id], 1)];
        ensureHandlerCapacity(id);
        HANDLERS[id] = this;
    }

    /** Grows {@link #HANDLERS} to cover {@code interfaceId} if it doesn't already fit (see constructor doc). */
    private static void ensureHandlerCapacity(int interfaceId) {
        if (interfaceId >= HANDLERS.length) {
            InterfaceHandler[] grown = new InterfaceHandler[interfaceId + 1];
            System.arraycopy(HANDLERS, 0, grown, 0, HANDLERS.length);
            HANDLERS = grown;
        }
    }

    public static void register(int interfaceId, Consumer<InterfaceHandler> consumer) {
        InterfaceHandler handler = new InterfaceHandler(interfaceId);
        consumer.accept(handler);
        HANDLERS[interfaceId] = handler;
    }

    public static InterfaceAction getAction(Player player, int interfaceHash) {
        int interfaceId = interfaceHash >> 16;
        int childId = interfaceHash & 0xffff;
        if(childId == 65535)
            childId = -1;
        return getAction(player, interfaceId, childId);
    }

    public static InterfaceAction getAction(Player player, int interfaceId, int childId) {
        if(interfaceId < 0 || interfaceId >= HANDLERS.length)
            return null;
        if(!player.isVisibleInterface(interfaceId)) {
            return null;
        }
        InterfaceHandler handler = HANDLERS[interfaceId];
        if(handler == null)
            return null;
        if(childId < 0 || childId >= handler.actions.length)
            return null;
        return handler.actions[childId];
    }

}