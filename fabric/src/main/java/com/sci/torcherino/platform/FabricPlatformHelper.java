/*
 * 本文件：IPlatformHelper 的 Fabric 实现。
 * 说明：用 FabricLoader 回答平台名 / 模组是否加载 / 是否开发环境 / 配置目录，并把发包动作转交给 Fabric 网络类。
 */
package com.sci.torcherino.platform;

import com.sci.torcherino.blocks.tiles.TileTorcherino;
import com.sci.torcherino.network.TorcherinoFabricNetworking;
import com.sci.torcherino.platform.services.IPlatformHelper;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

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

    @Override
    public void openTorcherinoScreen(ServerPlayer player, TileTorcherino torcherino) {
        TorcherinoFabricNetworking.openScreen(player, torcherino);
    }

    @Override
    public void sendTorcherinoValues(BlockPos pos, int xRange, int zRange, int yRange, int speed, int redstoneMode) {
        TorcherinoFabricNetworking.sendValues(pos, xRange, zRange, yRange, speed, redstoneMode);
    }
}
