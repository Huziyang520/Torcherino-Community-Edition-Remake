/*
 * 本文件：Fabric 侧网络通道。
 * 说明：三个通道 —— 改装键状态（C2S）、编辑界面数值回传（C2S）、打开编辑界面（S2C）。
 *      1.20.1 的 fabric-networking-api-v1 还没有 payload API，因此沿用 ResourceLocation 通道写法。
 */
package com.sci.torcherino.network;

import com.sci.torcherino.Constants;
import com.sci.torcherino.TorcherinoKeyStates;
import com.sci.torcherino.blocks.tiles.TileTorcherino;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Fabric counterpart of the old {@code SimpleNetworkWrapper} channel, extended with the
 * two messages the editor needs.
 */
public final class TorcherinoFabricNetworking {

    public static final ResourceLocation MODIFIER_KEY = new ResourceLocation(Constants.MOD_ID, "modifier_key");
    public static final ResourceLocation SET_VALUES = new ResourceLocation(Constants.MOD_ID, "set_values");
    public static final ResourceLocation OPEN_SCREEN = new ResourceLocation(Constants.MOD_ID, "open_screen");

    /** Diagnostics only: proves once per session that the client streams reach the server. */
    private static boolean loggedFirstPacket;

    private TorcherinoFabricNetworking() {
    }

    /** Server side: registers the receivers of the two server bound channels. */
    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(MODIFIER_KEY, (server, player, handler, buf, responseSender) -> {
            final boolean pressed = buf.readBoolean();
            if (!loggedFirstPacket) {
                loggedFirstPacket = true;
                Constants.LOG.info("Torcherino modifier key packets received on the Fabric server");
            }
            server.execute(() -> TorcherinoKeyStates.set(player, pressed));
        });

        ServerPlayNetworking.registerGlobalReceiver(SET_VALUES, (server, player, handler, buf, responseSender) -> {
            final BlockPos pos = buf.readBlockPos();
            final int xRange = buf.readInt();
            final int zRange = buf.readInt();
            final int yRange = buf.readInt();
            final int speed = buf.readInt();
            final int redstoneMode = buf.readInt();
            server.execute(() -> {
                // Range checks happen again inside the block entity, so a modified client
                // cannot widen the area past its tier limits.
                final BlockEntity blockEntity = player.level().getBlockEntity(pos);
                if (blockEntity instanceof TileTorcherino torcherino) {
                    torcherino.setValues(xRange, zRange, yRange, speed, redstoneMode);
                }
            });
        });
    }

    /**
     * Server side: asks one client to open the editor. The payload also carries the block's
     * translation key (window title) and the tier factor (percentage label).
     */
    public static void openScreen(ServerPlayer player, TileTorcherino torcherino) {
        final FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(torcherino.getBlockPos());
        buf.writeUtf(torcherino.getBlockState().getBlock().getDescriptionId());
        buf.writeInt(torcherino.getXRange());
        buf.writeInt(torcherino.getZRange());
        buf.writeInt(torcherino.getYRange());
        buf.writeInt(torcherino.getSpeed());
        buf.writeInt(torcherino.getRedstoneMode());
        buf.writeInt(torcherino.getTierMultiplier());
        ServerPlayNetworking.send(player, OPEN_SCREEN, buf);
    }

    /** Client side. */
    public static void sendModifierKey(boolean pressed) {
        final FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(pressed);
        ClientPlayNetworking.send(MODIFIER_KEY, buf);
    }

    /** Client side: sends the values edited in the screen back to the server. */
    public static void sendValues(BlockPos pos, int xRange, int zRange, int yRange, int speed, int redstoneMode) {
        final FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeInt(xRange);
        buf.writeInt(zRange);
        buf.writeInt(yRange);
        buf.writeInt(speed);
        buf.writeInt(redstoneMode);
        ClientPlayNetworking.send(SET_VALUES, buf);
    }
}
