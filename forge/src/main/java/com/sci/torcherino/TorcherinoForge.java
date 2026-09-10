package com.sci.torcherino;

import com.sci.torcherino.blocks.ModBlockEntities;
import com.sci.torcherino.blocks.ModBlocks;
import com.sci.torcherino.client.TorcherinoForgeClientEvents;
import com.sci.torcherino.network.TorcherinoNetwork;
import com.sci.torcherino.platform.ForgeRegistrationHelper;
import com.sci.torcherino.platform.Services;

import net.minecraftforge.common.MinecraftForge;
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
            MinecraftForge.EVENT_BUS.addListener(TorcherinoForgeClientEvents::onClientTick);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(Torcherino::init);
    }
}
