package com.sci.torcherino.platform.services;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

/**
 * Performs the actual registry writes for the common code.
 *
 * <p>The common project is compiled twice - once against each loader - and must not
 * reference loader specific registry APIs. Registration is therefore expressed as a
 * narrow callback interface that each loader implements with its own preferred
 * mechanism (deferred registers on Forge, immediate registration on Fabric).</p>
 *
 * <p><b>Everything is a supplier on purpose.</b> {@code Block}'s constructor writes to
 * {@code BuiltInRegistries.BLOCK} through {@code createIntrusiveHolder}, so a block may
 * only be instantiated while the loader holds the registry open - inside the deferred
 * register callback on Forge, inside {@code onInitialize} on Fabric. Building instances
 * eagerly in the mod constructor throws {@code Registry is already frozen}.</p>
 *
 * <p>Block entity types are built by the loader as well, because vanilla's
 * {@code BlockEntityType.Builder.of} takes a package private supplier interface that the
 * common code cannot reference. The common side only supplies the factory and a lazy
 * array of valid blocks.</p>
 *
 * <p>Implementations are invoked from inside the loader registration window only.</p>
 */
public interface IRegistrationHelper {

    <T extends Block> RegistryEntry<T> registerBlock(String name, Supplier<T> supplier);

    <T extends Item> RegistryEntry<T> registerItem(String name, Supplier<T> supplier);

    <T extends BlockEntity> RegistryEntry<BlockEntityType<T>> registerBlockEntityType(
            String name, BlockEntityFactory<T> factory, Supplier<Block[]> validBlocks);
}
