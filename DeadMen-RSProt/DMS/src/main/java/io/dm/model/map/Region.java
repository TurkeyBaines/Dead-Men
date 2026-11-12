package io.dm.model.map;

import io.dm.Server;
import io.dm.api.buffer.InBuffer;
import io.dm.api.filestore.IndexFile;
import io.dm.model.entity.player.Player;
import io.dm.model.item.containers.bank.BankActions;
import io.dm.model.map.object.GameObject;
import io.dm.model.map.route.RouteFinder;
import io.dm.model.skills.construction.House;

import java.util.ArrayList;

public class Region {

    public static final int CLIENT_SIZE = 104;

    public static final Region[] LOADED = new Region[Short.MAX_VALUE + 1];

    /**
     * Separator
     */

    public final int id;

    public final int baseX, baseY;

    public final Bounds bounds;

    public final ArrayList<Player> players;

    public final ArrayList<Tile> activeTiles;

    private House house; // house instance that this region currently belongs to

    public Region(int id) {
        this.id = id;
        this.baseX = (id >> 8) * 64;
        this.baseY = (id & 0xff) * 64;
        this.bounds = Bounds.fromRegion(id);
        this.players = new ArrayList<>();
        this.activeTiles = new ArrayList<>();
    }

    /**
     * Data
     */

    public int[] keys;

    public Tile[][][] tiles;

    public int[][][][] dynamicData;

    public int dynamicIndex = -1;

    public int dynamicRegionBaseX, dynamicRegionBaseY;

    public boolean empty;

    public void init() { //not my favorite design, let's come back to this one day..
        byte[][][] tileData = new byte[4][64][64];
        byte[] mapData = getMapData();
        if(mapData != null) {
            InBuffer mapIn = new InBuffer(mapData);
            // FIX (Stage 13, 2026-07-14): opcode and overlay-id are BOTH 2-byte fields in this cache
            // generation, not 1-byte — confirmed against the Avexiis/RSPSi fork's `loadTerrain` decoder
            // (github.com/Avexiis/RSPSi, MapRegion.java), which the human visually verified renders
            // bridge flags and blocked water correctly against this exact stock cache. The OLD 1-byte
            // opcode read (still present in that same fork as `decodeOldMapData`, byte-identical to what
            // this method used to do) matches a legacy cache generation, not this one. Round 1 of this
            // investigation (see stages/STAGE-13-clipping-and-bridges.txt) only widened the overlay-id
            // read while leaving the opcode read at 1 byte — a half-fix that desyncs the stream worse,
            // which is why it made things worse elsewhere when tried. Widening BOTH together keeps the
            // stream aligned.
            for(int z = 0; z < 4; z++) {
                for(int x = 0; x < 64; x++) {
                    for(int y = 0; y < 64; y++) {
                        for(; ; ) {
                            int i = mapIn.readUnsignedShort();
                            if(i == 0)
                                break;
                            if(i == 1) {
                                mapIn.skipByte();
                                break;
                            }
                            if(i <= 49)
                                mapIn.skip(2);
                            else if(i <= 81)
                                tileData[z][x][y] = (byte) (i - 49);
                        }
                    }
                }
            }
            for(int z = 0; z < 4; z++) {
                for(int x = 0; x < 64; x++) {
                    for(int y = 0; y < 64; y++) {
                        if((tileData[z][x][y] & 0x1) == 1) {
                            int height = z;
                            if((tileData[1][x][y] & 0x2) == 2)
                                height--;
                            if(height >= 0) {
                                int absX = baseX + x;
                                int absY = baseY + y;
                                Tile tile = getTile(absX, absY, z, true);
                                tile.flagUnmovable();
                                tile.defaultClipping = tile.clipping;
                            }
                        }
                        if((tileData[z][x][y] & 0x4) != 0) {
                            int absX = baseX + x;
                            int absY = baseY + y;
                            Tile tile = getTile(absX, absY, z, true);
                            tile.roofExists = true;
                        }
                    }
                }
            }
        }
        byte[] landscapeData = null;
        boolean invalidKeys = false;
        try {
            landscapeData = getLandscapeData();
        } catch(Throwable t) {
            //System.err.println("Invalid Map Keys for Region (" + id + "): base=(" + baseX + ", " + baseY + ") keys=" + Arrays.toString(keys));
            invalidKeys = true;
        }
        if(landscapeData != null) {
            InBuffer landIn = new InBuffer(landscapeData);
            int objectId = -1;
            for(; ; ) {
                // FIX (Stage 14b, 2026-07-14): the object-id gap between successive locs is encoded
                // as an accumulating "smart2"-style value (each 32767 chunk means "add 32767 and keep
                // reading"), not a single plain smart. Confirmed against Avexiis/RSPSi's real decoder
                // (`Buffer.readUnsignedIntSmartShortCompat`/`readUSmartInt`, both used for this exact
                // outer id-offset loop in `unpackObjects`/`decodeConstructedLandscapes`) — this
                // codebase already has the matching accumulator (`InBuffer.readSmart2()`), it just
                // wasn't used here. Any region with an object-id gap >= 32767 (common once high,
                // sparsely-placed modern object ids are involved) desynced every read for the rest of
                // the region once the plain `readSmart()` misread the multi-chunk encoding as a
                // terminator/short value — explains the "~84 regions" ArrayIndexOutOfBounds/buffer-
                // overrun class of boot errors.
                int increment = landIn.readSmart2();
                if(increment == 0)
                    break;
                objectId += increment;
                int positionHash = 0;
                for(; ; ) {
                    int increment2 = landIn.readSmart();
                    if(increment2 == 0)
                        break;
                    positionHash += increment2 - 1;
                    int localX = (positionHash >> 6) & 0x3f;
                    int localY = positionHash & 0x3f;
                    int height = positionHash >> 12;
                    // Defensive clamp matching the real client's own decoder (RSPSi's
                    // `unpackObjects`: `if (height >= 4) height = 3;`) — a legitimate belt-and-braces
                    // guard in the real client, not a workaround invented here.
                    if(height >= 4)
                        height = 3;
                    int objectHash = landIn.readByteUnsafe() & 0xFF;
                    int type = objectHash >> 2;
                    int direction = objectHash & 0x3;
                    if(localX < 0 || localX >= 64 || localY < 0 || localY >= 64)
                        continue;
                    if (id == 14936 && (objectId == 26670 || objectId == 26669 || objectId == 26672 || objectId == 26671)) {
                        System.out.println("[MLM_RAW] objectId=" + objectId + " localX=" + localX + " localY=" + localY
                                + " absX=" + (baseX + localX) + " absY=" + (baseY + localY)
                                + " rawHeight=" + height + " type=" + type + " direction=" + direction
                                + " tileData0=" + tileData[0][localX][localY] + " tileData1=" + tileData[1][localX][localY]
                                + " tileData2=" + tileData[2][localX][localY] + " tileData3=" + tileData[3][localX][localY]
                                + " bridgeFlag=" + ((tileData[1][localX][localY] & 0x2) == 2));
                    }
                    if((tileData[1][localX][localY] & 0x2) == 2)
                        height--;
                    if (id == 14936 && (objectId == 26670 || objectId == 26669 || objectId == 26672 || objectId == 26671)) {
                        System.out.println("[MLM_RAW] -> finalHeight=" + height);
                    }
                    if(height >= 0) {
                        int absX = baseX + localX;
                        int absY = baseY + localY;
                        GameObject obj = new GameObject(objectId, absX, absY, height, type, direction);
                        getTile(absX, absY, height, true).addObject(obj);
                        BankActions.markTiles(obj);
                    }
                }
            }
        }
        empty = !invalidKeys && mapData == null && landscapeData == null;
    }

