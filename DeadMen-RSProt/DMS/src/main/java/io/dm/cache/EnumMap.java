package io.dm.cache;

import io.dm.Server;
import io.dm.api.buffer.InBuffer;
import io.dm.api.filestore.IndexFile;

import java.util.HashMap;
import java.util.Map;

public class EnumMap {

    public static EnumMap get(int id) {
        IndexFile index = Server.fileStore.get(2);
        byte[] data = index.getFile(8, id);
        EnumMap map = new EnumMap();
        if(data != null)
            map.decode(new InBuffer(data));
        return map;
    }

    // Default to empty rather than null: an id with no data in the loaded cache (or the wrong
    // opcode branch for its actual type - see strings()/ints() below) leaves these untouched, and
    // some callers (e.g. IKOD.java) iterate `map.keys` directly with a for-each instead of going
    // through length-bounded accessors.
    public int[] keys = new int[0];
    public char keyType;
    public String defaultString = "null";
    public int[] intValues = new int[0];
    public int length = 0;
    public int defaultInt;
    public char valType;
    public String[] stringValues = new String[0];

    void decode(InBuffer in) {
        for(; ; ) {
            int var3 = in.readUnsignedByte();
            if(var3 == 0)
                break;
            method4342(in, var3);
        }
    }

    void method4342(InBuffer in, int var2) {
        if(var2 == 1)
            keyType = (char) in.readUnsignedByte();
        else if(var2 == 2)
            valType = (char) in.readUnsignedByte();
        else if(var2 == 3)
            defaultString = in.readString();
        else if(var2 == 4)
            defaultInt = in.readInt();
        else if(var2 == 5) {
            length = in.readUnsignedShort();
            keys = new int[length];
            stringValues = new String[length];
            for(int i = 0; i < length; i++) {
                keys[i] = in.readInt();
                stringValues[i] = in.readString();
            }
        } else if(var2 == 6) {
            length = in.readUnsignedShort();
            keys = new int[length];
            intValues = new int[length];
            for(int i = 0; i < length; i++) {
                keys[i] = in.readInt();
                intValues[i] = in.readInt();
            }
        }
    }

    // stringValues/intValues are only populated (grown to `length`) by whichever of opcode 5/6
    // this enum's data actually used (decode() never touches both, so the other stays at its
    // empty default) - a caller asking for the wrong shape (e.g. an id assumed to be
    // string-valued that's actually int-valued in this cache) previously ran off the end of the
    // still-empty array instead of getting an empty map back, same "which shape is this really"
    // gap an absent enum (data == null, length left at 0) already handles gracefully.

    public Map<Integer, String> strings() {
        Map<Integer, String> map = new HashMap<>(length);
        if(stringValues.length < length)
            return map;
        for(int i = 0; i < length; i++)
            map.put(keys[i], stringValues[i]);
        return map;
    }

    public Map<Integer, Integer> ints() {
        Map<Integer, Integer> map = new HashMap<>(length);
        if(intValues.length < length)
            return map;
        for(int i = 0; i < length; i++)
            map.put(keys[i], intValues[i]);
        return map;
    }

    public Map<Integer, Integer> valuesAsKeysInts() {
        Map<Integer, Integer> map = new HashMap<>(length);
        if(intValues.length < length)
            return map;
        for(int i = 0; i < length; i++)
            map.put(intValues[i], keys[i]);
        return map;
    }

}