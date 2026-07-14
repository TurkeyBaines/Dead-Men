package io.dm.cache;

import io.dm.api.filestore.IndexFile;
import io.dm.network.rsprot.RsProt;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * One-shot startup dump comparing every loaded item's LEGACY-cache {@code inventoryOptions}
 * (already in {@link ItemDef#cached} via {@link ItemDef#load()}) against the same item decoded
 * fresh from Cache228 ({@link Cache228ItemDefs}) — written to {@value #FILE_NAME} (project root).
 *
 * <p>Exists to answer a live-test report (2026-07-08): some items (Armadyl hilt 11810, godsword
 * blade 11798, coins 995, ...) show no "Use" menu option while most items do, even though nothing
 * in this codebase sets "Use" text programmatically (confirmed via grep — it must be literal
 * per-item cache text, same mechanism as "Wield"/"Drop"). This dump distinguishes the two possible
 * causes: (a) a genuine content gap — both caches agree the item has no such option, meaning it was
 * never authored, vs (b) a Cache228 read/parse problem specific to certain items (e.g. an unhandled
 * opcode desyncing {@link ItemDef#decodeStandalone}'s byte parser for that item) — which would show
 * up as Cache228 returning null/empty where the legacy cache has real content, or the two disagreeing
 * outright. See stages/STAGE-05-inventory-actions.txt and HANDOFF.txt's "STAGE 05" section.
 *
 * <p>Call only after {@link RsProt#cache228} is set (i.e. after {@code RsProt.start()} returns) —
 * unlike {@link CacheInterfaceDumper}, which only ever needed the legacy {@code Server.fileStore}.
 */
public final class ItemOptionDumper {

    private static final String FILE_NAME = "ITEM_OPTION_DUMP.txt";

    private ItemOptionDumper() {
    }

    public static void dump() {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME, false))) {
            out.println("Item Option Dump (legacy vs Cache228) - generated "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            out.println();

            if (RsProt.cache228 == null) {
                out.println("RsProt.cache228 not set yet - call ItemOptionDumper.dump() after RsProt.start() returns.");
                System.out.println("[ItemOptionDumper] cache228 unavailable, wrote empty dump.");
                return;
            }

            int mismatches = 0;
            int cache228Missing = 0;
            int bothEmpty = 0;
            int total = 0;
            // opcode -> (count of items hitting it as the first unrecognized opcode, one example id)
            Map<Integer, int[]> unknownOpcodeCounts = new TreeMap<>();
            Map<Integer, Integer> unknownOpcodeExample = new TreeMap<>();

            IndexFile cache228Items = RsProt.cache228.get(2);

            for (ItemDef legacy : ItemDef.cached.values()) {
                if (legacy == null)
                    continue;
                total++;

                // Read each item's raw Cache228 bytes EXACTLY ONCE and derive both the decoded def
                // and the unknown-opcode check from that same array. IndexFile.getFile()'s cache is
                // "consume and null on read" (see IndexFile.java:103-111): the first call to
                // getFile(10, anyId) decompresses the WHOLE ~24k-item archive in one shot, but each
                // individual id's slot is nulled the moment it's returned. Calling getFile(10, id)
                // a second time for the SAME id (as this used to do, once via Cache228ItemDefs.get()
                // and again directly here) finds that slot already null and re-triggers a full
                // re-decompression of the entire archive from scratch — ~24k redundant full-archive
                // decompressions, which is what actually made this dump take 15+ minutes. One read
                // per item avoids that entirely (the archive is decompressed once, on the first
                // item, and every other id's slot is still populated from that same pass).
                byte[] raw = cache228Items != null ? cache228Items.getFile(10, legacy.id) : null;
                ItemDef real = raw != null ? ItemDef.decodeStandalone(legacy.id, raw) : null;
                boolean legacyHasAny = hasAnyOption(legacy.inventoryOptions);
                boolean realHasAny = real != null && hasAnyOption(real.inventoryOptions);

                // Independent of the decoded result: does Cache228's raw byte stream for this item
                // hit an opcode decode(InBuffer,int) doesn't recognize? This is what actually causes
                // desync corruption (garbage text, wrong fields) rather than a clean "no options".
                if (raw != null) {
                    Integer unknown = ItemDef.findFirstUnknownOpcode(raw);
                    if (unknown != null && unknown != -1) {
                        unknownOpcodeCounts.computeIfAbsent(unknown, k -> new int[1])[0]++;
                        unknownOpcodeExample.putIfAbsent(unknown, legacy.id);
                    }
                }

                if (real == null) {
                    cache228Missing++;
                    if (legacyHasAny) {
                        out.println("id=" + legacy.id + " name=" + legacy.name
                                + " | legacy=" + Arrays.toString(legacy.inventoryOptions)
                                + " | cache228=MISSING (legacy has real options - possible Cache228 read/parse failure)");
                    }
                    continue;
                }

                if (!legacyHasAny && !realHasAny) {
                    bothEmpty++;
                    continue;
                }

                if (!Arrays.equals(legacy.inventoryOptions, real.inventoryOptions)) {
                    mismatches++;
                    out.println("id=" + legacy.id + " name=" + legacy.name
                            + " | legacy=" + Arrays.toString(legacy.inventoryOptions)
                            + " | cache228=" + Arrays.toString(real.inventoryOptions));
                }
            }

            out.println();
            out.println("==== Summary: " + total + " legacy items checked, " + mismatches
                    + " content mismatches, " + cache228Missing + " Cache228 reads failed/missing, "
                    + bothEmpty + " have no options in either cache (Drop-only or fully blank) ====");

            out.println();
            out.println("==== Unknown opcodes hit while parsing Cache228 item bytes (likely cause of ====");
            out.println("==== mismatches/corruption above - decode(InBuffer,int) has no case for these) ====");
            if (unknownOpcodeCounts.isEmpty()) {
                out.println("(none - every item's Cache228 bytes parsed using only known opcodes)");
            } else {
                for (Map.Entry<Integer, int[]> e : unknownOpcodeCounts.entrySet()) {
                    out.println("opcode " + e.getKey() + ": hit first on " + e.getValue()[0]
                            + " item(s), e.g. id=" + unknownOpcodeExample.get(e.getKey()));
                }
            }
            System.out.println("[ItemOptionDumper] wrote " + FILE_NAME + " (" + mismatches + " mismatches, "
                    + cache228Missing + " Cache228 failures, " + unknownOpcodeCounts.size()
                    + " distinct unknown opcodes, out of " + total + " items).");
        } catch (Throwable t) {
            System.out.println("[ItemOptionDumper] failed to write " + FILE_NAME + ": " + t);
        }
    }

    private static boolean hasAnyOption(String[] options) {
        if (options == null)
            return false;
        for (String s : options) {
            if (s != null && !s.equalsIgnoreCase("Drop"))
                return true;
        }
        return false;
    }
}
