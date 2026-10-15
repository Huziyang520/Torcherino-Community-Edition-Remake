/*
 * 本文件：二重压缩灯笼形态的加速火把。
 * 说明：注册名 blockdoublecompressedlantern，继承 BlockLantern，方块实体为 ×81 的 TileDoubleCompressedTorcherino。
 */
package com.sci.torcherino.blocks.blocks;

import com.sci.torcherino.blocks.tiles.TileDoubleCompressedTorcherino;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Compression tier 2 Lantern. */
public final class BlockDoubleCompressedLantern extends BlockLantern {

    public BlockDoubleCompressedLantern(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileDoubleCompressedTorcherino(pos, state);
    }
}
