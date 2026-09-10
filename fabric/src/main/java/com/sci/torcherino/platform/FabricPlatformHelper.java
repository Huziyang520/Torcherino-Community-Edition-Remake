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
