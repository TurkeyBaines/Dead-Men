package io.dm.network.rsprot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.dm.Server;
import io.dm.api.filestore.FileStore;
import io.dm.api.filestore.IndexFile;
import io.dm.api.protocol.login.LoginInfo;
import io.dm.api.utils.ListUtils;
import io.dm.model.World;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.player.PlayerFile;
import io.dm.model.entity.player.PlayerGroup;
import io.dm.net.DmCacheProvider;
import io.dm.net.DmIncomingPackets;
import io.dm.net.DmLogin;
import io.dm.net.DmLoginService;
import io.dm.net.DmNetworkPlayer;
import io.dm.net.FilestoreCacheProvider;
import io.dm.net.NetworkBootstrap;
import io.dm.net.RsaKeys;
import io.dm.network.logging.NetworkLogger;
import kotlin.Unit;
import net.rsprot.crypto.rsa.RsaKeyPair;
import net.rsprot.crypto.xtea.XteaKey;
import net.rsprot.protocol.api.NetworkService;
import net.rsprot.protocol.api.Session;
import net.rsprot.protocol.api.login.GameLoginResponseHandler;
import net.rsprot.protocol.common.client.OldSchoolClientType;
import net.rsprot.protocol.game.incoming.buttons.If3Button;
import net.rsprot.protocol.game.incoming.buttons.IfButtonD;
import net.rsprot.protocol.game.incoming.buttons.IfButtonT;
import net.rsprot.protocol.game.incoming.locs.OpLoc;
import net.rsprot.protocol.game.incoming.locs.OpLocT;
import net.rsprot.protocol.game.incoming.messaging.MessagePublic;
import net.rsprot.protocol.game.incoming.misc.user.ClientCheat;
import net.rsprot.protocol.game.incoming.misc.user.MoveGameClick;
import net.rsprot.protocol.game.incoming.misc.user.MoveMinimapClick;
import net.rsprot.protocol.game.incoming.npcs.OpNpc;
import net.rsprot.protocol.game.incoming.npcs.OpNpcT;
import net.rsprot.protocol.game.incoming.objs.OpObj;
import net.rsprot.protocol.game.incoming.objs.OpObjT;
import net.rsprot.protocol.game.incoming.players.OpPlayer;
import net.rsprot.protocol.game.incoming.players.OpPlayerT;
import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfoProtocol;
import net.rsprot.protocol.game.outgoing.info.npcinfo.SetNpcUpdateOrigin;
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfo;
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol;
import net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityProtocol;
import net.rsprot.protocol.game.outgoing.interfaces.IfOpenSub;
import net.rsprot.protocol.game.outgoing.interfaces.IfOpenTop;
import net.rsprot.protocol.game.outgoing.map.RebuildLogin;
import net.rsprot.protocol.game.outgoing.map.RebuildNormal;
import net.rsprot.protocol.game.outgoing.map.util.XteaProvider;
import net.rsprot.protocol.game.outgoing.worldentity.SetActiveWorld;
import net.rsprot.protocol.loginprot.incoming.util.AuthenticationType;
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock;
import net.rsprot.protocol.loginprot.outgoing.LoginResponse;
import net.rsprot.protocol.message.codec.incoming.MessageConsumer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * RSProt bootstrap + game-thread bridge for the server. Replaces {@code io.dm.api.netty.NettyServer}.
 *
 * <p><b>Stage 3 bind:</b> RSProt is now wired into the REAL {@code io.dm.model.World} tick. Login
 * builds an actual {@link Player} (registered in {@code World.players}) and allocates its RSProt info
 * avatars; the legacy {@code CoreWorker} 600ms tick then drives the RSProt player/npc/world-entity
 * info protocols each cycle via {@link #processIncoming}, {@link #driveUpdate} and {@link #onLogout}.
 * This reuses the legacy movement/collision/pathfinding ({@code Player.process()} +
 * {@code RouteFinder}) instead of the throwaway Stage-2 shim.
 *
 * <p>The JS5 cache is a <b>separate</b> {@link FileStore} from {@code Server.fileStore}: the game
 * still loads its (older-rev) definitions + collision from the legacy cache while RSProt serves the
 * 228 cache to the client.
 *
 * <p>Threading: RSProt info-protocol {@code alloc}/{@code update}/{@code dealloc} must all run on one
 * thread. Login is marshalled onto {@code Server.worker} (the game thread) and every drive hook is
 * invoked from {@code CoreWorker} which also runs there, so the affinity holds.
 */
public final class RsProt {

    /** Fixed-mode root interface id (the classic game frame). Resizable modes use 161/164. */
    private static final int FIXED_TOP_LEVEL_INTERFACE = 548;

    /**
     * The fixed-mode game-frame child panels: {@code {interfaceId, componentSlot}} pairs opened into
     * the 548 root via {@link IfOpenSub}. Slots/ids from AlterRSPS's {@code InterfaceDestination}
     * (fixed column). This is the tab sidebar + chatbox + minimap that make the frame look complete.
     */
    private static final int[][] FIXED_FRAME_PANES = {
            {162, 10}, // chatbox
            {122, 33}, // xp counter
            {593, 80}, // combat options
            {320, 81}, // skills
            {629, 82}, // quest / character summary
            {149, 83}, // inventory
            {387, 84}, // worn equipment
            {541, 85}, // prayer
            {218, 86}, // magic spellbook
            {707, 87}, // clan chat
            {109, 88}, // account management
            {429, 89}, // friends list
            {182, 90}, // logout
            {116, 91}, // settings
            {216, 92}, // emotes
            {239, 93}, // music player
            {163, 35}, // private chat
            {160, 24}, // minimap
    };

    // RSProt runtime, set in start(); driven by CoreWorker on the Server.worker (game) thread.
    private static NetworkService<DmNetworkPlayer> service;
    private static PlayerInfoProtocol playerInfoProtocol;
    private static NpcInfoProtocol npcInfoProtocol;
    private static WorldEntityProtocol worldEntityInfoProtocol;
    private static XteaProvider xtea;

    private RsProt() {
    }

    public static NetworkService<DmNetworkPlayer> start(int gamePort, String js5CachePath) throws IOException {
        FileStore js5Cache = new FileStore(js5CachePath);
        RsaKeyPair rsa = RsaKeys.INSTANCE.loadOrGenerate(new File("server-data/rsa.properties"));
        DmCacheProvider cache = cacheProvider(js5Cache);
        xtea = loadXteas(js5CachePath);

        NetworkService<DmNetworkPlayer> svc = NetworkBootstrap.INSTANCE.start(
                gamePort, rsa, cache, loginService(),
                builder -> {
                    // Real handlers: map/minimap click → BFS pathfind (fills the Movement queue that
                    // Player.process() then walks with collision).
                    builder.addListener(MoveGameClick.class,
                            (MessageConsumer<DmNetworkPlayer, MoveGameClick>) (player, msg) ->
                                    onWalk(player, msg.getX(), msg.getZ()));
                    builder.addListener(MoveMinimapClick.class,
                            (MessageConsumer<DmNetworkPlayer, MoveMinimapClick>) (player, msg) ->
                                    onWalk(player, msg.getX(), msg.getZ()));
                    // ::commands -> the legacy CommandHandler.
                    builder.addListener(ClientCheat.class,
                            (MessageConsumer<DmNetworkPlayer, ClientCheat>) (player, msg) ->
                                    onCommand(player, msg.getCommand()));
                    // Public chat -> echo onto the player's avatar extended info so it renders.
                    builder.addListener(MessagePublic.class,
                            (MessageConsumer<DmNetworkPlayer, MessagePublic>) (player, msg) ->
                                    onPublicChat(player, msg));
                    // Entity clicks (options 1-5 for objects/npcs/ground-items, 1-8 for players) ->
                    // the legacy per-entity action handlers (attack, talk-to, pick up, trade, etc.).
                    builder.addListener(OpLoc.class,
                            (MessageConsumer<DmNetworkPlayer, OpLoc>) (player, msg) ->
                                    onObjectAction(player, msg));
                    builder.addListener(OpNpc.class,
                            (MessageConsumer<DmNetworkPlayer, OpNpc>) (player, msg) ->
                                    onNpcAction(player, msg));
                    builder.addListener(OpObj.class,
                            (MessageConsumer<DmNetworkPlayer, OpObj>) (player, msg) ->
                                    onGroundItemAction(player, msg));
                    builder.addListener(OpPlayer.class,
                            (MessageConsumer<DmNetworkPlayer, OpPlayer>) (player, msg) ->
                                    onPlayerAction(player, msg));
                    // Interface/inventory/equipment component clicks (options 1-10) -> the legacy
                    // ActionButtonHandler. Its combinedId bit-packs interfaceId/componentId exactly
                    // like the legacy interfaceHash, so it plugs straight into the existing dispatch
                    // (Wield/Wear = inventory option 1, Remove = equipment-screen option 1).
                    builder.addListener(If3Button.class,
                            (MessageConsumer<DmNetworkPlayer, If3Button>) (player, msg) ->
                                    onButton(player, msg));
                    // Item-on-X / spell-on-X combinations -> the legacy InterfaceOn*Handlers.
                    builder.addListener(OpLocT.class,
                            (MessageConsumer<DmNetworkPlayer, OpLocT>) (player, msg) ->
                                    onItemOnObject(player, msg));
                    builder.addListener(OpObjT.class,
                            (MessageConsumer<DmNetworkPlayer, OpObjT>) (player, msg) ->
                                    onItemOnGroundItem(player, msg));
                    builder.addListener(OpNpcT.class,
                            (MessageConsumer<DmNetworkPlayer, OpNpcT>) (player, msg) ->
                                    onItemOnNpc(player, msg));
                    builder.addListener(OpPlayerT.class,
                            (MessageConsumer<DmNetworkPlayer, OpPlayerT>) (player, msg) ->
                                    onItemOnPlayer(player, msg));
                    builder.addListener(IfButtonT.class,
                            (MessageConsumer<DmNetworkPlayer, IfButtonT>) (player, msg) ->
                                    onItemOnItem(player, msg));
                    // Interface drag (e.g. reordering inventory slots) -> the legacy InterfaceDragHandler.
                    builder.addListener(IfButtonD.class,
                            (MessageConsumer<DmNetworkPlayer, IfButtonD>) (player, msg) ->
                                    onInterfaceDrag(player, msg));
                    // Everything else is accepted-and-dropped so processIncomingPackets never throws.
                    DmIncomingPackets.INSTANCE.registerNoOpsExcept(builder, java.util.Set.of(
                            MoveGameClick.class, MoveMinimapClick.class,
                            ClientCheat.class, MessagePublic.class,
                            OpLoc.class, OpNpc.class, OpObj.class, OpPlayer.class,
                            If3Button.class, OpLocT.class, OpObjT.class, OpNpcT.class, OpPlayerT.class,
                            IfButtonT.class, IfButtonD.class));
                    return Unit.INSTANCE;
                });
        service = svc;
        playerInfoProtocol = svc.getPlayerInfoProtocol();
        npcInfoProtocol = svc.getNpcInfoProtocol();
        worldEntityInfoProtocol = svc.getWorldEntityInfoProtocol();
        return svc;
    }

    // ------------------------------------------------------------------ incoming

    /**
     * Incoming map/minimap click → route to the destination with the real BFS pathfinder (routes
     * around objects, respects collision). Runs on the game thread (invoked from
     * {@code processIncoming}); fills {@code Player.getMovement()} which {@code Player.process()} walks.
     * RSProt gives absolute world coords; {@code routeAbsolute} takes (x, y) = (x, RSProt-z).
     */
    private static void onWalk(DmNetworkPlayer player, int destX, int destY) {
        if (player instanceof Player p) {
            p.getRouteFinder().routeAbsolute(destX, destY);
        }
    }

    /** Incoming {@code ::command} → the legacy command dispatcher. Runs on the game thread. */
    private static void onCommand(DmNetworkPlayer player, String command) {
        if (player instanceof Player p) {
            io.dm.network.incoming.handlers.CommandHandler.handle(p, command);
        }
    }

    /**
     * Incoming public chat → echo onto the sender's player-info extended info so it renders (chat
     * bubble + chatbox line for the player and nearby observers). {@code setChat(colour, effect,
     * modIcon, autotyper, message, pattern)}; modIcon = staff crown.
     */
    private static void onPublicChat(DmNetworkPlayer player, MessagePublic msg) {
        if (player instanceof Player p && p.playerInfo != null) {
            int modIcon = p.isAdmin() ? 2 : p.isModerator() ? 1 : 0;
            p.playerInfo.getAvatar().getExtendedInfo().setChat(
                    msg.getColour(), msg.getEffect(), modIcon, false, msg.getMessage(), null);
        }
    }

    // ------------------------------------------------------------------ entity / interface interactions

    /**
     * Incoming object (loc) click, options 1-5 → the legacy {@code ObjectActionHandler}: routes the
     * player to the object (respecting collision) then fires its {@code ObjectAction}/default action
     * for the clicked option. {@code isLocked}/{@code resetActions} are replicated here because the
     * legacy 184 entry point ({@code ObjectActionHandler.handle}) applies them before calling into
     * {@code handleAction}, which is the method this bridges straight into.
     */
    private static void onObjectAction(DmNetworkPlayer player, OpLoc msg) {
        if (player instanceof Player p) {
            NetworkLogger.log(p, "OpLoc", -1, -1, msg.getOp(), -1, msg.getId(),
                    "x=" + msg.getX() + " z=" + msg.getZ());
            if (p.isLocked()) {
                return;
            }
            p.resetActions(true, true, true);
            io.dm.network.incoming.handlers.ObjectActionHandler.handleAction(
                    p, msg.getOp(), msg.getId(), msg.getX(), msg.getZ(), msg.getControlKey() ? 1 : 0);
        }
    }

    /** Incoming NPC click, options 1-5 → the legacy {@code NPCActionHandler} (attack/talk-to/etc.). */
    private static void onNpcAction(DmNetworkPlayer player, OpNpc msg) {
        if (player instanceof Player p) {
            NetworkLogger.log(p, "OpNpc", -1, -1, msg.getOp(), -1, msg.getIndex());
            if (p.isLocked()) {
                return;
            }
            p.resetActions(true, true, true);
            io.dm.network.incoming.handlers.NPCActionHandler.handleAction(
                    p, msg.getOp(), msg.getIndex(), msg.getControlKey() ? 1 : 0);
        }
    }

    /** Incoming ground-item click, options 1-5 → the legacy {@code GroundItemActionHandler}. */
    private static void onGroundItemAction(DmNetworkPlayer player, OpObj msg) {
        if (player instanceof Player p) {
            NetworkLogger.log(p, "OpObj", -1, -1, msg.getOp(), -1, msg.getId(),
                    "x=" + msg.getX() + " z=" + msg.getZ());
            if (p.isLocked()) {
                return;
            }
            p.resetActions(true, true, true);
            io.dm.network.incoming.handlers.GroundItemActionHandler.handleAction(
                    p, msg.getOp(), msg.getId(), msg.getX(), msg.getZ(), msg.getControlKey() ? 1 : 0);
        }
    }

    /** Incoming player click, options 1-8 (attack/trade/follow/etc.) → the legacy {@code PlayerActionHandler}. */
    private static void onPlayerAction(DmNetworkPlayer player, OpPlayer msg) {
        if (player instanceof Player p) {
            NetworkLogger.log(p, "OpPlayer", -1, -1, msg.getOp(), -1, msg.getIndex());
            if (p.isLocked()) {
                return;
            }
            p.resetActions(true, true, true);
            io.dm.network.incoming.handlers.PlayerActionHandler.handleAction(
                    p, msg.getOp(), msg.getIndex(), msg.getControlKey() ? 1 : 0);
        }
    }

    /**
     * Incoming if3 interface component click, options 1-10 → the legacy {@code ActionButtonHandler}.
     * {@code msg.getCombinedId()} bit-packs interfaceId/componentId exactly like the legacy
     * {@code interfaceHash} (verified against RSProt's {@code CombinedId}: {@code (interfaceId shl 16)
     * or (componentId and 0xffff)}, same convention {@code ActionButtonHandler}/{@code InterfaceHandler}
     * already use), so it plugs straight into the existing dispatcher — including inventory
     * Wield/Wear (equip option 1 → {@code TabInventory} → {@code Equipment.equip}) and worn-equipment
     * Remove (option 1 → {@code TabEquipment} → {@code Equipment.unequip}). No {@code isLocked} guard
     * here: the legacy button entry points never gated clicks behind it either.
     */
    private static void onButton(DmNetworkPlayer player, If3Button msg) {
        if (player instanceof Player p) {
            NetworkLogger.log(p, "If3Button", msg.getInterfaceId(), msg.getComponentId(),
                    msg.getOp(), msg.getSub(), msg.getObj());
            io.dm.network.incoming.handlers.ActionButtonHandler.handleAction(
                    p, msg.getOp(), msg.getCombinedId(), msg.getSub(), msg.getObj(), false);
        }
    }

    /** Incoming item-used-on-object (or component-targeted-on-loc) → the legacy {@code InterfaceOnObjectHandler}. */
    private static void onItemOnObject(DmNetworkPlayer player, OpLocT msg) {
        if (player instanceof Player p) {
            NetworkLogger.log(p, "OpLocT", NetworkLogger.interfaceIdFromCombined(msg.getSelectedCombinedId()),
                    NetworkLogger.componentIdFromCombined(msg.getSelectedCombinedId()), -1,
                    msg.getSelectedSub(), msg.getSelectedObj(),
                    "targetLocId=" + msg.getId() + " x=" + msg.getX() + " z=" + msg.getZ());
            io.dm.network.incoming.handlers.InterfaceOnObjectHandler.handleAction(
                    p, msg.getSelectedCombinedId(), msg.getSelectedSub(), msg.getSelectedObj(),
                    msg.getId(), msg.getX(), msg.getZ(), msg.getControlKey() ? 1 : 0);
        }
    }

    /** Incoming item-used-on-ground-item → the legacy {@code InterfaceOnGroundItemHandler}. */
    private static void onItemOnGroundItem(DmNetworkPlayer player, OpObjT msg) {
        if (player instanceof Player p) {
            NetworkLogger.log(p, "OpObjT", NetworkLogger.interfaceIdFromCombined(msg.getSelectedCombinedId()),
                    NetworkLogger.componentIdFromCombined(msg.getSelectedCombinedId()), -1,
                    msg.getSelectedSub(), msg.getSelectedObj(),
                    "targetGroundItemId=" + msg.getId() + " x=" + msg.getX() + " z=" + msg.getZ());
            io.dm.network.incoming.handlers.InterfaceOnGroundItemHandler.handleAction(
                    p, msg.getSelectedCombinedId(), msg.getSelectedSub(), msg.getSelectedObj(),
                    msg.getId(), msg.getX(), msg.getZ(), msg.getControlKey() ? 1 : 0);
        }
    }

    /** Incoming item/spell-used-on-npc → the legacy {@code InterfaceOnEntityHandler}. */
    private static void onItemOnNpc(DmNetworkPlayer player, OpNpcT msg) {
        if (player instanceof Player p) {
            NetworkLogger.log(p, "OpNpcT", NetworkLogger.interfaceIdFromCombined(msg.getSelectedCombinedId()),
                    NetworkLogger.componentIdFromCombined(msg.getSelectedCombinedId()), -1,
                    msg.getSelectedSub(), msg.getSelectedObj(), "targetNpcIndex=" + msg.getIndex());
            io.dm.network.incoming.handlers.InterfaceOnEntityHandler.handleAction(
                    p, World.getNpc(msg.getIndex()), msg.getSelectedCombinedId(), msg.getSelectedSub(),
                    msg.getSelectedObj(), msg.getControlKey() ? 1 : 0);
        }
    }

    /** Incoming item/spell-used-on-player → the legacy {@code InterfaceOnEntityHandler}. */
    private static void onItemOnPlayer(DmNetworkPlayer player, OpPlayerT msg) {
        if (player instanceof Player p) {
            NetworkLogger.log(p, "OpPlayerT", NetworkLogger.interfaceIdFromCombined(msg.getSelectedCombinedId()),
                    NetworkLogger.componentIdFromCombined(msg.getSelectedCombinedId()), -1,
                    msg.getSelectedSub(), msg.getSelectedObj(), "targetPlayerIndex=" + msg.getIndex());
            io.dm.network.incoming.handlers.InterfaceOnEntityHandler.handleAction(
                    p, World.getPlayer(msg.getIndex()), msg.getSelectedCombinedId(), msg.getSelectedSub(),
                    msg.getSelectedObj(), msg.getControlKey() ? 1 : 0);
        }
    }

    /** Incoming item-used-on-item (combining two inventory items) → the legacy {@code InterfaceOnInterfaceHandler}. */
    private static void onItemOnItem(DmNetworkPlayer player, IfButtonT msg) {
        if (player instanceof Player p) {
            NetworkLogger.log(p, "IfButtonT", NetworkLogger.interfaceIdFromCombined(msg.getSelectedCombinedId()),
                    NetworkLogger.componentIdFromCombined(msg.getSelectedCombinedId()), -1,
                    msg.getSelectedSub(), msg.getSelectedObj(),
                    "targetInterface=" + NetworkLogger.interfaceIdFromCombined(msg.getTargetCombinedId())
                            + " targetComponent=" + NetworkLogger.componentIdFromCombined(msg.getTargetCombinedId())
                            + " targetSlot=" + msg.getTargetSub() + " targetItem=" + msg.getTargetObj());
            io.dm.network.incoming.handlers.InterfaceOnInterfaceHandler.handleAction(
                    p, msg.getSelectedCombinedId(), msg.getSelectedSub(), msg.getSelectedObj(),
                    msg.getTargetCombinedId(), msg.getTargetSub(), msg.getTargetObj());
        }
    }

    /** Incoming interface drag (e.g. reordering inventory slots) → the legacy {@code InterfaceDragHandler}. */
    private static void onInterfaceDrag(DmNetworkPlayer player, IfButtonD msg) {
        if (player instanceof Player p) {
            NetworkLogger.log(p, "IfButtonD", msg.getSelectedInterfaceId(), msg.getSelectedComponentId(),
                    -1, msg.getSelectedSub(), msg.getSelectedObj(),
                    "toInterface=" + msg.getTargetInterfaceId() + " toComponent=" + msg.getTargetComponentId()
                            + " toSlot=" + msg.getTargetSub() + " toItem=" + msg.getTargetObj());
            io.dm.network.incoming.handlers.InterfaceDragHandler.handleDrag(
                    p, msg.getSelectedCombinedId(), msg.getSelectedSub(), msg.getSelectedObj(),
                    msg.getTargetCombinedId(), msg.getTargetSub(), msg.getTargetObj());
        }
    }

    // ------------------------------------------------------------------ login

    /**
     * Plaintext password out of an RSProt login block, or null if none (reconnect uses XteaKey,
     * not a password). RSProt models fresh-connect auth as {@code PasswordAuthentication(Password)}.
     */
    private static String extractPassword(LoginBlock<?> block) {
        Object auth = block.getAuthentication();
        if (auth instanceof net.rsprot.protocol.loginprot.incoming.util.AuthenticationType.PasswordAuthentication pa) {
            return pa.getPassword().asString();
        }
        return null;
    }

    private static DmLoginService loginService() {
        return new DmLoginService() {
            @Override
            public void connect(GameLoginResponseHandler<DmNetworkPlayer> rh,
                                LoginBlock<AuthenticationType> block, boolean reconnect) {
                Server.worker.execute(() -> doLogin(rh, block));
            }

            @Override
            public void reconnect(GameLoginResponseHandler<DmNetworkPlayer> rh, LoginBlock<XteaKey> block) {
                Server.worker.execute(() -> doLogin(rh, block));
            }
        };
    }

    /**
     * Builds a real {@link Player}, registers it in the world, allocates its RSProt info avatars and
     * pushes the initial world state (RebuildLogin + build areas + appearance + game frame). Runs on
     * {@code Server.worker}. Mirrors {@code PlayerLogin.load} + the channel-less {@code PlayerBot}
     * recipe; the legacy Netty channel is null (legacy 184 sends are dropped by PacketSender's guard).
     */
    private static void doLogin(GameLoginResponseHandler<DmNetworkPlayer> rh, LoginBlock<?> block) {
        String username = block.getUsername();
        Player player = null;
        int index = -1;
        try {
            // Extract the plaintext password from the block for auth (fresh-connect only; reconnect
            // uses XteaKey and skips auth). In dev mode auth is skipped so a fresh username creates.
            String password = extractPassword(block);
            player = PlayerFile.loadForRsProt(username, World.isDev() ? null : password);
            if (player == null) {
                DmLogin.INSTANCE.reject(rh, LoginResponse.InvalidUsernameOrPassword.INSTANCE);
                System.out.println("[LOGIN] '" + username + "' invalid password");
                return;
            }
            index = World.players.add(player, 1);
            player.setIndex(index);

            LoginInfo info = new LoginInfo(null, username, password == null ? "" : password, "", "",
                    null, 0, false, 0, World.id, new int[4]);
            info.update(username, "", ListUtils.toList(
                    (World.isDev() ? PlayerGroup.ADMINISTRATOR : PlayerGroup.REGISTERED).id));
            player.init(info);
            // Fresh accounts default to Edgeville (3086,3495 in Player.init); override to Lumbridge.
            // Loaded saves keep their persisted tile (init only defaults `position` when null).
            if (player.newPlayer) {
                player.getPosition().copy(new io.dm.model.map.Position(3222, 3218, 0));
                // Temporary starter kit (standard OSRS item ids, present in the 228 cache) so the
                // inventory/equipment can be tested without the chatbox (::commands are gated by the
                // display-name prompt). Remove once a proper new-player flow / commands are available.
                player.getInventory().add(995, 5000);   // coins
                player.getInventory().add(1321, 1);      // bronze scimitar
                player.getInventory().add(1155, 1);      // bronze full helm
                player.getInventory().add(1117, 1);      // bronze platebody
                player.getInventory().add(1075, 1);      // bronze platelegs
                player.getInventory().add(1189, 1);      // bronze kiteshield
            }
            player.lastRegion = player.getPosition().getRegion();
            player.newPlayer = false;
            player.setOnline(true);

            int level = player.getHeight();
            int x = player.getAbsX();
            int z = player.getAbsY();

            player.playerInfo = playerInfoProtocol.alloc(index, OldSchoolClientType.DESKTOP);
            player.npcInfo = npcInfoProtocol.alloc(index, OldSchoolClientType.DESKTOP);
            player.worldEntityInfo = worldEntityInfoProtocol.alloc(index, OldSchoolClientType.DESKTOP);
            player.playerInfo.updateCoord(level, x, z);
            player.npcInfo.updateCoord(-1, level, x, z);
            player.worldEntityInfo.updateCoord(-1, level, x, z);

            final Player fp = player;
            final int fi = index;
            Session<DmNetworkPlayer> s = DmLogin.INSTANCE.accept(
                    rh, block, player, index, 0, false, true, -1L, -1L, -1L,
                    () -> onLogout(fp, fi));
            if (player.getSession() == null) {
                cleanup(fp, fi);
                System.out.println("[LOGIN] '" + username + "' response channel closed");
                return;
            }

            // RebuildLogin carries the GPI init block (built from playerInfo); sent before build areas.
            s.queue(new RebuildLogin(x >> 3, z >> 3, -1, xtea, player.playerInfo));
            setBuildAreas(player, (x >> 3) - 6, (z >> 3) - 6);
            syncAppearance(player);
            openFrame(player);

            // Milestone 3 fix (2026-07-07): Player.start() fires the one-shot login packet burst —
            // welcome message, LoginListener UI, pet spawn, and critically the chatbox/display-name
            // unlock sequence: sendVarp(1737,-2147483648), sendClientScript(1105,"i",1),
            // sendClientScript(423,"s",name) [sets the client's own display-name state — this is the
            // actual CS2 call the client checks, not a standalone varp], sendClientScript(2498,"i11",...),
            // Config.HAS_DISPLAY_NAME.set (varbit 8199 -> parent varp, the proven 184 display-name flag),
            // sendClientScript(828,"i",1), sendClientScript(233,...) x2. All of these go through
            // PacketSender.queueRsprot(), which no-ops silently when player.getSession() is null — and
            // start() used to run BEFORE DmLogin.accept() below created the session, so every one of
            // these was being dropped on the floor. That's why toggling varp 1055 alone never cleared
            // the gate: the real fix isn't a different varp, it's that script 423 (and its siblings)
            // never reached the client at all. Moved here, after the session + frame exist, so
            // queueRsprot() actually queues them instead of dropping them.
            player.start();

            System.out.println("[LOGIN] '" + username + "' idx=" + index + " at " + x + "," + z);
        } catch (Throwable t) {
            System.out.println("[LOGIN] ERROR for '" + username + "': " + t);
            t.printStackTrace();
            if (player != null && index != -1) {
                cleanup(player, index);
            }
            try {
                DmLogin.INSTANCE.reject(rh, LoginResponse.ConnectFail.INSTANCE);
            } catch (Throwable ignore) {
            }
        }
    }

    /** Disconnect hook (fired by Netty). Marshal the cleanup back onto the game thread. */
    private static void onLogout(Player player, int index) {
        Server.worker.execute(() -> {
            System.out.println("[LOGOUT] '" + player.getUsername() + "'");
            cleanup(player, index);
        });
    }

    /** Game-thread only: save the player, release the RSProt avatars, remove them from the world. */
    private static void cleanup(Player player, int index) {
        try {
            if (player.getName() != null && !player.getName().isEmpty()) {
                PlayerFile.save(player, 0); // persist skills/inventory/position for the next login
            }
        } catch (Throwable t) {
            System.out.println("[LOGOUT] save failed for " + player.getName() + ": " + t);
        }
        deallocAvatars(player);
        try {
            player.setOnline(false);
        } catch (Throwable ignore) {
        }
        try {
            World.players.remove(index);
        } catch (Throwable ignore) {
        }
    }

    private static void deallocAvatars(Player p) {
        if (p.playerInfo != null) {
            try {
                playerInfoProtocol.dealloc(p.playerInfo);
            } catch (Throwable ignore) {
            }
            p.playerInfo = null;
        }
        if (p.npcInfo != null) {
            try {
                npcInfoProtocol.dealloc(p.npcInfo);
            } catch (Throwable ignore) {
            }
            p.npcInfo = null;
        }
        if (p.worldEntityInfo != null) {
            try {
                worldEntityInfoProtocol.dealloc(p.worldEntityInfo);
            } catch (Throwable ignore) {
            }
            p.worldEntityInfo = null;
        }
    }

    // ------------------------------------------------------------------ CoreWorker drive hooks

    /**
     * Drain a player's decoded incoming packets (calls the registered consumers, e.g. movement →
     * routeAbsolute). Called from {@code CoreWorker.logic()} before {@code player.process()}.
     */
    public static void processIncoming(Player p) {
        Session<DmNetworkPlayer> s = p.getSession();
        if (s == null) {
            return;
        }
        try {
            s.processIncomingPackets(p);
        } catch (Throwable t) {
            System.out.println("[IN] " + p.getName() + ": " + t);
        }
    }

    /**
     * Drives the RSProt player/npc/world-entity info protocols for all online RSProt players and
     * flushes each session. Called ONCE per game cycle from {@code CoreWorker.update()}. Mirrors
     * AlterRSPS's {@code SequentialSynchronizationTask} order (world-entity info first).
     *
     * <p>Milestone 2 (2026-07-07): step 0 below bridges each player's legacy per-tick update masks
     * (animation, graphics, hits, chat, face-entity, face-angle, force-text, forced movement,
     * movement/teleport mode, appearance) onto their {@code PlayerAvatarExtendedInfo} — see
     * {@link #pushMaskUpdates}. It runs first, before step 3's {@code playerInfoProtocol.update()}
     * gathers each avatar's currently-flagged extended-info blocks into this cycle's snapshot.
     */
    public static void driveUpdate() {
        if (service == null) {
            return;
        }
        // 0) bridge legacy per-tick update masks onto PlayerAvatarExtendedInfo (Milestone 2) —
        // must run before step 3's playerInfoProtocol.update() gathers this cycle's snapshot.
        for (Player p : World.players) {
            if (notReady(p)) {
                continue;
            }
            try {
                pushMaskUpdates(p);
            } catch (Throwable t) {
                System.out.println("[TICK] masks " + p.getName() + ": " + t);
            }
        }
        // 1) push each player's current tile into all three info avatars; rebuild region if needed.
        for (Player p : World.players) {
            if (notReady(p)) {
                continue;
            }
            int level = p.getHeight(), x = p.getAbsX(), z = p.getAbsY();
            p.playerInfo.updateCoord(level, x, z);
            p.npcInfo.updateCoord(-1, level, x, z);
            p.worldEntityInfo.updateCoord(-1, level, x, z);
            maybeRebuildRegion(p, x, z);
        }
        // 2) world-entity info first, then set player render coords.
        worldEntityInfoProtocol.update();
        for (Player p : World.players) {
            if (notReady(p)) {
                continue;
            }
            try {
                p.getSession().queue(p.worldEntityInfo.toPacket());
            } catch (Throwable t) {
                System.out.println("[TICK] we " + p.getName() + ": " + t);
            }
            p.playerInfo.updateRenderCoord(-1, p.getHeight(), p.getAbsX(), p.getAbsY());
        }
        // 3) player + npc info, then flush the world-entity → player → npc sequence.
        playerInfoProtocol.update();
        npcInfoProtocol.update();
        for (Player p : World.players) {
            if (notReady(p)) {
                continue;
            }
            Session<DmNetworkPlayer> s = p.getSession();
            int level = p.getHeight(), x = p.getAbsX(), z = p.getAbsY();
            try {
                s.queue(new SetActiveWorld(new SetActiveWorld.RootWorldType(level)));
                s.queue(p.playerInfo.toPacket());
                s.queue(new SetNpcUpdateOrigin(
                        x - (p.buildAreaZoneX << 3), z - (p.buildAreaZoneZ << 3)));
                s.queue(p.npcInfo.toPacket(-1));
                s.flush();
            } catch (Throwable t) {
                System.out.println("[TICK] out " + p.getName() + ": " + t);
            }
        }
    }

    /** True when a player isn't a fully logged-in RSProt player (skip it in the drive loops). */
    private static boolean notReady(Player p) {
        return p == null || !p.isOnline() || p.getSession() == null || p.playerInfo == null;
    }

    /**
     * Milestone 2 (2026-07-07): bridges the 11 player-applicable legacy per-tick update masks —
     * Animation, Graphics, Hits, Chat, face-entity, face-angle, force-text, forced movement,
     * movement mode, teleport mode, and Appearance — onto this player's {@code PlayerAvatarExtendedInfo}.
     * The 12th mask class, {@code TransformUpdate}, is NPC-only (never instantiated in {@code
     * Player.init()}'s {@code masks[]} array) and belongs to a future NPC-info bridging pass, not
     * this one — {@code NpcAvatarExtendedInfo} is a separate RSProt type from the player one bridged
     * here.
     *
     * <p>Reads each mask's current tick state directly (the same fields {@code PlayerUpdater.process()}
     * would otherwise have packed for a legacy 184 client) and translates it into the equivalent
     * RSProt setter call. Does not touch {@code isSent()}/{@code reset()} — {@code
     * Entity.resetUpdates()} already clears that uniformly for both transports at the end of {@code
     * CoreWorker.update()}, after {@link #driveUpdate} has run.
     *
     * <p>Known best-effort gap: {@code HitsUpdate}'s legacy {@code Splat} only keeps the hit's
     * type + damage, not its attacker, so {@code addHitMark}/{@code addHeadBar} below are pushed
     * with {@code sourceIndex = -1} — every observer sees the same untinted hitmark/healthbar rather
     * than the attacker-tinted variant. Functionally correct (damage numbers and hp bar render),
     * cosmetically best-effort, same spirit as the curve/progress and type/loops notes under
     * "Milestone 1 Fix" in HANDOFF.txt.
     *
     * <p><b>Every branch below calls {@code setSent(true)} on the mask it just consumed.</b> {@code
     * Entity.resetUpdates()} (called later in the same {@code CoreWorker.update()} cycle, for every
     * online player regardless of transport) only clears a mask via {@code reset()} when {@code
     * isSent()} is true — and the only other place that ever sets it is the legacy {@code
     * PlayerUpdater.writeMasks()}, which never runs for RSProt players ({@code
     * CoreWorker.update()} guards it behind {@code session == null}). Without marking these sent
     * here ourselves, {@code hasUpdate(false)} would stay true forever after the first trigger and
     * this method would replay the same animation/graphic/hit/etc. every single tick.
     */
    private static void pushMaskUpdates(Player p) {
        var ext = p.playerInfo.getAvatar().getExtendedInfo();

        var anim = p.getAnimationUpdate();
        if (anim.hasUpdate(false)) {
            ext.setSequence(anim.id, anim.delay);
            anim.setSent(true);
        }

        var gfx = p.getGraphicsUpdate();
        if (gfx.hasUpdate(false)) {
            ext.setSpotAnim(0, gfx.getId(), gfx.getDelay(), gfx.getHeight());
            gfx.setSent(true);
        }

        var hits = p.hitsUpdate;
        if (hits.hasUpdate(false)) {
            int splatCount = hits.splatCount();
            for (int i = 0; i < splatCount; i++) {
                int type = hits.splatType(i);
                ext.addHitMark(-1, type, type, hits.splatDamage(i), hits.splatDelay(i));
            }
            int ratio = hits.getHpBarRatio();
            ext.addHeadBar(-1, hits.hpBarType, hits.hpBarType, ratio, ratio, 0, 0);
            hits.setSent(true);
        }

        var chat = p.getChatUpdate();
        if (chat.hasUpdate(false)) {
            ext.setChat(chat.getType(), chat.getEffects(), chat.getRankId(), false, chat.getMessage(), null);
            chat.setSent(true);
        }

        // No setSent() here: consumeResolvedUpdate() already self-clears resolvedDirty above, independently
        // of the sent/reset()/hasUpdate() machinery every other branch in this method relies on.
        var faceEntity = p.getEntityDirectionUpdate();
        if (faceEntity.consumeResolvedUpdate()) {
            ext.setFacePathingEntity(toRsProtEntityIndex(faceEntity.getResolvedTarget()));
        }

        var faceAngle = p.getMapDirectionUpdate();
        if (faceAngle.hasUpdate(false)) {
            ext.setFaceAngle(faceAngle.getDirection());
            faceAngle.setSent(true);
        }

        var forceText = p.getForceTextUpdate();
        if (forceText.hasUpdate(false)) {
            ext.setSay(forceText.getForceText());
            forceText.setSent(true);
        }

        var forceMove = p.getForceMovementUpdate();
        if (forceMove.hasUpdate(false)) {
            ext.setExactMove(
                    forceMove.getDiffX1(), forceMove.getDiffY1(), forceMove.getSpeed1(),
                    forceMove.getDiffX2(), forceMove.getDiffY2(), forceMove.getSpeed2(),
                    forceMove.getDirection());
            forceMove.setSent(true);
        }

        var moveMode = p.getMovementModeUpdate();
        if (moveMode.hasUpdate(false)) {
            ext.setMoveSpeed(moveMode.getMode());
            moveMode.setSent(true);
        }

        var teleMode = p.getTeleportModeUpdate();
        if (teleMode.hasUpdate(false)) {
            ext.setTempMoveSpeed(teleMode.getMode());
            teleMode.setSent(true);
        }

        var appearance = p.getAppearance();
        if (appearance.hasUpdate(false)) {
            syncAppearance(p);
            appearance.setSent(true);
        }
    }

    /**
     * Converts the legacy "client index" encoding used by {@code Entity.getClientIndex()} and
     * {@code EntityDirectionUpdate} (players = realIndex + 32768, NPCs = realIndex, -1 = none) into
     * RSProt's face-pathingentity index space (players = realIndex + 0x10000, NPCs = realIndex
     * as-is, -1 = none). The two encodings use different offsets for "this is a player" (0x8000 vs
     * 0x10000), so passing the raw value straight through would silently misdirect the face-lock
     * for any player target instead of just being off by a constant.
     */
    private static int toRsProtEntityIndex(int legacyClientIndex) {
        if (legacyClientIndex == -1) {
            return -1;
        }
        if (legacyClientIndex >= 32768) {
            return 0x10000 + (legacyClientIndex - 32768);
        }
        return legacyClientIndex;
    }

    private static void setBuildAreas(Player p, int zoneX, int zoneZ) {
        p.buildAreaZoneX = zoneX;
        p.buildAreaZoneZ = zoneZ;
        p.playerInfo.updateBuildArea(-1, zoneX, zoneZ, 13, 13);
        p.npcInfo.updateBuildArea(-1, zoneX, zoneZ, 13, 13);
        p.worldEntityInfo.updateBuildArea(zoneX, zoneZ, 13, 13);
    }

    /**
     * Re-centre the 13x13-zone build area + send {@link RebuildNormal} when the player nears the
     * edge (>=4 zones from centre, ~16-tile margin). Keeps the client's scene loaded as it roams.
     */
    private static void maybeRebuildRegion(Player p, int x, int z) {
        int zoneX = x >> 3, zoneZ = z >> 3;
        if (Math.abs(zoneX - (p.buildAreaZoneX + 6)) < 4 && Math.abs(zoneZ - (p.buildAreaZoneZ + 6)) < 4) {
            return;
        }
        setBuildAreas(p, zoneX - 6, zoneZ - 6);
        p.getSession().queue(new RebuildNormal(zoneX, zoneZ, -1, xtea));
    }

    /**
     * Open the top-level game frame + child panels so the client has a full viewport to render.
     * Routes through {@code PacketSender.sendGameFrame}/{@code sendInterface} rather than queuing raw
     * RSProt messages directly, because those methods are also what update {@code Player}'s
     * {@code gameFrameId}/{@code visibleInterfaces} bookkeeping — the same bookkeeping
     * {@code InterfaceHandler.getAction} consults on every click via {@code isVisibleInterface}.
     * Queuing {@link IfOpenTop}/{@link IfOpenSub} directly rendered every fixed-frame pane (inventory,
     * equipment, prayer, etc.) correctly on the client but left them permanently "not visible"
     * server-side, so every click on any of them (wield, drop, drag, equipment remove, ...) silently
     * no-opped even once the incoming packets were wired up correctly.
     *
     * <p>Milestone 4 Fix — Live Testing Round 2 (2026-07-08): opening these panes was never enough on
     * its own for the two item-holding ones. Rev 228's client-side interface system (IF3) only enables
     * clicking/dragging on a component's slots once the server has explicitly sent it an access mask
     * (RSProt's {@link net.rsprot.protocol.game.outgoing.interfaces.IfSetEvents}); without one the
     * client never even builds an outgoing {@code If3Button}/{@code IfButtonD} packet for that
     * component, which is exactly what live testing showed — zero {@code [BUTTON]}/{@code [DRAG]}
     * diagnostic output from {@code onButton}/{@code onInterfaceDrag} for any inventory click or drag.
     * This codebase already sends access masks for every OTHER item-holding interface (bank, trade,
     * rune pouch, looting bag, item sets, barrows loot, ...) — grepping confirmed the standalone
     * inventory (149) and worn-equipment (387) panes were the only two that never got one.
     * <ul>
     * <li>Inventory (149, component 0, slots 0-27): mask 1181694 reuses the exact value this codebase's
     * own {@code Bank.java} already sends for {@code Interface.BANK_INVENTORY} component 3 — a
     * proven-shape precedent for "the same 28-slot personal inventory, fully interactive" (bits 1-10 =
     * all ten click options, matching {@code TabInventory}'s own option-10-is-examine / options-1-5-
     * are-item-actions handling, plus bits 17+20 = full drag-reorder).</li>
     * <li>Equipment (387, components 6-16, one worn slot each): best-effort, not copied from an
     * existing proven analog like the inventory fix above — equipment slots hold one item each rather
     * than a 0-27 array, so they're addressed with RSProt's "no sub-range" convention ({@code start =
     * end = -1}, the same form this codebase already uses for other single, non-list components like
     * {@code NPCDialogue}/{@code YesNoDialogue}), and the mask (2046 = bits 1-10, no drag bits — real
     * OSRS equipment slots aren't drag-reorderable) is derived from {@code TabEquipment.itemAction}'s
     * own option range (1-6 custom actions + option 10 examine) rather than copied wholesale. Included
     * because {@code TabEquipment}'s unequip path was reported broken alongside inventory in the same
     * live-testing round and shares this exact root cause; flagged here as needing live-client
     * confirmation rather than assumed correct.</li>
     * </ul>
     */
    private static void openFrame(Player player) {
        player.getPacketSender().sendGameFrame(FIXED_TOP_LEVEL_INTERFACE);
        for (int[] pane : FIXED_FRAME_PANES) {
            player.getPacketSender().sendInterface(pane[0], FIXED_TOP_LEVEL_INTERFACE, pane[1], 1);
        }
        player.getPacketSender().sendAccessMask(io.dm.model.inter.Interface.INVENTORY, 0, 0, 27, 1181694);
        for (int childId = 6; childId <= 16; childId++) {
            player.getPacketSender().sendAccessMask(io.dm.model.inter.Interface.EQUIPMENT, childId, -1, -1, 2046);
        }
    }

    /**
     * Encodes the player's appearance (identkit + equipment + colours + name + combat level + icons)
     * as RSProt extended-info so the client renders them properly. Sources everything from the real
     * {@link Player.appearance appearance} / {@link Player#equipment} — loaded from the save when
     * present, defaults from {@link io.dm.model.entity.shared.masks.Appearance} otherwise. Mirrors
     * Alter's {@code PlayerInfo.syncAppearance()} + the OSRS worn-obj/identkit convention:
     * equipped items suppress the identkit slot they cover.
     */
    private static void syncAppearance(Player player) {
        var ext = player.playerInfo.getAvatar().getExtendedInfo();
        io.dm.model.entity.shared.masks.Appearance ap = player.getAppearance();
        io.dm.model.item.containers.Equipment eq = player.getEquipment();

        // Identkit (7 slots: hair, beard, top, arms, wrists, legs, shoes)
        for (int i = 0; i < 7; i++) {
            ext.setIdentKit(i, ap.styles[i]);
        }
        // Worn equipment: the 12 wear positions are the equipment slots (hat, cape, amulet, weapon,
        // chest, shield, [arms], legs, [hair], hands, feet, [beard]); absent slots (null) fall back to
        // the identkit body part. RSProt merges worn items + identkit into the appearance block.
        for (int i = 0; i < 12; i++) {
            io.dm.model.item.Item item = eq.get(i);
            // Legacy Kronos 184 custom item ids that don't exist in the stock 228 obj cache crash the
            // client component if sent as a worn-obj — render an empty slot instead (see ItemDef#isValid).
            boolean valid = item != null && io.dm.cache.ItemDef.isValid(item.getId());
            ext.setWornObj(i, valid ? item.getId() : -1, -1, -1);
        }
        // Default idle/walk/run anims (Alter's DEFAULT_ANIM_SET). Weapon-specific overrides = TODO.
        ext.setBaseAnimationSet(808, 823, 819, 820, 821, 822, 824);
        // Body colours (5: hair, torso, legs, shoes, skin).
        for (int i = 0; i < 5; i++) {
            ext.setColour(i, ap.colors[i]);
        }
        ext.setName(player.getName());
        ext.setCombatLevel(Math.max(3, io.dm.model.combat.CombatUtils.getCombatLevel(player)));
        ext.setMale(ap.isMale());
        ext.setOverheadIcon(-1);
        ext.setSkullIcon(ap.getSkullIcon());
        ext.transformToNpc(-1);
        ext.setHidden(false);
    }

    // ------------------------------------------------------------------ JS5 cache serving (unchanged)

    private static DmCacheProvider cacheProvider(FileStore fs) {
        return new FilestoreCacheProvider(
                (archive, group) -> rawGroup(fs, archive, group),
                () -> huffmanTable(fs)
        );
    }

    /**
     * Loads the OpenRS2-format XTEA key set (sibling {@code xteas.json} next to the JS5 cache dir)
     * into a per-mapsquare {@link XteaProvider}. RSProt's {@link RebuildLogin} calls
     * {@code provide(mapsquareId)} for every mapsquare in the build area to encrypt the map data;
     * the {@code mapsquare} field in the json is the same {@code (regionX << 8) | regionZ} id.
     */
    private static XteaProvider loadXteas(String js5CachePath) {
        File cacheDir = new File(js5CachePath);
        File parent = cacheDir.getParentFile();
        File xteaFile = new File(parent != null ? parent : new File("."), "xteas.json");
        Map<Integer, XteaKey> keys = new HashMap<>();
        try (Reader r = new FileReader(xteaFile)) {
            JsonArray arr = new JsonParser().parse(r).getAsJsonArray();
            for (JsonElement e : arr) {
                JsonObject o = e.getAsJsonObject();
                int mapsquare = o.get("mapsquare").getAsInt();
                JsonArray k = o.getAsJsonArray("key");
                keys.put(mapsquare, new XteaKey(new int[]{
                        k.get(0).getAsInt(), k.get(1).getAsInt(), k.get(2).getAsInt(), k.get(3).getAsInt(),
                }));
            }
            System.out.println("[XTEA] loaded " + keys.size() + " keys from " + xteaFile);
        } catch (Exception ex) {
            System.out.println("[XTEA] failed to load " + xteaFile + " (" + ex + "); serving zero keys");
        }
        XteaKey zero = new XteaKey(0, 0, 0, 0);
        return mapsquare -> keys.getOrDefault(mapsquare, zero);
    }

    /**
     * Returns the full stored cache container [compression][length][data][version] as-is. The JS5
     * group INCLUDES the 2-byte version trailer (data groups have it stripped; the master index and
     * reference tables do not). Input for RSProt's {@code Js5Service.prepareJs5Buffer}.
     */
    private static byte[] rawGroup(FileStore fs, int archive, int group) {
        try {
            byte[] container;
            if (archive == 255 && group == 255) {
                container = buildMasterIndex(fs);
            } else {
                IndexFile idx = fs.get(archive);
                container = (idx == null) ? null : idx.getArchiveData(group);
            }
            if (container == null) {
                return new byte[0];
            }
            if (archive != 255 && container.length >= 2) {
                container = java.util.Arrays.copyOf(container, container.length - 2);
            }
            return container;
        } catch (Throwable t) {
            System.out.println("[JS5] req " + archive + "/" + group + " -> ERROR " + t);
            return new byte[0];
        }
    }

    /**
     * OSRS JS5 master index (255/255), matching openrune's VersionTableBuilder: a simple uncompressed
     * container of {@code crc + revision} per index, 8 bytes each, for indices 0..indexCount-1.
     *   [compression=0][length = indexCount*8][per index: crc:4, revision:4]
     */
    private static byte[] buildMasterIndex(FileStore fs) {
        IndexFile[] files = fs.files;
        int indexCount = files.length;
        int size = indexCount * 8;
        byte[] container = new byte[5 + size];
        container[0] = 0;             // compression NONE
        writeInt(container, 1, size); // length
        for (int i = 0; i < indexCount; i++) {
            IndexFile f = files[i];
            int pos = 5 + i * 8;
            if (f != null && f.table != null) {
                writeInt(container, pos, f.table.crc);          // crc of the reference-table container
                writeInt(container, pos + 4, f.table.revision); // revision
            } // else leave zeros (empty index)
        }
        return container;
    }

    /** Decompressed Huffman table (cache archive 10, group "huffman") for RSProt's HuffmanCodec. */
    private static byte[] huffmanTable(FileStore fs) {
        IndexFile idx = fs.get(10);
        return idx.getFile(idx.getArchiveId("huffman"), idx.getFileId(""));
    }

    private static void writeInt(byte[] a, int p, int v) {
        a[p] = (byte) (v >> 24);
        a[p + 1] = (byte) (v >> 16);
        a[p + 2] = (byte) (v >> 8);
        a[p + 3] = (byte) v;
    }
}
