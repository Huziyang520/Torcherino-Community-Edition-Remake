/*
 * 本文件：二重压缩加速火把的落地形态方块。
 * 说明：注册名 blockdoublecompressedtorcherino；方块实体换成 ×81 的 TileDoubleCompressedTorcherino。
 */
package com.sci.torcherino.blocks.blocks;

import com.sci.torcherino.blocks.tiles.TileDoubleCompressedTorcherino;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Compression tier 2 floor torch. */
public final class BlockDoubleCompressedTorcherino extends BlockTorcherino {

    public BlockDoubleCompressedTorcherino(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileDoubleCompressedTorcherino(pos, state);
    }
}
