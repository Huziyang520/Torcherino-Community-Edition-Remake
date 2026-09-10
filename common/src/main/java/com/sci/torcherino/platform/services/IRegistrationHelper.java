package com.sci.torcherino.platform.services;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Performs the actual registry writes for the common code.
 *
 * <p>The common project is compiled twice - once against each loader - and must not
 * reference loader specific registry APIs. Registration is therefore expressed as a
 * narrow callback interface that each loader implements with its own preferred
 * mechanism (deferred registers on Forge, immediate registration on Fabric).</p>
 *
 * <p>Block entity types deserve special mention: the vanilla {@code Builder.of} takes a
 * package private supplier interface that the common code cannot reference, so the
 * loader creates and returns the type instead.</p>
 *
 * <p>Implementations are invoked from inside the loader registration window only.</p>
 */
public interface IRegistrationHelper {

    void registerBlock(String name, Block block);

    void registerItem(String name, Item item);

    <T extends BlockEntity> BlockEntityType<T> registerBlockEntityType(
            String name, BlockEntityFactory<T> factory, Block... validBlocks);
}
