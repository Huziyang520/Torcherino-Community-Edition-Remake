/*
 * 本文件：压缩灯笼形态的加速火把。
 * 说明：注册名 blockcompressedlantern，继承 BlockLantern，方块实体为 ×9 的 TileCompressedTorcherino。
 */
package com.sci.torcherino.blocks.blocks;

import com.sci.torcherino.blocks.tiles.TileCompressedTorcherino;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Compression tier 1 Lantern. */
public final class BlockCompressedLantern extends BlockLantern {

    public BlockCompressedLantern(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileCompressedTorcherino(pos, state);
    }
}
