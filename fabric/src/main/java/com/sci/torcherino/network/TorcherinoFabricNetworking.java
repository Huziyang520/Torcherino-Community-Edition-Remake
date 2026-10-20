/*
 * 本文件：Fabric 侧网络通道。
 * 说明：1.21.1 的 fabric-networking-api-v1 只保留 payload API（旧的 ResourceLocation 通道已删除），
 *      因此四个包都定义成 CustomPacketPayload（见 common 的 TorcherinoPayloads），这里只做类型注册与收包。
 *      数值回传与所有者设置都在服务端复核权限。
 */
package com.sci.torcherino.network;

import com.sci.torcherino.Constants;
import com.sci.torcherino.TorcherinoConfig;
import com.sci.torcherino.TorcherinoInteraction;
import com.sci.torcherino.TorcherinoKeyStates;
import com.sci.torcherino.blocks.tiles.TileTorcherino;
import com.sci.torcherino.network.TorcherinoPayloads.ModifierKey;
import com.sci.torcherino.network.TorcherinoPayloads.OpenScreen;
import com.sci.torcherino.network.TorcherinoPayloads.OwnerSettings;
import com.sci.torcherino.network.TorcherinoPayloads.SetValues;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Fabric network channel built on the payload API.
 */
public final class TorcherinoFabricNetworking {

    /** Diagnostics only: proves once per session that the client streams reach the server. */
    private static boolean loggedFirstPacket;

    private TorcherinoFabricNetworking() {
    }

    /**
     * Common side: registers every payload type on both sides. Fabric requires the type to be
     * known by the sender as well, so the S2C types are registered here too - the client only
     * receiver is registered separately in the client initialiser.
     */
    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playC2S().register(ModifierKey.TYPE, ModifierKey.CODEC);
        PayloadTypeRegistry.playC2S().register(SetValues.TYPE, SetValues.CODEC);
        PayloadTypeRegistry.playC2S().register(OwnerSettings.TYPE, OwnerSettings.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenScreen.TYPE, OpenScreen.CODEC);
    }

    /** Server side: registers the receivers of the three client bound to server payloads. */
    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(ModifierKey.TYPE, (payload, context) -> {
            if (!loggedFirstPacket) {
                loggedFirstPacket = true;
                Constants.LOG.info("Torcherino modifier key packets received on the Fabric server");
            }
            context.server().execute(() -> TorcherinoKeyStates.set(context.player(), payload.pressed()));
        });

        ServerPlayNetworking.registerGlobalReceiver(SetValues.TYPE, (payload, context) ->
                context.server().execute(() -> {
                    // Range checks happen again inside the block entity, so a modified client
                    // cannot widen the area past its tier limits; the owner check happens here.
                    final ServerPlayer player = context.player();
                    final BlockEntity blockEntity = player.level().getBlockEntity(payload.pos());
                    if (blockEntity instanceof TileTorcherino torcherino && torcherino.mayEdit(player)) {
                        torcherino.setValues(payload.xRange(), payload.zRange(), payload.yRange(),
                                payload.speed(), payload.redstoneMode());
                    }
                }));

        ServerPlayNetworking.registerGlobalReceiver(OwnerSettings.TYPE, (payload, context) ->
                context.server().execute(() -> {
                    final ServerPlayer player = context.player();
                    final BlockEntity blockEntity = player.level().getBlockEntity(payload.pos());
                    if (!(blockEntity instanceof TileTorcherino torcherino) || !torcherino.isOwner(player)) {
                        // Only the owner may change who is allowed to edit, or drop the claim.
                        player.displayClientMessage(Component.translatable("message.torcherino.locked"), true);
                        return;
                    }
                    if (payload.unclaim()) {
                        torcherino.clearOwner();
                    } else {
                        torcherino.setOthersCanEdit(payload.othersCanEdit());
                    }
                }));
    }

    /**
     * Server side: asks one client to open the editor. The payload also carries the block's
     * translation key (window title), the tier factor (percentage label) and the owner
     * information the editor shows.
     */
    public static void openScreen(ServerPlayer player, TileTorcherino torcherino) {
        ServerPlayNetworking.send(player, new OpenScreen(
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
                TorcherinoConfig.ownerSystem));
    }

    /** Client side. */
    public static void sendModifierKey(boolean pressed) {
        ClientPlayNetworking.send(new ModifierKey(pressed));
    }

    /** Client side: sends the values edited in the screen back to the server. */
    public static void sendValues(BlockPos pos, int xRange, int zRange, int yRange,
                                  int speed, int redstoneMode) {
        ClientPlayNetworking.send(
                new SetValues(pos, xRange, zRange, yRange, speed, redstoneMode));
    }

    /** Client side: owner only, either flips the "others may edit" flag or drops the claim. */
    public static void sendOwnerSettings(BlockPos pos, boolean othersCanEdit, boolean unclaim) {
        ClientPlayNetworking.send(
                new OwnerSettings(pos, othersCanEdit, unclaim));
    }
}
