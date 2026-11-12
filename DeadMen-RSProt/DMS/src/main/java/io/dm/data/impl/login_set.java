package io.dm.data.impl;

import com.google.gson.annotations.Expose;
import io.dm.api.protocol.Response;
import io.dm.api.utils.JsonUtils;
import io.dm.data.DataFile;
import io.dm.model.World;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.player.PlayerGroup;

import java.util.HashMap;
import java.util.Map;

public class login_set extends DataFile {

    private static final Map<String, Set> LOADED = new HashMap<>();

    @Override
    public String path() {
        return "logins/*.json";
    }

    @Override
    public Object fromJson(String fileName, String json) {
        Set set = JsonUtils.fromJson(json, Set.class);
        LOADED.put(fileName, set);
        return set;
    }

    private static final class Set {

        @Expose public PlayerGroup[] allowed_groups;
        @Expose public String[] allowed_names;
        @Expose public Response deny_response;

    }

    /**
     * Active
     */

    private static Set ACTIVE_SET;

    public static void setActive(Player player, String name) {
        if(player != null) {
            LOADED.clear();
            DataFile.reload(player, login_set.class);
        }
        if((name = name.trim()).isEmpty())
            name = World.stage.name().toLowerCase();
        if((ACTIVE_SET = LOADED.get(name)) != null) {
            if(player != null)
                player.sendMessage("Login set \"" + name + "\" is now active.");
        } else {
            if(player != null)
                player.sendMessage("Login set \"" + name + "\" does not exist!");
            else
                System.err.println("Login set \"" + name + "\" does not exist!");
        }
    }

}
