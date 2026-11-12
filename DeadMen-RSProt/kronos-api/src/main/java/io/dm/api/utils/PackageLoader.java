package io.dm.api.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PackageLoader {

    /**
     * Packages excluded from the eager {@code Class.forName(..)} scan.
     *
     * <p>The scan force-<em>initializes</em> every class it visits (to run content
     * self-registration static blocks). The in-process Server Panel ({@code io.dm.panel})
     * is a JavaFX UI whose control classes must NOT be initialized on the boot/main
     * thread before the JavaFX toolkit is up — doing so throws "Toolkit not initialized"
     * and permanently poisons those classes. The panel has no static registration to
     * run here, so it is simply skipped; it initializes lazily on the JavaFX thread.
     */
    private static final String[] EXCLUDED_PACKAGES = { "io.dm.panel" };

    /**
     * Individual classes excluded from the eager {@code Class.forName(..)} scan, by fully
     * qualified name. Unlike {@link #EXCLUDED_PACKAGES}, these are auto-generated, pure
     * {@code public static final int} constant-holder classes (no static block, nothing to
     * register) that happen to live in a package ({@code io.dm.cache}) full of classes that
     * genuinely DO need loading (e.g. {@code ItemDef}, {@code ObjectDef}). Every reference to one
     * of these constants elsewhere in the codebase (e.g. {@code ItemID.CANNON_BASE}) is a
     * compile-time-constant field access that javac already inlines as a literal into the
     * referencing class's own bytecode (JLS 13.4.9) — nothing at runtime actually needs these
     * three classes loaded. Force-loading them anyway cost ~5.2 seconds combined at boot (measured
     * via a temp per-class boot timer, 2026-07-18: ObjectID ~3.3s, ItemID ~1.3s, NpcID ~0.7s) —
     * verifying/parsing a class file with tens of thousands of constant-pool entries is genuinely
     * expensive, for zero behavioral benefit.
     */
    private static final String[] EXCLUDED_CLASSES = {
            "io.dm.cache.ItemID", "io.dm.cache.NpcID", "io.dm.cache.ObjectID"
    };

    private static boolean isExcluded(String classOrPackageName) {
        for (String excluded : EXCLUDED_PACKAGES) {
            if (classOrPackageName.equals(excluded) || classOrPackageName.startsWith(excluded + ".")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isExcludedClass(String fullyQualifiedClassName) {
        for (String excluded : EXCLUDED_CLASSES) {
            if (fullyQualifiedClassName.equals(excluded))
                return true;
        }
        return false;
    }

    public static Class[] load(String packageName, Class<?> ext) throws Exception {
        return load(packageName, ext, true);
    }

    public static Class[] load(String packageName, Class<?> ext, boolean allowAnonymous) throws Exception {
        Class[] classes = load(packageName);
        ArrayList<Class> list = new ArrayList<>();
        for(Class c : classes) {
            if(ext != c && ext.isAssignableFrom(c))
                list.add(c);
        }
        if(!allowAnonymous)
            list.removeIf(Class::isAnonymousClass);
        return list.toArray(new Class[list.size()]);
    }

    public static Class[] load(String packageName) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if(classLoader == null)
            throw new IOException("Could not access ClassLoader!");
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        ArrayList<File> dirs = new ArrayList<>();
        while(resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String fileName = resource.getFile().replace("%20", " ").replace("%5c", File.separator);
            dirs.add(new File(fileName));
        }
        ArrayList<Class> classes = new ArrayList<>();
        for(File dir : dirs) {
            List<Class> loaded = findClasses(dir, packageName);
            if(loaded != null)
                classes.addAll(loaded);
        }
        return classes.toArray(new Class[classes.size()]);
    }

    private static ArrayList<Class> findClasses(File dir, String packageName) throws IOException {
        ArrayList<Class> classes = new ArrayList<>();
        if(dir.getPath().contains(".jar!")) {
            /**
             * Loaded from jar
             */
            String[] split = dir.getPath().split("!");
            String jarName = split[0].substring(5);
            ZipInputStream in = null;
            try {
                in = new ZipInputStream(new FileInputStream(jarName));
                ZipEntry entry;
                while((entry = in.getNextEntry()) != null) {
                    String name = codePath(entry.getName());
                    if(name.endsWith(".class") && name.startsWith(packageName)
                            && !isExcluded(name.substring(0, name.length() - 6))
                            && !isExcludedClass(name.substring(0, name.length() - 6))) {
                        try {
                            classes.add(Class.forName(name.substring(0, name.length() - 6)));
                        } catch(Throwable t) {
                            System.err.println("Failed to load class: " + name);
                        }
                    }
                }
            } finally {
                if(in != null)
                    in.close();
            }
        } else {
            /**
             * Loaded from directory
             */
            File[] files = dir.listFiles();
            if(files == null || files.length == 0)
                return null;
            for(File file : files) {
                String name = file.getName();
                if(file.isDirectory()) {
                    if(name.contains("."))
                        continue;
                    if(isExcluded(packageName + "." + name))
                        continue;
                    List<Class> loaded = findClasses(file, packageName + "." + name);
                    if(loaded != null)
                        classes.addAll(loaded);
                    continue;
                }
                if(name.endsWith(".class") && !isExcluded(packageName)
                        && !isExcludedClass(packageName + '.' + name.substring(0, name.length() - 6))) {
                    try {
                        classes.add(Class.forName(packageName + '.' + name.substring(0, name.length() - 6)));
                    } catch(Throwable t) {
                        ServerWrapper.logError("Package Failure: " + name, t);
                    }
                }
            }
        }
        return classes;
    }

    private static String codePath(String path) {
        return path.replace('\\', '.').replace('/', '.');
    }

}
