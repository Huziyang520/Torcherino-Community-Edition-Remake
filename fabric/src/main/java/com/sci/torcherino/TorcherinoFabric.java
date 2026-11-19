/*
 * 本文件：Fabric 侧模组入口。
 * 说明：onInitialize 里先登记配置（Forge Config API Port 承接两个 TOML），再做方块 / 方块实体 /
 *      网络 / 事件注册，最后调用 Torcherino.init()。
 */
package com.sci.torcherino;

import com.google.gson.JsonObject;
import com.sci.torcherino.blocks.ModBlockEntities;
import com.sci.torcherino.blocks.ModBlocks;
import com.sci.torcherino.network.TorcherinoFabricNetworking;
import com.sci.torcherino.platform.Services;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Fabric entry point. Registration happens first so that every {@code ModBlocks} field
 * is populated before {@link Torcherino#init()} reads the blacklist.
 */
public class TorcherinoFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        registerConfig();
        ModBlocks.register(Services.REGISTRATION);
        ModBlockEntities.register(Services.REGISTRATION);
        registerRecipeConditions();
        TorcherinoFabricNetworking.registerServerReceivers();
        TorcherinoFabricEvents.register();
        Torcherino.init();
    }

    /**
     * Hands both files to Forge Config API Port, the config system this loader runs on, and
     * mirrors every loaded or edited file into {@link TorcherinoConfig}.
     */
    private static void registerConfig() {
        ForgeConfigRegistry.INSTANCE.register(Constants.MOD_ID, ModConfig.Type.CLIENT,
                TorcherinoConfigSpec.Client.SPEC, TorcherinoConfig.CLIENT_FILE_NAME);
        ForgeConfigRegistry.INSTANCE.register(Constants.MOD_ID, ModConfig.Type.COMMON,
                TorcherinoConfigSpec.Server.SPEC, TorcherinoConfig.SERVER_FILE_NAME);
        ModConfigEvents.loading(Constants.MOD_ID).register(TorcherinoConfigSpec::apply);
        ModConfigEvents.reloading(Constants.MOD_ID).register(TorcherinoConfigSpec::apply);
    }

    /**
     * Recipe condition used by {@code data/torcherino/recipes}.
     *
     * <p>Fabric evaluates {@code fabric:load_conditions} while the data pack loads. An id
     * that nobody registered makes Fabric log "Unknown recipe condition" and skip the
     * recipe, which is what happened to the two Torcherino recipes before this.</p>
     */
    private static void registerRecipeConditions() {
        ResourceConditions.register(new ResourceLocation(Constants.MOD_ID, "config"),
                TorcherinoFabric::matchesConfigFlag);
    }

    private static boolean matchesConfigFlag(JsonObject json) {
        return TorcherinoConfig.flag(GsonHelper.getAsString(json, "config"))
                == GsonHelper.getAsBoolean(json, "value");
    }
}
