package io.dm.data.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.dm.Server;
import io.dm.data.DataFile;
import io.dm.model.map.MultiZone;
import io.dm.model.map.Region;
import io.dm.model.map.dynamic.DynamicMap;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class region_keys extends DataFile {

    public static final int[] NULL_KEYS = new int[4];

    @Override
    public String path() {
        return "region_keys.json";
    }

    @Override
    public int priority() {
        return 2;
    }

    /**
     * Region keys come from the stock cache's own {@code xteas.json} (sibling of {@link
     * Server#cacheDir}), the same OpenRS2 key set {@code RsProt.loadXteas} feeds the client
     * protocol with — NOT from the {@code json} DataFile content, which was a hand-maintained
     * key dump left over from the legacy 184 cache and is stale/wrong against the stock cache.
     */
    @Override
    public Object fromJson(String fileName, String json) {

        Map<Integer, int[]> keys = loadStockXteas();
        for(int regionId = 0; regionId < Region.LOADED.length; regionId++) {
            Region region = new Region(regionId);
            region.keys = keys.get(regionId);
            Region.LOADED[regionId] = region;
        }

        // Load each region. The old code ran a throwaway getLandscapeData() trial read per
        // keyed region first (isValid()) purely to decide whether to null out a bad key before
        // this real pass - removed as genuinely redundant (region.init() already try-catches its
        // own failure below, so a bad key degrades gracefully either way) and NOT a fix for
        // anything: verified directly that the ~84 regions that fail here still fail identically
        // whether or not that extra trial read happens first, so IndexFile's "decompress once,
        // null the slot on read" file cache was never the cause of the corruption below.
        //
        // Those ~84 regions (see a fresh boot's "Error loading region" lines) decrypt and
        // decompress cleanly - inspecting one's raw landscape bytes directly showed no exception
        // and a plausible-looking length - but Region.init()'s own object-placement parse
        // (readSmart deltas) degrades into negative/out-of-range values partway through the
        // buffer and runs off the end looking for a terminator that was never there. Their base
        // coordinates cluster almost entirely in OSRS's non-mainland "instance template" Y-bands
        // (~4224-5120 and ~9280-10240), not explorable content, and none of them account for any
        // of the still-open object/tile lookup failures elsewhere in this codebase (checked each
        // one's own region id directly - no overlap). Root cause not resolved: would need either
        // a reference decoder for this exact cache format or a live rev-228 client capture of
        // one of these specific squares to tell a genuinely-wrong/stale key apart from an
        // encoding detail unique to instance-template storage that this legacy parser predates.
        for(Region region : Region.LOADED) {
            try {
                region.init();
            } catch (Exception ex) {
                System.err.println("Error loading region " + region.id);
                ex.printStackTrace();
            }
        }

        MultiZone.load();
        DynamicMap.load();
        return keys;
    }

    /**
     * Parses the OpenRS2-format {@code xteas.json} that ships alongside the stock cache
     * (sibling of {@link Server#cacheDir}) — same file and format {@code RsProt.loadXteas}
     * reads for the client protocol. Regions absent from the file are unencrypted (no entry
     * needed, not "missing"); only {@link #isValid} results get logged as real problems.
     */
    private static Map<Integer, int[]> loadStockXteas() {
        Map<Integer, int[]> keys = new HashMap<>();
        File cacheDir = Server.cacheDir;
        File xteaFile = new File(cacheDir != null ? cacheDir.getParentFile() : new File("."), "xteas.json");
        try (Reader r = new FileReader(xteaFile)) {
            JsonArray arr = new JsonParser().parse(r).getAsJsonArray();
            for (JsonElement e : arr) {
                JsonObject o = e.getAsJsonObject();
                int mapsquare = o.get("mapsquare").getAsInt();
                JsonArray k = o.getAsJsonArray("key");
                keys.put(mapsquare, new int[]{
                        k.get(0).getAsInt(), k.get(1).getAsInt(), k.get(2).getAsInt(), k.get(3).getAsInt(),
                });
            }
            System.out.println("[region_keys] loaded " + keys.size() + " keys from " + xteaFile);
        } catch (Exception ex) {
            System.out.println("[region_keys] failed to load " + xteaFile + " (" + ex + "); all regions treated as unencrypted");
        }
        return keys;
    }

}