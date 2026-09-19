/*
 * 本文件：Fabric 侧 Mod Menu 集成（可选前置）。
 * 说明：把模组的配置界面挂到 Mod Menu 的「配置」按钮上；未安装 Mod Menu 时本类不会被加载，模组照常运行。
 *      Forge/NeoForge 侧的对等物是 Configured（见 TorcherinoForge 里的 ConfigScreenHandler 注册）。
 */
package com.sci.torcherino.client;

import com.sci.torcherino.client.screen.TorcherinoConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Hooks {@link TorcherinoConfigScreen} into Mod Menu.
 */
public class TorcherinoModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TorcherinoConfigScreen::new;
    }
}
