/*
 * 本文件：三重压缩灯笼形态的加速火把。
 * 说明：注册名 blocktriplecompressedlantern，继承 BlockLantern，方块实体为 ×729 的 TileTripleCompressedTorcherino。
 */
package com.sci.torcherino.blocks.blocks;

import com.sci.torcherino.blocks.tiles.TileTripleCompressedTorcherino;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Compression tier 3 Lantern. */
public final class BlockTripleCompressedLantern extends BlockLantern {

    public BlockTripleCompressedLantern(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileTripleCompressedTorcherino(pos, state);
    }
}
