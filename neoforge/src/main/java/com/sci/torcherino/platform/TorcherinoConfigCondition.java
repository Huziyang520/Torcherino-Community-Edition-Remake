/*
 * 本文件：NeoForge 侧的配方条件（条件 id：torcherino:config）。
 * 说明：让 data/torcherino/recipe 里的配方按配置开关决定是否加载。条件以 MapCodec 形式注册进
 *      NeoForge 的条件序列化器注册表；JSON 里写在 "neoforge:conditions" 数组下。
 */
package com.sci.torcherino.platform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sci.torcherino.TorcherinoConfig;

import net.neoforged.neoforge.common.conditions.ICondition;

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

    @Override
    public boolean test(IContext context) {
        return TorcherinoConfig.flag(this.config) == this.expected;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
