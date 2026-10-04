/*
 * 本文件：Forge 侧配置（改用 Forge 配置体系 ForgeConfigSpec）。
 * 说明：声明两个配置 —— CLIENT（可视化编辑界面 / 加速倍率自由调节）与 COMMON（配方开关、黑名单、每刻补跑上限）。
 *      Configured 只识别 Forge 配置体系，因此只有走这里，本模组才会出现在它的「客户端 / 服务端配置」列表里并带一键重置。
 *      文件仍是 config/torcherino-client.toml 与 config/torcherino-server.toml。
 */
package com.sci.torcherino;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Forge configuration, split the same way the TOML reader does: a client file and a server
 * file. Registering it through {@code ForgeConfigSpec} is what makes Configured able to
 * render both files as an in-game screen with a reset button.
 */
public final class TorcherinoForgeConfig {

    private TorcherinoForgeConfig() {
    }

    /** Client side preferences; written to {@code torcherino-client.toml}. */
    public static final class Client {

        public static final ForgeConfigSpec SPEC;
        private static final ForgeConfigSpec.BooleanValue USE_GUI;
        private static final ForgeConfigSpec.BooleanValue FREE_SPEED_MULTIPLIER;
        private static final ForgeConfigSpec.BooleanValue JOIN_NOTICE;

        static {
            final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
            builder.comment("Client side preferences of the Torcherino editor.").push("gui");
            USE_GUI = builder
                    .comment("Open the graphical editor when right clicking a Torcherino. Set to false to use the classic quick interaction instead; only one of the two is active.")
                    .define("useGui", true);
            FREE_SPEED_MULTIPLIER = builder
                    .comment("Offer every hundredth of a level on the speed slider, so the multiplier can be dialled anywhere between 0 % and the tier maximum. Off = the classic eight gears. Only the speed slider is affected; the three ranges always count whole blocks.")
                    .define("freeSpeedMultiplier", false);
            JOIN_NOTICE = builder
                    .comment("Print a chat notice about the recipe switches every time a world is joined. Set to false to silence it.")
                    .define("joinNotice", true);
            builder.pop();
            SPEC = builder.build();
        }
    }

    /** Server side behaviour; written to {@code torcherino-server.toml}. */
    public static final class Server {

        public static final ForgeConfigSpec SPEC;
        private static final ForgeConfigSpec.BooleanValue OVER_POWERED_RECIPE;
        private static final ForgeConfigSpec.BooleanValue COMPRESSED;
        private static final ForgeConfigSpec.BooleanValue DOUBLE_COMPRESSED;
        private static final ForgeConfigSpec.BooleanValue TRIPLE_COMPRESSED;
        private static final ForgeConfigSpec.BooleanValue MAX_ACCELERATED_TICKS_ENABLED;
        private static final ForgeConfigSpec.IntValue MAX_ACCELERATED_TICKS_PER_TICK;
        private static final ForgeConfigSpec.BooleanValue MAX_ACCELERATED_TICKS_GLOBAL_ENABLED;
        private static final ForgeConfigSpec.IntValue MAX_ACCELERATED_TICKS_PER_TICK_GLOBAL;
        private static final ForgeConfigSpec.BooleanValue ADAPTIVE_THROTTLE;
        private static final ForgeConfigSpec.IntValue ADAPTIVE_THROTTLE_THRESHOLD_MS;
        private static final ForgeConfigSpec.IntValue ADAPTIVE_THROTTLE_MAX_DIVISOR;
        private static final ForgeConfigSpec.BooleanValue SKIP_SATURATED_POSITIONS;
        private static final ForgeConfigSpec.BooleanValue PROBABILISTIC_RANDOM_TICK;
        private static final ForgeConfigSpec.BooleanValue OWNER_ONLY_WHEN_ONLINE;
        private static final ForgeConfigSpec.IntValue STATS_LOG_INTERVAL_SECONDS;
        private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_BLOCKS;
        private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_TILES;

