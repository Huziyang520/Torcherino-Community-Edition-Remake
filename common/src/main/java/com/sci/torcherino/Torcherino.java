/*
 * 本文件：common 侧的统一初始化入口。
 * 说明：由各加载器入口在「注册窗口已打开」时调用，负责读取配置并重建默认黑名单；reload() 供配置重载复用。
 */
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
