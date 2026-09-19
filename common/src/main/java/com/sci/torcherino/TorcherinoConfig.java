/*
 * 本文件：TOML 配置读写。
 * 说明：读写 config/torcherino.toml；缺失的键会自动补写并带上注释，键名与段落保持稳定，跨版本可直接沿用旧设置。
 */
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
 * <p>Key names, sections and default values are stable across versions, so server owners
 * can carry their settings over by hand.</p>
 */
public final class TorcherinoConfig {

    private static final String GENERAL = "general";
    private static final String BLACKLIST = "blacklist";
    private static final String GUI = "gui";

    /**
     * Config location: {@code config/torcherino.toml}, the convention every current loader
     * uses. Key names and sections inside the file are stable across versions.
     */
    public static final String CONFIG_FILE_NAME = Constants.MOD_ID + ".toml";

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
    /**
     * {@code true} (default) opens the graphical editor when a Torcherino is right
     * clicked; {@code false} restores the classic quick interaction (right click cycles
     * the area, the modifier key cycles the speed). Only one of the two is ever active.
     */
    public static boolean useGui = true;
    /** {@code false} (default) makes the editor sliders snap to whole steps. */
    public static boolean smoothSlider = false;

    /** Entries of the form {@code modid:unlocalized}. */
    public static List<String> blacklistedBlocks = new ArrayList<>();
    /** Fully qualified class names of BlockEntity types. */
    public static List<String> blacklistedTiles = new ArrayList<>();

    private TorcherinoConfig() {
    }

    /**
     * Reads one of the boolean switches by name. Used by the recipe conditions of both
     * loaders, which have to answer "is this switch set to the expected value" while the
     * data pack is loading.
     *
     * @param name the key as written in the recipe JSON, e.g. {@code overPoweredRecipe}.
     * @return the current value, or {@code false} for an unknown name.
     */
    public static boolean flag(String name) {
        return switch (name) {
            case "logPlacement" -> logPlacement;
            case "overPoweredRecipe" -> overPoweredRecipe;
            case "compressedTorcherino" -> compressedTorcherino;
            case "doubleCompressedTorcherino" -> doubleCompressedTorcherino;
            case "tripleCompressedTorcherino" -> tripleCompressedTorcherino;
            case "useGui" -> useGui;
            case "smoothSlider" -> smoothSlider;
            default -> false;
        };
    }

    public static synchronized void load() {
        final Path path = Services.PLATFORM.getConfigDir().resolve(CONFIG_FILE_NAME);
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
        } catch (Exception e) {
            Constants.LOG.warn("Could not create the config directory for {}", path, e);
        }
        Constants.LOG.info("Loading Torcherino configuration from {}", path.toAbsolutePath());

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
            changed |= put(cfg, GENERAL + ".useGui", true,
                    "Open the graphical editor when right clicking a Torcherino. Set to false to use the classic quick interaction instead; only one of the two is active.");
            changed |= put(cfg, GUI + ".smoothSlider", false,
                    "true = the editor sliders move continuously and round on release; false = every step snaps.");
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

    /**
     * Writes the current switches back to the TOML file. Used by the configuration screen
     * that Configured picks up; the blacklist entries are left untouched.
     */
    public static synchronized void save() {
        final Path path = Services.PLATFORM.getConfigDir().resolve(CONFIG_FILE_NAME);
        try (CommentedFileConfig cfg = CommentedFileConfig.builder(path).preserveInsertionOrder().build()) {
            cfg.load();
            cfg.set(GENERAL + ".logPlacement", logPlacement);
            cfg.set(GENERAL + ".overPoweredRecipe", overPoweredRecipe);
            cfg.set(GENERAL + ".compressedTorcherino", compressedTorcherino);
            cfg.set(GENERAL + ".doubleCompressedTorcherino", doubleCompressedTorcherino);
            cfg.set(GENERAL + ".tripleCompressedTorcherino", tripleCompressedTorcherino);
            cfg.set(GENERAL + ".useGui", useGui);
            cfg.set(GUI + ".smoothSlider", smoothSlider);
            cfg.save();
            Constants.LOG.info("Saved Torcherino configuration to {}", path.toAbsolutePath());
        } catch (Exception e) {
            Constants.LOG.error("Failed to save {}", path, e);
        }
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
        useGui = bool(cfg, GENERAL + ".useGui", true);
        smoothSlider = bool(cfg, GUI + ".smoothSlider", false);
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
