package com.sci.torcherino;

import com.sci.torcherino.platform.Services;

/**
 * Common entry point. Invoked by the loader entry point once the registry window is
 * open, so that every {@code ModBlocks} field already holds a live instance.
 */
public final class Torcherino {

    private Torcherino() {
    }

    public static void init() {
        TorcherinoConfig.load();
        TorcherinoRegistry.registerDefaults();
        Constants.LOG.info("Torcherino {} initialised on {}",
                Services.PLATFORM.getEnvironmentName(), Services.PLATFORM.getPlatformName());
    }

    /**
     * Re-reads the configuration and rebuilds the default blacklist.
     */
    public static void reload() {
        TorcherinoConfig.reload();
        TorcherinoRegistry.registerDefaults();
    }
}
