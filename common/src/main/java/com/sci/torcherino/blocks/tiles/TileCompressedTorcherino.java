/*
 * 本文件：压缩（第 1 级）加速火把的方块实体。
 * 说明：只覆写倍率 —— 基础速度 ×9，其余逻辑全部继承 TileTorcherino。
 */
package com.sci.torcherino.blocks.tiles;

import com.sci.torcherino.blocks.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Compression tier 1 - nine times the base speed. */
public final class TileCompressedTorcherino extends TileTorcherino {

    public TileCompressedTorcherino(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPRESSED_TORCHERINO.get(), pos, state);
    }

    @Override
    protected int speed(int base) {
        return base * 9;
    }
}
