package io.dm.panel.core;

import io.dm.cache.ItemDef;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves item icons for the panel's visual inventory/bank views.
 *
 * <p>Item icons are <em>not</em> stored as images in the OSRS cache — the client renders
 * them from 3D models, and that renderer isn't part of the server build. So this service
 * resolves an icon in three tiers, first hit wins:
 * <ol>
 *   <li><b>Local folder</b> — {@code &lt;cache&gt;/item-icons/&lt;id&gt;.png}. Drop dumped
 *       sprites here and they're used verbatim (fully offline, honors "from the cache").</li>
 *   <li><b>RuneLite icon CDN</b> — {@code https://static.runelite.net/cache/item/icon/&lt;id&gt;.png},
 *       background-loaded, for standard OSRS item IDs.</li>
 *   <li><b>Fallback tile</b> — a styled box with the id + name initials, so a cell is
 *       never blank (custom items / offline).</li>
 * </ol>
 * Loaded {@link Image}s are cached in memory by id.
 */
public final class ItemIconService {

    private static final String CDN = "https://static.runelite.net/cache/item/icon/";
    private static final Map<Integer, Image> CACHE = new ConcurrentHashMap<>();
    private static final File LOCAL_DIR = resolveLocalDir();

    private ItemIconService() {}

    private static File resolveLocalDir() {
        for (String p : new String[]{"../Cache/item-icons", "Cache/item-icons", "item-icons"}) {
            File f = new File(p);
            if (f.isDirectory()) {
                return f;
            }
        }
        return null;
    }

    /** Whether a local icon folder was found (used to tell the operator in the UI). */
    public static boolean hasLocalIcons() {
        return LOCAL_DIR != null;
    }

    /** Background-loaded image for an item id, or {@code null} if no source is available. */
    public static Image image(int id, double size) {
        Image cached = CACHE.get(id);
        if (cached != null) {
            return cached;
        }
        Image img = null;
        if (LOCAL_DIR != null) {
            File f = new File(LOCAL_DIR, id + ".png");
            if (f.isFile()) {
                img = new Image(f.toURI().toString(), size, size, true, true, true);
            }
        }
        if (img == null) {
            img = new Image(CDN + id + ".png", size, size, true, true, true);
        }
        CACHE.put(id, img);
        return img;
    }

    /**
     * A complete item cell: the icon (or fallback tile), an amount badge, and a
     * name/id tooltip. {@code size} is the icon box in px.
     */
    public static StackPane tile(int id, int amount, double size) {
        StackPane holder = new StackPane();
        holder.setPrefSize(size, size);
        holder.setMinSize(size, size);
        holder.getStyleClass().add("item-cell");

        holder.getChildren().add(fallback(id, size));

        Image img = image(id, size);
        if (img != null) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(size);
            iv.setFitHeight(size);
            iv.setPreserveRatio(true);
            // Only reveal the real icon once it has loaded without error; otherwise the
            // styled fallback underneath shows through.
            iv.visibleProperty().bind(
                    img.errorProperty().not().and(img.progressProperty().greaterThanOrEqualTo(1.0)));
            holder.getChildren().add(iv);
        }

        if (amount != 1) {
            Label qty = new Label(abbreviate(amount));
            qty.getStyleClass().add("item-qty");
            StackPane.setAlignment(qty, Pos.TOP_LEFT);
            holder.getChildren().add(qty);
        }

        String name = nameOf(id);
        Tooltip.install(holder, new Tooltip(name + "  (id " + id + ")" + (amount > 1 ? "  x" + amount : "")));
        return holder;
    }

    private static Label fallback(int id, double size) {
        String n = nameOf(id);
        String initials = n.length() <= 3 ? n : n.replaceAll("[^A-Za-z0-9 ]", "").trim();
        if (initials.length() > 4) {
            initials = initials.substring(0, 4);
        }
        Label l = new Label(initials.isBlank() ? String.valueOf(id) : initials);
        l.getStyleClass().add("item-fallback");
        l.setPrefSize(size, size);
        l.setWrapText(true);
        l.setAlignment(Pos.CENTER);
        return l;
    }

    /** OSRS-style quantity badge: 1..99999 raw, then K, then M. */
    public static String abbreviate(int amount) {
        if (amount >= 10_000_000) {
            return (amount / 1_000_000) + "M";
        }
        if (amount >= 100_000) {
            return (amount / 1000) + "K";
        }
        return Integer.toString(amount);
    }

    public static String nameOf(int id) {
        try {
            ItemDef def = ItemDef.get(id);
            if (def != null && def.name != null && !def.name.isBlank() && !def.name.equalsIgnoreCase("null")) {
                return def.name;
            }
        } catch (Throwable ignored) {}
        return "Item " + id;
    }
}
