/*
 * 本文件：配置字段（加载器无关的唯一读写面）。
 * 说明：值的定义、落盘与界面都在 TorcherinoConfigSpec 里；本类只保留静态字段与常量，
 *      以 {@code gui} / {@code general} / {@code blacklist} 三段对应两个 TOML 文件。
 */
package com.sci.torcherino;

import java.util.ArrayList;
import java.util.List;

/**
 * Loader independent configuration values.
 *
 * <p>The split exists because the two halves have completely different owners and
 * lifetimes: {@code gui.*} is a local client preference, while the recipe switches and the
 * blacklist are read by the server when the data pack loads.</p>
 */
public final class TorcherinoConfig {

    /**
     * Bumped whenever a shipped default changes. Only used to migrate an existing file once:
     * a value below this one triggers the migration in {@link TorcherinoConfigSpec#apply} and
     * is then written back, so it never runs twice.
     */
    public static final int CONFIG_VERSION = 2;

    /** The jukebox block entity restarts its song on every tick, so it is never accelerated. */
    public static final String JUKEBOX_BLOCK = "minecraft:jukebox";

    /** Client preferences. Never read by a dedicated server. */
    public static final String CLIENT_FILE_NAME = Constants.MOD_ID + "-client.toml";
    /** Server behaviour: recipe switches and the blacklist. */
    public static final String SERVER_FILE_NAME = Constants.MOD_ID + "-server.toml";

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
     * Owner system master switch, server side. {@code false} (default) means placing a
     * Torcherino never claims it: no owner is recorded, the editor shows no owner row and no
     * owner buttons, and everybody may edit. Already claimed Torcherinos keep their stored
     * owner (the data is never cleared); they are simply ignored while the switch is off, so
     * turning it back on restores the old claims.
     */
    public static boolean ownerSystem = false;
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
}