    public Tile getTile(int x, int y, int z, boolean create) {
        int localX = x - baseX;
        int localY = y - baseY;
        if(tiles == null) {
            if(!create)
                return null;
            tiles = new Tile[4][64][64];
        }
        Tile tile = tiles[z][localX][localY];
        if(tile == null && create)
            tile = tiles[z][localX][localY] = new Tile(this);
        return tile;
    }

    private byte[] getMapData() {
        IndexFile index = Server.fileStore.get(5);
        int mapArchiveId = index.getArchiveId("m" + ((baseX >> 3) / 8) + "_" + ((baseY >> 3) / 8));
        return mapArchiveId == -1 ? null : index.getFile(mapArchiveId, 0);
    }

    public byte[] getLandscapeData() {
        if(keys == null)
            return null;
        IndexFile index = Server.fileStore.get(5);
        int landArchiveId = index.getArchiveId("l" + ((baseX >> 3) / 8) + "_" + ((baseY >> 3) / 8));
        return landArchiveId == -1 ? null : index.getFile(landArchiveId, 0, keys[0] == 0 && keys[1] == 0 && keys[2] == 0 && keys[3] == 0 ? null : keys);
    }

    public static Region get(int regionId) {
        return LOADED[regionId];
    }

    public static Region get(int absX, int absY) {
        return get(getId(absX, absY));
    }

    public static int getId(int absX, int absY) {
        return ((absX >> 6) << 8) | absY >> 6;
    }

    public static int getClipping(int x, int y, int z) {
        Region region = Region.get(x, y);
        if(region.empty)
            return RouteFinder.UNMOVABLE_MASK;
        Tile tile = region.getTile(x, y, z, false);
        return tile == null ? 0 : tile.clipping;
    }

    /**
     * Updating
     */

    public static void update(Player player) {
        player.getPacketSender().clearChunks();
        for(Region region : player.getRegions()) {
            for(Tile tile : region.activeTiles)
                tile.update(player);
        }
    }

    /**
     * Destroy
     */

    public void destroy() {
        players.clear();
        if(!activeTiles.isEmpty()) {
            for(Tile tile : activeTiles)
                tile.destroy();
            activeTiles.clear();
        }
        tiles = null;
        dynamicIndex = -1;
        dynamicData = null;
        empty = true;
        house = null;
    }


    public House getHouse() {
        return house;
    }

    public void setHouse(House house) {
        this.house = house;
    }
}