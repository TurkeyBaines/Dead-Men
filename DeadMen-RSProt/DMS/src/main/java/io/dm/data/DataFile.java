package io.dm.data;

import io.dm.Server;
import io.dm.api.utils.FileUtils;
import io.dm.api.utils.JsonUtils;
import io.dm.api.utils.PackageLoader;
import io.dm.api.utils.ServerWrapper;
import io.dm.model.entity.player.Player;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//If you're wondering why the files in the impl folder are named unconventionally,
//it's so that their file names don't clash or match with actual model file names..
//Example: npc_combat.java and NPCCombat.java
@Slf4j
public abstract class DataFile {

    public abstract String path();


    /**
     * Loads the
     * @param fileName
     * @param json
     * @return
     */
    public Object fromJson(String fileName, String json) {
        throw new UnsupportedOperationException("neither @fromJson(loadedFile, fileName, json) or @fromJson(fileName, json) has been overridden!");
    }

    public Object fromJson(File loadedFile, String fileName, String json){
        return fromJson(fileName, json);
    }

    public int priority() { return Integer.MAX_VALUE; }

    public static void load() throws Exception {
        if (Server.dataFolder.exists() && Server.dataFolder.isDirectory())
            loadUnpacked();
        else
            loadPacked();
    }

    public static void loadUnpacked() throws Exception {
        List<PackedFile> packedFiles = new ArrayList<>();
        /**
         * Find all files
         */
        List<DataFile> dataFiles = new ArrayList<>();
        for(Class c : PackageLoader.load("io.dm.data.impl", DataFile.class))
            dataFiles.add((DataFile) c.newInstance());
        dataFiles.sort(Comparator.comparingInt(DataFile::priority));
        for(DataFile dataFile : dataFiles)
            packedFiles.addAll(load(dataFile));
        /**
         * Save all files to one "packed" file.
         */
//        File packedFile = Server.getFile("server-data.json");
//        JsonUtils.toFile(packedFile, JsonUtils.toJson(packedFiles));
    }

    public static void loadPacked() throws Exception {
        ServerWrapper.println("Data folder does not exist, attempting to load packed data...");
        File packedFile = Server.getFile("server-data.json");
        if(!packedFile.exists())
            throw new IOException(packedFile.getAbsolutePath() + " not found!");
        List<PackedFile> packedFiles = JsonUtils.fromJson(JsonUtils.fromFile(packedFile), List.class, PackedFile.class);
        packedFiles.forEach(PackedFile::load);
    }

    public static List<PackedFile> load(DataFile dataFile) throws Exception {
        /**
         * Load unpacked files
         */
        List<File> unpackedFiles = new ArrayList<>();
        String path = dataFile.path();
        int i = path.indexOf("*");
        if(i != -1) {
            File searchFolder = FileUtils.get(Server.dataFolder.getAbsolutePath() + "/" + path.substring(0, i));
            String searchExtension = path.substring(i + 1, path.length());
            if(!searchFolder.exists()){
                log.warn("Folder {} was not found!", searchFolder);
                return new ArrayList<>();
            }
            Files.walk(searchFolder.toPath()).forEach(p -> {
                File unpackedFile = p.toAbsolutePath().toFile();
                if(unpackedFile.isDirectory() || !unpackedFile.getName().endsWith(searchExtension))
                    return;
                unpackedFiles.add(unpackedFile);
            });
        } else {
            File unpackedFile = FileUtils.get(Server.dataFolder.getAbsolutePath() + "/" + path);
            unpackedFiles.add(unpackedFile);
        }
        unpackedFiles.sort(Comparator.comparing(File::getName));
        /**
         * Pack files
         */
        List<PackedFile> packedFiles = new ArrayList<>();
        for(File unpackedFile : unpackedFiles) {
            if(!unpackedFile.exists()) {
                System.err.println("Failed to find data file: " + unpackedFile.getAbsolutePath());
                continue;
            }
            try {
                packedFiles.add(new PackedFile(dataFile, unpackedFile));
            } catch(Throwable t) {
                ServerWrapper.logError("DataFile Failure: " + unpackedFile.getAbsolutePath(), t);
            }
        }
        return packedFiles;
    }

    public static void reload(Player player, Class<? extends DataFile> c) {
        String name = c.getSimpleName();
        try {
            player.sendMessage("Reloading data files for class \"" + name + "\" ...");
            List<PackedFile> files = load(c.newInstance());
            if(files.size() == 1)
                player.sendMessage("Successfully reloaded 1 data file for class \"" + name + "\" !");
            else
                player.sendMessage("Successfully reloaded " + files.size() + " data files for class \"" + name + "\" !");
        } catch(Exception e) {
            ServerWrapper.logError("Failed to reload data files for class \"" + name + "\" !", e);
        }
    }

    public static final class PackedFile {

        final String loaderClass;

        final String fileName;

        final String json;

        PackedFile(DataFile dataFile, File unpackedFile) throws IOException {
            this(dataFile, unpackedFile, false);
        }

        /**
         * PERFORMANCE (2026-07-18): {@code computePackedJson} lets callers skip re-serializing
         * the parsed result back into a JSON string via Gson. That string ({@link #json}) is only
         * ever read by {@link #loadPacked()}, replaying a previously-written
         * {@code server-data.json} — but the write path that would produce that file
         * ({@code loadUnpacked()}'s own commented-out {@code JsonUtils.toFile(...)} call) has been
         * dead code for a while, so in practice nothing ever reads {@code server-data.json} either.
         * {@link #load(DataFile)} (used by both {@code loadUnpacked()}, the live boot path since
         * this project's {@code data/} folder always exists, and the in-game {@code ::reload}
         * command) never used the returned {@code json} field for anything beyond the discarded
         * {@code packedFiles} list — every one of ~1640 JSON data files was paying for a full Gson
         * re-serialization of its own parsed object graph (NPC combat data, spawn lists, ...)
         * purely to build a value nobody read. Measured cost: real work
         * ({@code dataFile.fromJson(...)}, actually building the game's NPC/spawn/item data) plus
         * this wasted step together were ~7s of a ~26s boot (see git history) — skipping the waste
         * is the safe part of that; the real parse work is unchanged and still runs every time.
         */
        PackedFile(DataFile dataFile, File unpackedFile, boolean computePackedJson) throws IOException {
            loaderClass = dataFile.getClass().getName();
            fileName = unpackedFile.getName().replace(".json", "");

            String rawJson = JsonUtils.fromFile(unpackedFile);
            if (Server.dataOnlyMode) {
                json = rawJson;
            } else {
                Object parsed = dataFile.fromJson(unpackedFile, fileName, rawJson);
                json = computePackedJson ? JsonUtils.GSON_EXPOSE.toJson(parsed) : null;
            }
        }

        void load() {
            try {
                DataFile dataFile = (DataFile) Class.forName(loaderClass).newInstance();
                dataFile.fromJson(fileName, json);
            } catch(Exception e) {
                ServerWrapper.logError("Failed to load packed data file! class=" + loaderClass, e);
            }
        }

    }

    public static int priority(String dataName, int priority) {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        char c = dataName.toLowerCase().charAt(0);
        int i = alphabet.indexOf(c) + 1;
        return (i * 100000) + priority;
    }

}