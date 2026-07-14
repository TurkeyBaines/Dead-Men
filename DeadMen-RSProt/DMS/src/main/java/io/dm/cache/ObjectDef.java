package io.dm.cache;

import com.google.common.collect.Maps;
import io.dm.Server;
import io.dm.api.buffer.InBuffer;
import io.dm.api.filestore.IndexFile;
import io.dm.api.utils.StringUtils;
import io.dm.model.item.actions.ItemObjectAction;
import io.dm.model.map.object.actions.ObjectAction;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import static io.dm.cache.ObjectID.PORTAL_OF_CHAMPIONS;

public class ObjectDef {

    public static boolean aBool1550 = false;
    private static byte[] EMPTY_BUFFER = new byte[] {0};

    public static Map<Integer, ObjectDef> LOADED = Maps.newConcurrentMap();
    public static ObjectDef[] LOADED_EXTRA = new ObjectDef[10];

    @SuppressWarnings("Duplicates")
    public static void load() {
        IndexFile index = Server.fileStore.get(2);
        int size = index.getLastFileId(6) + 1;
        Map<Integer, int[]> unknownOpcodeCounts = new TreeMap<>();
        Map<Integer, Integer> unknownOpcodeExample = new TreeMap<>();
        for (int id = 0; id < size; id++) {
            byte[] data = index.getFile(6, id);
            if (data != null) {
                ObjectDef def = new ObjectDef();
                def.id = id;
                def.decode(new InBuffer(data));
                if (def.someFlag) {
                    def.clipType = 0;
                    def.tall = false;
                }
                if (StringUtils.vowelStart(def.name))
                    def.descriptiveName = "an " + def.name;
                else
                    def.descriptiveName = "a " + def.name;
                LOADED.put(id, def);

                // Diagnostic (Stage 07 Round 2, 2026-07-13): reuse the exact bytes just decoded to
                // check whether decode(InBuffer,int) hit an opcode it doesn't recognize for THIS
                // object. Same bug class already confirmed real and fixed for ItemDef (see
                // stages/STAGE-05-inventory-actions.txt's "Opcode desync" section) - an unrecognized
                // opcode here silently consumes zero payload bytes and desyncs every field read
                // afterward for that object (xLength/yLength/clipType/someDirection/options/etc,
                // several of which RouteFinder.routeObject() reads directly). Costs one extra
                // InBuffer pass over bytes already in memory, no extra cache I/O.
                Integer unknown = findFirstUnknownOpcode(data);
                if (unknown != null && unknown != -1) {
                    unknownOpcodeCounts.computeIfAbsent(unknown, k -> new int[1])[0]++;
                    unknownOpcodeExample.putIfAbsent(unknown, id);
                }
            }
        }
        dumpOpcodeAudit(unknownOpcodeCounts, unknownOpcodeExample);
    }

    /**
     * Diagnostic: walks {@code data}'s opcode stream the same way {@link #decode(InBuffer)} does,
     * but stops at the FIRST opcode not in this decoder's known set instead of silently reading
     * nothing for it (which is what {@link #decode(InBuffer, int)} does today - no {@code else}
     * branch - desyncing every field read afterward for that object). Mirrors {@code
     * ItemDef.findFirstUnknownOpcode}; see that method's doc for the full rationale.
     *
     * @return the first unrecognized opcode, or null if the stream was consumed cleanly through to
     * the terminating 0 byte (or -1 if the buffer ran out while consuming a KNOWN opcode's payload,
     * which would indicate a length bug in {@link #decode(InBuffer, int)} itself).
     */
    public static Integer findFirstUnknownOpcode(byte[] data) {
        InBuffer buffer = new InBuffer(data);
        try {
            for (;;) {
                int opcode = buffer.readUnsignedByte();
                if (opcode == 0)
                    return null;
                if (!isKnownOpcode(opcode))
                    return opcode;
                SKIP.decode(buffer, opcode);
            }
        } catch (Throwable t) {
            return -1;
        }
    }

    private static final ObjectDef SKIP = new ObjectDef();

