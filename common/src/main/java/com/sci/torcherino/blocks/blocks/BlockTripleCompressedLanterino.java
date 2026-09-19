/*
 * 本文件：三重压缩南瓜灯形态的加速火把。
 * 说明：注册名 blocktriplecompressedlanterino，继承 BlockLanterino，方块实体为 ×729 的 TileTripleCompressedTorcherino。
 */
package com.sci.torcherino.blocks.blocks;

import com.sci.torcherino.blocks.tiles.TileTripleCompressedTorcherino;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Compression tier 3 Lanterino. */
public final class BlockTripleCompressedLanterino extends BlockLanterino {

    public BlockTripleCompressedLanterino(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileTripleCompressedTorcherino(pos, state);
    }
}
