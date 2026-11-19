/*
 * 本文件：Fabric 侧 Mod Menu 集成。
 * 说明：把配置界面挂到 Mod Menu 的「配置」按钮上；界面本体来自 Forge Config API Port 携带的
 *      NeoForge ConfigurationScreen，键名与 NeoForge 侧完全一致。
 */
package com.sci.torcherino.client;

import com.sci.torcherino.Constants;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.neoforged.neoforge.client.gui.ConfigurationScreen;

/**
 * Hooks the configuration screen into Mod Menu.
 *
 * <p>The screen is the ported NeoForge one that comes with Forge Config API Port, so both
 * loaders show the same layout and read the same translation keys.</p>
 */
public class TorcherinoModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ConfigurationScreen(Constants.MOD_ID, parent);
    }
}
