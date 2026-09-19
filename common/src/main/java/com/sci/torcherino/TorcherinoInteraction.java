/*
 * 本文件：加速火把的共用右键交互逻辑。
 * 说明：所有火把 / 南瓜灯方块共用；服务端切换范围或速度，并把提示推到玩家行动栏（返回 true 表示已消费该次交互）。
 */
package com.sci.torcherino;

import com.sci.torcherino.blocks.tiles.TileTorcherino;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Shared right-click behaviour of every Torcherino block.
 *
 * <p>The off hand is ignored,
 * the interaction is only consumed when the clicked block actually owns a
 * {@link TileTorcherino}, the mode change happens server side and the resulting
 * description is pushed to the player's action bar.</p>
 */
public final class TorcherinoInteraction {

    private TorcherinoInteraction() {
    }

    /**
     * @return true when the interaction was consumed and the loader event must be cancelled.
     */
    public static boolean useBlock(Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND) {
            return false;
        }

        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof TileTorcherino torcherino)) {
            return false;
        }

        if (!level.isClientSide()) {
            final Boolean modifier = TorcherinoKeyStates.get(player);
            torcherino.changeMode(modifier != null && modifier);
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(torcherino.getDescription(), true);
            }
        }

        return true;
    }
}
