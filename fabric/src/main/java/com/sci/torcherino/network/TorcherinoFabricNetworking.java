/*
 * 本文件：Fabric 侧网络注册（1.20.5 起的 payload 体系）。
 * 说明：四个包的类型登记在这里；三个 C2S 包统一转交 common 的 TorcherinoServerHandlers，
 *      S2C 的「打开编辑界面」转交客户端类。包体定义在 common，两端字节完全一致。
 */
package com.sci.torcherino.network;

import com.sci.torcherino.network.TorcherinoPayloads;
import com.sci.torcherino.network.TorcherinoServerHandlers;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * Fabric packet registration.
 */
public final class TorcherinoFabricNetworking {

    private static boolean registered;

    private TorcherinoFabricNetworking() {
    }

    /** Common side: registers the C2S types and the server side receivers. */
    public static void registerServerReceivers() {
        if (registered) {
            return;
        }
        registered = true;

        PayloadTypeRegistry.serverboundPlay().register(TorcherinoPayloads.ModifierKey.TYPE, TorcherinoPayloads.ModifierKey.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(TorcherinoPayloads.SetValues.TYPE, TorcherinoPayloads.SetValues.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(TorcherinoPayloads.OwnerSettings.TYPE, TorcherinoPayloads.OwnerSettings.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TorcherinoPayloads.OpenScreen.TYPE, TorcherinoPayloads.OpenScreen.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TorcherinoPayloads.ModifierKey.TYPE,
                (payload, context) -> context.server().execute(
                        () -> TorcherinoServerHandlers.handleModifierKey(context.player(), payload.pressed())));
        ServerPlayNetworking.registerGlobalReceiver(TorcherinoPayloads.SetValues.TYPE,
                (payload, context) -> context.server().execute(
                        () -> TorcherinoServerHandlers.handleSetValues(context.player(), payload)));
        ServerPlayNetworking.registerGlobalReceiver(TorcherinoPayloads.OwnerSettings.TYPE,
                (payload, context) -> context.server().execute(
                        () -> TorcherinoServerHandlers.handleOwnerSettings(context.player(), payload)));
    }

    /** Server side: asks one client to open the editor. */
    public static void openScreen(ServerPlayer player, TorcherinoPayloads.OpenScreen payload) {
        ServerPlayNetworking.send(player, payload);
    }

    /** Client side: sends one packet to the server. */
    public static void sendToServer(CustomPacketPayload payload) {
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(payload);
    }
}
