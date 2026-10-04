/*
 * 本文件：Fabric 侧 Jade 插件入口（fabric.mod.json 的 "jade" entrypoint）。
 * 说明：Jade 通过 entrypoint 收集插件，因此未安装 Jade 时这个类不会被加载，模组照常运行。
 */
package com.sci.torcherino.compat.jade;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;

/**
 * Fabric entry point of the optional Jade integration, declared as the {@code jade}
 * entrypoint in {@code fabric.mod.json}.
 */
public class TorcherinoJadeFabricPlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        TorcherinoJadeProvider.register(registration);
    }
}
