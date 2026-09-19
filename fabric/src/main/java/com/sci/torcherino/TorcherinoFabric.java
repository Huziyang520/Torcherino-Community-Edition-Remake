/*
 * 本文件：Fabric 侧模组入口。
 * 说明：onInitialize 里依次做方块 / 方块实体 / 网络 / 事件注册，最后调用 Torcherino.init()（此时注册表尚未冻结，可以立即构造实例）。
 */
package com.sci.torcherino;

import com.sci.torcherino.blocks.ModBlockEntities;
import com.sci.torcherino.blocks.ModBlocks;
import com.sci.torcherino.network.TorcherinoFabricNetworking;
import com.sci.torcherino.platform.Services;

import net.fabricmc.api.ModInitializer;

/**
 * Fabric entry point. Registration happens first so that every {@code ModBlocks} field
 * is populated before {@link Torcherino#init()} reads the blacklist.
 */
public class TorcherinoFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModBlocks.register(Services.REGISTRATION);
        ModBlockEntities.register(Services.REGISTRATION);
        TorcherinoFabricNetworking.registerServerReceivers();
        TorcherinoFabricEvents.register();
        Torcherino.init();
    }
}
