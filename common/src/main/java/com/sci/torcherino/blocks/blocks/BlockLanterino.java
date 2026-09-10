package com.sci.torcherino.blocks.blocks;

import com.sci.torcherino.blocks.tiles.TileTorcherino;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Jack o'Lanterino - the pumpkin shaped Torcherino variant.
 *
 * <p>1.12.2 derived this from {@code BlockPumpkin}; the modern counterpart is
 * {@code CarvedPumpkinBlock}. The vanilla golem patterns only match
 * {@code carved_pumpkin} and {@code jack_o_lantern}, so extending this class does not
 * make Lanterinos spawn golems - exactly like the original.</p>
 */
public class BlockLanterino extends CarvedPumpkinBlock implements EntityBlock {

    public BlockLanterino(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileTorcherino(pos, state);
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
