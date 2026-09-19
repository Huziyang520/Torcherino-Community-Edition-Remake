/*
 * 本文件：IPlatformHelper 的 Forge 实现。
 * 说明：用 FMLLoader / ModList / FMLPaths 回答平台信息，并把发包动作转交给 Forge 网络类。
 */
package com.sci.torcherino.platform;

import com.sci.torcherino.blocks.tiles.TileTorcherino;
import com.sci.torcherino.network.TorcherinoNetwork;
import com.sci.torcherino.platform.services.IPlatformHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

/**
 * Forge implementation of the platform service.
 */
public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return net.minecraftforge.fml.loading.FMLLoader.getDist() != null
                && net.minecraftforge.fml.ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !net.minecraftforge.fml.loading.FMLLoader.isProduction();
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    /**
     * Forge owns the configuration files through {@code ForgeConfigSpec}, which is also the
     * only thing Configured can turn into a screen. The common TOML reader stays out of the
     * way on this loader.
     */
    @Override
    public boolean usesForgeConfigSystem() {
        return true;
    }

    @Override
    public void sendModifierKeyToServer(boolean pressed) {
        TorcherinoNetwork.sendToServer(pressed);
    }

    @Override
    public void openTorcherinoScreen(ServerPlayer player, TileTorcherino torcherino) {
        TorcherinoNetwork.openScreen(player, torcherino);
    }

    @Override
    public void sendTorcherinoValues(BlockPos pos, int xRange, int zRange, int yRange, int speed, int redstoneMode) {
        TorcherinoNetwork.sendValues(pos, xRange, zRange, yRange, speed, redstoneMode);
    }
}
