/*
 * 本文件：灯笼形态的加速火把（基础款）。
 * 说明：注册名 blocklantern，继承 LanternBlock；也是三个压缩款的父类。
 */
package com.sci.torcherino.blocks.blocks;

import com.sci.torcherino.blocks.tiles.TileTorcherino;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Lantern shaped Torcherino.
 *
 * <p>A vanilla style lantern: it can be placed on the ground or hung from a ceiling,
 * which is what {@code LanternBlock}'s {@code HANGING} property encodes. It shares
 * {@link TileTorcherino} with the floor and wall torch variants.</p>
 */
public class BlockLantern extends LanternBlock implements EntityBlock {

    public BlockLantern(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileTorcherino(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        TorcherinoSupport.recordOwner(level, pos, placer);
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return TorcherinoSupport.serverTicker(level);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        TorcherinoSupport.refreshPoweredState(level, pos);
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        TorcherinoSupport.refreshPoweredState(level, pos);
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }
}
