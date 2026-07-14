package io.dm.network.logging;

import io.dm.model.entity.player.Player;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Permanent, file-based dump of every incoming interaction packet (button clicks, drags, entity
 * clicks, item-on-X combinations, ...). Appends one timestamped line per packet to
 * {@value #FILE_NAME} (project root) so live click sessions build up a raw, total source of truth
 * for rev 228 client behaviour instead of relying on ad hoc console prints. See HANDOFF.txt.
 *
 * <p>Replaces the old temporary {@code [BUTTON]}/{@code [DRAG]}/{@code [INV-CLICK]} console
 * diagnostics (RsProt.java / TabInventory.java) with a single always-on, uniform logger.
 */
public final class NetworkLogger {

    private static final String FILE_NAME = "PROTOCOL_DUMP.txt";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Object LOCK = new Object();

    private NetworkLogger() {
    }

    /**
     * Logs one incoming interaction packet. Interface/component/option/slot fields that don't
     * apply to a given packet type (e.g. a world entity click has no interface/component) should
     * be passed as -1 rather than omitted, so every line in the dump has the same shape.
     *
     * @param player        the player the packet came from (may be null)
     * @param packetName    raw RSProt message class name, e.g. "If3Button", "OpLoc"
     * @param interfaceId   interface id, or -1 if not applicable
     * @param componentId   component (child) id, or -1 if not applicable
     * @param option        click option (1-10 style), or -1 if not applicable
     * @param slot          slot/sub index, or -1 if not applicable
     * @param itemOrTargetId item id, or the target's id/index (npc/player/loc/ground-item), or -1
     */
    public static void log(Player player, String packetName, int interfaceId, int componentId,
                            int option, int slot, int itemOrTargetId) {
        log(player, packetName, interfaceId, componentId, option, slot, itemOrTargetId, null);
    }

    /**
     * Same as {@link #log(Player, String, int, int, int, int, int)} with an extra free-form
     * segment appended for packet-specific raw data that doesn't fit the common field set (e.g.
     * world coordinates, or the second target of an item-on-item combination).
     */
    public static void log(Player player, String packetName, int interfaceId, int componentId,
                            int option, int slot, int itemOrTargetId, String extra) {
        String line = "[" + LocalDateTime.now().format(TIMESTAMP_FORMAT) + "] "
                + "player=" + (player == null ? "?" : player.getName())
                + " packet=" + packetName
                + " interface=" + interfaceId
                + " component=" + componentId
                + " option=" + option
                + " slot=" + slot
                + " item=" + itemOrTargetId
                + (extra == null || extra.isEmpty() ? "" : " | " + extra);
        write(line);
    }

    /**
     * Unpacks the interface id half of RSProt's combined id
     * ({@code (interfaceId << 16) | (componentId & 0xffff)}, per HANDOFF.txt's "Why the plumbing
     * 'just fit'" note), normalizing the 0xffff "no value" sentinel to -1. Needed because the
     * item-on-X message types (OpLocT/OpObjT/OpNpcT/OpPlayerT/IfButtonT) only expose the packed
     * combined id, not separate interface/component getters.
     */
    public static int interfaceIdFromCombined(int combinedId) {
        int id = (combinedId >> 16) & 0xffff;
        return id == 0xffff ? -1 : id;
    }

    /** Component id half of a combined id. See {@link #interfaceIdFromCombined(int)}. */
    public static int componentIdFromCombined(int combinedId) {
        int id = combinedId & 0xffff;
        return id == 0xffff ? -1 : id;
    }

    /**
     * Stage 05 diagnostic: logs the server-side option resolution for an inventory item click that
     * fell through {@code TabInventory.click} without matching a custom action, drop, or equip
     * branch, so a live-test can be correlated against {@link #log}'s recorded {@code option}
     * without guessing at the item's cache-derived option table. See stages/STAGE-05-inventory
     * -actions.txt's "PRIME UNTESTED HYPOTHESIS".
     */
    public static void logItemOptionMismatch(Player player, int itemId, int receivedOption,
                                               int dropOption, int equipOption, String[] inventoryOptions) {
        logItemOptionMismatch(player, itemId, receivedOption, dropOption, equipOption, inventoryOptions, "unhandled");
    }

    /** Same as above, with an explicit reason (e.g. "locked", "slot/id mismatch", "unhandled"). */
    public static void logItemOptionMismatch(Player player, int itemId, int receivedOption,
                                               int dropOption, int equipOption, String[] inventoryOptions,
                                               String reason) {
        String line = "[" + LocalDateTime.now().format(TIMESTAMP_FORMAT) + "] "
                + "player=" + (player == null ? "?" : player.getName())
                + " packet=ItemOptionMismatch"
                + " reason=" + reason
                + " item=" + itemId
                + " receivedOption=" + receivedOption
                + " dropOption=" + dropOption
                + " equipOption=" + equipOption
                + " inventoryOptions=" + java.util.Arrays.toString(inventoryOptions);
        write(line);
    }

    private static void write(String line) {
        synchronized (LOCK) {
            try (PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME, true))) {
                out.println(line);
            } catch (IOException e) {
                System.out.println("[NetworkLogger] failed to write " + FILE_NAME + ": " + e);
            }
        }
    }
}