        static {
            final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
            builder.comment("Server side settings. Recipe switches are read while the data pack loads, so a change needs a restart or /reload.").push("general");
            OVER_POWERED_RECIPE = builder
                    .comment("Use the cheap recipe instead of the nether star one.")
                    .define("overPoweredRecipe", true);
            COMPRESSED = builder
                    .comment("Enable the recipes of the Compressed Torcherino and Jack o'Lanterino.")
                    .define("compressedTorcherino", true);
            DOUBLE_COMPRESSED = builder
                    .comment("Enable the recipes of the Double Compressed variants. Only takes effect if compressedTorcherino is enabled as well.")
                    .define("doubleCompressedTorcherino", true);
            TRIPLE_COMPRESSED = builder
                    .comment("Enable the recipes of the Triple Compressed variants. Only takes effect if compressedTorcherino and doubleCompressedTorcherino are enabled as well.")
                    .define("tripleCompressedTorcherino", true);
            MAX_ACCELERATED_TICKS_ENABLED = builder
                    .comment("Switch of the per Torcherino cap below. false (default) leaves the acceleration uncapped, exactly like 1.4.0 and earlier.")
                    .define("maxAcceleratedTicksEnabled", false);
            MAX_ACCELERATED_TICKS_PER_TICK = builder
                    .comment("Maximum acceleration calls (extra random ticks plus block entity ticks) one Torcherino may spend per server tick. Only read while maxAcceleratedTicksEnabled is true; the default is the recommended starting point, and 0 still means unlimited. The scan starts at a rotating section, so every part of the area still gets accelerated.")
                    .defineInRange("maxAcceleratedTicksPerTick", 16384, 0, Integer.MAX_VALUE);
            ADAPTIVE_THROTTLE = builder
                    .comment("Adaptive brake. false (default) always plays back the configured speed, exactly like every release up to now. true divides the acceleration of every Torcherino while the server tick is slower than adaptiveThrottleThresholdMs and returns to full speed as soon as it recovers, so a large area or a high compression tier cannot make the server unplayable.")
                    .define("adaptiveThrottle", false);
            ADAPTIVE_THROTTLE_THRESHOLD_MS = builder
                    .comment("Tick duration in milliseconds above which the adaptive brake starts scaling the acceleration down. 50 ms is the TPS 20 warning line; raise it on a heavy modpack whose healthy tick is already longer. Only read while adaptiveThrottle is true.")
                    .defineInRange("adaptiveThrottleThresholdMs", 50, 10, 10000);
            ADAPTIVE_THROTTLE_MAX_DIVISOR = builder
                    .comment("Upper bound of the adaptive brake divisor, so a completely stuck server cannot scale the acceleration down to nothing. Only read while adaptiveThrottle is true.")
                    .defineInRange("adaptiveThrottleMaxDivisor", 256, 1, 100000);
            MAX_ACCELERATED_TICKS_GLOBAL_ENABLED = builder
                    .comment("Switch of the server wide cap below. false (default) leaves it unused, so only the per Torcherino cap can apply.")
                    .define("maxAcceleratedTicksGlobalEnabled", false);
            MAX_ACCELERATED_TICKS_PER_TICK_GLOBAL = builder
                    .comment("Maximum acceleration calls the whole mod may spend per server tick, shared by every Torcherino and divided over the ones that scanned in the previous tick. Only read while maxAcceleratedTicksGlobalEnabled is true; the default is the recommended starting point, and 0 still means unlimited.")
                    .defineInRange("maxAcceleratedTicksPerTickGlobal", 65536, 0, Integer.MAX_VALUE);
            SKIP_SATURATED_POSITIONS = builder
                    .comment("LOWER RATE, OFF BY DEFAULT. When true, a position whose block and six neighbouring blocks stayed the same for a couple of ticks is accelerated only once per tick until something around it changes. Saturated farms stop consuming the whole tick, but stable blocks are no longer played back at the configured speed. Positions on the border of a chunk section are never throttled.")
                    .define("skipSaturatedPositions", false);
            PROBABILISTIC_RANDOM_TICK = builder
                    .comment("DIFFERENT MODEL, ON BY DEFAULT since 1.4.1. When true, a randomly ticking position receives a single random tick with the probability rounds * randomTickSpeed / 4096 instead of 'rounds' calls, which is the vanilla random tick model scaled by the configured multiplier. The expected amount of growth matches, while the cost per tick is proportional to the number of positions instead of positions x multiplier. Set it to false for the call by call behaviour of the older releases.")
                    .define("probabilisticRandomTick", true);
            OWNER_ONLY_WHEN_ONLINE = builder
                    .comment("When true, a Torcherino only accelerates while the player who placed it is online. Torcherinos without a recorded owner (placed before 1.4.1, or placed by a machine) keep running.")
                    .define("ownerOnlyWhenOnline", false);
            STATS_LOG_INTERVAL_SECONDS = builder
                    .comment("Seconds between two diagnostic log lines that report how much work the acceleration loop asked for. 0 = silent (default).")
                    .defineInRange("statsLogIntervalSeconds", 0, 0, 86400);
            builder.pop();
            builder.comment("Things the Torcherino may never accelerate.").push("blacklist");
            BLACKLISTED_BLOCKS = builder
                    .comment("Blocks that must not be accelerated. Format: modid:unlocalized")
                    .defineListAllowEmpty("blacklistedBlocks", List.of(), element -> element instanceof String);
            BLACKLISTED_TILES = builder
                    .comment("BlockEntity classes that must not be accelerated. Format: fully qualified class name")
                    .defineListAllowEmpty("blacklistedTiles", List.of(), element -> element instanceof String);
            builder.pop();
            SPEC = builder.build();
        }
    }

