/*
 * 本文件：Forge 侧的配置注册。
 * 说明：两个文件（CLIENT / COMMON）交给 Forge 登记，事件里把值同步回 TorcherinoConfig 的静态字段。
 */
package com.sci.torcherino;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Registers the shared {@link TorcherinoConfigSpec} with Forge and keeps the static fields
 * in step with the tracked files.
 */
public final class TorcherinoForgeConfig {

    private TorcherinoForgeConfig() {
    }

    public static void register(IEventBus modEventBus) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, TorcherinoConfigSpec.Client.SPEC,
                TorcherinoConfig.CLIENT_FILE_NAME);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TorcherinoConfigSpec.Server.SPEC,
                TorcherinoConfig.SERVER_FILE_NAME);
        modEventBus.addListener(TorcherinoForgeConfig::onLoading);
        modEventBus.addListener(TorcherinoForgeConfig::onReloading);
    }

    private static void onLoading(ModConfigEvent.Loading event) {
        TorcherinoConfigSpec.apply(event.getConfig());
    }

    private static void onReloading(ModConfigEvent.Reloading event) {
        TorcherinoConfigSpec.apply(event.getConfig());
    }
}
