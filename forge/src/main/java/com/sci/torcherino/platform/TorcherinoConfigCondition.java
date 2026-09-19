/*
 * 本文件：Forge 侧的配方条件实现（条件 id：torcherino:config）。
 * 说明：让 data/torcherino/recipes 里的配方按 config/torcherino.toml 的开关决定是否加载。
 *      没有它，Forge 会因为「Unknown condition type: torcherino:config」把整条配方丢弃。
 */
package com.sci.torcherino.platform;

import com.google.gson.JsonObject;
import com.sci.torcherino.Constants;
import com.sci.torcherino.TorcherinoConfig;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

/**
 * Recipe condition backed by one boolean of the TOML configuration.
 *
 * <p>JSON shape used by the shipped recipes:</p>
 * <pre>
 * "conditions": [
 *   { "type": "torcherino:config", "config": "overPoweredRecipe", "value": true }
 * ]
 * </pre>
 */
public final class TorcherinoConfigCondition implements ICondition {

    /** Shared with the Fabric side so both loaders read the same condition id. */
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "config");

    private final String config;
    private final boolean expected;

    private TorcherinoConfigCondition(String config, boolean expected) {
        this.config = config;
        this.expected = expected;
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return TorcherinoConfig.flag(this.config) == this.expected;
    }

    /**
     * Serializer handed to {@code CraftingHelper.register}, which only stores it in a
     * static map and therefore may be called from the mod constructor.
     */
    public static final class Serializer implements IConditionSerializer<TorcherinoConfigCondition> {

        @Override
        public void write(JsonObject json, TorcherinoConfigCondition value) {
            json.addProperty("config", value.config);
            json.addProperty("value", value.expected);
        }

        @Override
        public TorcherinoConfigCondition read(JsonObject json) {
            return new TorcherinoConfigCondition(
                    GsonHelper.getAsString(json, "config"),
                    GsonHelper.getAsBoolean(json, "value"));
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
