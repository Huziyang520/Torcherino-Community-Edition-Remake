/*
 * 本文件：NeoForge 侧网络通道。
 * 说明：四个负载沿用 common 的 TorcherinoPayloads —— 改装键状态（C2S）、编辑界面数值回传（C2S）、
 *      所有者设置（C2S）、打开编辑界面（S2C）。数值回传与所有者设置都在服务端复核权限。
 */
package com.sci.torcherino.network;

import com.sci.torcherino.TorcherinoConfig;
import com.sci.torcherino.TorcherinoInteraction;
import com.sci.torcherino.TorcherinoKeyStates;
import com.sci.torcherino.blocks.tiles.TileTorcherino;
import com.sci.torcherino.client.TorcherinoNeoClientEvents;
import com.sci.torcherino.network.TorcherinoPayloads.ModifierKey;
import com.sci.torcherino.network.TorcherinoPayloads.OpenScreen;
import com.sci.torcherino.network.TorcherinoPayloads.OwnerSettings;
import com.sci.torcherino.network.TorcherinoPayloads.SetValues;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * NeoForge network registration built on the payload API of 1.21.
 */
public final class TorcherinoNeoNetwork {

    private TorcherinoNeoNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(ModifierKey.TYPE, ModifierKey.CODEC, (payload, context) ->
                TorcherinoKeyStates.set(context.player(), payload.pressed()));

        registrar.playToServer(SetValues.TYPE, SetValues.CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    // Range checks happen again inside the block entity, so a modified client
                    // cannot widen the area past its tier limits; the owner check happens here.
                    if (!(context.player() instanceof ServerPlayer player)) {
                        return;
                    }
                    final BlockEntity blockEntity = player.level().getBlockEntity(payload.pos());
                    if (blockEntity instanceof TileTorcherino torcherino && torcherino.mayEdit(player)) {
                        torcherino.setValues(payload.xRange(), payload.zRange(), payload.yRange(),
                                payload.speed(), payload.redstoneMode());
                    }
                }));

        registrar.playToServer(OwnerSettings.TYPE, OwnerSettings.CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) {
                        return;
                    }
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

        // The handler only touches client only classes when a payload actually arrives, which
        // never happens on a dedicated server.
        registrar.playToClient(OpenScreen.TYPE, OpenScreen.CODEC, (payload, context) ->
                TorcherinoNeoClientEvents.openScreen(payload));
    }

    public static void sendToServer(boolean pressed) {
        PacketDistributor.sendToServer(new ModifierKey(pressed));
    }

    /** Server side: asks one client to open the editor. */
    public static void openScreen(ServerPlayer player, TileTorcherino torcherino) {
        PacketDistributor.sendToPlayer(player, new OpenScreen(
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

    /** Client side: sends the values edited in the screen back to the server. */
    public static void sendValues(BlockPos pos, int xRange, int zRange, int yRange, int speed, int redstoneMode) {
        PacketDistributor.sendToServer(new SetValues(pos, xRange, zRange, yRange, speed, redstoneMode));
    }

    /** Client side: owner only, either flips the "others may edit" flag or drops the claim. */
    public static void sendOwnerSettings(BlockPos pos, boolean othersCanEdit, boolean unclaim) {
        PacketDistributor.sendToServer(new OwnerSettings(pos, othersCanEdit, unclaim));
    }
}
