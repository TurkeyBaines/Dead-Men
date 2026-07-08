package io.dm.model.entity.player;

import com.google.gson.*;
import io.dm.Server;
import io.dm.api.process.ProcessFactory;
import io.dm.api.protocol.Protocol;
import io.dm.api.protocol.Response;
import io.dm.api.utils.*;
import io.dm.model.World;
import io.dm.model.inter.utils.Config;
import io.dm.model.skills.construction.RoomDefinition;
import io.dm.model.skills.construction.room.Room;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerFile {

    private static final File CHARACTERS_FOLDER = new File(io.dm.api.utils.ServerPaths.getCharacters());
    private static final ExecutorService SAVE_EXECUTOR = Executors.newSingleThreadExecutor(new ProcessFactory("save-worker", Thread.NORM_PRIORITY - 1));
    private static final Gson GSON_LOADER = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().registerTypeAdapter(Room.class, new JsonDeserializer<Room>() {

        @Override
        public Room deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject obj = jsonElement.getAsJsonObject();
            RoomDefinition definition = RoomDefinition.valueOf(obj.get("definition").getAsString());
            return jsonDeserializationContext.deserialize(jsonElement, definition.getHandler());
        }
    }).create();

    public static void loadPlayer(PlayerLogin login) {
        if (Protocol.method360(login.info.name) == null) {
            login.deny(Response.CHANGE_DISPLAY_NAME);
            return;
        }
        if (!World.isDev()) {
            if (UUIDBan.isUUIDBanned(login.info.uuid)
                    || IPBans.isIPBanned(login.info.ipAddress)
                    || MACBan.isMACBanned(login.info.macAddress)) {
                login.deny(Response.DISABLED_ACCOUNT);
                return;
            }
        }
        String saved = "";
        File savedFile = new File(CHARACTERS_FOLDER, login.info.name.toLowerCase() + ".json");
        if (savedFile.exists()) {
            try {
                saved = new String(Files.readAllBytes(savedFile.toPath()));
            } catch (Exception e) {
                Server.logError("Failed to load player file for " + login.info.name, e);
                login.deny(Response.ERROR_LOADING_ACCOUNT);
                return;
            }
        }
        if (World.isDev()) {
            login.info.update(login.info.name, saved, ListUtils.toList(PlayerGroup.ADMINISTRATOR.id));
        } else {
            login.info.update(login.info.name, saved, ListUtils.toList(PlayerGroup.REGISTERED.id));
        }
        login.success();
    }

    /**
     * RSProt-path loader: reads {@code characters/{name}.json} if present and deserializes it into
     * a {@link Player} using the same GSON (so custom type-adapters like {@link Room} work), or
     * returns {@code new Player()} for a fresh account. Runs the {@link Config#load(Player)} pass
     * for parity with the legacy loader. Returns the Player + whether the save existed (via a
     * two-element record so the caller can decide the spawn tile / initial state).
     * <p>
     * If {@code checkPassword} is non-null and the save has a password, verifies it (BCrypt if
     * hashed, plain string equality otherwise). Returns {@code null} on auth failure.
     */
    public static Player loadForRsProt(String name, String checkPassword) {
        try {
            File savedFile = new File(CHARACTERS_FOLDER, name.toLowerCase() + ".json");
            Player player;
            if (savedFile.exists()) {
                String saved = new String(Files.readAllBytes(savedFile.toPath()));
                player = GSON_LOADER.fromJson(saved, Player.class);
                if (player == null) {
                    player = new Player();
                }
                if (checkPassword != null && player.getPassword() != null) {
                    boolean ok = player.getPassword().startsWith("$2a$")
                            ? BCrypt.checkpw(checkPassword, player.getPassword())
                            : checkPassword.equals(player.getPassword());
                    if (!ok) {
                        return null;
                    }
                }
            } else {
                player = new Player();
            }
            Config.load(player);
            return player;
        } catch (Throwable t) {
            Server.logError("Failed to load player file (RSProt) for " + name, t);
            return null;
        }
    }

    public static Player load(PlayerLogin login) {
        try {
            Player player;
            if (login.info.saved == null || login.info.saved.isEmpty())
                player = new Player();
            else
                player = GSON_LOADER.fromJson(login.info.saved, Player.class);
            Config.load(player);

            if (player.getSavedGroupIds() != null && player.getSavedGroupIds().contains(PlayerGroup.BANNED.id)) {
                login.deny(Response.DISABLED_ACCOUNT);
                return null;
            }
            if (!World.isDev() && player.getPassword() != null) {
                boolean passwordMatch;
                if (player.getPassword().startsWith("$2a$")) {
                    passwordMatch = BCrypt.checkpw(login.info.password, player.getPassword());
                } else {
                    passwordMatch = login.info.password.equals(player.getPassword());
                }
                if (!passwordMatch) {
                    login.deny(Response.INVALID_LOGIN);
                    return null;
                }
            }

            return player;
        } catch (Throwable t) {
            Server.logError("", t);
            return null;
        }
    }

    public static void save(Player player, int logoutAttempt) {
        SAVE_EXECUTOR.execute(() -> {
            Config.save(player);
            String json;
            try {
                json = JsonUtils.GSON_EXPOSE.toJson(player);
            } catch (Exception e) {
                Server.logError("", e);
                return;
            }
            try {
                if (!CHARACTERS_FOLDER.exists() && !CHARACTERS_FOLDER.mkdirs())
                    throw new IOException("characters directory could not be created!");
                Files.write(new File(CHARACTERS_FOLDER, player.getName().toLowerCase() + ".json").toPath(), json.getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            } catch (IOException e) {
                Server.logError("Failed to save player " + player.getName(), e);
            } finally {
                if (logoutAttempt != -1)
                    player.finishLogout(logoutAttempt);
            }
        });
    }
}
