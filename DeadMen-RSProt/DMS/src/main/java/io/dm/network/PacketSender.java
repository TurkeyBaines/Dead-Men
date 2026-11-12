package io.dm.network;

import io.netty.channel.ChannelFutureListener;
import io.dm.Server;
import io.dm.api.buffer.OutBuffer;
import io.dm.api.protocol.Protocol;
import io.dm.api.protocol.login.LoginInfo;
import io.dm.api.utils.ISAACCipher;
import io.dm.data.impl.teleports;
import io.dm.model.World;
import io.dm.model.entity.Entity;
import io.dm.model.entity.player.Player;
import io.dm.model.entity.player.ai.AIPlayer;
import io.dm.model.inter.InterfaceType;
import io.dm.model.inter.Widget;
//import io.dm.model.inter.handlers.BuyCredits;
import io.dm.model.inter.WidgetInfo;
import io.dm.model.item.Item;
import io.dm.model.item.attributes.AttributeExtensions;
import io.dm.model.map.Position;
import io.dm.model.map.Region;
import io.dm.model.map.ground.GroundItem;
import io.dm.model.shop.ShopItem;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
public class PacketSender {

    private final Player player;

    private final ISAACCipher cipher;

    public PacketSender(Player player, ISAACCipher cipher) {
        this.player = player;
        this.cipher = cipher;
    }

    public void write(OutBuffer out) { //this has to be called by main thread or else gg because of cipher
        if (player instanceof AIPlayer || player.getChannel() == null) {
            // AI bots and RSProt-bound players have no legacy Netty channel. During the Stage-3 bind
            // the 773-line legacy 184 PacketSender is progressively replaced by RSProt messages; until
            // a given call is ported, it is dropped here rather than crashing on a null channel.
            return;
        }
        if(!Thread.currentThread().getName().equals("server-worker #1"))
            Server.logError(player.getName() + " wrote packet off main thread!", new Throwable());
        player.getChannel().write(out.encode(cipher).toBuffer());
    }

    /**
     * Stage-3 bind helper. If this player is an RSProt-bound session, queue [msg] on it and return
     * true so the caller can skip the legacy 184 encoding. For legacy players (session == null),
     * returns false and the caller writes the OutBuffer as before. Progressively used by each send
     * method as it's ported — until a method opts in, RSProt players silently drop that packet.
     */
    private boolean queueRsprot(net.rsprot.protocol.message.OutgoingGameMessage msg) {
        var s = player.getSession();
        if (s == null) return false;
        s.queue(msg);
        return true;
    }

    // Shared Netty allocator for building UpdateZonePartialEnclosed payload buffers. Not tied to any
    // player's channel (RSProt players have none) — RSProt's buildCache() only uses it as a byte
    // source, so a pooled global allocator is the correct choice here.
    private static final io.netty.buffer.ByteBufAllocator RSPROT_ZONE_ALLOC = io.netty.buffer.PooledByteBufAllocator.DEFAULT;

    /**
     * Milestone 1 (Zone-Scoped Payloads) bind helper. Wraps a single ZoneProt sub-message (loc/obj/
     * projectile/graphic/area-sound) inside the structural UpdateZonePartialEnclosed envelope RSProt
     * requires, keyed off the zone containing (x, y). Queues on this player's RSProt session and
     * returns true so the caller can skip the legacy 184 OutBuffer path; returns false (no session)
     * for legacy/AI players so the caller falls through unchanged.
     *
     * <p><b>Coordinate-bug fix (2026-07-08):</b> {@code UpdateZonePartialEnclosed}'s own KDoc (verified
     * against the real {@code osrs-228-model} sources jar) is explicit that {@code zoneX}/{@code zoneZ}
     * are the zone's south-western corner in <i>absolute tile coordinates</i> — e.g. for a player at
     * absolute x=50, the documented expected value is 48, not 6 — and that this is deliberately
     * different from the "shifted zone index" form (plain {@code x >> 3}) that {@code RebuildNormal}/
     * {@code updateBuildArea} use for their own zoneX/zoneZ. The previous code passed the shifted form
     * ({@code x >> 3}) here, which pointed every zone-scoped packet (ObjAdd, LocAddChangeV2, LocDel,
     * MapAnim, MapProjAnim, SoundArea) at completely the wrong zone — the client silently ignored it
     * since that zone wasn't the one the player is actually standing in, matching exactly the reported
     * symptom ("no server errors, but nothing appears on the client"). Fixed by masking off the
     * in-zone bits ({@code x & ~0x7}) instead of shifting them out.
     *
     * <p><b>Second coordinate-bug fix (Stage 02, 2026-07-08):</b> the fix above was necessary but not
     * sufficient — it was still one step short. {@code UpdateZonePartialEnclosed._zoneX}/{@code _zoneZ}
     * are declared {@code UByte} in the sources jar (range 0-255), and the KDoc's own worked example
     * ("player at absolute coordinate of 50, 40 <i>within the build area (104x104)</i>") makes clear
     * that "absolute, not shifted" means relative to the 104x104 <i>build area's own origin</i>, NOT
     * the world's absolute tile grid — "absolute" there only disambiguates from the divided-by-8 zone-
     * index form, exactly like {@link #queueRsprot} vs the coordinate math already proven correct
     * elsewhere in this codebase for {@code SetNpcUpdateOrigin} ({@code x - (p.buildAreaZoneX << 3)},
     * {@code RsProt.java}'s {@code driveUpdate()}). The prior fix passed {@code x & ~0x7} directly —
     * still a raw world-tile value, typically in the low thousands — which silently truncates when
     * narrowed to a {@code UByte} (e.g. world zone-corner 3216 truncates to 144, still outside the
     * valid 0-104 build-area window), landing every zone-scoped packet on an essentially arbitrary
     * wrong zone the client's local build-area grid doesn't recognise as adjacent to the player.
     * Fixed by subtracting the player's own build-area origin (in tiles, {@code buildAreaZoneX << 3})
     * to get a build-area-local coordinate in the 0-104 range the client actually expects.
     */
    private boolean queueZonePayload(int x, int y, int level, net.rsprot.protocol.message.ZoneProt payload) {
        var s = player.getSession();
        if (s == null) return false;
        int zoneX = (x & ~0x7) - (player.buildAreaZoneX << 3);
        int zoneZ = (y & ~0x7) - (player.buildAreaZoneZ << 3);
        var buf = net.rsprot.protocol.game.outgoing.codec.zone.header.DesktopUpdateZonePartialEnclosedEncoder.Companion
                .buildCache(RSPROT_ZONE_ALLOC, java.util.List.of(payload));
        s.queue(new net.rsprot.protocol.game.outgoing.zone.header.UpdateZonePartialEnclosed(
                zoneX, zoneZ, level, buf));
        return true;
    }

