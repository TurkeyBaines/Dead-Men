import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class XteaToRegionMap {

    private static final String DIR = System.getProperty("user.dir");
    // Using double backslashes for Windows, but File.separator is usually safer!
    private static final String KEYS_PATH = DIR + "\\DMS\\data\\region_keys.json";
    private static final String UPDATES_PATH = DIR + "\\DMS\\data\\region_184.json";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String... args) {
        try {
            Type mapType = new TypeToken<HashMap<String, int[]>>(){}.getType();
            HashMap<String, int[]> regionMap;

            try (FileReader reader = new FileReader(KEYS_PATH)) {
                regionMap = GSON.fromJson(reader, mapType);
                if (regionMap == null) {
                    regionMap = new HashMap<>();
                }
            }

            try (FileReader reader = new FileReader(UPDATES_PATH)) {
                KeyUpdate[] updates = GSON.fromJson(reader, KeyUpdate[].class);

                if (updates != null) {
                    for (KeyUpdate update : updates) {
                        regionMap.put(String.valueOf(update.mapsquare), update.key);
                    }
                }
            }

            try (FileWriter writer = new FileWriter(KEYS_PATH)) {
                GSON.toJson(regionMap, writer);
            }

            System.out.println("Update complete! region_keys.json now contains " + regionMap.size() + " entries.");

        } catch (FileNotFoundException e) {
            System.err.println("Could not find the file at: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This class matches the structure inside region_184.json
    private static class KeyUpdate {
        int mapsquare;
        int[] key;
    }
}