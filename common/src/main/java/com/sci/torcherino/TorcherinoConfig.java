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
    public static boolean freeSpeedMultiplier = false;
    /**
     * {@code true} (default) prints the "recipes are controlled by the server" notice in
     * chat when a world is joined. Client side, purely informational.
     */
    public static boolean joinNotice = true;

    // ---- server file -------------------------------------------------------------------

    /** Is the cheap recipe used instead of the nether star one? */
    public static boolean overPoweredRecipe = true;
    /** Is the recipe for the Compressed Torcherino enabled? On by default since 1.4.0. */
    public static boolean compressedTorcherino = true;
    /** Only takes effect if Compressed Torcherinos are enabled. On by default since 1.4.0. */
    public static boolean doubleCompressedTorcherino = true;
    /** Only takes effect if Compressed and Double Compressed Torcherinos are enabled. On by default since 1.4.0. */
    public static boolean tripleCompressedTorcherino = true;
    /**
     * Switch of the per Torcherino cap below. {@code false} (default) means the cap is not
     * used at all, which is the uncapped behaviour of 1.4.0 and earlier.
     */
    public static boolean maxAcceleratedTicksEnabled = false;
    /**
     * Hard cap of acceleration calls (extra random ticks plus block entity ticks) a single
     * Torcherino may spend per server tick, only read while the switch above is on. The
     * default is the recommended starting point; {@code 0} still means unlimited, in case
     * someone wants the switch to have no effect. A positive value stops the scan of that
     * Torcherino once the budget is used up. Blocks are not skipped permanently: the scan
     * starts at a rotating section, so every part of the area still gets its turn.
     */
    public static int maxAcceleratedTicksPerTick = 16384;
    /**
     * Adaptive brake. {@code false} (default) plays the configured speed back exactly as
     * written, which is the behaviour of every release up to now. When switched on, a
     * Torcherino divides its rounds while the server tick is slower than 50 ms and returns
     * to full speed as soon as it recovers, which keeps the server playable instead of
     * letting a large area or a high tier eat the whole tick.
     */
    public static boolean adaptiveThrottle = false;
    /**
     * Switch of the server wide cap below. {@code false} (default) leaves it unused, so only
     * the per Torcherino cap can apply.
     */
    public static boolean maxAcceleratedTicksGlobalEnabled = false;
    /**
     * Hard cap of acceleration calls the whole mod may spend per server tick, shared by every
     * Torcherino and divided over the ones that scanned in the previous tick. Only read while
     * the switch above is on; {@code 0} means unlimited. The default is the recommended
     * starting point for a server where the per Torcherino cap is not enough.
     */
    public static int maxAcceleratedTicksPerTickGlobal = 65536;
    /** Tick duration above which the adaptive brake starts scaling down; 50 ms = TPS 20. */
    public static int adaptiveThrottleThresholdMs = 50;
    /** Upper bound of the adaptive brake divisor, so a stuck server cannot scale to zero. */
    public static int adaptiveThrottleMaxDivisor = 256;
    /**
     * Saturation skip. {@code false} (default) plays the configured speed back everywhere.
     * When switched on, a position whose block and six neighbours stayed the same for a
     * couple of ticks is capped at one call per tick until something around it changes,
     * which stops saturated farms from eating the whole tick. It lowers the configured rate,
     * which is why it is off by default.
     */
    public static boolean skipSaturatedPositions = false;
    /**
     * Probabilistic random ticks. {@code true} (default since 1.4.1, user decision: this one is
     * important enough to be on out of the box) turns the burst of {@code randomTick} calls per
     * position into a single call with the probability {@code rounds * randomTickSpeed / 4096} -
     * the vanilla random tick model scaled by the configured multiplier. The expected growth
     * matches, while the cost per tick becomes proportional to the number of positions instead
     * of positions x multiplier. Set it to {@code false} to get the call by call behaviour of
     * the releases before it.
     */
    public static boolean probabilisticRandomTick = true;
    /**
     * Owner gate for servers: when on, a Torcherino only accelerates while the player who
     * placed it is online. Off by default, and ignored for Torcherinos without a recorded
     * owner (for example ones placed before this option existed).
     */
    public static boolean ownerOnlyWhenOnline = false;
    /** Seconds between two diagnostic log lines of the acceleration loop; 0 = silent. */
    public static int statsLogIntervalSeconds = 0;

    /**
     * @return the per Torcherino cap that is really in effect: {@code 0} while the switch is
     *         off (or the value is {@code 0}), which is what the acceleration core reads.
     */
    public static int effectiveMaxAcceleratedTicksPerTick() {
        return maxAcceleratedTicksEnabled ? Math.max(0, maxAcceleratedTicksPerTick) : 0;
    }

    /** @return the server wide cap that is really in effect; see the method above. */
    public static int effectiveMaxAcceleratedTicksGlobal() {
        return maxAcceleratedTicksGlobalEnabled ? Math.max(0, maxAcceleratedTicksPerTickGlobal) : 0;
    }


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
            case "freeSpeedMultiplier" -> freeSpeedMultiplier;
            default -> false;
        };
    }

    public static synchronized void load() {
        if (Services.PLATFORM.usesForgeConfigSystem()) {
            // Forge reads the two files through its own config system (which is what
            // Configured turns into a screen), so the TOML code below must not touch them.
            return;
        }
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
        if (Services.PLATFORM.usesForgeConfigSystem()) {
            Services.PLATFORM.persistClientConfig();
            return;
        }
        final Path path = Services.PLATFORM.getConfigDir().resolve(CLIENT_FILE_NAME);
        try (CommentedFileConfig cfg = CommentedFileConfig.builder(path).preserveInsertionOrder().build()) {
            cfg.load();
            cfg.set(GUI + ".useGui", useGui);
            cfg.set(GUI + ".freeSpeedMultiplier", freeSpeedMultiplier);
            cfg.set(GUI + ".joinNotice", joinNotice);
            cfg.save();
            Constants.LOG.info("Saved Torcherino client configuration to {}", path.toAbsolutePath());
        } catch (Exception e) {
            Constants.LOG.error("Failed to save {}", path, e);
        }
    }

    /**
     * Writes the server switches and the two black lists back to
     * {@code torcherino-server.toml}. The lists are written too because the configuration
     * screen lets a player edit them; comments already present in the file are preserved.
     */
    public static synchronized void saveServer() {
        if (Services.PLATFORM.usesForgeConfigSystem()) {
            Services.PLATFORM.persistServerConfig();
            return;
        }
        final Path path = Services.PLATFORM.getConfigDir().resolve(SERVER_FILE_NAME);
        try (CommentedFileConfig cfg = CommentedFileConfig.builder(path).preserveInsertionOrder().build()) {
            cfg.load();
            cfg.set(GENERAL + ".overPoweredRecipe", overPoweredRecipe);
            cfg.set(GENERAL + ".compressedTorcherino", compressedTorcherino);
            cfg.set(GENERAL + ".doubleCompressedTorcherino", doubleCompressedTorcherino);
            cfg.set(GENERAL + ".tripleCompressedTorcherino", tripleCompressedTorcherino);
            cfg.set(GENERAL + ".maxAcceleratedTicksEnabled", maxAcceleratedTicksEnabled);
            cfg.set(GENERAL + ".maxAcceleratedTicksPerTick", maxAcceleratedTicksPerTick);
            cfg.set(GENERAL + ".maxAcceleratedTicksGlobalEnabled", maxAcceleratedTicksGlobalEnabled);
            cfg.set(GENERAL + ".maxAcceleratedTicksPerTickGlobal", maxAcceleratedTicksPerTickGlobal);
            cfg.set(GENERAL + ".adaptiveThrottle", adaptiveThrottle);
            cfg.set(GENERAL + ".adaptiveThrottleThresholdMs", adaptiveThrottleThresholdMs);
            cfg.set(GENERAL + ".adaptiveThrottleMaxDivisor", adaptiveThrottleMaxDivisor);
            cfg.set(GENERAL + ".skipSaturatedPositions", skipSaturatedPositions);
            cfg.set(GENERAL + ".probabilisticRandomTick", probabilisticRandomTick);
            cfg.set(GENERAL + ".ownerOnlyWhenOnline", ownerOnlyWhenOnline);
            cfg.set(GENERAL + ".statsLogIntervalSeconds", statsLogIntervalSeconds);
            // The lists are editable in the configuration screen too; a copy keeps the
            // configuration free to keep its own list instance.
            cfg.set(BLACKLIST + ".blacklistedBlocks", new ArrayList<>(blacklistedBlocks));
            cfg.set(BLACKLIST + ".blacklistedTiles", new ArrayList<>(blacklistedTiles));
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
            changed |= put(cfg, legacy, GUI + ".freeSpeedMultiplier", false,
                    "CLIENT SIDE. true = the editor handles can be dragged continuously inside a step (the value is still rounded to a whole step); false = every handle snaps onto the nearest step.");
            changed |= put(cfg, legacy, GUI + ".joinNotice", true,
                    "CLIENT SIDE. Print a chat notice about the recipe switches every time a world is joined. Set to false to silence it.");
            if (changed) {
                cfg.save();
            }
            useGui = bool(cfg, GUI + ".useGui", true);
            freeSpeedMultiplier = bool(cfg, GUI + ".freeSpeedMultiplier", false);
            joinNotice = bool(cfg, GUI + ".joinNotice", true);
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
            changed |= put(cfg, legacy, GENERAL + ".compressedTorcherino", true,
                    "SERVER SIDE. Enable the recipes of the Compressed Torcherino. On by default since 1.4.0. Needs a restart or /reload.");
            changed |= put(cfg, legacy, GENERAL + ".doubleCompressedTorcherino", true,
                    "SERVER SIDE. Enable the recipes of the Double Compressed Torcherino. Only takes effect if compressedTorcherino is enabled as well. On by default since 1.4.0. Needs a restart or /reload.");
            changed |= put(cfg, legacy, GENERAL + ".tripleCompressedTorcherino", true,
                    "SERVER SIDE. Enable the recipes of the Triple Compressed Torcherino. Only takes effect if compressedTorcherino and doubleCompressedTorcherino are enabled as well. On by default since 1.4.0. Needs a restart or /reload.");
            changed |= put(cfg, legacy, GENERAL + ".maxAcceleratedTicksEnabled", false,
                    "SERVER SIDE. Switch of the per Torcherino cap below. false (default) leaves the acceleration uncapped, exactly like 1.4.0 and earlier. Turn it on when one Torcherino with a large area, a high speed or a high compression tier makes the tick too long. Applies immediately, no restart needed.");
            changed |= put(cfg, legacy, GENERAL + ".maxAcceleratedTicksPerTick", 16384,
                    "SERVER SIDE. Maximum acceleration calls (extra random ticks plus block entity ticks) one Torcherino may spend per server tick. Only read while maxAcceleratedTicksEnabled is true; the default is the recommended starting point, and 0 still means unlimited. The scan starts at a rotating section, so every part of the area still gets accelerated. Applies immediately, no restart needed.");
            changed |= put(cfg, legacy, GENERAL + ".adaptiveThrottle", false,
                    "SERVER SIDE. Adaptive brake. false (default) always plays back the configured speed, exactly like every release up to now. true divides the acceleration of every Torcherino while the server tick is slower than the configured healthy tick time and returns to full speed as soon as it recovers, so a large area or a high compression tier cannot make the server unplayable. Applies immediately, no restart needed.");
            changed |= put(cfg, legacy, GENERAL + ".maxAcceleratedTicksGlobalEnabled", false,
                    "SERVER SIDE. Switch of the server wide cap below. false (default) leaves it unused, so only the per Torcherino cap can apply. Use it when one player has so many Torcherinos that the per Torcherino cap is not enough. Applies immediately, no restart needed.");
            changed |= put(cfg, legacy, GENERAL + ".maxAcceleratedTicksPerTickGlobal", 65536,
                    "SERVER SIDE. Maximum acceleration calls (extra random ticks plus block entity ticks) the whole mod may spend per server tick, shared by every Torcherino and divided over the ones that scanned in the previous tick. Only read while maxAcceleratedTicksGlobalEnabled is true; the default is the recommended starting point, and 0 still means unlimited. Applies immediately, no restart needed.");
            changed |= put(cfg, legacy, GENERAL + ".adaptiveThrottleThresholdMs", 50,
                    "SERVER SIDE. Tick duration in milliseconds above which the adaptive brake starts scaling the acceleration down. 50 ms is the TPS 20 warning line; raise it on a heavy modpack whose healthy tick is already longer. Only read while adaptiveThrottle is true. Applies immediately, no restart needed.");
            changed |= put(cfg, legacy, GENERAL + ".adaptiveThrottleMaxDivisor", 256,
                    "SERVER SIDE. Upper bound of the adaptive brake divisor, so a completely stuck server cannot scale the acceleration down to nothing. Only read while adaptiveThrottle is true. Applies immediately, no restart needed.");
            changed |= put(cfg, legacy, GENERAL + ".skipSaturatedPositions", false,
                    "SERVER SIDE. LOWER RATE, OFF BY DEFAULT. When true, a position whose block and six neighbouring blocks stayed the same for a couple of ticks is accelerated only once per tick until something around it changes - saturated farms stop consuming the whole tick, but stable blocks are no longer played back at the configured speed. Positions on the border of a chunk section are never throttled. Applies immediately, no restart needed.");
            changed |= put(cfg, legacy, GENERAL + ".probabilisticRandomTick", true,
                    "SERVER SIDE. DIFFERENT MODEL, ON BY DEFAULT since 1.4.1. When true, a randomly ticking position receives a single random tick with the probability rounds * randomTickSpeed / 4096 instead of 'rounds' calls - that is the vanilla random tick model scaled by the configured multiplier. The expected amount of growth matches, while the cost per tick is proportional to the number of positions instead of positions x multiplier. Set it to false for the call by call behaviour of the older releases. Applies immediately, no restart needed.");
            changed |= put(cfg, legacy, GENERAL + ".ownerOnlyWhenOnline", false,
                    "SERVER SIDE. When true, a Torcherino only accelerates while the player who placed it is online. Torcherinos without a recorded owner (placed before 1.4.1, or placed by a machine) keep running. Applies immediately, no restart needed.");
            changed |= put(cfg, legacy, GENERAL + ".statsLogIntervalSeconds", 0,
                    "SERVER SIDE. Seconds between two diagnostic log lines that report how much work the acceleration loop asked for (positions, extra random ticks, extra block entity ticks, skipped sections, brake divisor, remaining global budget). 0 = silent (default). Applies immediately, no restart needed.");
            changed |= put(cfg, legacy, BLACKLIST + ".blacklistedBlocks", new ArrayList<String>(),
                    "Blocks the Torcherino may not accelerate. Format: modid:unlocalized");
            changed |= put(cfg, legacy, BLACKLIST + ".blacklistedTiles", new ArrayList<String>(),
                    "BlockEntity classes the Torcherino may not accelerate. Format: Fully qualified class name");
            if (changed) {
                cfg.save();
            }
            overPoweredRecipe = bool(cfg, GENERAL + ".overPoweredRecipe", true);
            compressedTorcherino = bool(cfg, GENERAL + ".compressedTorcherino", true);
            doubleCompressedTorcherino = bool(cfg, GENERAL + ".doubleCompressedTorcherino", true);
            tripleCompressedTorcherino = bool(cfg, GENERAL + ".tripleCompressedTorcherino", true);
            maxAcceleratedTicksEnabled = bool(cfg, GENERAL + ".maxAcceleratedTicksEnabled", false);
            maxAcceleratedTicksPerTick = integer(cfg, GENERAL + ".maxAcceleratedTicksPerTick", 16384);
            maxAcceleratedTicksGlobalEnabled = bool(cfg, GENERAL + ".maxAcceleratedTicksGlobalEnabled", false);
            maxAcceleratedTicksPerTickGlobal = integer(cfg, GENERAL + ".maxAcceleratedTicksPerTickGlobal", 65536);
            adaptiveThrottle = bool(cfg, GENERAL + ".adaptiveThrottle", false);
            adaptiveThrottleThresholdMs = integer(cfg, GENERAL + ".adaptiveThrottleThresholdMs", 50);
            adaptiveThrottleMaxDivisor = integer(cfg, GENERAL + ".adaptiveThrottleMaxDivisor", 256);
            skipSaturatedPositions = bool(cfg, GENERAL + ".skipSaturatedPositions", false);
            probabilisticRandomTick = bool(cfg, GENERAL + ".probabilisticRandomTick", true);
            ownerOnlyWhenOnline = bool(cfg, GENERAL + ".ownerOnlyWhenOnline", false);
            statsLogIntervalSeconds = integer(cfg, GENERAL + ".statsLogIntervalSeconds", 0);
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

    private static int integer(CommentedFileConfig cfg, String key, int fallback) {
        final Object value = cfg.get(key);
        return value instanceof Number number ? number.intValue() : fallback;
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
