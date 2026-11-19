/*
 * 本文件：NeoForge 侧的配置注册。
 * 说明：两个文件（CLIENT / SERVER）交给容器登记，事件里把值同步回 TorcherinoConfig 的静态字段。
 */
package com.sci.torcherino;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;

/**
 * Registers the shared {@link TorcherinoConfigSpec} with NeoForge and keeps the static
 * fields in step with the tracked files.
 */
public final class TorcherinoNeoForgeConfig {

    private TorcherinoNeoForgeConfig() {
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, TorcherinoConfigSpec.Client.SPEC,
                TorcherinoConfig.CLIENT_FILE_NAME);
        container.registerConfig(TorcherinoConfigSpec.serverType(), TorcherinoConfigSpec.Server.SPEC,
                TorcherinoConfig.SERVER_FILE_NAME);
        container.getEventBus().addListener(TorcherinoNeoForgeConfig::onLoading);
        container.getEventBus().addListener(TorcherinoNeoForgeConfig::onReloading);
    }

    private static void onLoading(ModConfigEvent.Loading event) {
        TorcherinoConfigSpec.apply(event.getConfig());
    }

    private static void onReloading(ModConfigEvent.Reloading event) {
        TorcherinoConfigSpec.apply(event.getConfig());
    }
}
