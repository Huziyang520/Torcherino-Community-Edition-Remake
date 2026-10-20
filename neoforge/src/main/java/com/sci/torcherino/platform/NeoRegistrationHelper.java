/*
 * 本文件：IRegistrationHelper 的 NeoForge 实现。
 * 说明：用 DeferredRegister 延迟注册；DeferredHolder 未就绪时 get() 会抛异常，故包一层 isBound() 以满足「未注册返回 null」的约定。
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

import java.util.function.Supplier;

/**
 * NeoForge registration through deferred registers.
 *
 * <p>The suppliers are handed straight to NeoForge and only invoked while the matching
 * registry is being filled, which is the only window in which writing to a registry is
 * allowed. Building the instances earlier - for example in the mod constructor - fails
 * with {@code IllegalStateException: Registry is already frozen}, because
 * {@code Block}'s constructor registers an intrusive holder.</p>
 */
public class NeoRegistrationHelper implements IRegistrationHelper {

    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, Constants.MOD_ID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, Constants.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);

    /** Must be called from the mod constructor with the mod event bus. */
    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
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
        // The deferred supplier runs after the block registry has been filled, so the
        // valid blocks resolve correctly. Building the type here is also what makes the
        // package private BlockEntitySupplier usable at all.
        return wrap(BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder
                .of((pos, state) -> factory.create(pos, state), validBlocks.get())
                .build(null)));
    }

    /**
     * {@code DeferredHolder#get} throws when the entry is not registered yet, whereas
     * {@link RegistryEntry#get} is documented to answer {@code null}. The guard keeps the
     * common code free of that distinction.
     */
    private static <R, T extends R> RegistryEntry<T> wrap(DeferredHolder<R, ? extends T> holder) {
        return new SimpleRegistryEntry<>(holder.getId(), () -> holder.isBound() ? holder.get() : null);
    }
}
