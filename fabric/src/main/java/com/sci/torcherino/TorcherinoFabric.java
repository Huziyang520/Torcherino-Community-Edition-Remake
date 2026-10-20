/*
 * 本文件：Fabric 侧模组入口。
 * 说明：onInitialize 里依次做方块 / 方块实体 / 网络 / 事件注册，最后调用 Torcherino.init()
 *      （此时注册表尚未冻结，可以立即构造实例）。配方条件用 Fabric 自己的条件类型登记。
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
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;

/**
 * Fabric entry point. Registration happens first so that every {@code ModBlocks} field
 * is populated before {@link Torcherino#init()} reads the blacklist.
 */
public class TorcherinoFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModBlocks.register(Services.REGISTRATION);
        ModBlockEntities.register(Services.REGISTRATION);
        registerRecipeConditions();
        TorcherinoFabricNetworking.registerServerReceivers();
        TorcherinoFabricEvents.register();
        Torcherino.init();
    }

    /**
     * Recipe condition used by {@code data/torcherino/recipe}.
     *
     * <p>Fabric evaluates {@code fabric:load_conditions} while the data pack loads. An id
     * that nobody registered makes Fabric log "Unknown recipe condition" and skip the
     * recipe.</p>
     */
    private static void registerRecipeConditions() {
        ResourceConditions.register(ConfigCondition.TYPE);
    }

    /** {@code torcherino:config} condition, backed by one boolean of the configuration. */
    public record ConfigCondition(String config, boolean value) implements ResourceCondition {

        public static final MapCodec<ConfigCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("config").forGetter(ConfigCondition::config),
                Codec.BOOL.fieldOf("value").forGetter(ConfigCondition::value)
        ).apply(instance, ConfigCondition::new));

        public static final ResourceConditionType<ConfigCondition> TYPE = ResourceConditionType.create(
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "config"), CODEC);

        @Override
        public ResourceConditionType<?> getType() {
            return TYPE;
        }

        @Override
        public boolean test(RegistryOps.RegistryInfoLookup lookup) {
            return TorcherinoConfig.flag(this.config) == this.value;
        }
    }
}
