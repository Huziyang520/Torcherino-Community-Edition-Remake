/*
 * 本文件：NeoForge 侧的配方条件实现（条件 id：torcherino:config）。
 * 说明：让 data/torcherino/recipes 里的配方按服务端配置的开关决定是否加载。
 *      1.21 的 NeoForge 把条件序列化器放进 neoforge:condition_codecs 注册表，不再用 CraftingHelper.register。
 *      没有它，NeoForge 会因为「Unknown recipe condition」把整条配方丢弃。
 */
package com.sci.torcherino.platform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sci.torcherino.Constants;
import com.sci.torcherino.TorcherinoConfig;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Recipe condition backed by one boolean of the configuration.
 *
 * <p>JSON shape used by the shipped recipes:</p>
 * <pre>
 * "neoforge:conditions": [
 *   { "type": "torcherino:config", "config": "overPoweredRecipe", "value": true }
 * ]
 * </pre>
 */
public record TorcherinoConfigCondition(String config, boolean expected) implements ICondition {

    public static final MapCodec<TorcherinoConfigCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("config").forGetter(TorcherinoConfigCondition::config),
            Codec.BOOL.fieldOf("value").forGetter(TorcherinoConfigCondition::expected)
    ).apply(instance, TorcherinoConfigCondition::new));

    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, Constants.MOD_ID);

    /** Must be called from the mod constructor: the codecs have to exist before data packs load. */
    public static void register(IEventBus modEventBus) {
        CONDITIONS.register("config", () -> CODEC);
        CONDITIONS.register(modEventBus);
    }

    @Override
    public boolean test(IContext context) {
        return TorcherinoConfig.flag(this.config) == this.expected;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
