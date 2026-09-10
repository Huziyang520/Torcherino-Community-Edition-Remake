package com.sci.torcherino.blocks.blocks;

import com.sci.torcherino.blocks.tiles.TileCompressedTorcherino;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Compression tier 1 floor torch. */
public final class BlockCompressedTorcherino extends BlockTorcherino {

    public BlockCompressedTorcherino(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileCompressedTorcherino(pos, state);
    }
}
