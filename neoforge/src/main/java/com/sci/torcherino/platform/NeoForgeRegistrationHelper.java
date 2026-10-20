/*
 * 本文件：IRegistrationHelper 的 NeoForge 实现。
 * 说明：用 DeferredRegister 延迟注册；实例只在注册窗口内构造（方块构造函数会写内置注册表）。
 *      方块实体类型用 NeoForge 访问转换开放的原版构造器直接创建，valid blocks 在延迟回调里解析。
 */
package com.sci.torcherino.platform;

import com.sci.torcherino.Constants;
import com.sci.torcherino.platform.services.BlockEntityFactory;
import com.sci.torcherino.platform.services.IRegistrationHelper;
import com.sci.torcherino.platform.services.RegistryEntry;
import com.sci.torcherino.platform.services.SimpleRegistryEntry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;
import java.util.function.Supplier;

/**
 * NeoForge registration through deferred registers.
 *
 * <p>The suppliers are handed straight to the loader and only invoked while the matching
 * registry event runs, which is the only window in which writing to a registry is allowed.
 * Building the instances earlier - for example in the mod constructor - fails with
 * {@code IllegalStateException: Registry is already frozen}, because {@code Block}'s
 * constructor registers an intrusive holder.</p>
 */
public class NeoForgeRegistrationHelper implements IRegistrationHelper {

    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, Constants.MOD_ID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, Constants.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);
    /** Recipe condition codecs (the NeoForge replacement of the old serializer objects). */
    private static final DeferredRegister<com.mojang.serialization.MapCodec<? extends net.neoforged.neoforge.common.conditions.ICondition>> CONDITIONS =
            DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.CONDITION_CODECS, Constants.MOD_ID);

    /** Must be called from the mod constructor with the mod event bus. */
    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        CONDITIONS.register("config", () -> TorcherinoConfigCondition.CODEC);
        CONDITIONS.register(modEventBus);
    }

    @Override
    public <T extends Block> RegistryEntry<T> registerBlock(String name, Supplier<T> supplier) {
        return wrap(BLOCKS.register(name, supplier));
    }

    @Override
    public <T extends Item> RegistryEntry<T> registerItem(String name, Supplier<T> supplier) {
        return wrap(ITEMS.register(name, supplier));
    }

    @Override
    public <T extends BlockEntity> RegistryEntry<BlockEntityType<T>> registerBlockEntityType(
            String name, BlockEntityFactory<T> factory, Supplier<Block[]> validBlocks) {
        // The deferred supplier runs after the block registry has been filled, so the valid
        // blocks resolve correctly. The vanilla constructor and its supplier interface are
        // opened by NeoForge's access transformer.
        return wrap(BLOCK_ENTITIES.register(name, () -> new BlockEntityType<T>(
                (pos, state) -> factory.create(pos, state), Set.of(validBlocks.get()))));
    }

    /** {@code DeferredHolder#get} throws before binding, while the common contract is null. */
    private static <T, I extends T> RegistryEntry<I> wrap(DeferredHolder<T, I> holder) {
        return new SimpleRegistryEntry<>(holder.getId(), () -> holder.isBound() ? holder.get() : null);
    }
}
