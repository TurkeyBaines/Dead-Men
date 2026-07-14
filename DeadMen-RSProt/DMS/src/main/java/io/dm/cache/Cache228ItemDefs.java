package io.dm.cache;

import io.dm.api.filestore.IndexFile;
import io.dm.network.rsprot.RsProt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reads item definitions straight from Cache228 ({@link RsProt#cache228}, the real rev-228 cache
 * served to clients) instead of {@link ItemDef#load()}'s {@code Server.fileStore}, which reads the
 * LEGACY cache. Exists because {@code dropOption}/{@code equipOption} computed from the legacy
 * cache's {@code inventoryOptions} strings don't reliably match the option numbers the real 228
 * client sends (confirmed live for item 4151 — see stages/STAGE-05-inventory-actions.txt and
 * HANDOFF.txt's "STAGE 05" section): legacy said Wield=2/Drop=5, the client actually sent 3 and 7.
 *
 * <p>Reuses {@link ItemDef#decodeStandalone(int, byte[])} — the exact same opcode parser
 * {@code ItemDef.load()} trusts for the legacy cache — pointed at a different archive, so this
 * stays byte-for-byte consistent with the legacy reader; only the source cache differs. Lazily
 * decoded and cached per item id since Cache228 reads hit disk.
 */
public final class Cache228ItemDefs {

    private static final int ITEM_ARCHIVE = 2;
    private static final int ITEM_GROUP = 10;
    private static final Map<Integer, ItemDef> cache = new ConcurrentHashMap<>();

    private Cache228ItemDefs() {
    }

    /** @return the Cache228-decoded def for {@code itemId}, or null if unavailable (cache not
     * loaded yet, id doesn't exist in Cache228, or decoding failed). Callers should fall back to
     * the legacy {@link ItemDef} in that case rather than failing the click. */
    public static ItemDef get(int itemId) {
        if (itemId < 0)
            return null;
        ItemDef def = cache.computeIfAbsent(itemId, Cache228ItemDefs::load);
        return def == MISSING ? null : def;
    }

    // Sentinel so computeIfAbsent doesn't retry a failed/missing lookup on every call.
    private static final ItemDef MISSING = new ItemDef();

    private static ItemDef load(int itemId) {
        if (RsProt.cache228 == null)
            return MISSING;
        try {
            IndexFile index = RsProt.cache228.get(ITEM_ARCHIVE);
            if (index == null)
                return MISSING;
            byte[] data = index.getFile(ITEM_GROUP, itemId);
            if (data == null)
                return MISSING;
            return ItemDef.decodeStandalone(itemId, data);
        } catch (Throwable t) {
            System.out.println("[Cache228ItemDefs] failed to decode item " + itemId + ": " + t);
            return MISSING;
        }
    }
}
