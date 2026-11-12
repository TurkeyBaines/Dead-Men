package io.dm.cache;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.dm.Server;
import io.dm.api.filestore.FileStore;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One-shot standalone audit+fix: loads Cache228 directly (no full server boot) and cross-checks
 * every npc spawn JSON in DMS/data/npcs/spawns against the real in-cache NPC name for each
 * spawn's id. The spawn files were authored against an older cache revision (184); Cache228 is
 * newer, so ids that used to resolve to the named NPC may now resolve to something else (or to a
 * bare/placeholder def with no name at all — cache reused that id slot for later content).
 *
 * For every entry whose id doesn't resolve to its stated name in Cache228, this looks up every id
 * Cache228 actually has registered under that name (NPCDef#getByName) and rewrites the entry to
 * one of them, round-robining through the candidates per name so repeated spawns of the same NPC
 * get some visual variance instead of all collapsing onto a single id. Candidates that are
 * themselves placeholder/inert defs (no combat level and no right-click options) are skipped so a
 * "fix" never points a spawn at a non-functional NPC. Entries whose name has no real candidate at
 * all are left untouched and reported separately — those need a human call, not a guess.
 *
 * Writes NPC_SPAWN_AUDIT.txt (repo root) summarizing every change made and everything left as a
 * manual TODO, and rewrites the spawn JSON files in place (same 2-space-indent pretty format).
 */
public final class NpcSpawnAudit {

    private static final Map<String, Integer> ROTATION = new HashMap<>();

    public static void main(String[] args) throws Exception {
        String cachePath = args.length > 0 ? args[0] : "Cache228/cache";
        String spawnsDir = args.length > 1 ? args[1] : "DMS/data/npcs/spawns";

        Server.fileStore = new FileStore(cachePath);
        NPCDef.load();
        System.out.println("Loaded " + NPCDef.cached.size() + " NPC defs from cache.");

        File dir = new File(spawnsDir);
        File[] jsonFiles = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (jsonFiles == null) {
            System.err.println("No spawn files found in " + dir.getAbsolutePath());
            return;
        }

        com.google.gson.Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();

        try (PrintWriter out = new PrintWriter(new FileWriter("NPC_SPAWN_AUDIT.txt", false))) {
            int totalEntries = 0, totalMismatches = 0, totalFixed = 0, totalUnfixable = 0;
            for (File f : jsonFiles) {
                String json = new String(Files.readAllBytes(f.toPath()));
                JsonElement root = new JsonParser().parse(json);
                if (!root.isJsonArray())
                    continue;
                JsonArray arr = root.getAsJsonArray();
                List<String> fixedLines = new ArrayList<>();
                List<String> unfixableLines = new ArrayList<>();
                boolean changed = false;

                for (JsonElement el : arr) {
                    JsonObject obj = el.getAsJsonObject();
                    if (!obj.has("id") || !obj.has("name"))
                        continue;
                    totalEntries++;
                    int id = obj.get("id").getAsInt();
                    String jsonName = obj.get("name").getAsString();
                    NPCDef def = NPCDef.get(id);
                    String cacheName = def == null ? null : def.name;

                    if (matches(jsonName, cacheName))
                        continue;

                    totalMismatches++;
                    String loc = (obj.has("x") && obj.has("y"))
                            ? " at (" + obj.get("x").getAsInt() + "," + obj.get("y").getAsInt() + ")" : "";

                    List<NPCDef> candidates = usableCandidates(jsonName);
                    if (candidates.isEmpty()) {
                        totalUnfixable++;
                        unfixableLines.add("  [id " + id + "] \"" + jsonName + "\"" + loc
                                + " -> no usable cache NPC for this name in Cache228; left unchanged (TODO manual review)");
                        continue;
                    }

                    NPCDef chosen = rotate(jsonName, candidates);
                    fixedLines.add("  [id " + id + " -> " + chosen.id + "] \"" + jsonName + "\"" + loc
                            + " (cache had \"" + cacheName + "\" at old id)");
                    obj.addProperty("id", chosen.id);
                    totalFixed++;
                    changed = true;
                }

                if (changed) {
                    try (PrintWriter fw = new PrintWriter(new FileWriter(f, false))) {
                        fw.print(gsonPretty.toJson(arr));
                    }
                }
                if (!fixedLines.isEmpty() || !unfixableLines.isEmpty()) {
                    out.println("==== " + f.getName() + " (" + fixedLines.size() + " fixed, "
                            + unfixableLines.size() + " unfixable) ====");
                    for (String m : fixedLines) out.println(m);
                    for (String m : unfixableLines) out.println(m);
                    out.println();
                }
            }
            out.println("==== Summary: " + totalMismatches + " / " + totalEntries + " mismatched — "
                    + totalFixed + " auto-fixed, " + totalUnfixable + " left for manual review ====");
            System.out.println("Audit complete: " + totalFixed + " fixed, " + totalUnfixable
                    + " unfixable, out of " + totalMismatches + " mismatches / " + totalEntries + " entries.");
        }
    }

    /** Candidates registered under this name that aren't bare/inert placeholder defs. */
    private static List<NPCDef> usableCandidates(String name) {
        List<NPCDef> result = new ArrayList<>();
        for (NPCDef def : NPCDef.getByName(name)) {
            boolean hasOption = false;
            if (def.options != null)
                for (String o : def.options)
                    if (o != null) { hasOption = true; break; }
            if (hasOption || def.combatLevel > 0)
                result.add(def);
        }
        return result;
    }

    private static NPCDef rotate(String name, List<NPCDef> candidates) {
        int i = ROTATION.merge(name.toLowerCase(), 1, Integer::sum) - 1;
        return candidates.get(i % candidates.size());
    }

    private static boolean matches(String jsonName, String cacheName) {
        if (cacheName == null)
            return false;
        if (jsonName.equalsIgnoreCase(cacheName))
            return true;
        // tolerate punctuation/whitespace-only differences, e.g. "Sir Tiffy Cashien" vs itself,
        // apostrophe variants, "null" placeholder names, etc.
        String a = jsonName.toLowerCase().replaceAll("[^a-z0-9]", "");
        String b = cacheName.toLowerCase().replaceAll("[^a-z0-9]", "");
        return a.equals(b);
    }
}