    /**
     * Registers both files and hooks the value transfer into the loader independent
     * {@link TorcherinoConfig} fields that the rest of the mod reads.
     */
    public static void register(net.minecraftforge.eventbus.api.IEventBus modEventBus) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Client.SPEC, TorcherinoConfig.CLIENT_FILE_NAME);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Server.SPEC, TorcherinoConfig.SERVER_FILE_NAME);
        modEventBus.addListener(TorcherinoForgeConfig::onLoading);
        modEventBus.addListener(TorcherinoForgeConfig::onReloading);
    }

    private static void onLoading(net.minecraftforge.fml.event.config.ModConfigEvent.Loading event) {
        apply(event.getConfig());
    }

    private static void onReloading(net.minecraftforge.fml.event.config.ModConfigEvent.Reloading event) {
        apply(event.getConfig());
    }

    /** Copies the values the player edited (or reset) into the shared fields. */
    private static void apply(ModConfig config) {
        if (config.getSpec() == Client.SPEC) {
            TorcherinoConfig.useGui = Client.USE_GUI.get();
            TorcherinoConfig.freeSpeedMultiplier = Client.FREE_SPEED_MULTIPLIER.get();
            TorcherinoConfig.joinNotice = Client.JOIN_NOTICE.get();
            return;
        }
        if (config.getSpec() == Server.SPEC) {
            TorcherinoConfig.overPoweredRecipe = Server.OVER_POWERED_RECIPE.get();
            TorcherinoConfig.compressedTorcherino = Server.COMPRESSED.get();
            TorcherinoConfig.doubleCompressedTorcherino = Server.DOUBLE_COMPRESSED.get();
            TorcherinoConfig.tripleCompressedTorcherino = Server.TRIPLE_COMPRESSED.get();
            TorcherinoConfig.maxAcceleratedTicksEnabled = Server.MAX_ACCELERATED_TICKS_ENABLED.get();
            TorcherinoConfig.maxAcceleratedTicksPerTick = Server.MAX_ACCELERATED_TICKS_PER_TICK.get();
            TorcherinoConfig.maxAcceleratedTicksGlobalEnabled = Server.MAX_ACCELERATED_TICKS_GLOBAL_ENABLED.get();
            TorcherinoConfig.maxAcceleratedTicksPerTickGlobal = Server.MAX_ACCELERATED_TICKS_PER_TICK_GLOBAL.get();
            TorcherinoConfig.adaptiveThrottle = Server.ADAPTIVE_THROTTLE.get();
            TorcherinoConfig.adaptiveThrottleThresholdMs = Server.ADAPTIVE_THROTTLE_THRESHOLD_MS.get();
            TorcherinoConfig.adaptiveThrottleMaxDivisor = Server.ADAPTIVE_THROTTLE_MAX_DIVISOR.get();
            TorcherinoConfig.skipSaturatedPositions = Server.SKIP_SATURATED_POSITIONS.get();
            TorcherinoConfig.probabilisticRandomTick = Server.PROBABILISTIC_RANDOM_TICK.get();
            TorcherinoConfig.ownerOnlyWhenOnline = Server.OWNER_ONLY_WHEN_ONLINE.get();
            TorcherinoConfig.statsLogIntervalSeconds = Server.STATS_LOG_INTERVAL_SECONDS.get();
            TorcherinoConfig.blacklistedBlocks = new ArrayList<>(Server.BLACKLISTED_BLOCKS.get());
            TorcherinoConfig.blacklistedTiles = new ArrayList<>(Server.BLACKLISTED_TILES.get());
            TorcherinoRegistry.registerDefaults();
        }
    }

    /**
     * Writes the shared client fields back into the spec, which is how the in-game
     * configuration screen saves on Forge.
     */
    public static void persistClient() {
        Client.USE_GUI.set(TorcherinoConfig.useGui);
        Client.FREE_SPEED_MULTIPLIER.set(TorcherinoConfig.freeSpeedMultiplier);
        Client.JOIN_NOTICE.set(TorcherinoConfig.joinNotice);
        Client.SPEC.save();
        Constants.LOG.info("Saved the Torcherino client configuration");
    }

    /** Counterpart of {@link #persistClient()} for the server file. */
    public static void persistServer() {
        Server.OVER_POWERED_RECIPE.set(TorcherinoConfig.overPoweredRecipe);
        Server.COMPRESSED.set(TorcherinoConfig.compressedTorcherino);
        Server.DOUBLE_COMPRESSED.set(TorcherinoConfig.doubleCompressedTorcherino);
        Server.TRIPLE_COMPRESSED.set(TorcherinoConfig.tripleCompressedTorcherino);
        Server.MAX_ACCELERATED_TICKS_ENABLED.set(TorcherinoConfig.maxAcceleratedTicksEnabled);
        Server.MAX_ACCELERATED_TICKS_PER_TICK.set(TorcherinoConfig.maxAcceleratedTicksPerTick);
        Server.MAX_ACCELERATED_TICKS_GLOBAL_ENABLED.set(TorcherinoConfig.maxAcceleratedTicksGlobalEnabled);
        Server.MAX_ACCELERATED_TICKS_PER_TICK_GLOBAL.set(TorcherinoConfig.maxAcceleratedTicksPerTickGlobal);
        Server.ADAPTIVE_THROTTLE.set(TorcherinoConfig.adaptiveThrottle);
        Server.ADAPTIVE_THROTTLE_THRESHOLD_MS.set(TorcherinoConfig.adaptiveThrottleThresholdMs);
        Server.ADAPTIVE_THROTTLE_MAX_DIVISOR.set(TorcherinoConfig.adaptiveThrottleMaxDivisor);
        Server.SKIP_SATURATED_POSITIONS.set(TorcherinoConfig.skipSaturatedPositions);
        Server.PROBABILISTIC_RANDOM_TICK.set(TorcherinoConfig.probabilisticRandomTick);
        Server.OWNER_ONLY_WHEN_ONLINE.set(TorcherinoConfig.ownerOnlyWhenOnline);
        Server.STATS_LOG_INTERVAL_SECONDS.set(TorcherinoConfig.statsLogIntervalSeconds);
        // The two lists are editable in the configuration screen as well.
        Server.BLACKLISTED_BLOCKS.set(new ArrayList<>(TorcherinoConfig.blacklistedBlocks));
        Server.BLACKLISTED_TILES.set(new ArrayList<>(TorcherinoConfig.blacklistedTiles));
        Server.SPEC.save();
        Constants.LOG.info("Saved the Torcherino server configuration");
    }
}
