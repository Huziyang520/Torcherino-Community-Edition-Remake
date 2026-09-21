/*
 * 本文件：加速火把的共用右键交互逻辑。
 * 说明：默认打开可视化编辑界面；把 general.useGui 关掉后回落到 7.5 的快捷操作（右键切范围、按住改装键切速度），
 *      两种方式互斥，只会生效一种。
 */
package com.sci.torcherino;

import com.sci.torcherino.blocks.tiles.TileTorcherino;
import com.sci.torcherino.platform.Services;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Shared right-click behaviour of every Torcherino block.
 *
 * <p>With the graphical editor enabled (the default) the server only tells the client to
 * open the editor. With {@code general.useGui} disabled the classic interaction applies:
 * the off hand is ignored, the interaction is only consumed when the clicked block
 * actually owns a {@link TileTorcherino}, the mode change happens server side and the
 * resulting description is pushed to the player's action bar.</p>
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

        if (TorcherinoConfig.useGui) {
            // Sneaking keeps the vanilla interaction: the editor is deliberately not opened
            // so a player can still place blocks against the torch without a screen popping
            // up. The server side sees the same sneak state, so both sides agree.
            if (player.isShiftKeyDown()) {
                return false;
            }
            // The editor itself lives on the client, so nothing is changed here; the server
            // merely asks the client to open it.
            if (player instanceof ServerPlayer serverPlayer) {
                Services.PLATFORM.openTorcherinoScreen(serverPlayer, torcherino);
            }
            return true;
        }

        if (!level.isClientSide()) {
            // Sneaking is the modifier, exactly like the original mod: right click cycles the
            // area, sneak + right click cycles the speed. No key is registered for this, so
            // nothing can conflict with vanilla sneak.
            torcherino.changeMode(player.isShiftKeyDown());
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(torcherino.getDescription(), true);
            }
        }

        return true;
    }
}
