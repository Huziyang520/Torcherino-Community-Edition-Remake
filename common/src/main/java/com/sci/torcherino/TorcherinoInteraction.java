/*
 * 本文件：加速火把的共用右键交互逻辑。
 * 说明：默认打开可视化编辑界面；把 general.useGui 关掉后回落到 7.5 的快捷操作（右键切范围、按住改装键切速度），
 *      两种方式互斥，只会生效一种。
 */
package com.sci.torcherino;

import com.mojang.authlib.GameProfile;
import com.sci.torcherino.blocks.tiles.TileTorcherino;
import com.sci.torcherino.platform.Services;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;
import java.util.UUID;

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
     * Human readable owner of a Torcherino, resolved server side where the player list and the
     * name cache live.
     *
     * @return the online name, the cached name, a short UUID when neither is available, or an
     *         empty string while the Torcherino has no owner.
     */
    public static String ownerDisplay(ServerPlayer viewer, TileTorcherino torcherino) {
        final String owner = torcherino.getOwnerId();
        if (owner == null || owner.isEmpty()) {
            return "";
        }
        final UUID uuid;
        try {
            uuid = UUID.fromString(owner);
        } catch (IllegalArgumentException e) {
            // Unreadable owner: show what is stored instead of hiding it.
            return owner;
        }
        final ServerPlayer online = viewer.server.getPlayerList().getPlayer(uuid);
        if (online != null) {
            return online.getGameProfile().getName();
        }
        if (viewer.server.getProfileCache() != null) {
            final Optional<GameProfile> cached = viewer.server.getProfileCache().get(uuid);
            if (cached.isPresent()) {
                return cached.get().getName();
            }
        }
        return owner.length() <= 8 ? owner : owner.substring(0, 8);
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
            // A claimed Torcherino with "other players may not edit" rejects the quick
            // interaction just like it rejects the editor values.
            if (!torcherino.mayEdit(player)) {
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.displayClientMessage(
                            Component.translatable("message.torcherino.locked"), true);
                }
                return true;
            }
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
