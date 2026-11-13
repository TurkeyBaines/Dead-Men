import com.displee.cache.CacheLibrary;
import com.displee.cache.index.Index;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Packs a PNG into a rev-228 cache as a single-frame sprite (index 8, group = sprite id, file 0).
 *
 * Sprite format (same as the client's SpritePixels loader): per frame [flags][w*h palette indices]
 * ([w*h alpha bytes] if flags&2), then (paletteSize-1) RGB triplets (index 0 = transparent), then the
 * trailer: maxWidth u16, maxHeight u16, paletteSize-1 u8, offsetX[] u16, offsetY[] u16, width[] u16,
 * height[] u16, frameCount u16. Colours beyond 255 are median-cut quantised; alpha is kept per pixel.
 *
 * Usage (classpath as in PackSpriteScript.java):
 *   java -cp ".;$CP" PackSprite check  &lt;cachePath&gt;                      // validate the format against every stock sprite
 *   java -cp ".;$CP" PackSprite pack   &lt;cachePath&gt; &lt;png&gt; [spriteId]  // spriteId defaults to the next free group
 * The server must be stopped while this runs, then restarted. Never commit Cache228/ binaries.
 */
public class PackSprite {

    static final int INDEX_SPRITES = 8;

    // ---------------------------------------------------------------- encode

    static byte[] encode(BufferedImage im) throws Exception {
        int w = im.getWidth(), h = im.getHeight();
        int[] argb = new int[w * h];
        boolean needAlpha = false;
        Map<Integer, Integer> counts = new HashMap<>();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p = im.getRGB(x, y);
                int a = p >>> 24;
                if (a != 0 && a != 255) needAlpha = true;
                if (a == 0) p = 0;
                argb[y * w + x] = p;
                if (a != 0) counts.merge(p & 0xffffff, 1, Integer::sum);
            }
        }
        int[] palette = quantise(counts, 255);          // palette[i] is the colour of index i+1

        byte[] idx = new byte[w * h];
        byte[] alpha = new byte[w * h];
        Map<Integer, Integer> cache = new HashMap<>();
        for (int i = 0; i < argb.length; i++) {
            int a = argb[i] >>> 24;
            alpha[i] = (byte) a;
            if (a == 0) {
                idx[i] = 0;
                continue;
            }
            int rgb = argb[i] & 0xffffff;
            idx[i] = (byte) (1 + cache.computeIfAbsent(rgb, c -> nearest(palette, c)));
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeByte(needAlpha ? 2 : 0);               // flags: bit0 = vertical order, bit1 = alpha
        out.write(idx);
        if (needAlpha) out.write(alpha);
        for (int c : palette) {                         // the loader maps rgb 0 to 1 itself; do it here too
            int v = c == 0 ? 1 : c;
            out.writeByte(v >> 16);
            out.writeByte(v >> 8);
            out.writeByte(v);
        }
        out.writeShort(w);                              // trailer
        out.writeShort(h);
        out.writeByte(palette.length);                  // stored as paletteSize - 1
        out.writeShort(0);                              // offsetX
        out.writeShort(0);                              // offsetY
        out.writeShort(w);
        out.writeShort(h);
        out.writeShort(1);                              // frame count
        return bytes.toByteArray();
    }

    static int nearest(int[] palette, int rgb) {
        int best = 0, bestD = Integer.MAX_VALUE;
        for (int i = 0; i < palette.length; i++) {
            int d = dist(palette[i], rgb);
            if (d < bestD) {
                bestD = d;
                best = i;
            }
        }
        return best;
    }

    static int dist(int a, int b) {
        int dr = ((a >> 16) & 255) - ((b >> 16) & 255);
        int dg = ((a >> 8) & 255) - ((b >> 8) & 255);
        int db = (a & 255) - (b & 255);
        return dr * dr + dg * dg + db * db;
    }

    /** Median cut over the weighted distinct colours; returns at most {@code max} palette colours. */
    static int[] quantise(Map<Integer, Integer> counts, int max) {
        List<int[]> all = new ArrayList<>();            // {rgb, count}
        counts.forEach((c, n) -> all.add(new int[]{c, n}));
        if (all.size() <= max) {
            int[] p = new int[all.size()];
            for (int i = 0; i < p.length; i++) p[i] = all.get(i)[0];
            return p;
        }
        List<List<int[]>> boxes = new ArrayList<>();
        boxes.add(all);
        while (boxes.size() < max) {
            int pick = -1, pickRange = 0;
            for (int i = 0; i < boxes.size(); i++) {
                if (boxes.get(i).size() < 2) continue;
                int r = widestRange(boxes.get(i))[1];
                if (r > pickRange) {
                    pickRange = r;
                    pick = i;
                }
            }
            if (pick < 0) break;
            List<int[]> box = boxes.remove(pick);
            int channel = widestRange(box)[0];
            int shift = 16 - 8 * channel;
            box.sort(Comparator.comparingInt(e -> (e[0] >> shift) & 255));
            long total = 0;
            for (int[] e : box) total += e[1];
            long run = 0;
            int cut = 1;
            for (int i = 0; i < box.size() - 1; i++) {
                run += box.get(i)[1];
                cut = i + 1;
                if (run * 2 >= total) break;
            }
            boxes.add(new ArrayList<>(box.subList(0, cut)));
            boxes.add(new ArrayList<>(box.subList(cut, box.size())));
        }
        int[] p = new int[boxes.size()];
        for (int i = 0; i < p.length; i++) {
            long r = 0, g = 0, b = 0, n = 0;
            for (int[] e : boxes.get(i)) {
                r += (long) ((e[0] >> 16) & 255) * e[1];
                g += (long) ((e[0] >> 8) & 255) * e[1];
                b += (long) (e[0] & 255) * e[1];
                n += e[1];
            }
            p[i] = ((int) (r / n) << 16) | ((int) (g / n) << 8) | (int) (b / n);
        }
        return p;
    }

    /** {channel index (0=r,1=g,2=b), range} of the widest colour channel in the box. */
    static int[] widestRange(List<int[]> box) {
        int[] lo = {255, 255, 255}, hi = {0, 0, 0};
        for (int[] e : box) {
            for (int c = 0; c < 3; c++) {
                int v = (e[0] >> (16 - 8 * c)) & 255;
                lo[c] = Math.min(lo[c], v);
                hi[c] = Math.max(hi[c], v);
            }
        }
        int best = 0;
        for (int c = 1; c < 3; c++) if (hi[c] - lo[c] > hi[best] - lo[best]) best = c;
        return new int[]{best, hi[best] - lo[best]};
    }

    // ---------------------------------------------------------------- decode (verification)

    /** Decodes frame 0 to ARGB, or returns null when the container does not parse exactly. */
    static int[] decode(byte[] d, int[] dims) {
        try {
            ByteBuffer b = ByteBuffer.wrap(d);
            b.position(d.length - 2);
            int count = b.getShort() & 0xffff;
            int trailer = 7 + count * 8;
            b.position(d.length - trailer);
            int maxW = b.getShort() & 0xffff, maxH = b.getShort() & 0xffff;
            int palLen = (b.get() & 0xff) + 1;
            int[] ox = new int[count], oy = new int[count], ws = new int[count], hs = new int[count];
            for (int i = 0; i < count; i++) ox[i] = b.getShort() & 0xffff;
            for (int i = 0; i < count; i++) oy[i] = b.getShort() & 0xffff;
            for (int i = 0; i < count; i++) ws[i] = b.getShort() & 0xffff;
            for (int i = 0; i < count; i++) hs[i] = b.getShort() & 0xffff;
            int palStart = d.length - trailer - (palLen - 1) * 3;
            int[] pal = new int[palLen];
            b.position(palStart);
            for (int i = 1; i < palLen; i++) {
                int c = ((b.get() & 0xff) << 16) | ((b.get() & 0xff) << 8) | (b.get() & 0xff);
                pal[i] = c == 0 ? 1 : c;
            }
            b.position(0);
            int[] first = null;
            for (int s = 0; s < count; s++) {
                int w = ws[s], h = hs[s];
                int flags = b.get() & 0xff;
                byte[] ix = new byte[w * h];
                if ((flags & 1) == 0) {
                    b.get(ix);
                } else {
                    for (int x = 0; x < w; x++) for (int y = 0; y < h; y++) ix[y * w + x] = b.get();
                }
                byte[] al = null;
                if ((flags & 2) != 0) {
                    al = new byte[w * h];
                    if ((flags & 1) == 0) {
                        b.get(al);
                    } else {
                        for (int x = 0; x < w; x++) for (int y = 0; y < h; y++) al[y * w + x] = b.get();
                    }
                }
                if (s == 0) {
                    first = new int[w * h];
                    for (int i = 0; i < first.length; i++) {
                        int p = ix[i] & 0xff;
                        int a = al != null ? al[i] & 0xff : (p == 0 ? 0 : 255);
                        first[i] = p == 0 ? 0 : (a << 24) | pal[p];
                    }
                    dims[0] = w;
                    dims[1] = h;
                }
            }
            return b.position() == palStart ? first : null;   // pixel data must end exactly at the palette
        } catch (Exception e) {
            return null;
        }
    }

    // ---------------------------------------------------------------- main

    public static void main(String[] args) throws Exception {
        String mode = args[0];
        String cachePath = args[1];
        CacheLibrary lib = CacheLibrary.create(cachePath);
        Index sprites = lib.index(INDEX_SPRITES);

        if (mode.equals("check")) {
            int ok = 0, bad = 0;
            for (int id : sprites.archiveIds()) {
                int[] dims = new int[2];
                if (decode(sprites.archive(id).file(0).getData(), dims) != null) ok++;
                else {
                    bad++;
                    if (bad <= 5) System.out.println("  does not parse: sprite " + id);
                }
            }
            System.out.println("stock sprites parsed exactly: " + ok + ", failed: " + bad);
            lib.close();
            return;
        }

        BufferedImage im = ImageIO.read(new File(args[2]));
        int[] ids = sprites.archiveIds();
        int id = args.length > 3 ? Integer.parseInt(args[3]) : ids[ids.length - 1] + 1;
        if (sprites.archive(id) != null) {
            System.out.println("sprite " + id + " already exists - pick another id");
            lib.close();
            System.exit(3);
        }
        byte[] data = encode(im);

        int[] dims = new int[2];
        int[] back = decode(data, dims);
        if (back == null || dims[0] != im.getWidth() || dims[1] != im.getHeight()) {
            System.out.println("self-check failed: encoded sprite does not decode");
            lib.close();
            System.exit(2);
        }
        int maxColourErr = 0, alphaMismatch = 0;
        for (int y = 0; y < im.getHeight(); y++) {
            for (int x = 0; x < im.getWidth(); x++) {
                int src = im.getRGB(x, y), dst = back[y * dims[0] + x];
                if ((src >>> 24) != (dst >>> 24)) alphaMismatch++;
                if ((src >>> 24) != 0) maxColourErr = Math.max(maxColourErr, (int) Math.sqrt(dist(src & 0xffffff, dst & 0xffffff)));
            }
        }
        System.out.println("self-check: decodes " + dims[0] + "x" + dims[1] + ", alpha mismatches=" + alphaMismatch
                + ", worst colour error=" + maxColourErr + "/441");

        lib.put(INDEX_SPRITES, id, data, null);
        boolean ok = sprites.update();
        lib.close();
        CacheLibrary check = CacheLibrary.create(cachePath);
        byte[] read = check.index(INDEX_SPRITES).archive(id).file(0).getData();
        System.out.println("packed sprite " + id + " into index " + INDEX_SPRITES + " (update=" + ok + ", " + read.length
                + " bytes, identical=" + Arrays.equals(read, data) + ", index8 revision=" + check.index(INDEX_SPRITES).getRevision() + ")");
        check.close();
    }
}
