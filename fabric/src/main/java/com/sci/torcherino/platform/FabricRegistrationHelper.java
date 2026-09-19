/*
 * 本文件：IRegistrationHelper 的 Fabric 实现。
 * 说明：Fabric 是「立即注册」——直接写 BuiltInRegistries，并用 FabricBlockEntityTypeBuilder 构造方块实体类型。
 */
package com.sci.torcherino.platform;

import com.sci.torcherino.Constants;
import com.sci.torcherino.platform.services.BlockEntityFactory;
import com.sci.torcherino.platform.services.IRegistrationHelper;
import com.sci.torcherino.platform.services.RegistryEntry;
import com.sci.torcherino.platform.services.SimpleRegistryEntry;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

/**
 * Fabric registers directly and immediately.
 *
 * <p>Everything happens inside {@code ModInitializer#onInitialize}, which still runs
 * before the registries freeze, so the suppliers can be invoked straight away. That is
 * also why a block may not be constructed any earlier than this: {@code Block}'s
 * constructor writes to {@code BuiltInRegistries.BLOCK} through
 * {@code createIntrusiveHolder}.</p>
 */
public class FabricRegistrationHelper implements IRegistrationHelper {

    private static ResourceLocation id(String name) {
        return new ResourceLocation(Constants.MOD_ID, name);
    }

    @Override
    public <T extends Block> RegistryEntry<T> registerBlock(String name, Supplier<T> supplier) {
        final ResourceLocation id = id(name);
        final T value = Registry.register(BuiltInRegistries.BLOCK, id, supplier.get());
        return new SimpleRegistryEntry<>(id, () -> value);
    }

    @Override
    public <T extends Item> RegistryEntry<T> registerItem(String name, Supplier<T> supplier) {
        final ResourceLocation id = id(name);
        final T value = Registry.register(BuiltInRegistries.ITEM, id, supplier.get());
        return new SimpleRegistryEntry<>(id, () -> value);
    }

    @Override
    public <T extends BlockEntity> RegistryEntry<BlockEntityType<T>> registerBlockEntityType(
            String name, BlockEntityFactory<T> factory, Supplier<Block[]> validBlocks) {
        final ResourceLocation id = id(name);
        // Fabric API exists precisely so that mods do not have to touch the package
        // private BlockEntityType.BlockEntitySupplier.
        final BlockEntityType<T> type = FabricBlockEntityTypeBuilder
                .create(factory::create, validBlocks.get())
                .build();
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, type);
        return new SimpleRegistryEntry<>(id, () -> type);
    }
}
