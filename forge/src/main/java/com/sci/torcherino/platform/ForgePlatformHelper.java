package com.sci.torcherino.platform;

import com.sci.torcherino.platform.services.IPlatformHelper;
import com.sci.torcherino.network.TorcherinoNetwork;

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

    @Override
    public void sendModifierKeyToServer(boolean pressed) {
        TorcherinoNetwork.sendToServer(pressed);
    }
}
