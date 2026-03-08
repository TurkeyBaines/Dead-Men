package io.dm.cache;

import com.google.common.collect.Maps;
import io.dm.Server;
import io.dm.api.buffer.InBuffer;
import io.dm.api.filestore.IndexFile;
import io.dm.api.utils.StringUtils;
import io.dm.data.impl.npcs.npc_combat;
import io.dm.model.World;
import io.dm.model.activities.cluescrolls.impl.AnagramClue;
import io.dm.model.activities.cluescrolls.impl.CrypticClue;
import io.dm.model.entity.npc.NPCAction;
import io.dm.model.entity.npc.NPCCombat;
import io.dm.model.entity.player.KillCounter;
import io.dm.model.entity.player.Player;
import io.dm.model.item.actions.ItemNPCAction;
import io.dm.model.item.loot.LootTable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class NPCDef {

    public static Map<Integer, NPCDef> cached = Maps.newConcurrentMap();

    public static void load() {
        IndexFile index = Server.fileStore.get(2);
        int size = index.getLastFileId(9) + 1;
        for(int id = 0; id < size; id++) {
            byte[] data = index.getFile(9, id);
            if (data != null) {
                NPCDef def = new NPCDef();
                def.id = id;
                def.decode(new InBuffer(data));
                cached.put(id, def);
            }
        }
    }

    public static void forEach(Consumer<NPCDef> consumer) {
        for(NPCDef def : cached.values()) {
            if(def != null)
                consumer.accept(def);
        }
    }

    public static NPCDef get(int id) {
        return cached.get(id);
    }


    /**
     * Custom data
     */

    public String descriptiveName;

    public Class<? extends NPCCombat> combatHandlerClass;

    public LootTable lootTable;

    public NPCAction[] defaultActions;

    public HashMap<Integer, ItemNPCAction> itemActions;

    public ItemNPCAction defaultItemAction;

    public AnagramClue anagram;

    public CrypticClue cryptic;

    public npc_combat.Info combatInfo;

    public int attackOption = -1;

    public boolean flightClipping, swimClipping;

    public boolean occupyTiles = true;

    public boolean ignoreOccupiedTiles;

    public double giantCasketChance; // only used for bosses atm, other npcs use a formula (see GoldCasket)

    public boolean dragon;

    public boolean demon;

    public boolean ignoreMultiCheck = false;

    public Function<Player, KillCounter> killCounter;

    /**
     * Cache data
     */

    public int id;
    public boolean isMinimapVisible = true;
    public int anInt3558;
    public int standAnimation = -1;
    public int size = 1;
    public int walkBackAnimation = -1;
    public String name = "null";
    public int anInt3564 = -1;
    public int anInt3565 = -1;
    public int walkAnimation = -1;
    public int walkLeftAnimation = -1;
    public int walkRightAnimation = -1;
    public boolean aBool3573 = false;
    public String[] options = new String[5];
    public int combatLevel = -1;
    public boolean hasRenderPriority = false;
    public int headIcon = -1;
    public int rotation = 32;
    public int[] showIds;
    public boolean isClickable = true;
    public boolean aBool3588 = true;
    short[] retextureToReplace;
    short[] retextureToFind;
    public int varpId = -1;
    public int[] models;
    short[] recolorToFind;
    int[] models_2;
    public int varpbitId = -1;
    short[] recolorToReplace;
    int resizeX = 128;
    int resizeY = 128;
    int ambient = 0;
    int contrast = 0;
    public HashMap<Object, Object> attributes;

    void decode(InBuffer buffer) {
        for(;;) {
            int opcode = buffer.readUnsignedByte();
            if(opcode == 0)
                break;
            decode(buffer, opcode);
        }
        /**
         * Keep updated with client lol
         */
        if(id == 5442) {
            name = "Security Advisor";
            options[2] = "Check Pin Settings";
            options[3] = "Check 2FA Settings";
        } else if(id == 2882) {
            /* Horvik */
            options[0] = "Repair-items";
        } else if(id == 3894) {
            /* Sigmund the Merchant */
            options[0] = "Buy-items";
            options[2] = "Sell-items";
            options[3] = "Sets";
            options[4] = null;
        } else if(id == 3278) {
            name = "Construction Worker";
        } else if(id == 2668) {
            name = "Max hit dummy";
            options[2] = options[3] = options[4] = null;
        } else if (id == 6118) {
            name = "Elvarg";
            combatLevel = 280;
        } else if (id == 3358) {
            name = "Ket'ian";
            combatLevel = 420;
            resizeX *= 2;
            resizeY *= 2;
            size = 2;
        } else if(id == 5906) {
            options[2] = null;
        } else if("Pick-up".equals(options[0]) && "Talk-to".equals(options[2]) && "Chase".equals(options[3]) && "Interact".equals(options[4])) {
            options[3] = "Age";
            options[4] = null;
        } else if(id == 7759) {
            options[0] = options[2] = null;
        } else if(id == 7941) {
            options[2] = options[3] = options[4] = null;
        } else if(id == 8009) {
            options[3] = "Metamorphosis";
        } else if(id == 8507) {
            options[0] = "Trade";
        } else if(id == 7297) { // Skotizo (For eco world)
            // Replaces Mistag
            copy(7286);
        } else if(id == 6002) {
            name = "Caretaker";
            options[0] = "Trade";
        } else if(id == 119) {
            name = "Doomsayer";
        } else if(id == 8500) {
            name = "Old man";
            options[1] = "Trade";
        }
        /* START OF DM CUSTOM NPC DEFS */
        else if (id == 5567) { // Death
            options[1] = "Escape";
        } else if (id == 1798 || id == 8149 || id == 1799 || id == 1800 || id == 1829) { // White Knights > Citadel Guards
            name = "Citadel Guard";
            combatLevel = 700;
            options[1] = "Talk-to";
            options[2] = "Attack";
        } else if (id == 2713) {
            System.out.println("WE EDITED THE WISE OLD MAN SHIT!");
            options[0] = "Talk-to";
            options[1] = "Check-task";
            options[2] = "Quick-task";
            options[3] = "Cancel-task";
            System.out.println("[0]=" + options[0]);
            System.out.println("[1]=" + options[1]);
            System.out.println("[2]=" + options[2]);
            System.out.println("[3]=" + options[3]);
        }

        if(name != null) {
            if(name.isEmpty())
                descriptiveName = name;
            else if(name.equals("Kalphite Queen") || name.equals("Corporeal Beast") || name.equals("King Black Dragon")
                    || name.equals("Kraken") || name.equals("Thermonuclear Smoke Devil") || name.equalsIgnoreCase("Crazy archaeologist") || name.equalsIgnoreCase("Chaos Fanatic") || name.equalsIgnoreCase("Chaos Elemental"))
                descriptiveName = "the " + name;
            else if (name.toLowerCase().matches("dagannoth (rex|prime|supreme)")
                    || name.equals("Cerberus") || name.equals("Zulrah") || name.equals("Callisto") || name.equals("Venenatis") || name.equals("Vet'ion") || name.equals("Scorpia"))
                descriptiveName = name;
            else if(StringUtils.vowelStart(name))
                descriptiveName = "an " + name;
            else
                descriptiveName = "a " + name;
        }

        attackOption = getOption("attack", "fight");
        flightClipping = name.toLowerCase().contains("impling") || name.toLowerCase().contains("butterfly");
        dragon = name.toLowerCase().contains("dragon") || name.equalsIgnoreCase("elvarg") || name.equalsIgnoreCase("wyrm") || name.equalsIgnoreCase("drake") || name.toLowerCase().contains("hydra") || name.toLowerCase().contains("great olm");
        demon = name.toLowerCase().contains("demon") || name.equalsIgnoreCase("skotizo") || name.equalsIgnoreCase("imp") || name.toLowerCase().contains("nechryael") || name.toLowerCase().contains("abyssal sire") || name.toLowerCase().contains("k'ril") || name.toLowerCase().contains("balfrug") || name.toLowerCase().contains("tstanon") || name.toLowerCase().contains("zakl'n");
    }

    void decode(InBuffer var1, int var2) {
        if(var2 == 1) {
            int var4 = var1.readUnsignedByte();
            models = new int[var4];
            for(int var5 = 0; var5 < var4; var5++)
                models[var5] = var1.readUnsignedShort();
        } else if(var2 == 2)
            name = var1.readString();
        else if(var2 == 12)
            size = var1.readUnsignedByte();
        else if(var2 == 13)
            standAnimation = var1.readUnsignedShort();
        else if(var2 == 14)
            walkAnimation = var1.readUnsignedShort();
        else if(var2 == 15)
            anInt3564 = var1.readUnsignedShort();
        else if(var2 == 16)
            anInt3565 = var1.readUnsignedShort();
        else if(var2 == 17) {
            walkAnimation = var1.readUnsignedShort();
            walkBackAnimation = var1.readUnsignedShort();
            walkLeftAnimation = var1.readUnsignedShort();
            walkRightAnimation = var1.readUnsignedShort();
        } else if(var2 >= 30 && var2 < 35) {
            options[var2 - 30] = var1.readString();
            if(options[var2 - 30].equalsIgnoreCase("Hidden"))
                options[var2 - 30] = null;
        } else if(var2 == 40) {
            int var4 = var1.readUnsignedByte();
            recolorToFind = new short[var4];
            recolorToReplace = new short[var4];
            for(int var5 = 0; var5 < var4; var5++) {
                recolorToFind[var5] = (short) var1.readUnsignedShort();
                recolorToReplace[var5] = (short) var1.readUnsignedShort();
            }
        } else if(var2 == 41) {
            int var4 = var1.readUnsignedByte();
            retextureToFind = new short[var4];
            retextureToReplace = new short[var4];
            for(int var5 = 0; var5 < var4; var5++) {
                retextureToFind[var5] = (short) var1.readUnsignedShort();
                retextureToReplace[var5] = (short) var1.readUnsignedShort();
            }
        } else if(var2 == 60) {
            int var4 = var1.readUnsignedByte();
            models_2 = new int[var4];
            for(int var5 = 0; var5 < var4; var5++)
                models_2[var5] = var1.readUnsignedShort();
        } else if(var2 == 93)
            isMinimapVisible = false;
        else if(var2 == 95)
            combatLevel = var1.readUnsignedShort();
        else if(var2 == 97)
            resizeX = var1.readUnsignedShort();
        else if(var2 == 98)
            resizeY = var1.readUnsignedShort();
        else if(var2 == 99)
            hasRenderPriority = true;
        else if(var2 == 100)
            ambient = var1.readByte();
        else if(var2 == 101)
            contrast = var1.readByte() * 5;
        else if(var2 == 102)
            headIcon = var1.readUnsignedShort();
        else if(var2 == 103)
            rotation = var1.readUnsignedShort();
        else if(var2 == 106 || var2 == 118) {
            varpbitId = var1.readUnsignedShort();
            if(varpbitId == 65535)
                varpbitId = -1;
            varpId = var1.readUnsignedShort();
            if(varpId == 65535)
                varpId = -1;
            int var4 = -1;
            if(var2 == 118) {
                var4 = var1.readUnsignedShort();
                if(var4 == 65535)
                    var4 = -1;
            }
            int var5 = var1.readUnsignedByte();
            showIds = new int[var5 + 2];
            for(int var6 = 0; var6 <= var5; var6++) {
                showIds[var6] = var1.readUnsignedShort();
                if(showIds[var6] == 65535)
                    showIds[var6] = -1;
            }
            showIds[var5 + 1] = var4;
        } else if (var2 == 107)
            isClickable = false;
        else if (var2 == 109)
            aBool3588 = false;
        else if (var2 == 111)
            aBool3573 = true;
        else if (var2 == 249) {
            int size = var1.readUnsignedByte();
            if (attributes == null)
                attributes = new HashMap<>();
            for (int i = 0; i < size; i++) {
                boolean stringType = var1.readUnsignedByte() == 1;
                int key = var1.readMedium();
                if (stringType)
                    attributes.put(key, var1.readString());
                else
                    attributes.put(key, var1.readInt());
            }
        }

    }

    void copy(int otherId) {
        NPCDef otherDef = cached.get(otherId);
        try {
            isMinimapVisible = otherDef.isMinimapVisible;
            anInt3558 = otherDef.anInt3558;
            standAnimation = otherDef.standAnimation;
            size = otherDef.size;
            walkBackAnimation = otherDef.walkBackAnimation;
            name = otherDef.name;
            anInt3564 = otherDef.anInt3564;
            anInt3565 = otherDef.anInt3565;
            walkAnimation = otherDef.walkAnimation;
            walkLeftAnimation = otherDef.walkLeftAnimation;
            walkRightAnimation = otherDef.walkRightAnimation;
            aBool3573 = otherDef.aBool3573;
            options = otherDef.options == null ? null : otherDef.options.clone();
            combatLevel = otherDef.combatLevel;
            hasRenderPriority = otherDef.hasRenderPriority;
            headIcon = otherDef.headIcon;
            rotation = otherDef.rotation;
            showIds = otherDef.showIds == null ? null : otherDef.showIds.clone();
            isClickable = otherDef.isClickable;
            aBool3588 = otherDef.aBool3588;
            retextureToReplace = otherDef.retextureToReplace == null ? null : otherDef.retextureToReplace.clone();
            retextureToFind = otherDef.retextureToFind == null ? null : otherDef.retextureToFind.clone();
            varpId = otherDef.varpId;
            models = otherDef.models;
            recolorToFind = otherDef.recolorToFind == null ? null : otherDef.recolorToFind.clone();
            models_2 = otherDef.models_2 == null ? null : otherDef.models_2.clone();
            varpbitId = otherDef.varpbitId;
            recolorToReplace = otherDef.recolorToReplace == null ? null : otherDef.recolorToReplace.clone();
            resizeX = otherDef.resizeX;
            resizeY = otherDef.resizeY;
            ambient = otherDef.ambient;
            contrast = otherDef.contrast;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean hasOption(String... searchOptions) {
        return getOption(searchOptions) != -1;
    }

    public int getOption(String... searchOptions) {
        if(options != null) {
            for(String s : searchOptions) {
                for(int i = 0; i < options.length; i++) {
                    String option = options[i];
                    if(s.equalsIgnoreCase(option))
                        return i + 1;
                }
            }
        }
        return -1;
    }

}
