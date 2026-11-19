/*
 * 本文件：IPlatformHelper 的 NeoForge 实现。
 * 说明：平台信息走 FML 的公开入口（26.x 禁止静态调 FMLLoader.getDist），发包转交 payload 体系。
 */
package com.sci.torcherino.platform;

import com.sci.torcherino.blocks.tiles.TileTorcherino;
import com.sci.torcherino.network.TorcherinoNetwork;
import com.sci.torcherino.network.TorcherinoPayloads;
import com.sci.torcherino.network.TorcherinoServerHandlers;
import com.sci.torcherino.platform.services.IPlatformHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.ModList;

/**
 * NeoForge implementation of the platform service.
 */
public class NeoForgePlatformHelper implements IPlatformHelper {

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
        return !FMLEnvironment.isProduction();
    }

    @Override
    public void sendModifierKeyToServer(boolean pressed) {
        TorcherinoNetwork.sendToServer(new TorcherinoPayloads.ModifierKey(pressed));
    }

    @Override
    public void openTorcherinoScreen(ServerPlayer player, TileTorcherino torcherino) {
        TorcherinoNetwork.sendToPlayer(player, TorcherinoServerHandlers.openScreenPacket(player, torcherino));
    }

    @Override
    public void sendTorcherinoValues(BlockPos pos, int xRange, int zRange, int yRange, int speed, int redstoneMode) {
        TorcherinoNetwork.sendToServer(new TorcherinoPayloads.SetValues(pos, xRange, zRange, yRange, speed, redstoneMode));
    }

    @Override
    public void sendTorcherinoOwnerSettings(BlockPos pos, boolean othersCanEdit, boolean unclaim) {
        TorcherinoNetwork.sendToServer(new TorcherinoPayloads.OwnerSettings(pos, othersCanEdit, unclaim));
    }
}