    /**
     * Packets
     */

    public void sendLogin(LoginInfo info) {
        if (player instanceof AIPlayer || player.getChannel() == null) {
            return;
        }
        OutBuffer out = new OutBuffer(11);
        out.addByte(2);
        out.addByte(13);

        if(info.tfaTrust) {
            out.addByte(1);
            out.startEncrypt().addInt(info.tfaTrustValue).stopEncrypt();
            out.encrypt(cipher);
        } else {
            out.addByte(0);
            out.skip(4);
        }

        out.addByte((World.isDev() || player.isAdmin()) ? 2 : player.isModerator() ? 1 : 0)
                .addByte(1)
                .addShort(player.getIndex())
                .addByte(1);
        player.getChannel().write(out.toBuffer()); //no encryption needed!
        sendRegion(true);
        Region.update(player);
    }

    public void sendLogout() {
        // RSProt: Logout.INSTANCE — the client-side "you have been logged out" packet. Session close
        // happens via Netty when the disconnect hook fires; queue the message so the client sees it.
        if (queueRsprot(net.rsprot.protocol.game.outgoing.logout.Logout.INSTANCE)) {
            return;
        }
        if (player.getChannel() == null) {
            return;
        }
        OutBuffer out = new OutBuffer(1).sendFixedPacket(16);
        player.getChannel().writeAndFlush(out.encode(cipher).toBuffer()).addListener(ChannelFutureListener.CLOSE);
    }

    public void sendRegion(boolean login) {
        player.removeFromRegions();
        Position position = player.getPosition();
        Region region = player.lastRegion = position.getRegion();
        int chunkX = position.getChunkX();
        int chunkY = position.getChunkY();
        int depth = Region.CLIENT_SIZE >> 4;
        boolean dynamic = region.dynamicData != null;
        OutBuffer out = new OutBuffer(255);
        if(login || !dynamic) {
            /**
             * Regular map
             */
            out.sendVarShortPacket(66);
            if(login)
                player.getUpdater().init(out);
            if(dynamic) {
                /**
                 * Dynamic region must be sent after regular region on login. //todo check if this is still the case in 171 since they changed how this packet is sent
                 */
                player.getUpdater().updateRegion = true;
                chunkX = chunkY = 0;
            }
            out.addLEShortA(chunkX);
            out.addLEShort(chunkY);
            int countPos = out.position();
            out.addShort(0);
            boolean forceSend = false; //Resets the landscape client sided
            if((chunkX / 8 == 48 || chunkX / 8 == 49) && chunkY / 8 == 48)
                forceSend = true;
            if(chunkX / 8 == 48 && chunkY / 8 == 148)
                forceSend = true;
            int regionCount = 0;
            for(int xCalc = (chunkX - depth) / 8; xCalc <= (chunkX + depth) / 8; xCalc++) {
                for(int yCalc = (chunkY - depth) / 8; yCalc <= (chunkY + depth) / 8; yCalc++) {
                    int regionId = yCalc + (xCalc << 8);
                    if(!forceSend || (yCalc != 49 && yCalc != 149 && yCalc != 147 && xCalc != 50 && (xCalc != 49 || yCalc != 47))) {
                        Region r = Region.get(regionId);
                        if(r.keys == null)
                            out.skip(16);
                        else
                            out.addInt(r.keys[0]).addInt(r.keys[1]).addInt(r.keys[2]).addInt(r.keys[3]);
                        player.addRegion(r);
                        regionCount++;
                    }
                }
            }
            int curPos = out.position();
            out.position(countPos);
            out.addShort(regionCount);
            out.position(curPos);
        } else {
            /**
             * Dynamic map
             */
            out.sendVarShortPacket(71)
                    .addShort(chunkY)
                    .addByteS(0) //force refresh
                    .addLEShortA(chunkX);

            int startPos = out.position();
            out.addShort(0);

            ArrayList<Integer> chunkRegionIds = new ArrayList<>();
            out.initBitAccess();
            for(int pointZ = 0; pointZ < 4; pointZ++) {
                for(int xCalc = (chunkX - depth); xCalc <= (chunkX + depth); xCalc++) {
                    for(int yCalc = (chunkY - depth); yCalc <= (chunkY + depth); yCalc++) {
                        Region r = Region.LOADED[(xCalc / 8) << 8 | (yCalc / 8)];
                        if(r == null || r.dynamicData == null || r.dynamicIndex != region.dynamicIndex) {
                            out.addBits(1, 0);
                            continue;
                        }
                        int[] chunkData = r.dynamicData[pointZ][xCalc % 8][yCalc % 8];
                        int chunkHash = chunkData[0];
                        int chunkRegionId = chunkData[1];
                        if(chunkHash == 0 || chunkRegionId == 0) {
                            out.addBits(1, 0);
                            continue;
                        }
                        out.addBits(1, 1);
                        out.addBits(26, chunkHash);
                        if(!chunkRegionIds.contains(chunkRegionId))
                            chunkRegionIds.add(chunkRegionId);
                        if(!player.getRegions().contains(r))
                            player.addRegion(r);
                    }
                }
            }
            out.finishBitAccess();

            int endPos = out.position();
            out.position(startPos);
            out.addShort(chunkRegionIds.size());
            out.position(endPos);

            for(int id : chunkRegionIds) {
                Region r = Region.LOADED[id];
                if(r.keys == null)
                    out.skip(16);
                else
                    out.addInt(r.keys[0]).addInt(r.keys[1]).addInt(r.keys[2]).addInt(r.keys[3]);
            }
        }
        write(out);
    }

