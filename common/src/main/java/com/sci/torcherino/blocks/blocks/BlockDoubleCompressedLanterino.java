/*
 * 本文件：二重压缩南瓜灯形态的加速火把。
 * 说明：注册名 blockdoublecompressedlanterino，继承 BlockLanterino，方块实体为 ×81。（原版没有三压缩南瓜灯，此处也不做。）
 */
package com.sci.torcherino.blocks.blocks;

import com.sci.torcherino.blocks.tiles.TileDoubleCompressedTorcherino;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Compression tier 2 Lanterino. */
public class BlockDoubleCompressedLanterino extends BlockLanterino {

    public BlockDoubleCompressedLanterino(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileDoubleCompressedTorcherino(pos, state);
    }
}