    private static boolean isKnownOpcode(int opcode) {
        if (opcode == 1 || opcode == 2 || opcode == 5 || opcode == 14 || opcode == 15 || opcode == 17
                || opcode == 18 || opcode == 19 || opcode == 21 || opcode == 22 || opcode == 23
                || opcode == 24 || opcode == 27 || opcode == 28 || opcode == 29 || opcode == 39
                || opcode == 40 || opcode == 41 || opcode == 60 || opcode == 61 || opcode == 62
                || opcode == 64 || opcode == 65 || opcode == 66 || opcode == 67 || opcode == 68
                || opcode == 69 || opcode == 70 || opcode == 71 || opcode == 72 || opcode == 73
                || opcode == 74 || opcode == 75 || opcode == 77 || opcode == 78 || opcode == 79
                || opcode == 81 || opcode == 82 || opcode == 89 || opcode == 90 || opcode == 92
                || opcode == 249)
            return true;
        return opcode >= 30 && opcode < 35;
    }

    private static void dumpOpcodeAudit(Map<Integer, int[]> unknownOpcodeCounts, Map<Integer, Integer> unknownOpcodeExample) {
        try (PrintWriter out = new PrintWriter(new FileWriter("OBJECT_DEF_OPCODE_DUMP.txt", false))) {
            out.println("ObjectDef opcode audit (against real Server.fileStore / Cache228 as of the 2026-07-12 "
                    + "cache switch) - generated " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            out.println("Stage 07 Round 2 diagnostic - see stages/STAGE-07-object-interactions.txt.");
            out.println();
            if (unknownOpcodeCounts.isEmpty()) {
                out.println("(none - every loaded object's bytes parsed using only known opcodes; "
                        + "ObjectDef.decode is exhaustive against this cache snapshot)");
            } else {
                out.println("==== Unknown opcodes hit while parsing object bytes (decode(InBuffer,int) has no case for these) ====");
                for (Map.Entry<Integer, int[]> e : unknownOpcodeCounts.entrySet()) {
                    out.println("opcode " + e.getKey() + ": hit first on " + e.getValue()[0]
                            + " object(s), e.g. id=" + unknownOpcodeExample.get(e.getKey()));
                }
            }
            System.out.println("[ObjectDef] opcode audit: " + unknownOpcodeCounts.size()
                    + " distinct unknown opcodes across loaded objects - see OBJECT_DEF_OPCODE_DUMP.txt");
        } catch (IOException e) {
            System.out.println("[ObjectDef] failed to write OBJECT_DEF_OPCODE_DUMP.txt: " + e);
        }
    }

    public static void forEach(Consumer<ObjectDef> consumer) {
        for (ObjectDef def : LOADED.values()) {
            if (def != null)
                consumer.accept(def);
        }
    }

    public static ObjectDef get(int id) {
        return LOADED.get(id);
    }


    /**
     * Custom data
     */

    public String descriptiveName;

    public ObjectAction[] defaultActions;

    public HashMap<Integer, ItemObjectAction> itemActions;

    public ItemObjectAction defaultItemAction;

    public boolean bank;

    /**
     * Door data
     */

    public boolean gateType;

    public boolean longGate;

    public int doorOppositeId = -1;

    public boolean doorReversed, doorClosed;

    public int doorOpenSound = -1, doorCloseSound = -1;

    public boolean reversedConstructionDoor;

    /**
     * Separator
     */

    public int id;
    public int type22Int = -1;
    public int unknownOpcode75 = -1;
    public String name = "null";
    public int xLength = 1;
    public int yLength = 1;
    public int clipType = 2;
    public boolean tall = true;
    public boolean aBool1552 = false;
    public int unknownOpcode24 = -1; // idle animation
    public int anInt1595 = 16;
    public int mapMarkerId = -1;
    public boolean aBool1580 = true;
    public int anInt1578 = -1;
    public boolean type22Boolean = false;
    public int unknownOpcode78 = -1;
    public int unknownOpcode_78_79 = 0;
    public int anInt1548 = 0;
    public int anInt1571 = 0;
    public int[] anIntArray1597;
    public int[] showIds;
    public String[] options = new String[5];
    public int[] modelTypes;
    public int[] modelIds;
    int anInt1569 = -1;
    boolean aBool1582 = false;
    int unknownOpcode29 = 0;
    int anInt1575 = 0;
    public short[] originalModelColors;
    public short[] modifiedModelColors;
    short[] aShortArray1596;
    short[] aShortArray1563;
    public boolean verticalFlip = false;
    public int render0x1 = 128;
    public int render0x2 = 128;
    public int render0x4 = 128;
    int anInt1584 = 0;
    int anInt1585 = 0;
    int anInt1586 = 0;
    public boolean someFlag = false;
    public int varpBitId = -1;
    public int varpId = -1;

    public int someDirection;

    // Opcodes 61/89/249 confirmed real via AlterRSPS's ObjectDecoder.kt (Stage 07 Round 2,
    // 2026-07-13) - see decode(InBuffer,int)'s comments at those opcodes for the evidence trail.
    public int category = -1;
    public boolean randomizeAnimStart = false;
    public boolean delayAnimationUpdate = false;
    public HashMap<Integer, Object> attributes;


    /* SEARCH: OBJ DEF SERVER */
    private void decode(InBuffer in) {
        for (;;) {
            int opcode = in.readUnsignedByte();
            if (opcode == 0)
                break;
            decode(in, opcode);
        }

        switch (id) {
            case 11833: // Fight Caves Entrance
                options[1] = "Practice";
                break;

            case 31380: // Rejuvenation pool
                name = "Rejuvenation pool";
                options[0] = "Drink";
                clipType = 1;
                modelIds = new int[]{32101};
                break;

            case 1816: // KBD Lever
                options[0] = "Instance";
                options[2] = "Commune";
                break;

            case 30169: // Fuck knows tbh
            case 535: // Thermonuclear smoke devil crevice
            case 29705: // KQ Crack
            case 26502: // GWD Boss Doors
            case 26503:
            case 26504:
            case 26505:
                options[1] = "Instance";
                options[2] = "Peek";
                break;

            case 30971: // Overworld Fishing Tool Chest
                name = "Fisherman's Chest";
                options[0] = "Take-tool";
                options[1] = "null";
                break;

            case 51003: // Overworld Hopper
                name = "Resource hopper";
                options[0] = "Deposit";
                options[1] = "Note";
                options[2] = "Points";
                break;

            case 25834: // Overworld Herbs
                name = "Overworld Herbs";
                options[0] = "Pick";
                break;

            case 15127: // Overworld Pickaxe Rock
                options[0] = "Take-pickaxe";
                break;

            case 628: // Overworld Thieving Stalls
            case 633:
            case 629:
            case 631:
            case 630:
                options[0] = "Steal-from";
                break;

            case 32657: // Task Notice Board
                options[0] = "Read";
                options[1] = "Check-task";
                options[2] = "Quick-task";
                options[3] = "Cancel-task";
                break;

            case 32658: // Tournament Config Board
                options[0] = "Read";
                options[1] = "Override";
                options[2] = "Group Settings";
                break;

            case 33309:
                name = "Pile of buckets";
                options[0] = "Take-1";
                options[1] = "Take-5";
                options[2] = "Take-all";
                break;

            case 51001:
                options[0] = "Fill";
                break;

            case 33181:
                name = "Teleporter";
                options[0] = "Menu";
                break;

            case 10251:
                name = "Overworld teleporter";
                break;
        }
    }

    private void decode(InBuffer in, int i) {
        if (i == 1) {
            int i_7_ = in.readUnsignedByte();
            if (i_7_ > 0) {
                if (modelIds == null || aBool1550) {
                    modelTypes = new int[i_7_];
                    modelIds = new int[i_7_];
                    for (int i_8_ = 0; i_8_ < i_7_; i_8_++) {
                        modelIds[i_8_] = in.readUnsignedShort();
                        modelTypes[i_8_] = in.readUnsignedByte();
                    }
                } else in.skip(i_7_ * 3);
            }
        } else if (i == 2)
            name = in.readString();
        else if (i == 5) {
            int i_9_ = in.readUnsignedByte();
            if (i_9_ > 0) {
                if (modelIds == null || aBool1550) {
                    modelTypes = null;
                    modelIds = new int[i_9_];
                    for (int i_10_ = 0; i_10_ < i_9_; i_10_++)
                        modelIds[i_10_] = in.readUnsignedShort();
                } else in.skip(i_9_ * 2);
            }
        } else if (i == 14)
            xLength = in.readUnsignedByte();
        else if (i == 15)
            yLength = in.readUnsignedByte();
        else if (i == 17) {
            clipType = 0;
            tall = false;
        } else if (i == 18)
            tall = false;
        else if (i == 19)
            type22Int = in.readUnsignedByte();
        else if (i == 21)
            anInt1569 = 0;
        else if (i == 22)
            aBool1582 = true;
        else if (i == 23)
            aBool1552 = true;
        else if (i == 24) {
            unknownOpcode24 = in.readUnsignedShort();
            if (unknownOpcode24 == 65535)
                unknownOpcode24 = -1;
        } else if (i == 27)
            clipType = 1;
        else if (i == 28)
            anInt1595 = in.readUnsignedByte();
        else if (i == 29)
            unknownOpcode29 = in.readByte();
        else if (i == 39)
            anInt1575 = in.readByte() * 25;
        else if (i >= 30 && i < 35) {
            options[i - 30] = in.readString();
            if (options[i - 30].equalsIgnoreCase("Hidden"))
                options[i - 30] = null;
        } else if (i == 40) {
            int i_11_ = in.readUnsignedByte();
            originalModelColors = new short[i_11_];
            modifiedModelColors = new short[i_11_];
            for (int i_12_ = 0; i_12_ < i_11_; i_12_++) {
                originalModelColors[i_12_] = (short) in.readUnsignedShort();
                modifiedModelColors[i_12_] = (short) in.readUnsignedShort();
            }
        } else if (i == 41) {
            int i_13_ = in.readUnsignedByte();
            aShortArray1596 = new short[i_13_];
            aShortArray1563 = new short[i_13_];
            for (int i_14_ = 0; i_14_ < i_13_; i_14_++) {
                aShortArray1596[i_14_] = (short) in.readUnsignedShort();
                aShortArray1563[i_14_] = (short) in.readUnsignedShort();
            }
        } else if (i == 60 || i == 82) // 82 is Cache228's alias for the same field - AlterRSPS
            // ObjectDecoder.kt: "60,82 -> mapAreaId". Confirmed real (Stage 07 Round 2, 2026-07-13):
            // opcode 82 alone hit 357 objects as an unrecognized/desyncing opcode before this fix.
            mapMarkerId = in.readUnsignedShort();
        else if (i == 61)
            // Confirmed real via AlterRSPS ObjectDecoder.kt ("category = readUnsignedShort()").
            // By far the highest-impact fix this round: hit 6501 objects as the FIRST unrecognized
            // opcode (i.e. desyncing every field decoded afterward for each of them) before this fix.
            category = in.readUnsignedShort();
        else if (i == 89)
            // Confirmed real via AlterRSPS ObjectDecoder.kt ("randomizeAnimStart = true", 0 bytes).
            // 163 objects hit this as an unrecognized opcode before this fix.
            randomizeAnimStart = true;
        else if (i == 90)
            // Confirmed real via AlterRSPS ObjectDecoder.kt ("delayAnimationUpdate = true", 0 bytes).
            // 8 objects hit this as an unrecognized opcode before this fix.
            delayAnimationUpdate = true;
        else if (i == 62)
            verticalFlip = true;
        else if (i == 64)
            aBool1580 = false;
        else if (i == 65)
            render0x1 = in.readUnsignedShort();
        else if (i == 66)
            render0x2 = in.readUnsignedShort();
        else if (i == 67)
            render0x4 = in.readUnsignedShort();
        else if (i == 68)
            anInt1578 = in.readUnsignedShort();
        else if (i == 69)
            someDirection = in.readUnsignedByte();
        else if (i == 70)
            anInt1584 = in.readShort();
        else if (i == 71)
            anInt1585 = in.readShort();
        else if (i == 72)
            anInt1586 = in.readShort();
        else if (i == 73)
            type22Boolean = true;
        else if (i == 74)
            someFlag = true;
        else if (i == 75)
            unknownOpcode75 = in.readUnsignedByte();
        else if (i == 77 || i == 92) {
            varpBitId = in.readUnsignedShort();
            if (varpBitId == 65535)
                varpBitId = -1;
            varpId = in.readUnsignedShort();
            if (varpId == 65535)
                varpId = -1;
            int i_17_ = -1;
            if (i == 92) {
                i_17_ = in.readUnsignedShort();
                if (i_17_ == 65535)
                    i_17_ = -1;
            }
            int i_18_ = in.readUnsignedByte();
            showIds = new int[i_18_ + 2];
            for (int i_19_ = 0; i_19_ <= i_18_; i_19_++) {
                showIds[i_19_] = in.readUnsignedShort();
                if (showIds[i_19_] == 65535)
                    showIds[i_19_] = -1;
            }
            showIds[i_18_ + 1] = i_17_;
        } else if (i == 78) {
            unknownOpcode78 = in.readUnsignedShort();
            unknownOpcode_78_79 = in.readUnsignedByte();
            // Cache228 (rev>=220) appends a "soundRetain" byte here that this decoder never read -
            // AlterRSPS ObjectDecoder.kt: "if (revisionIsOrAfter(220)) soundRetain = readUnsignedByte()".
            // This deployment's cache is always rev228, so read it unconditionally. Discarded (no
            // current use), but consuming it is required to keep the byte stream in sync - Stage 07
            // Round 2, 2026-07-13.
            in.readUnsignedByte();
        } else if (i == 79) {
            anInt1548 = in.readUnsignedShort();
            anInt1571 = in.readUnsignedShort();
            unknownOpcode_78_79 = in.readUnsignedByte();
            in.readUnsignedByte(); // soundRetain, same rev>=220 field as opcode 78 above - discarded.
            int i_15_ = in.readUnsignedByte();
            anIntArray1597 = new int[i_15_];
            for (int i_16_ = 0; i_16_ < i_15_; i_16_++)
                anIntArray1597[i_16_] = in.readUnsignedShort();
        } else if (i == 81)
            anInt1569 = in.readUnsignedByte() * 256;
        else if (i == 249) {
            // Confirmed real via AlterRSPS ObjectDecoder.kt ("249 -> readParameters(buffer)"), same
            // wire format as ItemDef's opcode 249 (see ItemDef.decode(InBuffer,int)). 21 objects hit
            // this as an unrecognized opcode before this fix - Stage 07 Round 2, 2026-07-13.
            int size = in.readUnsignedByte();
            if (attributes == null)
                attributes = new HashMap<>();
            for (int k = 0; k < size; k++) {
                boolean stringType = in.readUnsignedByte() == 1;
                int key = in.readMedium();
                if (stringType)
                    attributes.put(key, in.readString());
                else
                    attributes.put(key, in.readInt());
            }
        }
    }

    public boolean isClippedDecoration() {
        return type22Int != 0 || clipType == 1 || type22Boolean;
    }

    public boolean hasOption(String... searchOptions) {
        return getOption(searchOptions) != -1;
    }

    public int getOption(String... searchOptions) {
        if (options != null) {
            for (String s : searchOptions) {
                for (int i = 0; i < options.length; i++) {
                    String option = options[i];
                    if (s.equalsIgnoreCase(option))
                        return i + 1;
                }
            }
        }
        return -1;
    }

    private void copy(int id) {
        if (this.id < id) {
            System.err.println("Unable to copy Object where target has lower id.");
            return;
        }

        ObjectDef from = LOADED.get(id);

        try {
            type22Int = from.type22Int;
            unknownOpcode75 = from.unknownOpcode75;
            name = from.name;
            xLength = from.xLength;
            yLength = from.yLength;
            clipType = from.clipType;
            tall = from.tall;
            aBool1552 = from.aBool1552;
            unknownOpcode24 = from.unknownOpcode24;
            anInt1595 = from.anInt1595;
            mapMarkerId = from.mapMarkerId;
            aBool1580 = from.aBool1580;
            anInt1578 = from.anInt1578;
            type22Boolean = from.type22Boolean;
            unknownOpcode78 = from.unknownOpcode78;
            unknownOpcode_78_79 = from.unknownOpcode_78_79;
            anInt1548 = from.anInt1548;
            anInt1571 = from.anInt1571;
            anIntArray1597 = from.anIntArray1597 == null ? null : Arrays.copyOf(from.anIntArray1597, from.anIntArray1597.length);
            showIds = from.showIds == null ? null : Arrays.copyOf(from.showIds, from.showIds.length);
            options = from.options == null ? null : Arrays.copyOf(from.options, from.options.length);
            modelTypes = from.modelTypes == null ? null : Arrays.copyOf(from.modelTypes, modelTypes.length);
            modelIds = from.modelIds == null ? null : Arrays.copyOf(from.modelIds, from.modelIds.length);
            anInt1569 = from.anInt1569;
            aBool1582 = from.aBool1582;
            unknownOpcode29 = from.unknownOpcode29;
            anInt1575 = from.anInt1575;
            originalModelColors = from.originalModelColors == null ? null : Arrays.copyOf(from.originalModelColors, from.originalModelColors.length);
            modifiedModelColors = from.modifiedModelColors == null ? null : Arrays.copyOf(from.modifiedModelColors, from.modifiedModelColors.length);
            aShortArray1596 = from.aShortArray1596 == null ? null : Arrays.copyOf(from.aShortArray1596, from.aShortArray1596.length);
            aShortArray1563 = from.aShortArray1563 == null ? null : Arrays.copyOf(from.aShortArray1563, from.aShortArray1563.length);
            verticalFlip = from.verticalFlip;
            render0x1 = from.render0x1;
            render0x2 = from.render0x2;
            render0x4 = from.render0x4;
            anInt1584 = from.anInt1584;
            anInt1585 = from.anInt1585;
            anInt1586 = from.anInt1586;
            someFlag = from.someFlag;
            varpBitId = from.varpBitId;
            varpId = from.varpId;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}
