/*
 * 本文件：火把方块族的包内共用小工具。
 * 说明：红石状态刷新与服务端 ticker 的构造，供 11 个方块类复用；包私有，不对外暴露。
 */
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
     * Re-reads the redstone state of the Torcherino at {@code pos}. Called from the
     * block's place and neighbour-changed hooks, so the state is correct no matter which
     * of them fires first.
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
