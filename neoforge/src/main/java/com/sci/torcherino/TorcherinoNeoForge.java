/*
 * 本文件：NeoForge 侧模组入口。
 * 说明：构造期只登记延迟注册器、网络与事件监听；客户端监听放在 Dist 判断里，
 *      避免专用服务端加载客户端类。真正的实例化推迟到 FMLCommonSetup。
 */
package com.sci.torcherino;

import com.sci.torcherino.blocks.ModBlockEntities;
import com.sci.torcherino.blocks.ModBlocks;
import com.sci.torcherino.client.TorcherinoNeoForgeClient;
import com.sci.torcherino.network.TorcherinoNetwork;
import com.sci.torcherino.platform.NeoForgeRegistrationHelper;
import com.sci.torcherino.platform.Services;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * NeoForge entry point.
 */
@Mod(Constants.MOD_ID)
public class TorcherinoNeoForge {

    public TorcherinoNeoForge(IEventBus modEventBus, ModContainer container) {
        NeoForgeRegistrationHelper.init(modEventBus);

        ModBlocks.register(Services.REGISTRATION);
        ModBlockEntities.register(Services.REGISTRATION);

        modEventBus.addListener(TorcherinoNetwork::register);
        TorcherinoNeoForgeConfig.register(container);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(TorcherinoNeoForgeEvents::onBuildCreativeTabContents);

        NeoForge.EVENT_BUS.addListener(TorcherinoNeoForgeEvents::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(TorcherinoNeoForgeEvents::onPlayerLoggedOut);

        // Client only wiring behind a dist guard, so the client classes are never loaded on
        // a dedicated server. 26.x forbids the static FMLLoader.getDist() call; the public
        // FMLEnvironment field is the replacement.
        if (FMLEnvironment.getDist().isClient()) {
            TorcherinoNeoForgeClient.init(container);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(Torcherino::init);
    }
}
