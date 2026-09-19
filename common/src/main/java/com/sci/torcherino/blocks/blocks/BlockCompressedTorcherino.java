/*
 * 本文件：压缩加速火把的落地形态方块。
 * 说明：注册名 blockcompressedtorcherino；只把方块实体换成 ×9 的 TileCompressedTorcherino，行为继承 BlockTorcherino。
 */
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
