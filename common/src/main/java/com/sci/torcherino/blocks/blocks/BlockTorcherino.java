package com.sci.torcherino.blocks.blocks;

import com.sci.torcherino.blocks.tiles.TileTorcherino;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Floor standing Torcherino.
 *
 * <p>1.12.2 modelled floor and wall torches with a single {@code BlockTorch} carrying
 * a {@code facing} property. Modern Minecraft splits them into {@code TorchBlock} and
 * {@code WallTorchBlock}; the matching wall block is
 * {@link BlockWallTorcherino} and the item is a {@code StandingAndWallBlockItem} so
 * placement still picks the right variant.</p>
 */
public class BlockTorcherino extends TorchBlock implements EntityBlock {

    public BlockTorcherino(BlockBehaviour.Properties properties) {
        super(properties, ParticleTypes.FLAME);
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
