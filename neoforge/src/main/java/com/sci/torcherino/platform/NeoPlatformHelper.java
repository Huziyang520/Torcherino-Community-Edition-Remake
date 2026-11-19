/*
 * 本文件：IPlatformHelper 的 NeoForge 实现。
 * 说明：用 FML 的公开入口回答平台信息，并把发包动作转交给 NeoForge 网络类。
 */
package com.sci.torcherino.platform;

import com.sci.torcherino.blocks.tiles.TileTorcherino;
import com.sci.torcherino.network.TorcherinoNeoNetwork;
import com.sci.torcherino.platform.services.IPlatformHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

/**
 * NeoForge implementation of the platform service.
 */
public class NeoPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get() != null && ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public void sendModifierKeyToServer(boolean pressed) {
        TorcherinoNeoNetwork.sendToServer(pressed);
    }

    @Override
    public void openTorcherinoScreen(ServerPlayer player, TileTorcherino torcherino) {
        TorcherinoNeoNetwork.openScreen(player, torcherino);
    }

    @Override
    public void sendTorcherinoValues(BlockPos pos, int xRange, int zRange, int yRange, int speed, int redstoneMode) {
        TorcherinoNeoNetwork.sendValues(pos, xRange, zRange, yRange, speed, redstoneMode);
    }

    @Override
    public void sendTorcherinoOwnerSettings(BlockPos pos, boolean othersCanEdit, boolean unclaim) {
        TorcherinoNeoNetwork.sendOwnerSettings(pos, othersCanEdit, unclaim);
    }
}
