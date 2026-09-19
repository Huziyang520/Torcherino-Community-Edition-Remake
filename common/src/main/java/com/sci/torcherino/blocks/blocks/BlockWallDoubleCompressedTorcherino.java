/*
 * 本文件：二重压缩加速火把的贴墙形态方块。
 * 说明：注册名 wall_blockdoublecompressedtorcherino，共用 ×81 的方块实体，不单独注册物品。
 */
package com.sci.torcherino.blocks.blocks;

import com.sci.torcherino.blocks.tiles.TileDoubleCompressedTorcherino;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Compression tier 2 wall torch. */
public final class BlockWallDoubleCompressedTorcherino extends BlockWallTorcherino {

    public BlockWallDoubleCompressedTorcherino(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileDoubleCompressedTorcherino(pos, state);
    }
}
