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

        static {
            final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
            builder.comment("Client side preferences of the Torcherino editor.").push("gui");
            USE_GUI = builder
                    .comment("Open the graphical editor when right clicking a Torcherino. Set to false to use the classic quick interaction instead; only one of the two is active.")
                    .define("useGui", true);
            FREE_SPEED_MULTIPLIER = builder
                    .comment("Offer every hundredth of a level on the speed slider, so the multiplier can be dialled anywhere between 0 % and the tier maximum. Off = the classic eight gears. Only the speed slider is affected; the three ranges always count whole blocks.")
                    .define("freeSpeedMultiplier", false);
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
        private static final ForgeConfigSpec.IntValue MAX_TICKS_PER_BLOCK;
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
                    .define("compressedTorcherino", false);
            DOUBLE_COMPRESSED = builder
                    .comment("Enable the recipes of the Double Compressed variants. Only takes effect if compressedTorcherino is enabled as well.")
                    .define("doubleCompressedTorcherino", false);
            TRIPLE_COMPRESSED = builder
                    .comment("Enable the recipes of the Triple Compressed variants. Only takes effect if compressedTorcherino and doubleCompressedTorcherino are enabled as well.")
                    .define("tripleCompressedTorcherino", false);
            MAX_TICKS_PER_BLOCK = builder
                    .comment("How many extra ticks one block may receive per game tick. A machine ticked hundreds of times inside a single game tick finishes its progress bar before you can see it; a smaller value keeps the progress animation visible and costs less server time.")
                    .defineInRange("maxTicksPerBlock", 20, 1, 1000);
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
            return;
        }
        if (config.getSpec() == Server.SPEC) {
            TorcherinoConfig.overPoweredRecipe = Server.OVER_POWERED_RECIPE.get();
            TorcherinoConfig.compressedTorcherino = Server.COMPRESSED.get();
            TorcherinoConfig.doubleCompressedTorcherino = Server.DOUBLE_COMPRESSED.get();
            TorcherinoConfig.tripleCompressedTorcherino = Server.TRIPLE_COMPRESSED.get();
            TorcherinoConfig.maxTicksPerBlock = Server.MAX_TICKS_PER_BLOCK.get();
            TorcherinoConfig.blacklistedBlocks = new ArrayList<>(Server.BLACKLISTED_BLOCKS.get());
            TorcherinoConfig.blacklistedTiles = new ArrayList<>(Server.BLACKLISTED_TILES.get());
            TorcherinoRegistry.registerDefaults();
        }
    }
}
