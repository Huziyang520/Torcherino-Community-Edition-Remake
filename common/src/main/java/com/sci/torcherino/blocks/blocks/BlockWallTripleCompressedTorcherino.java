package com.sci.torcherino.blocks.blocks;

import com.sci.torcherino.blocks.tiles.TileTripleCompressedTorcherino;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Compression tier 3 wall torch. */
public final class BlockWallTripleCompressedTorcherino extends BlockWallTorcherino {

    public BlockWallTripleCompressedTorcherino(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileTripleCompressedTorcherino(pos, state);
    }
}
