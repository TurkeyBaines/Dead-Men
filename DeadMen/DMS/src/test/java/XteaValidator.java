import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class XteaValidator {

    private static final String DIR = System.getProperty("user.dir");
    private static final String KEYS_PATH = DIR + "\\DMS\\data\\region_keys.json";
    private static final String UPDATES_PATH = DIR + "\\DMS\\data\\region_184.json";
    private static final Gson GSON = new Gson();

    public static void main(String... args) {
        try {
            // 1. Load the Master Map (region_keys.json)
            Type mapType = new TypeToken<HashMap<String, int[]>>(){}.getType();
            Map<String, int[]> masterMap;
            try (FileReader reader = new FileReader(KEYS_PATH)) {
                masterMap = GSON.fromJson(reader, mapType);
            }

            // 2. Load the Source List (region_184.json)
            KeyUpdate[] sourceList;
            try (FileReader reader = new FileReader(UPDATES_PATH)) {
                sourceList = GSON.fromJson(reader, KeyUpdate[].class);
            }

            if (masterMap == null || sourceList == null) {
                System.out.println("Error: One of the files is empty or missing.");
                return;
            }

            // 3. Perform Validation
            int totalInMaster = masterMap.size();
            int totalInSource = sourceList.length;
            int matches = 0;
            int mismatches = 0;
            int zeroKeys = 0;

            for (KeyUpdate sourceEntry : sourceList) {
                String id = String.valueOf(sourceEntry.mapsquare);

                if (masterMap.containsKey(id)) {
                    if (Arrays.equals(masterMap.get(id), sourceEntry.key)) {
                        matches++;
                    } else {
                        mismatches++;
                    }
                }
            }

            // Count how many keys in the master file are just zeros
            for (int[] key : masterMap.values()) {
                if (isZeroKey(key)) {
                    zeroKeys++;
                }
            }

            // 4. Report Results
            System.out.println("--- XTEA Validation Report ---");
            System.out.println("Total regions in Master: " + totalInMaster);
            System.out.println("Total regions in Source: " + totalInSource);
            System.out.println("------------------------------");
            System.out.println("Successful Sync Matches: " + matches);
            System.out.println("Data Mismatches:         " + mismatches);
            System.out.println("Empty (Zero) Keys:       " + zeroKeys);
            System.out.println("Valid (Active) Keys:      " + (totalInMaster - zeroKeys));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isZeroKey(int[] key) {
        if (key == null || key.length != 4) return true;
        return key[0] == 0 && key[1] == 0 && key[2] == 0 && key[3] == 0;
    }

    private static class KeyUpdate {
        int mapsquare;
        int[] key;
    }
}