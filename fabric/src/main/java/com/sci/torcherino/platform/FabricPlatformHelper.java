/*
 * 本文件：IPlatformHelper 的 Fabric 实现。
 * 说明：用 FabricLoader 回答平台名 / 模组是否加载 / 是否开发环境 / 配置目录，并把发包动作转交给 Fabric 网络类。
 */
package com.sci.torcherino.platform;

import com.sci.torcherino.platform.services.IRegistrationHelper;
import com.sci.torcherino.platform.services.IPlatformHelper;
import com.sci.torcherino.network.TorcherinoFabricNetworking;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

/**
 * Fabric implementation of the platform service.
 */
public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public void sendModifierKeyToServer(boolean pressed) {
        TorcherinoFabricNetworking.sendModifierKey(pressed);
    }
}
