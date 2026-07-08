package io.dm.data.impl;

import io.dm.api.utils.JsonUtils;
import io.dm.cache.EnumMap;
import io.dm.data.DataFile;
import io.dm.model.skills.slayer.SlayerTask;

import java.util.Map;

public class slayer_tasks extends DataFile {

    @Override
    public String path() {
        return "slayer_tasks.json";
    }

    @Override
    public int priority() {
        return 6;
    }

    @Override
    public Object fromJson(String fileName, String json) {
        Map<Integer, String> regMap = EnumMap.get(693).strings();
        Map<Integer, String> bossMap = EnumMap.get(1174).strings();
        SlayerTask.TASKS = JsonUtils.fromJson(json, Map.class, String.class, SlayerTask.class);
        SlayerTask.TASKS.forEach((name, task) -> {
            int key = getKey(name, task.type[0] == SlayerTask.Type.BOSS ? bossMap : regMap);
            task.name = name;
            if(key == -1) {
                //ServerWrapper.logWarning("Slayer task \"" + name + "\" is not in cache!");
                return;
            }
            task.key = key;
        });
        SlayerTask.BOSS_KEY = getKey("Bosses", bossMap);
        return SlayerTask.TASKS;
    }

    private static int getKey(String name, Map<Integer, String> map) {
        for(Map.Entry<Integer, String> entry : map.entrySet()) {
            if(entry.getValue().equals(name))
                return entry.getKey();
        }
        return -1;
    }

}