    public void sendModelInformation(int parentId, int childId, int zoom, int rotationX, int rotationY) {
        // RSProt: IfSetAngle(interfaceId, componentId, angleX, angleY, zoom).
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.interfaces.IfSetAngle(
                parentId, childId, rotationX, rotationY, zoom))) {
            return;
        }
        OutBuffer out = new OutBuffer(11).sendFixedPacket(13)
                .addShort(zoom).addShortA(rotationX).addLEShortA(rotationY).addInt(parentId << 16 | childId);
        write(out);
    }

    public void sendGameFrame(int id) {
        player.setGameFrameId(id);
        // RSProt: IfOpenTop(interfaceId) — the top-level game frame (fixed 548, resizable 161/164).
        // NOTE: our RSProt login already opens 548 + child panels; a mid-game switch will call this.
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.interfaces.IfOpenTop(id))) {
            return;
        }
        OutBuffer out = new OutBuffer(3).sendFixedPacket(79)
                .addShortA(id);
        write(out);
    }

    public void refreshGameFrame() {
        OutBuffer out = new OutBuffer(1).sendFixedPacket(58);
        write(out);
    }
    //TODO: 184 Revision Fix Custom Packet
    public void sendUrl(String url, boolean copy) {
        // RSProt (A15): UrlOpen has no copy-to-clipboard variant; always opens.
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.misc.client.UrlOpen(url))) {
            return;
        }
        OutBuffer out = new OutBuffer(2 + Protocol.strLen(url) + 1).sendVarBytePacket(87)
                .addString(url)
                .addByte(copy ? 1 : 0);
        write(out);
    }

    public void sendInterface(int interfaceId, int parentId, int childId, int overlayType) {
        player.setVisibleInterface(interfaceId, parentId, childId);
        // RSProt: IfOpenSub(destInterfaceId, destComponentId, interfaceId, type). Same semantics.
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.interfaces.IfOpenSub(
                parentId, childId, interfaceId, overlayType))) {
            return;
        }
        OutBuffer out = new OutBuffer(8).sendFixedPacket(84)
                .addInt(parentId << 16 | childId)
                .addByte(overlayType)
                .addShort(interfaceId);
        write(out);
    }

    public void sendModel(int parentId, int childId, int modelId) {
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.interfaces.IfSetModel(parentId, childId, modelId))) {
            return;
        }
        OutBuffer out = new OutBuffer(9).sendFixedPacket(63)
                .addInt1(parentId << 16 | childId).addInt1(modelId);
        write(out);
    }

    public void removeInterface(int parentId, int childId) {
        player.removeVisibleInterface(parentId, childId);
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.interfaces.IfCloseSub(parentId, childId))) {
            return;
        }
        OutBuffer out = new OutBuffer(5).sendFixedPacket(85)
                .addInt(parentId << 16 | childId);
        write(out);
    }

    public void moveInterface(int fromParentId, int fromChildId, int toParentId, int toChildId) {
        player.moveVisibleInterface(fromParentId, fromChildId, toParentId, toChildId);
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.interfaces.IfMoveSub(
                fromParentId, fromChildId, toParentId, toChildId))) {
            return;
        }
        OutBuffer out = new OutBuffer(9).sendFixedPacket(73)
                .addLEInt(toParentId << 16 | toChildId)
                .addInt(fromParentId << 16 | fromChildId);
        write(out);
    }

    public void sendString(int interfaceId, int childId, String string) {
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.interfaces.IfSetText(
                (interfaceId << 16) | childId, string))) {
            return;
        }
        OutBuffer out = new OutBuffer(3 + 4 + Protocol.strLen(string)).sendVarShortPacket(32)
                .addLEInt(interfaceId << 16 | childId)
                .addString(string);
        write(out);
    }

    public void setHidden(int interfaceId, int childId, boolean hide) {
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.interfaces.IfSetHide(
                (interfaceId << 16) | childId, hide))) {
            return;
        }
        OutBuffer out = new OutBuffer(6).sendFixedPacket(10)
                .addByte(hide ? 1 : 0)
                .addLEInt(interfaceId << 16 | childId);
        write(out);
    }

    public void sendItem(int parentId, int childId, int itemId, int amount) {
        // RSProt: IfSetObject(interfaceId, componentId, itemId, count). Guard against legacy Kronos
        // 184 custom item ids that don't exist in the stock 228 obj cache (see ItemDef#isValid) —
        // sending an out-of-range id here crashes the client component. Only applied to the RSProt
        // path: the legacy 184 client's own cache natively supports these custom ids.
        if (player.getSession() != null) {
            int safeId = io.dm.cache.ItemDef.isValid(itemId) ? itemId : -1;
            if (queueRsprot(new net.rsprot.protocol.game.outgoing.interfaces.IfSetObject(
                    parentId, childId, safeId, amount))) {
                return;
            }
        } else if (queueRsprot(new net.rsprot.protocol.game.outgoing.interfaces.IfSetObject(
                parentId, childId, itemId, amount))) {
            return;
        }
        OutBuffer out = new OutBuffer(13).sendFixedPacket(75)
                .addInt(parentId << 16 | childId)
                .addLEInt(amount)
                .addInt(itemId);
        write(out);
    }

    public void setAlignment(int parentId, int childId, int x, int y) {
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.interfaces.IfSetPosition(parentId, childId, x, y))) {
            return;
        }
        OutBuffer out = new OutBuffer(9).sendFixedPacket(26)
                .addLEShortA(y)
                .addLEShortA(x)
                .addLEInt(parentId << 16 | childId);
        write(out);
    }

    public void sendAccessMask(int interfaceId, int childParentId, int minChildId, int maxChildId, int mask) {
        // RSProt: IfSetEvents(interfaceId, componentId, start, end, events).
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.interfaces.IfSetEvents(
                interfaceId, childParentId, minChildId, maxChildId, mask))) {
            return;
        }
        OutBuffer out = new OutBuffer(13).sendFixedPacket(27)
                .addLEShort(minChildId)
                .addShort(maxChildId)
                .addLEInt(mask)
                .addLEInt(interfaceId << 16 | childParentId);
        write(out);
    }

    public void sendClientScript(int id, String type, Object... params) {
        // RSProt: RunClientScript(id, types, values). Types char[] mirrors the legacy type string
        // ("iiii" etc); values are in call order (same as the varargs).
        {
            var s = player.getSession();
            if (s != null) {
                s.queue(new net.rsprot.protocol.game.outgoing.misc.player.RunClientScript(
                        id, type.toCharArray(), java.util.Arrays.asList(params)));
                return;
            }
        }
        OutBuffer out = new OutBuffer(3 + Protocol.strLen(type) + (params.length * 4)).sendVarShortPacket(56)
                .addString(type);
        for(int i = type.length() - 1; i >= 0; i--) {
            Object param = params[i];
            if(param instanceof String)
                out.addString((String) param);
            else
                out.addInt((Integer) param);
        }
        out.addInt(id);
        write(out);
    }

    public void sendSystemUpdate(int time) {
        // RSProt: UpdateRebootTimer(gameCycles). Legacy converts a 30fps duration to a 50fps cycle
        // count via `time * 50 / 30`; do the same on the RSProt path.
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.misc.client.UpdateRebootTimer(time * 50 / 30))) {
            return;
        }
        OutBuffer out = new OutBuffer(3).sendFixedPacket(72)
                .addLEShort(time * 50 / 30);
        write(out);
    }

    public void setTextStyle(int parentId, int childId, int horizontalAlignment, int verticalAlignment, int lineHeight) {
        sendClientScript(600, "iiiI", horizontalAlignment, verticalAlignment, lineHeight, parentId << 16 | childId);
    }

    public void fadeIn() {
        sendClientScript(948, "iiiii", 0, 0, 0, 255, 50);
    }

    public void fadeOut() {
        InterfaceType.SECONDARY_OVERLAY.open(player, 174);
        sendClientScript(951, "");
    }

    public void clearFade() {
        sendClientScript(948, "iiiii", 0, 0, 0, 255, 0);
    }

    public void sendMessage(String message, String extension, int type) {
        // RSProt: type=0 = plain game message, type=1 = private msg from `extension` (sender), etc.
        // MessageGame(type, message) for no sender; MessageGame(type, sender, message) otherwise.
        if (queueRsprot(extension == null || extension.isEmpty()
                ? new net.rsprot.protocol.game.outgoing.misc.player.MessageGame(type, message)
                : new net.rsprot.protocol.game.outgoing.misc.player.MessageGame(type, extension, message))) {
            return;
        }
        OutBuffer out = Protocol.messagePacket(message, extension, type);
        write(out);
    }

    public void sendVarp(int id, int value) {
        if (id >= 20000) {
            if (id != 20000 && id != 20002 && id != 20004) {
                return;
            }
        }
        // RSProt: VarpSmall for byte-range values, VarpLarge otherwise (matches legacy pick).
        if (queueRsprot(value < Byte.MIN_VALUE || value > Byte.MAX_VALUE
                ? new net.rsprot.protocol.game.outgoing.varp.VarpLarge(id, value)
                : new net.rsprot.protocol.game.outgoing.varp.VarpSmall(id, value))) {
            return;
        }
        OutBuffer out;
        if(value < Byte.MIN_VALUE || value > Byte.MAX_VALUE)
            out = new OutBuffer(7).sendFixedPacket(42).addInt1(value).addLEShort(id);
        else
            out = new OutBuffer(4).sendFixedPacket(34).addShortA(id).addByte(value);
        write(out);
    }

    public void sendItems(int parentId, int childId, int containerId, Item... items) {
        sendItems(parentId << 16 | childId, containerId, items, items.length);
    }

    public void sendItems(int parentId, int childId, int containerId, Item[] items, int length) {
        sendItems(parentId << 16 | childId, containerId, items, length);
    }


    public void sendItems(WidgetInfo widgetInfo, int containerId, Item[] items, int length) {
        sendItems(widgetInfo.getPackedId(), containerId, items, length);
    }

    public void sendItems(int interfaceHash, int containerId, Item[] items, int length) {
        // RSProt: full container refresh. The interfaceHash packing (parent<<16|child) matches the
        // combinedId RSProt uses. NOTE: item attributes (per-slot appended metadata in the legacy
        // packet) are NOT transmitted here — RSProt's UpdateInvFull doesn't carry that side-channel.
        // Bank/inventory show correct id+count; upgrade attributes are a follow-up.
        {
            var s = player.getSession();
            if (s != null) {
                final Item[] fitems = items;
                final int flen = length;
                // combinedId is intentionally -1 (via the 3-arg UpdateInvFull ctor): per RSProt's own
                // docs a positive combinedId "will always lead to crashing" on IF3-type interfaces, so
                // -1 here is the correct/required form, not a workaround. The container is identified
                // by containerId; the interface displays it via its cache IF3 binding.
                // Item-id bounds guard: legacy Kronos 184 custom item ids that don't exist in the
                // stock 228 obj cache crash the client component if transmitted — treat them as an
                // empty slot instead (see io.dm.cache.ItemDef#isValid).
                s.queue(new net.rsprot.protocol.game.outgoing.inv.UpdateInvFull(
                        containerId, flen,
                        (int slot) -> {
                            if (slot >= flen) return net.rsprot.protocol.common.game.outgoing.inv.InventoryObject.NULL;
                            Item it = fitems[slot];
                            if (it == null || !io.dm.cache.ItemDef.isValid(it.getId())) return net.rsprot.protocol.common.game.outgoing.inv.InventoryObject.NULL;
                            return net.rsprot.protocol.common.game.outgoing.inv.InventoryObject.pack(it.getId(), it.getAmount());
                        }));
                return;
            }
        }
        OutBuffer out = new OutBuffer(255).sendVarShortPacket(49)
                .addInt(interfaceHash)
                .addShort(containerId)
                .addShort(length);
        for (int slot = 0; slot < length; slot++) {
            Item item = items[slot];
            out.addInt(item == null || item.getId() < 0 ? 0 : item.getId() + 1);
            out.addInt(item == null || item.getId() < 0 ? 0 : item.getAmount());
            String[] attributes = AttributeExtensions.getUpgradesForDisplay(item);
            out.addByte(attributes == null ? 0 : attributes.length);
            if (attributes != null) {
                for (String attribute : attributes) {
                    out.addString(attribute);
                }
            }
        }
        write(out);
    }

    public void sendShopItems(int interfaceHash, int containerId, ShopItem[] items, int length) {
        // RSProt (A6): same UpdateInvFull form as sendItems (combinedId -1; shop interface 300 is bound
        // to inv containerId by the cs2 149 init). Slot id is the display id (placeholders), and
        // custom 184 ids missing from Cache228 are sent as empty slots (ItemDef.isValid).
        if (player.getSession() != null) {
            final ShopItem[] fitems = items;
            final int flen = length;
            queueRsprot(new net.rsprot.protocol.game.outgoing.inv.UpdateInvFull(
                    containerId, flen,
                    (int slot) -> {
                        if (slot >= flen) return net.rsprot.protocol.common.game.outgoing.inv.InventoryObject.NULL;
                        ShopItem it = fitems[slot];
                        if (it == null) return net.rsprot.protocol.common.game.outgoing.inv.InventoryObject.NULL;
                        int id = it.getDisplayId(player);
                        if (id < 0 || !io.dm.cache.ItemDef.isValid(id)) return net.rsprot.protocol.common.game.outgoing.inv.InventoryObject.NULL;
                        return net.rsprot.protocol.common.game.outgoing.inv.InventoryObject.pack(id, it.getAmount());
                    }));
            return;
        }
        OutBuffer out = new OutBuffer(255).sendVarShortPacket(49)
                .addInt(interfaceHash)
                .addShort(containerId)
                .addShort(length);
        for (int slot = 0; slot < length; slot++) {
            ShopItem item = items[slot];
            out.addInt(item == null || item.getDisplayId(player) < 0 ? 0 : item.getDisplayId(player) + 1);
            out.addInt(item == null || item.getDisplayId(player) < 0 ? 0 : item.getAmount());
            String[] attributes = AttributeExtensions.getUpgradesForDisplay(item);
            out.addByte(attributes == null ? 0 : attributes.length);
            if (attributes != null) {
                for (String attribute : attributes) {
                    out.addString(attribute);
                }
            }
        }
        write(out);
    }

    public void updateItems(int interfaceHash, int containerId, Item[] items, boolean[] updatedSlots, int updatedCount) {
        // RSProt: UpdateInvPartial sends only the changed slots. Collect the changed indices, then
        // pass them + a per-slot lookup as an IndexedObjectProvider.
        {
            var s = player.getSession();
            if (s != null) {
                java.util.List<Integer> changed = new java.util.ArrayList<>();
                for (int i = 0; i < items.length; i++) if (updatedSlots[i]) changed.add(i);
                final Item[] fitems = items;
                s.queue(new net.rsprot.protocol.game.outgoing.inv.UpdateInvPartial(
                        containerId,
                        new net.rsprot.protocol.game.outgoing.inv.UpdateInvPartial.IndexedObjectProvider(changed.iterator()) {
                            @Override public long provide(int slot) {
                                Item it = slot < fitems.length ? fitems[slot] : null;
                                // Unlike UpdateInvFull (where position in the stream IS the slot), partial
                                // updates are sparse: RSProt reads the target slot back out of the packed
                                // long itself via InventoryObject.getSlot(). The 2-arg pack(id, count)
                                // overload silently packs slot 0, so every partial update told the client
                                // "slot 0 changed" no matter which real slot changed — each new/changed item
                                // visually replaced whatever was in slot 0 instead of showing in its own
                                // slot. Must use the 3-arg pack(slot, id, count) form here.
                                if (it == null || !io.dm.cache.ItemDef.isValid(it.getId())) return net.rsprot.protocol.common.game.outgoing.inv.InventoryObject.pack(slot, -1, -1);
                                return net.rsprot.protocol.common.game.outgoing.inv.InventoryObject.pack(slot, it.getId(), it.getAmount());
                            }
                        }));
                return;
            }
        }
        OutBuffer out = new OutBuffer(255).sendVarShortPacket(1).addInt(interfaceHash).addShort(containerId);
        for (int slot = 0; slot < items.length; slot++) {
            if (updatedSlots[slot]) {
                Item item = items[slot];
                out.addSmart(slot);
                out.addInt(item == null || item.getId() < 0 ? 0 : item.getId() + 1);
                out.addInt(item == null || item.getId() < 0 ? 0 : item.getAmount());
                String[] attributes = AttributeExtensions.getUpgradesForDisplay(item);
                out.addByte(attributes == null ? 0 : attributes.length);
                if (attributes != null) {
                    for (String attribute : attributes) {
                        out.addString(attribute);
                    }
                }
            }
        }
        write(out);
    }

    public void updateItems(int interfaceHash, int containerId, ShopItem[] items, boolean[] updatedSlots, int updatedCount) {
        // RSProt (A7): UpdateInvPartial, 3-arg pack(slot, id, count) (see the Item[] overload above).
        if (player.getSession() != null) {
            java.util.List<Integer> changed = new java.util.ArrayList<>();
            for (int i = 0; i < items.length; i++) if (updatedSlots[i]) changed.add(i);
            final ShopItem[] fitems = items;
            queueRsprot(new net.rsprot.protocol.game.outgoing.inv.UpdateInvPartial(
                    containerId,
                    new net.rsprot.protocol.game.outgoing.inv.UpdateInvPartial.IndexedObjectProvider(changed.iterator()) {
                        @Override public long provide(int slot) {
                            ShopItem it = slot < fitems.length ? fitems[slot] : null;
                            int id = it == null ? -1 : it.getDisplayId(player);
                            if (id < 0 || !io.dm.cache.ItemDef.isValid(id)) return net.rsprot.protocol.common.game.outgoing.inv.InventoryObject.pack(slot, -1, -1);
                            return net.rsprot.protocol.common.game.outgoing.inv.InventoryObject.pack(slot, id, it.getAmount());
                        }
                    }));
            return;
        }
        OutBuffer out = new OutBuffer(255).sendVarShortPacket(1)
                .addInt(interfaceHash)
                .addShort(containerId);
        for(int slot = 0; slot < items.length; slot++) {
            if(updatedSlots[slot]) {
                ShopItem item = items[slot];
                out.addSmart(slot);
                out.addInt(item == null || item.getDisplayId(player) < 0 ? 0 : item.getDisplayId(player) + 1);
                out.addInt(item == null || item.getDisplayId(player) < 0 ? 0 : item.getAmount());
                String[] attributes = AttributeExtensions.getUpgradesForDisplay(item);
                out.addByte(attributes == null ? 0 : attributes.length);
                if (attributes != null) {
                    for (String attribute : attributes) {
                        out.addString(attribute);
                    }
                }
            }
        }
        write(out);
    }

    public void unlinkItems(int containerId) {
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.inv.UpdateInvStopTransmit(containerId))) {
            return;
        }
        OutBuffer out = new OutBuffer(3).sendFixedPacket(57)
                .addShortA(containerId);
        write(out);
    }

    public void sendStat(int id, int currentLevel, int experience) {
        // RSProt: UpdateStatV2(stat, invisibleBoostedLevel, currentLevel, experience). We don't
        // model invisible boosts here, so mirror currentLevel.
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.misc.player.UpdateStatV2(
                id, currentLevel, currentLevel, experience))) {
            return;
        }
        OutBuffer out = new OutBuffer(7).sendFixedPacket(9)
                .addInt1(experience)
                .addByteA(id)
                .addByteS(currentLevel);
        write(out);
    }

    public void sendRunEnergy(int energy) {
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.misc.player.UpdateRunEnergy(energy))) {
            return;
        }
        OutBuffer out = new OutBuffer(2).sendFixedPacket(47)
                .addByte(energy);
        write(out);
    }

    public void sendWeight(int weight) {
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.misc.player.UpdateRunWeight(weight))) {
            return;
        }
        OutBuffer out = new OutBuffer(3).sendFixedPacket(69)
                .addShort(weight);
        write(out);
    }

    public void sendPlayerAction(String name, boolean top, int option) {
        OutBuffer out = new OutBuffer(4 + Protocol.strLen(name)).sendVarBytePacket(80)
                .addByteS(top ? 1 : 0)
                .addString(name)
                .addByteA(option);

        write(out);
    }

    public void worldHop(String host, int id, int flags) {
        OutBuffer out = new OutBuffer(50).sendFixedPacket(76)
                .addString(host)
                .addShort(id)
                .addInt(flags);
        //todo@@ write(out);
    }

    public void resetMapFlag() {
        // RSProt (A8): SetMapFlag(255, 255) clears the destination flag.
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.misc.player.SetMapFlag(255, 255))) {
            return;
        }
        OutBuffer out = new OutBuffer(3).sendFixedPacket(25)
                .addByte(-1)
                .addByte(-1);
        write(out);
    }

    public void clearChunks() {
        // RSProt (A9): the only callers (Region.update) run right after a RebuildNormal/RebuildRegion,
        // which already wipes every zone client-side, so there is nothing to send.
        if (player.getSession() != null) {
            return;
        }
        OutBuffer out = new OutBuffer(3).sendFixedPacket(65)
                .addByteS(-1)
                .addByteC(-1);
        write(out);
    }

    public void clearChunk(int chunkAbsX, int chunkAbsY) {
        // RSProt (A9): UpdateZoneFullFollows clears the zone; coords are build-area-relative tiles
        // (same convention as queueZonePayload).
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.zone.header.UpdateZoneFullFollows(
                (chunkAbsX & ~0x7) - (player.buildAreaZoneX << 3),
                (chunkAbsY & ~0x7) - (player.buildAreaZoneZ << 3),
                player.getHeight()))) {
            return;
        }
        int x = Position.getLocal(chunkAbsX, player.getPosition().getFirstChunkX());
        int y = Position.getLocal(chunkAbsY, player.getPosition().getFirstChunkY());
        OutBuffer out = new OutBuffer(3).sendFixedPacket(65)
                .addByteS(y)
                .addByteC(x);
        write(out);
    }

    private void sendMapPacket(int x, int y, int z, Function<Integer, OutBuffer> write) {
        if(player.getHeight() != z)
            return;
        int chunkAbsX = (x >> 3) << 3;
        int chunkAbsY = (y >> 3) << 3;
        int targetLocalX = x - chunkAbsX;
        int targetLocalY = y - chunkAbsY;
        int playerLocalX = Position.getLocal(chunkAbsX, player.getPosition().getFirstChunkX());
        int playerLocalY = Position.getLocal(chunkAbsY, player.getPosition().getFirstChunkY());
        if(playerLocalX >= 0 && playerLocalX < 104 && playerLocalY >= 0 && playerLocalY < 104) {
            write(new OutBuffer(3).sendFixedPacket(29).addByteC(playerLocalX).addByte(playerLocalY));
            write(write.apply((targetLocalX & 0x7) << 4 | (targetLocalY & 0x7)));
        }
    }

    public void sendGroundItem(GroundItem groundItem) {
        // RSProt: ObjAdd(id, quantity, xInZone, zInZone, opFlags). Stage 02 fix (2026-07-08): the 5th
        // param of this JVM-helper constructor is opFlags (which of the item's 5 cache-defined
        // right-click options to SHOW), not ownershipType as a previous comment here claimed —
        // ownershipType only exists on the 9-arg ctor and isn't reachable from this one at all. Passing
        // (byte) 0 (OpFlags.NONE_SHOWN) hid every cache-defined option, including "Take" — the item
        // rendered but its context menu fell back to client-only defaults (Walk here/Examine/Cancel).
        // Ownership VISIBILITY (who this packet is even sent to) is unrelated and already correctly
        // resolved by GroundItem.sendAdd()'s per-player loop before this is ever called.
        if (queueZonePayload(groundItem.x, groundItem.y, groundItem.z,
                new net.rsprot.protocol.game.outgoing.zone.payload.ObjAdd(
                        groundItem.id, groundItem.amount, groundItem.x & 0x7, groundItem.y & 0x7,
                        net.rsprot.protocol.game.outgoing.util.OpFlags.ALL_SHOWN))) {
            return;
        }
        sendMapPacket(groundItem.x, groundItem.y, groundItem.z, offsetHash ->
                new OutBuffer(6).sendFixedPacket(78)
                        .addShortA(groundItem.amount)
                        .addLEShort(groundItem.id)
                        .addByteA(offsetHash)

        );
    }

    public void sendRemoveGroundItem(GroundItem groundItem) {
        // RSProt: ObjDel(id, quantity, xInZone, zInZone) — never ported before Stage 02; ground-item
        // removal silently no-op'd for RSProt players (null Netty channel, same as every other
        // unported send). Field order verified against the osrs-228-model sources jar.
        if (queueZonePayload(groundItem.x, groundItem.y, groundItem.z,
                new net.rsprot.protocol.game.outgoing.zone.payload.ObjDel(
                        groundItem.id, groundItem.amount, groundItem.x & 0x7, groundItem.y & 0x7))) {
            return;
        }
        sendMapPacket(groundItem.x, groundItem.y, groundItem.z, offsetHash ->
                new OutBuffer(4).sendFixedPacket(30)
                        .addByteA(offsetHash)
                        .addLEShortA(groundItem.id)

        );
    }

    public void sendCreateObject(int id, int x, int y, int z, int type, int dir) {
        // RSProt: LocAddChangeV2(id, xInZone, zInZone, shape, rotation, opFlags). opFlags=0 (no
        // custom right-click ops beyond cache-defined ones).
        if (queueZonePayload(x, y, z, new net.rsprot.protocol.game.outgoing.zone.payload.LocAddChangeV2(
                id, x & 0x7, y & 0x7, type, dir, (byte) 0))) {
            return;
        }
        sendMapPacket(x, y, z, offsetHash ->
                new OutBuffer(5).sendFixedPacket(77)
                        .addByte(type << 2 | dir)
                        .addByteA(offsetHash)
                        .addShort(id)
        );
    }

    public void sendRemoveObject(int x, int y, int z, int type, int dir) {
        // RSProt: LocDel(xInZone, zInZone, shape, rotation).
        if (queueZonePayload(x, y, z, new net.rsprot.protocol.game.outgoing.zone.payload.LocDel(
                x & 0x7, y & 0x7, type, dir))) {
            return;
        }
        sendMapPacket(x, y, z, offsetHash ->
                new OutBuffer(3).sendFixedPacket(60)
                        .addByteC(type << 2 | dir)
                        .addByteA(offsetHash)
        );
    }

    public void sendObjectAnimation(int x, int y, int z, int type, int dir, int animationId) {
        // RSProt: LocAnim(id, xInZone, zInZone, shape, rotation) — same type/dir->shape/rotation
        // mapping already used by sendCreateObject/sendRemoveObject above.
        if (queueZonePayload(x, y, z, new net.rsprot.protocol.game.outgoing.zone.payload.LocAnim(
                animationId, x & 0x7, y & 0x7, type, dir))) {
            return;
        }
        sendMapPacket(x, y, z, offsetHash ->
                new OutBuffer(5).sendFixedPacket(54)
                        .addShortA(animationId)
                        .addByteC(offsetHash)
                        .addByte(type << 2 | dir)

        );
    }

    public void sendProjectile(int projectileId, int startX, int startY, int destX, int destY, int targetIndex, int startHeight, int endHeight, int delay, int duration, int curve, int something) {
        // RSProt (rev 228): MapProjAnim(id, startHeight, endHeight, startTime, endTime, angle,
        // progress, sourceIndex, targetIndex, xInZone, zInZone, deltaX, deltaZ) — a 1:1 rename of the
        // legacy wire fields (delay->startTime, duration->endTime, (destX-startX)->deltaX,
        // (destY-startY)->deltaZ), so no arithmetic recombination needed.
        //
        // Stage 10 (2026-07-13), verified against MapProjAnim.kt's KDoc in the osrs-228-model
        // sources jar:
        // - sourceIndex/targetIndex use the convention 0 = no lock, -(index+1) = player,
        //   (index+1) = NPC. Projectile.java already passes targetIndex in exactly that encoding
        //   (0 for tile targets, ±(index+1) for entities), so it passes through unchanged. The
        //   legacy signature has no source-entity concept, so sourceIndex = 0 (none) — the previous
        //   -1 here was wrong: in this encoding -1 means "locked to the player at index 0", which
        //   would visually snap every projectile's origin to an arbitrary player.
        // - curve->angle and something->progress are confirmed correct, not just positional guesses:
        //   the legacy presets in Projectile.java (dumped from real RS per its own print() method)
        //   carry curve=15/idk=11 for arrows — the canonical OSRS angle/progress values for that
        //   projectile family.
        if (queueZonePayload(startX, startY, player.getHeight(),
                new net.rsprot.protocol.game.outgoing.zone.payload.MapProjAnim(
                        projectileId, startHeight, endHeight, delay, duration, curve, something,
                        0, targetIndex, startX & 0x7, startY & 0x7, destX - startX, destY - startY))) {
            return;
        }
        sendMapPacket(startX, startY, player.getHeight(), offsetHash ->
                new OutBuffer(16).sendFixedPacket(38)
                        .addLEShortA(projectileId)
                        .addByteS(offsetHash)
                        .addLEShort(delay)
                        .addLEShortA(duration)
                        .addByteS((destY - startY))
                        .addByteS(endHeight)
                        .addByte(something)
                        .addByteA(curve)
                        .addByteA((destX - startX))
                        .addLEShort(targetIndex)
                        .addByteS(startHeight)
        );
    }

    public void sendGraphics(int id, int height, int delay, int x, int y, int z) {
        // RSProt: MapAnim(id, delay, height, xInZone, zInZone) — a spotanim at a fixed tile, no loc.
        if (queueZonePayload(x, y, z, new net.rsprot.protocol.game.outgoing.zone.payload.MapAnim(
                id, delay, height, x & 0x7, y & 0x7))) {
            return;
        }
        sendMapPacket(x, y, z, offsetHash ->
                new OutBuffer(7).sendFixedPacket(19)
                        .addLEShortA(delay)
                        .addByteA(height)
                        .addByteA(offsetHash)
                        .addLEShort(id)

        );
    }

    public void sendAreaSound(int id, int type, int delay, int x, int y, int distance) {
        // RSProt: SoundArea(id, delay, loops, range, dropOffRange, xInZone, zInZone). Legacy packed
        // `distance << 4 | type` into one byte — high nibble radius, low nibble loop count — so
        // distance->range and type->loops map 1:1 (every caller goes through Entity.publicSound,
        // which always passes type >= 1; SoundArea.kt's KDoc warns loops=0 means the sound never
        // plays at all, so if a future caller passes type=0 expecting "one-shot", that's the bug).
        //
        // Stage 10 fix (2026-07-13): dropOffRange is NOT a second radius — per SoundArea.kt's KDoc
        // it's the SIZE of the sound's origin ("in most cases, this should be a value of 1"; larger
        // values exist for big NPCs so sound flows from their whole footprint). Passing `distance`
        // (15) here made the client treat the coord as the SW corner of a 15x15 even-volume box,
        // destroying the distance falloff. The legacy signature has no origin-size concept, so 1.
        if (queueZonePayload(x, y, player.getHeight(),
                new net.rsprot.protocol.game.outgoing.zone.payload.SoundArea(
                        id, delay, type, distance, 1, x & 0x7, y & 0x7))) {
            return;
        }
        sendMapPacket(x, y, player.getHeight(), offsetHash ->
                new OutBuffer(6).sendFixedPacket(41)
                        .addByteS(offsetHash)
                        .addByteS(distance << 4 | type)
                        .addShort(id)
                        .addByteS(delay)
        );
    }

    public void sendSoundEffect(int id, int type, int delay) {
        // RSProt: SynthSound(id, loops, delay). Legacy `type` was 0=one-shot, 1=looping — approx.
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.sound.SynthSound(id, type, delay))) {
            return;
        }
        OutBuffer out = new OutBuffer(6).sendFixedPacket(33)
                .addShort(id)
                .addByte(type)
                .addShort(delay);
        write(out);
    }

    public void sendMusic(int id) {
        // RSProt: MidiSongV2(id, fadeOutDelay, fadeOutSpeed, fadeInDelay, fadeInSpeed). No fades = 0.
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.sound.MidiSongV2(id, 0, 0, 0, 0))) {
            return;
        }
        OutBuffer out = new OutBuffer(3).sendFixedPacket(44)
                .addShort(id);
        write(out);
    }

    //TODO: 184 Revision Fix Custom Packet
    public void sendSkillinterface(String donatorStatus) {
        /*OutBuffer out = new OutBuffer(2 + Protocol.strLen(donatorStatus)).sendVarShortPacket(95)
                .addString(donatorStatus);
        write(out);*/
    }

    //TODO: 184 Revision Fix Custom Packet
    public void sendTeleports(String title,
                              int selectedCategoryIndex, teleports.Category[] categories,
                              int selectedSubcategoryIndex, teleports.Subcategory[] subcategories,
                              teleports.Teleport[] teleports) {

        OutBuffer out = new OutBuffer(255).sendVarShortPacket(89);

        if(title != null)
            out.addString(title);
        else
            out.addByte(0);

        out.addByte(selectedCategoryIndex);
        if(categories != null) {
            out.addByte(categories.length);
            for(teleports.Category c : categories)
                out.addString(c.name);
        } else {
            out.addByte(0);
        }

        out.addByte(selectedSubcategoryIndex);
        if(subcategories != null) {
            out.addByte(subcategories.length);
            for(teleports.Subcategory c : subcategories)
                out.addString(c.name);
        } else {
            out.addByte(0);
        }

        if(teleports != null) {
            for(teleports.Teleport t : teleports)
                out.addString(t.name);
        }

        write(out);
    }
    public void sendWidget(Widget widget, int seconds) {
        OutBuffer out = new OutBuffer(4).sendVarShortPacket(91)
                .addByte(widget.getSpriteId())
                .addShort(seconds * 50)
                .addString(widget.getName())
                .addString(widget.getDescription());
        write(out);
    }

    public void sendPlayerHead(int parentId, int childId) {
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.interfaces.IfSetPlayerHead(
                parentId, childId))) {
            return;
        }
        OutBuffer out = new OutBuffer(5).sendFixedPacket(81)
                .addLEInt(parentId << 16 | childId);
        write(out);
    }

    public void sendNpcHead(int parentId, int childId, int npcId) {
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.interfaces.IfSetNpcHead(
                parentId, childId, npcId))) {
            return;
        }
        OutBuffer out = new OutBuffer(7).sendFixedPacket(37)
                .addInt2(parentId << 16 | childId)
                .addShortA(npcId);
        write(out);
    }

    public void animateInterface(int parentId, int childId, int animationId) {
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.interfaces.IfSetAnim(
                parentId, childId, animationId))) {
            return;
        }
        OutBuffer out = new OutBuffer(7).sendFixedPacket(39)
                .addShortA(animationId)
                .addInt2(parentId << 16 | childId);
        write(out);
    }

    public void sendMapState(int state) {
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.misc.client.MinimapToggle(state))) {
            return;
        }
        OutBuffer out = new OutBuffer(2).sendFixedPacket(55)
                .addByte(state);
        write(out);
    }

    public void sendHintIcon(Entity target) {
        // RSProt: HintArrow(HintArrowType) — NpcHintArrow/PlayerHintArrow by target kind.
        var type = target.player == null
                ? new net.rsprot.protocol.game.outgoing.misc.client.HintArrow.NpcHintArrow(target.getIndex())
                : new net.rsprot.protocol.game.outgoing.misc.client.HintArrow.PlayerHintArrow(target.getIndex());
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.misc.client.HintArrow(type))) return;
        OutBuffer out = new OutBuffer(8).sendFixedPacket(46)
                .addByte(target.player == null ? 1 : 10)
                .addShort(target.getIndex())
                .skip(3);
        write(out);
    }

    public void sendHintIcon(int x, int y) {
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.misc.client.HintArrow(
                new net.rsprot.protocol.game.outgoing.misc.client.HintArrow.TileHintArrow(
                        x, y, player.getHeight(), 0)))) return;
        OutBuffer out = new OutBuffer(8).sendFixedPacket(46)
                .addByte(2)
                .addShort(x)
                .addShort(y)
                .addByte(1);
        write(out);
    }

    public void sendHintIcon(Position position) {
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.misc.client.HintArrow(
                new net.rsprot.protocol.game.outgoing.misc.client.HintArrow.TileHintArrow(
                        position.getX(), position.getY(), position.getZ(), 0)))) return;
        OutBuffer out = new OutBuffer(8).sendFixedPacket(46)
                .addByte(2)
                .addShort(position.getX())
                .addShort(position.getY())
                .addByte(1);
        write(out);
    }

    public void resetHintIcon(boolean npcType) {
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.misc.client.HintArrow(
                net.rsprot.protocol.game.outgoing.misc.client.HintArrow.ResetHintArrow.INSTANCE))) return;
        OutBuffer out = new OutBuffer(8).sendFixedPacket(46)
                .addByte(npcType ? 1 : 10)
                .addShort(-1)
                .skip(3);
        write(out);
    }

    public void turnCameraToLocation(int x, int y, int cameraHeight, int constantSpeed, int variableSpeed) {
        // RSProt (A11): camera coords are build-area-relative tiles.
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.camera.CamLookAt(
                x - (player.buildAreaZoneX << 3), y - (player.buildAreaZoneZ << 3),
                cameraHeight, constantSpeed, variableSpeed))) {
            return;
        }
        Position pos = new Position(x, y, 0);
        int posX = pos.getSceneX(player.getPosition());
        int posY = pos.getSceneY(player.getPosition());
        OutBuffer out = new OutBuffer(7).sendFixedPacket(68)
                .addByte(posX)
                .addByte(posY)
                .addShort(cameraHeight)
                .addByte(constantSpeed)
                .addByte(variableSpeed);
        write(out);
    }

    public void moveCameraToLocation(int x, int y, int cameraHeight, int constantSpeed, int variableSpeed) {
        // RSProt (A12): camera coords are build-area-relative tiles.
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.camera.CamMoveTo(
                x - (player.buildAreaZoneX << 3), y - (player.buildAreaZoneZ << 3),
                cameraHeight, constantSpeed, variableSpeed))) {
            return;
        }
        Position pos = new Position(x, y, 0);
        int posX = pos.getSceneX(player.getPosition());
        int posY = pos.getSceneY(player.getPosition());
        OutBuffer out = new OutBuffer(7).sendFixedPacket(0)
                .addByte(posX)
                .addByte(posY)
                .addShort(cameraHeight)
                .addByte(constantSpeed)
                .addByte(variableSpeed);
        write(out);
    }

    /**
     * @param shakeType 0 shakes X, 1 shakes Z, 2 shakes Y, 3 shakes Yaw, 4 shakes Pitch
     */
    public void shakeCamera(int shakeType, int intensity) {
        // RSProt (A13): CamShake(axis, random, amplitude, rate); legacy sent the same value for the last three.
        if (queueRsprot(new net.rsprot.protocol.game.outgoing.camera.CamShake(
                shakeType, intensity, intensity, intensity))) {
            return;
        }
        OutBuffer out = new OutBuffer(5).sendFixedPacket(51)
                .addByte(shakeType)
                .addByte(intensity)
                .addByte(intensity)
                .addByte(intensity);
        write(out);
    }

    public void resetCamera() {
        if (queueRsprot(net.rsprot.protocol.game.outgoing.camera.CamReset.INSTANCE)) {
            return;
        }
        OutBuffer out = new OutBuffer(1).sendFixedPacket(76);
        write(out);
    }

}