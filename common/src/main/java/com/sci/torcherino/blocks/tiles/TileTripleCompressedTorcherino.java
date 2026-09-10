package com.sci.torcherino.blocks.tiles;

import com.sci.torcherino.blocks.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Compression tier 3 - seven hundred twenty-nine times the base speed. */
public class TileTripleCompressedTorcherino extends TileTorcherino {

    public TileTripleCompressedTorcherino(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRIPLE_COMPRESSED_TORCHERINO, pos, state);
    }

    @Override
    protected int speed(int base) {
        return base * 729;
    }
}
