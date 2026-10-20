/*
 * 本文件：服务端侧的三个 C2S 包处理（加载器无关）。
 * 说明：数值回传与所有者设置都在服务端复核权限，改装键状态只是记在内存表里。
 *      两个加载器的网络注册都只做「收包 → 调这里」。
 */
package com.sci.torcherino.network;

import com.sci.torcherino.Constants;
import com.sci.torcherino.TorcherinoInteraction;
import com.sci.torcherino.TorcherinoKeyStates;
import com.sci.torcherino.blocks.tiles.TileTorcherino;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Server side handlers of the client to server packets.
 *
 * <p>Everything a client sends is checked again here: the block entity is looked up from
 * the world (never taken from the packet), the ranges are clamped by the block entity and
 * the owner rules are enforced before any value is written.</p>
 */
public final class TorcherinoServerHandlers {

    /** Diagnostics only: proves once per session that the client streams reach the server. */
    private static boolean loggedFirstPacket;

    private TorcherinoServerHandlers() {
    }

    public static void handleModifierKey(ServerPlayer player, boolean pressed) {
        if (!loggedFirstPacket) {
            loggedFirstPacket = true;
            Constants.LOG.info("Torcherino modifier key packets received on the server");
        }
        TorcherinoKeyStates.set(player, pressed);
    }

    public static void handleSetValues(ServerPlayer player, TorcherinoPayloads.SetValues payload) {
        // Range checks happen again inside the block entity, so a modified client cannot
        // widen the area past its tier limits; the owner check happens here.
        final BlockEntity blockEntity = player.level().getBlockEntity(payload.pos());
        if (blockEntity instanceof TileTorcherino torcherino && torcherino.mayEdit(player)) {
            torcherino.setValues(payload.xRange(), payload.zRange(), payload.yRange(),
                    payload.speed(), payload.redstoneMode());
        }
    }

    public static void handleOwnerSettings(ServerPlayer player, TorcherinoPayloads.OwnerSettings payload) {
        final BlockEntity blockEntity = player.level().getBlockEntity(payload.pos());
        if (!(blockEntity instanceof TileTorcherino torcherino) || !torcherino.isOwner(player)) {
            // Only the owner may change who is allowed to edit, or drop the claim.
            player.sendOverlayMessage(Component.translatable("message.torcherino.locked"));
            return;
        }
        if (payload.unclaim()) {
            torcherino.clearOwner();
        } else {
            torcherino.setOthersCanEdit(payload.othersCanEdit());
        }
    }

    /**
     * Builds the "open the editor" packet for one player. Kept next to the handlers so both
     * loaders pass exactly the same data to the client.
     */
    public static TorcherinoPayloads.OpenScreen openScreenPacket(ServerPlayer player, TileTorcherino torcherino) {
        return new TorcherinoPayloads.OpenScreen(
                torcherino.getBlockPos(),
                torcherino.getBlockState().getBlock().getDescriptionId(),
                torcherino.getXRange(),
                torcherino.getZRange(),
                torcherino.getYRange(),
                torcherino.getSpeedScaled(),
                torcherino.getRedstoneMode(),
                torcherino.getTierMultiplier(),
                TorcherinoInteraction.ownerDisplay(player, torcherino),
                torcherino.othersCanEdit(),
                torcherino.isOwner(player),
                com.sci.torcherino.TorcherinoConfig.ownerSystem);
    }
}
