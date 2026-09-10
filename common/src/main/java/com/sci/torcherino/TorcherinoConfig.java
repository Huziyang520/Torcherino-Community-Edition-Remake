package com.sci.torcherino;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.sci.torcherino.platform.Services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * TOML backed configuration.
 *
 * <p>Key names, sections and default values are carried over verbatim from the
 * original 1.12.2 {@code sci4me/torcherino.cfg} so that server owners can migrate
 * their settings by hand. The file itself moved to the modern location
 * {@code config/torcherino.toml}.</p>
 */
public final class TorcherinoConfig {

    private static final String GENERAL = "general";
    private static final String BLACKLIST = "blacklist";

    /** (For Server Owners) Is it logged when someone places a Torcherino? */
    public static boolean logPlacement = false;
    /** Is the recipe for Torcherino extremely OP? */
    public static boolean overPoweredRecipe = true;
    /** Is the recipe for the Compressed Torcherino enabled? */
    public static boolean compressedTorcherino = false;
    /** Only takes effect if Compressed Torcherinos are enabled. */
    public static boolean doubleCompressedTorcherino = false;
    /** Is the recipe for the Triple Compressed Torcherino enabled? */
    public static boolean tripleCompressedTorcherino = false;

    /** Entries of the form {@code modid:unlocalized}. */
    public static List<String> blacklistedBlocks = new ArrayList<>();
    /** Fully qualified class names of BlockEntity types. */
    public static List<String> blacklistedTiles = new ArrayList<>();

    private TorcherinoConfig() {
    }

    public static synchronized void load() {
        final Path path = Services.PLATFORM.getConfigDir().resolve(Constants.MOD_ID + ".toml");
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
        } catch (Exception e) {
            Constants.LOG.warn("Could not create the config directory for {}", path, e);
        }

        try (CommentedFileConfig cfg = CommentedFileConfig.builder(path).preserveInsertionOrder().build()) {
            cfg.load();

            boolean changed = false;
            changed |= put(cfg, GENERAL + ".logPlacement", false,
                    "(For Server Owners) Is it logged when someone places a Torcherino?");
            changed |= put(cfg, GENERAL + ".overPoweredRecipe", true,
                    "Is the recipe for Torcherino extremely OP?");
            changed |= put(cfg, GENERAL + ".compressedTorcherino", false,
                    "Is the recipe for the Compressed Torcherino enabled?");
            changed |= put(cfg, GENERAL + ".doubleCompressedTorcherino", false,
                    "Is the recipe for the Double Compressed Torcherino enabled? Only takes effect if Compressed Torcherinos are enabled.");
            changed |= put(cfg, GENERAL + ".tripleCompressedTorcherino", false,
                    "Is the recipe for the Triple Compressed Torcherino enabled?");
            changed |= put(cfg, BLACKLIST + ".blacklistedBlocks", new ArrayList<String>(),
                    "Blocks the Torcherino may not accelerate. Format: modid:unlocalized");
            changed |= put(cfg, BLACKLIST + ".blacklistedTiles", new ArrayList<String>(),
                    "BlockEntity classes the Torcherino may not accelerate. Format: Fully qualified class name");

            if (changed) {
                cfg.save();
            }

            read(cfg);
        } catch (Exception e) {
            Constants.LOG.error("Failed to load {}", path, e);
        }
    }

    public static synchronized void reload() {
        load();
    }

    private static boolean put(CommentedFileConfig cfg, String key, Object value, String comment) {
        if (cfg.contains(key)) {
            return false;
        }
        cfg.set(key, value);
        cfg.setComment(key, comment);
        return true;
    }

    private static void read(CommentedFileConfig cfg) {
        logPlacement = bool(cfg, GENERAL + ".logPlacement", false);
        overPoweredRecipe = bool(cfg, GENERAL + ".overPoweredRecipe", true);
        compressedTorcherino = bool(cfg, GENERAL + ".compressedTorcherino", false);
        doubleCompressedTorcherino = bool(cfg, GENERAL + ".doubleCompressedTorcherino", false);
        tripleCompressedTorcherino = bool(cfg, GENERAL + ".tripleCompressedTorcherino", false);
        blacklistedBlocks = stringList(cfg, BLACKLIST + ".blacklistedBlocks");
        blacklistedTiles = stringList(cfg, BLACKLIST + ".blacklistedTiles");
    }

    private static boolean bool(CommentedFileConfig cfg, String key, boolean fallback) {
        final Object value = cfg.get(key);
        return value instanceof Boolean b ? b : fallback;
    }

    private static List<String> stringList(CommentedFileConfig cfg, String key) {
        final List<String> result = new ArrayList<>();
        if (cfg.get(key) instanceof List<?> list) {
            for (Object element : list) {
                if (element instanceof String s) {
                    result.add(s);
                }
            }
        }
        return result;
    }
}
