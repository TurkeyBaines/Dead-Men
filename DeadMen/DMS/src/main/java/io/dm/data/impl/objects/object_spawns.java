package io.dm.data.impl.objects;

import com.google.gson.annotations.Expose;
import io.dm.api.protocol.world.WorldType;
import io.dm.api.utils.JsonUtils;
import io.dm.data.DataFile;
import io.dm.model.World;
import io.dm.model.map.object.GameObject;

import java.util.List;

public class object_spawns extends DataFile {

    @Override
    public String path() {
        return "objects/spawns/*.json";
    }

    @Override
    public Object fromJson(String fileName, String json) {
        List<Spawn> spawns = JsonUtils.fromJson(json, List.class, Spawn.class);
        spawns.forEach(spawn -> {
            if(spawn.world != null && spawn.world != World.type)
                return;
            GameObject.spawn(spawn.id, spawn.x, spawn.y, spawn.z, spawn.type, spawn.direction);
        });
        return spawns;
    }

    private static final class Spawn {
        @Expose public int id;
        @Expose public int x, y, z;
        @Expose public int type, direction;
        @Expose public WorldType world;
    }
    
}