/*
 * 本文件：TOML 配置读写（拆分为客户端 / 服务端两个文件）。
 * 说明：config/torcherino-client.toml 只管可视化界面（右键是否开界面、滑条是否连续拖动），
 *      config/torcherino-server.toml 管服务端行为（配方开关、黑名单）；缺失的键会自动补写并带注释。
 *      若存在拆分前的旧文件 config/torcherino.toml，首次生成新文件时会从旧文件搬取值。
 */
package com.sci.torcherino;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.sci.torcherino.platform.Services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * TOML backed configuration, split into a client and a server file.
 *
 * <p>The split exists because the two halves have completely different owners and
 * lifetimes: {@code gui.*} is a local client preference, while the recipe switches and the
 * blacklist are read by the server when the data pack loads. Mixing them in one file made
 * client side toggles look broken on a server.</p>
 */
public final class TorcherinoConfig {

    private static final String GUI = "gui";
    private static final String GENERAL = "general";
    private static final String BLACKLIST = "blacklist";

    /** Client preferences. Never read by a dedicated server. */
    public static final String CLIENT_FILE_NAME = Constants.MOD_ID + "-client.toml";
    /** Server behaviour: recipe switches and the blacklist. */
    public static final String SERVER_FILE_NAME = Constants.MOD_ID + "-server.toml";
    /** The single file used before the split; only read once, to migrate values. */
    private static final String LEGACY_FILE_NAME = Constants.MOD_ID + ".toml";

    // ---- client file -------------------------------------------------------------------

    /**
     * {@code true} (default) opens the graphical editor when a Torcherino is right
     * clicked; {@code false} restores the classic quick interaction (right click cycles
     * the area, the modifier key cycles the speed). Only one of the two is ever active.
     */
    public static boolean useGui = true;
    /**
     * {@code true} slides the editor handles continuously inside a step, {@code false}
     * (default) pulls every handle onto the nearest step while dragging.
     */
    public static boolean smoothSlider = false;

    // ---- server file -------------------------------------------------------------------

