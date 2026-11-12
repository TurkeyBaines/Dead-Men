import com.displee.cache.CacheLibrary;
import com.displee.cache.index.Index;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Arrays;

/**
 * Packs the {@code dm_set_sprite} client script into a rev-228 cache (archive 12, group = script id).
 *
 * <pre>
 * dm_set_sprite(component, sprite)       // int args, in this order
 *     push_int_local 1                   // sprite
 *     push_int_local 0                   // component hash (interface &lt;&lt; 16 | child) - must be on top
 *     if_setgraphic                      // opcode 2105
 *     return
 * </pre>
 *
 * The component is an argument, so one script serves every graphic child (629:56, 629:57, 629:58, ...).
 * The bytecode layout was checked against stock script 44, which assembles to the identical 34 bytes.
 *
 * Usage (jars from the gradle cache: rs-cache-library 7.3.0, disio 2.2, kotlin-stdlib 1.9.22, lzma-java 1.3, ant 1.10.14):
 * <pre>
 *   javac -cp "$CP" PackSpriteScript.java
 *   java  -cp ".;$CP" PackSpriteScript ../../Cache228/cache [scriptId=7849] [--force]
 * </pre>
 * The server must be stopped while this runs, then restarted so it re-reads the reference table.
 * Never commit the resulting Cache228/ binaries.
 */
public class PackSpriteScript {

    static final int INDEX_CLIENTSCRIPTS = 12;
    static final int OP_PUSH_INT_LOCAL = 33;
    static final int OP_IF_SETGRAPHIC = 2105;
    static final int OP_RETURN = 21;

    static byte[] assemble() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeByte(0);                       // script name (none)
        out.writeShort(OP_PUSH_INT_LOCAL);      // opcodes < 100 (except 21/38/39) carry a 4-byte operand
        out.writeInt(1);
        out.writeShort(OP_PUSH_INT_LOCAL);
        out.writeInt(0);
        out.writeShort(OP_IF_SETGRAPHIC);       // everything else carries a 1-byte operand
        out.writeByte(0);
        out.writeShort(OP_RETURN);
        out.writeByte(0);
        out.writeInt(4);                        // opcode count
        out.writeShort(2);                      // local ints
        out.writeShort(0);                      // local strings
        out.writeShort(2);                      // int args
        out.writeShort(0);                      // string args
        out.writeByte(0);                       // switch tables
        out.writeShort(1);                      // length of the switch section
        return bytes.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        String path = args[0];
        int id = 7849;
        boolean force = false;
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("--force")) {
                force = true;
            } else {
                id = Integer.parseInt(args[i]);
            }
        }
        byte[] script = assemble();

        CacheLibrary lib = CacheLibrary.create(path);
        Index scripts = lib.index(INDEX_CLIENTSCRIPTS);

        byte[] stock = scripts.archive(44) == null ? null : scripts.archive(44).file(0).getData();
        if (stock != null && Arrays.equals(stock, script)) {
            System.out.println("self-check: assembled bytes are identical to stock script 44 (if_setgraphic proc)");
        } else {
            System.out.println("WARNING: assembled bytes differ from stock script 44 - bytecode layout not confirmed");
            if (!force) {
                System.exit(2);
            }
        }

        var existing = scripts.archive(id);
        if (existing != null) {
            if (Arrays.equals(existing.file(0).getData(), script)) {
                System.out.println("script " + id + " is already packed and identical - nothing to do");
                lib.close();
                return;
            }
            if (!force) {
                System.out.println("script " + id + " already exists with different content - pick another id or pass --force");
                lib.close();
                System.exit(3);
            }
        }

        lib.put(INDEX_CLIENTSCRIPTS, id, script, null);
        boolean ok = scripts.update();
        lib.close();
        System.out.println("packed script " + id + " into index " + INDEX_CLIENTSCRIPTS + " (update=" + ok + ")");

        // re-open and verify
        CacheLibrary check = CacheLibrary.create(path);
        byte[] back = check.index(INDEX_CLIENTSCRIPTS).archive(id).file(0).getData();
        System.out.println("verify: read back " + back.length + " bytes, identical=" + Arrays.equals(back, script)
                + ", index12 revision=" + check.index(INDEX_CLIENTSCRIPTS).getRevision());
        check.close();
    }
}
