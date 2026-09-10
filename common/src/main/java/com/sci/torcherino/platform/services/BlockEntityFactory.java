package com.sci.torcherino.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Loader independent replacement for the vanilla {@code BlockEntityType.BlockEntitySupplier}.
 *
 * <p>The vanilla supplier interface is package private, so the common code cannot name it.
 * Declaring our own public interface lets the common code hand a factory to the loader,
 * which then adapts it to whatever builder that loader exposes.</p>
 */
@FunctionalInterface
public interface BlockEntityFactory<T extends BlockEntity> {

    T create(BlockPos pos, BlockState state);
}
