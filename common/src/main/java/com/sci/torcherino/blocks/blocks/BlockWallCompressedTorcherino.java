/*
 * 本文件：压缩加速火把的贴墙形态方块。
 * 说明：注册名 wall_blockcompressedtorcherino，共用 ×9 的方块实体，不单独注册物品。
 */
package com.sci.torcherino.blocks.blocks;

import com.sci.torcherino.blocks.tiles.TileCompressedTorcherino;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Compression tier 1 wall torch. */
public final class BlockWallCompressedTorcherino extends BlockWallTorcherino {

    public BlockWallCompressedTorcherino(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileCompressedTorcherino(pos, state);
    }
}
