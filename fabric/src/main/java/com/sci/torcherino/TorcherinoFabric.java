/*
 * 本文件：Fabric 侧模组入口。
 * 说明：onInitialize 里依次做方块 / 方块实体 / 网络 / 事件注册，注册配方条件，最后调用 Torcherino.init()
 *      （此时注册表尚未冻结，可以立即构造实例）。
 */
package com.sci.torcherino;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sci.torcherino.blocks.ModBlockEntities;
import com.sci.torcherino.blocks.ModBlocks;
import com.sci.torcherino.network.TorcherinoFabricNetworking;
import com.sci.torcherino.platform.Services;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;

/**
 * Fabric entry point. Registration happens first so that every {@code ModBlocks} field
 * is populated before {@link Torcherino#init()} reads the blacklist.
 */
public class TorcherinoFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModBlocks.register(Services.REGISTRATION);
        ModBlockEntities.register(Services.REGISTRATION);
        registerRecipeCondition();
        TorcherinoFabricNetworking.registerPayloadTypes();
        TorcherinoFabricNetworking.registerServerReceivers();
        TorcherinoFabricEvents.register();
        Torcherino.init();
    }

    /**
     * Recipe condition used by {@code data/torcherino/recipes}.
     *
     * <p>Fabric evaluates {@code fabric:load_conditions} while the data pack loads. An id
     * that nobody registered makes Fabric log "Unknown recipe condition" and skip the
     * recipe, which is what happened to the two Torcherino recipes before this.</p>
     */
    private static void registerRecipeCondition() {
        ResourceConditions.register(ConfigCondition.TYPE);
    }

    /**
     * One boolean of the configuration, addressed by name. JSON shape used by the shipped
     * recipes:
     *
     * <pre>
     * "fabric:load_conditions": [
     *   { "condition": "torcherino:config", "config": "overPoweredRecipe", "value": true }
     * ]
     * </pre>
     */
    public record ConfigCondition(String config, boolean expected) implements ResourceCondition {

        public static final MapCodec<ConfigCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("config").forGetter(ConfigCondition::config),
                Codec.BOOL.fieldOf("value").forGetter(ConfigCondition::expected)
        ).apply(instance, ConfigCondition::new));

        public static final ResourceConditionType<ConfigCondition> TYPE = ResourceConditionType.create(
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "config"), CODEC);

        @Override
        public ResourceConditionType<?> getType() {
            return TYPE;
        }

        @Override
        public boolean test(HolderLookup.Provider registries) {
            return TorcherinoConfig.flag(this.config) == this.expected;
        }
    }
}
