/*
 * 本文件：三重压缩加速火把的落地形态方块。
 * 说明：注册名 blocktriplecompressedtorcherino；方块实体换成 ×729 的 TileTripleCompressedTorcherino。
 */
package com.sci.torcherino.blocks.blocks;

import com.sci.torcherino.blocks.tiles.TileTripleCompressedTorcherino;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Compression tier 3 floor torch. */
public final class BlockTripleCompressedTorcherino extends BlockTorcherino {

    public BlockTripleCompressedTorcherino(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileTripleCompressedTorcherino(pos, state);
    }
}
