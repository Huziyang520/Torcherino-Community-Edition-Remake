/*
 * 本文件：二重压缩（第 2 级）加速火把的方块实体。
 * 说明：只覆写倍率 —— 基础速度 ×81。
 */
package com.sci.torcherino.blocks.tiles;

import com.sci.torcherino.blocks.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Compression tier 2 - eighty-one times the base speed. */
public class TileDoubleCompressedTorcherino extends TileTorcherino {

    public TileDoubleCompressedTorcherino(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DOUBLE_COMPRESSED_TORCHERINO.get(), pos, state);
    }

    @Override
    protected int speed(int base) {
        return base * 81;
    }
}
