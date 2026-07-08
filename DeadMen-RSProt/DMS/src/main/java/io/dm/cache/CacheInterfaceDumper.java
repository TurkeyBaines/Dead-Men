package io.dm.cache;

import io.dm.Server;
import io.dm.api.filestore.IndexFile;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * One-shot startup dump of every interface group in the loaded cache (index 3, the same index
 * {@link InterfaceDef} reads) — id, total child component count, and any scripts/varp configs
 * {@link InterfaceDef#getScripts} can resolve for it — written to {@value #FILE_NAME} (project
 * root) as a permanent reference. See HANDOFF.txt.
 *
 * <p>Every per-interface step is individually try-caught so one missing or corrupt archive entry
 * only produces an "ERROR" line for that interface instead of aborting the dump or, worse,
 * server boot itself.
 */
public final class CacheInterfaceDumper {

    private static final String FILE_NAME = "CACHE_INTERFACE_DUMP.txt";
    private static final int INTERFACE_INDEX = 3;

    private CacheInterfaceDumper() {
    }

    public static void dump() {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME, false))) {
            out.println("Cache Interface Dump - generated "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            out.println();

            IndexFile idx = Server.fileStore == null ? null : Server.fileStore.get(INTERFACE_INDEX);
            if (idx == null) {
                out.println("Interface index (" + INTERFACE_INDEX + ") not found in loaded cache.");
                System.out.println("[CacheInterfaceDumper] interface index " + INTERFACE_INDEX + " unavailable, wrote empty dump.");
                return;
            }

            int lastArchiveId = idx.getLastArchiveId();
            int dumped = 0;
            int skipped = 0;
            for (int interfaceId = 0; interfaceId <= lastArchiveId; interfaceId++) {
                try {
                    if (dumpInterface(out, idx, interfaceId)) {
                        dumped++;
                    }
                } catch (Throwable t) {
                    skipped++;
                    out.println("==== Interface " + interfaceId + " ====");
                    out.println("  ERROR reading this interface group: " + t);
                    out.println();
                }
            }
            out.println("==== Summary: " + dumped + " interface groups dumped, " + skipped + " skipped due to errors ====");
            System.out.println("[CacheInterfaceDumper] wrote " + FILE_NAME + " (" + dumped + " interfaces, " + skipped + " skipped).");
        } catch (Throwable t) {
            System.out.println("[CacheInterfaceDumper] failed to write " + FILE_NAME + ": " + t);
        }
    }

    /** @return true if {@code interfaceId} existed in the archive and was written. */
    private static boolean dumpInterface(PrintWriter out, IndexFile idx, int interfaceId) {
        if (!idx.archiveExists(interfaceId)) {
            return false;
        }
        int childCount = idx.getLastFileId(interfaceId) + 1;
        out.println("==== Interface " + interfaceId + " ====");
        out.println("  Components: " + childCount);
        try {
            dumpConfigs(out, interfaceId);
        } catch (Throwable t) {
            out.println("  Configs: ERROR resolving scripts/configs: " + t);
        }
        out.println();
        return true;
    }

    /** Reuses {@link InterfaceDef#getScripts} (non-recursive) to list the scripts + varp/varbit configs an interface group uses. */
    private static void dumpConfigs(PrintWriter out, int interfaceId) {
        Set<ScriptDef> scripts = InterfaceDef.getScripts(interfaceId, false);
        if (scripts == null || scripts.isEmpty()) {
            out.println("  Scripts: none");
            return;
        }

        StringBuilder scriptIds = new StringBuilder("  Scripts:");
        for (ScriptDef script : scripts) {
            scriptIds.append(' ').append(script.id);
        }
        out.println(scriptIds);

        Set<Integer> varpIds = new LinkedHashSet<>();
        for (ScriptDef script : scripts) {
            for (int i = 0; i < script.instructions.length; i++) {
                int instruction = script.instructions[i];
                if (instruction == 1) {
                    varpIds.add(script.intArgs[i]);
                } else if (instruction == 25) {
                    Varpbit bit = Varpbit.get(script.intArgs[i]);
                    if (bit != null) {
                        varpIds.add(bit.varpId);
                    }
                }
            }
        }

        if (varpIds.isEmpty()) {
            out.println("  Configs: none");
            return;
        }
        out.println("  Configs:");
        for (int varpId : varpIds) {
            out.println("    varp " + varpId);
            Varp varp = Varp.get(varpId);
            if (varp != null && varp.bits != null) {
                for (Varpbit bit : varp.bits) {
                    out.println("      varbit " + bit.id + " (shift " + bit.leastSigBit + ")");
                }
            }
        }
    }
}
