/*
 * 本文件：NeoForge 侧模组入口。
 * 说明：构造期只登记延迟注册器、配置、网络与事件监听（客户端监听放在 Dist 判断里，避免专用服务端加载客户端类），
 *      真正的实例化推迟到注册窗口；配置界面用 NeoForge 自带的 ConfigurationScreen。
 */
package com.sci.torcherino;

import com.sci.torcherino.blocks.ModBlockEntities;
import com.sci.torcherino.blocks.ModBlocks;
import com.sci.torcherino.client.TorcherinoNeoClientEvents;
import com.sci.torcherino.network.TorcherinoNeoNetwork;
import com.sci.torcherino.platform.NeoRegistrationHelper;
import com.sci.torcherino.platform.Services;
import com.sci.torcherino.platform.TorcherinoConfigCondition;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

/**
 * NeoForge entry point.
 */
@Mod(Constants.MOD_ID)
public class TorcherinoNeoForge {

    public TorcherinoNeoForge(IEventBus modEventBus, ModContainer container) {
        NeoRegistrationHelper.init(modEventBus);

        // Recipe condition used by data/torcherino/recipes. Without it NeoForge logs
        // "Unknown recipe condition" and drops the recipe entirely.
        TorcherinoConfigCondition.register(modEventBus);

        ModBlocks.register(Services.REGISTRATION);
        ModBlockEntities.register(Services.REGISTRATION);
        modEventBus.addListener(TorcherinoNeoNetwork::register);

        // Configuration through NeoForge's own config system: it is what the built in
        // ConfigurationScreen renders. Registering it here (mod construction time) is
        // required, the values arrive later through ModConfigEvent.
        TorcherinoNeoConfig.register(container, modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(TorcherinoNeoEvents::onBuildCreativeTabContents);

        NeoForge.EVENT_BUS.addListener(TorcherinoNeoEvents::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(TorcherinoNeoEvents::onPlayerLoggedOut);

        // Client only listeners are registered behind a dist guard so that the client
        // only classes are never loaded on a dedicated server.
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(TorcherinoNeoClientEvents::onClientSetup);
            // Chat notice about the recipe switches, printed once per world join.
            NeoForge.EVENT_BUS.addListener(TorcherinoNeoClientEvents::onLoggedIn);
            // The mod list config button, backed by NeoForge's ConfigurationScreen.
            container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(Torcherino::init);
    }
}
