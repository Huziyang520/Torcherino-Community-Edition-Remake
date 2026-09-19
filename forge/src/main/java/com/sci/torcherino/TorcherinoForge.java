/*
 * 本文件：Forge 侧模组入口。
 * 说明：构造期只登记延迟注册器与事件监听（客户端监听放在 Dist 判断里，避免专用服务端加载客户端类），真正的实例化推迟到 FMLCommonSetup。
 */
package com.sci.torcherino;

import com.sci.torcherino.blocks.ModBlockEntities;
import com.sci.torcherino.blocks.ModBlocks;
import com.sci.torcherino.client.TorcherinoForgeClientEvents;
import com.sci.torcherino.network.TorcherinoNetwork;
import com.sci.torcherino.platform.ForgeRegistrationHelper;
import com.sci.torcherino.platform.Services;
import com.sci.torcherino.platform.TorcherinoConfigCondition;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

/**
 * Forge entry point.
 */
@Mod(Constants.MOD_ID)
public class TorcherinoForge {

    public TorcherinoForge() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ForgeRegistrationHelper.init(modEventBus);

        // Recipe condition used by data/torcherino/recipes. Without it Forge logs
        // "Unknown condition type: torcherino:config" and drops the recipe entirely.
        // CraftingHelper.register only stores the serializer in a static map, so calling
        // it here (mod construction time) is fine.
        CraftingHelper.register(new TorcherinoConfigCondition.Serializer());

        ModBlocks.register(Services.REGISTRATION);
        ModBlockEntities.register(Services.REGISTRATION);
        TorcherinoNetwork.register();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(TorcherinoForgeEvents::onBuildCreativeTabContents);

        MinecraftForge.EVENT_BUS.addListener(TorcherinoForgeEvents::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(TorcherinoForgeEvents::onPlayerLoggedOut);

        // Client only listeners are registered behind a dist guard so that the client
        // only classes are never loaded on a dedicated server.
        if (FMLLoader.getDist().isClient()) {
            modEventBus.addListener(TorcherinoForgeClientEvents::onRegisterKeyMappings);
            modEventBus.addListener(TorcherinoForgeClientEvents::onClientSetup);
            MinecraftForge.EVENT_BUS.addListener(TorcherinoForgeClientEvents::onClientTick);

            // Lets the vanilla mod list and "Configured" show a config button. Registered
            // behind the dist guard because the handler class is client only.
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(
                            (minecraft, parent) -> TorcherinoForgeClientEvents.createConfigScreen(parent)));
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(Torcherino::init);
    }
}
