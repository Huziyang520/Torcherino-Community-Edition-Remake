package com.sci.torcherino.blocks.blocks;

import com.sci.torcherino.blocks.tiles.TileTorcherino;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

/**
 * Small shared helpers for the Torcherino block family. Lives in the same package as
 * every block class so it can stay package private.
 */
final class TorcherinoSupport {

    private TorcherinoSupport() {
    }

    /**
     * Re-reads the redstone state of the Torcherino at {@code pos}. Ported from the
     * original {@code BlockTorcherino#onBlockAdded} and {@code #neighborChanged}
     * handlers, both of which used {@code World#isBlockIndirectlyGettingPowered}.
     */
    static void refreshPoweredState(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof TileTorcherino torcherino) {
            torcherino.setPoweredByRedstone(level.hasNeighborSignal(pos));
        }
    }

    /**
     * @return the server side ticker for Torcherino block entities, or {@code null} on
     *         the client so the acceleration loop never runs there.
     */
    static <T extends BlockEntity> BlockEntityTicker<T> serverTicker(Level level) {
        if (level.isClientSide()) {
            return null;
        }
        return (tickLevel, pos, state, blockEntity) -> {
            if (blockEntity instanceof TileTorcherino torcherino) {
                torcherino.tick(tickLevel, pos);
            }
        };
    }
}
