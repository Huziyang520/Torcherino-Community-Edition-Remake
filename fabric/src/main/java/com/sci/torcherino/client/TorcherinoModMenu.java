/*
 * 本文件：Fabric 侧 Mod Menu 集成（可选前置）。
 * 说明：把模组的配置界面挂到 Mod Menu 的「配置」按钮上；未安装 Mod Menu 时本类不会被加载，模组照常运行。
 *      配置界面由可选的 Cloth Config API 构建，未安装时返回空，Mod Menu 自己就不显示按钮。
 *      Forge/NeoForge 侧的对等物是 Configured + 模组列表的配置按钮（见 TorcherinoForge）。
 */
package com.sci.torcherino.client;

import com.sci.torcherino.client.screen.TorcherinoClothConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Hooks the configuration screen into Mod Menu.
 *
 * <p>The screen is built with Cloth Config, which is an optional integration: when the API
 * is not installed this returns {@code null}, which is also what the default implementation
 * of {@link ModMenuApi} does, so Mod Menu simply shows no config button for this mod.</p>
 */
public class TorcherinoModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> isClothConfigLoaded() ? TorcherinoClothConfig.create(parent) : null;
    }

    private static boolean isClothConfigLoaded() {
        final FabricLoader loader = FabricLoader.getInstance();
        return loader.isModLoaded("cloth-config") || loader.isModLoaded("cloth_config");
    }
}