    /** Is the cheap recipe used instead of the nether star one? */
    public static boolean overPoweredRecipe = true;
    /** Is the recipe for the Compressed Torcherino enabled? */
    public static boolean compressedTorcherino = false;
    /** Only takes effect if Compressed Torcherinos are enabled. */
    public static boolean doubleCompressedTorcherino = false;
    /** Only takes effect if Compressed and Double Compressed Torcherinos are enabled. */
    public static boolean tripleCompressedTorcherino = false;

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
        final Path dir = Services.PLATFORM.getConfigDir();
        final CommentedFileConfig legacy = openLegacy(dir);
        try {
            loadClient(dir, legacy);
            loadServer(dir, legacy);
        } finally {
            if (legacy != null) {
                legacy.close();
            }
        }
    }

    public static synchronized void reload() {
        load();
    }

    /**
     * Writes the client switches back to {@code torcherino-client.toml}. Called by the
     * configuration screen.
     */
    public static synchronized void saveClient() {
        final Path path = Services.PLATFORM.getConfigDir().resolve(CLIENT_FILE_NAME);
        try (CommentedFileConfig cfg = CommentedFileConfig.builder(path).preserveInsertionOrder().build()) {
            cfg.load();
            cfg.set(GUI + ".useGui", useGui);
            cfg.set(GUI + ".smoothSlider", smoothSlider);
            cfg.save();
            Constants.LOG.info("Saved Torcherino client configuration to {}", path.toAbsolutePath());
        } catch (Exception e) {
            Constants.LOG.error("Failed to save {}", path, e);
        }
    }

    /**
     * Writes the server switches back to {@code torcherino-server.toml}. The blacklist
     * entries are left untouched.
     */
    public static synchronized void saveServer() {
        final Path path = Services.PLATFORM.getConfigDir().resolve(SERVER_FILE_NAME);
        try (CommentedFileConfig cfg = CommentedFileConfig.builder(path).preserveInsertionOrder().build()) {
            cfg.load();
            cfg.set(GENERAL + ".overPoweredRecipe", overPoweredRecipe);
            cfg.set(GENERAL + ".compressedTorcherino", compressedTorcherino);
            cfg.set(GENERAL + ".doubleCompressedTorcherino", doubleCompressedTorcherino);
            cfg.set(GENERAL + ".tripleCompressedTorcherino", tripleCompressedTorcherino);
            cfg.save();
            Constants.LOG.info("Saved Torcherino server configuration to {}", path.toAbsolutePath());
        } catch (Exception e) {
            Constants.LOG.error("Failed to save {}", path, e);
        }
    }

    private static void loadClient(Path dir, CommentedFileConfig legacy) {
        final Path path = createDirectories(dir.resolve(CLIENT_FILE_NAME));
        try (CommentedFileConfig cfg = CommentedFileConfig.builder(path).preserveInsertionOrder().build()) {
            cfg.load();
            boolean changed = false;
            changed |= put(cfg, legacy, GUI + ".useGui", true,
                    "CLIENT SIDE. Open the graphical editor when right clicking a Torcherino. Set to false to use the classic quick interaction instead; only one of the two is active.");
            changed |= put(cfg, legacy, GUI + ".smoothSlider", false,
                    "CLIENT SIDE. true = the editor handles can be dragged continuously inside a step (the value is still rounded to a whole step); false = every handle snaps onto the nearest step.");
            if (changed) {
                cfg.save();
            }
            useGui = bool(cfg, GUI + ".useGui", true);
            smoothSlider = bool(cfg, GUI + ".smoothSlider", false);
        } catch (Exception e) {
            Constants.LOG.error("Failed to load {}", path, e);
        }
    }

    private static void loadServer(Path dir, CommentedFileConfig legacy) {
        final Path path = createDirectories(dir.resolve(SERVER_FILE_NAME));
        try (CommentedFileConfig cfg = CommentedFileConfig.builder(path).preserveInsertionOrder().build()) {
            cfg.load();
            boolean changed = false;
            changed |= put(cfg, legacy, GENERAL + ".overPoweredRecipe", true,
                    "SERVER SIDE. Use the cheap recipe instead of the nether star one. Recipes are read while the data pack loads, so a change needs a restart or /reload.");
            changed |= put(cfg, legacy, GENERAL + ".compressedTorcherino", false,
                    "SERVER SIDE. Enable the recipes of the Compressed Torcherino. Needs a restart or /reload.");
            changed |= put(cfg, legacy, GENERAL + ".doubleCompressedTorcherino", false,
                    "SERVER SIDE. Enable the recipes of the Double Compressed Torcherino. Only takes effect if compressedTorcherino is enabled as well. Needs a restart or /reload.");
            changed |= put(cfg, legacy, GENERAL + ".tripleCompressedTorcherino", false,
                    "SERVER SIDE. Enable the recipes of the Triple Compressed Torcherino. Only takes effect if compressedTorcherino and doubleCompressedTorcherino are enabled as well. Needs a restart or /reload.");
            changed |= put(cfg, legacy, BLACKLIST + ".blacklistedBlocks", new ArrayList<String>(),
                    "Blocks the Torcherino may not accelerate. Format: modid:unlocalized");
            changed |= put(cfg, legacy, BLACKLIST + ".blacklistedTiles", new ArrayList<String>(),
                    "BlockEntity classes the Torcherino may not accelerate. Format: Fully qualified class name");
            if (changed) {
                cfg.save();
            }
            overPoweredRecipe = bool(cfg, GENERAL + ".overPoweredRecipe", true);
            compressedTorcherino = bool(cfg, GENERAL + ".compressedTorcherino", false);
            doubleCompressedTorcherino = bool(cfg, GENERAL + ".doubleCompressedTorcherino", false);
            tripleCompressedTorcherino = bool(cfg, GENERAL + ".tripleCompressedTorcherino", false);
            blacklistedBlocks = stringList(cfg, BLACKLIST + ".blacklistedBlocks");
            blacklistedTiles = stringList(cfg, BLACKLIST + ".blacklistedTiles");
        } catch (Exception e) {
            Constants.LOG.error("Failed to load {}", path, e);
        }
    }

    /**
     * Opens the pre-split configuration file, if it is still around. Its values seed the
     * new files once, so an existing server keeps its settings.
     */
    private static CommentedFileConfig openLegacy(Path dir) {
        final Path path = dir.resolve(LEGACY_FILE_NAME);
        if (!Files.exists(path)) {
            return null;
        }
        try {
            final CommentedFileConfig cfg = CommentedFileConfig.builder(path).build();
            cfg.load();
            Constants.LOG.info("Migrating settings from the pre-split {} into {}/{}",
                    LEGACY_FILE_NAME, CLIENT_FILE_NAME, SERVER_FILE_NAME);
            return cfg;
        } catch (Exception e) {
            Constants.LOG.warn("Could not read the pre-split configuration {}", path, e);
            return null;
        }
    }

    private static Path createDirectories(Path path) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
        } catch (Exception e) {
            Constants.LOG.warn("Could not create the config directory for {}", path, e);
        }
        return path;
    }

    /**
     * Adds a missing key. The value of the pre-split file wins over the hardcoded default,
     * which is what makes the migration transparent.
     */
    private static boolean put(CommentedFileConfig cfg, CommentedFileConfig legacy, String key, Object value, String comment) {
        if (cfg.contains(key)) {
            return false;
        }
        cfg.set(key, migratedValue(legacy, key, value));
        cfg.setComment(key, comment);
        return true;
    }

    private static Object migratedValue(CommentedFileConfig legacy, String key, Object fallback) {
        if (legacy == null || !legacy.contains(key)) {
            return fallback;
        }
        final Object value = legacy.get(key);
        return value != null ? value : fallback;
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
