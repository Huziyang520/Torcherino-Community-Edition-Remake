/*
 * 本文件：NeoForge 侧网络注册（payload 体系）。
 * 说明：四个包的类型登记在这里；三个 C2S 包统一转交 common 的 TorcherinoServerHandlers，
 *      S2C 的「打开编辑界面」转交客户端类（只有客户端会执行）。
 */
package com.sci.torcherino.network;

import com.sci.torcherino.Constants;
import com.sci.torcherino.client.TorcherinoNeoForgeClient;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * NeoForge packet registration.
 *
 * <p>The payload records themselves live in the common project, so both loaders put the
 * exact same bytes on the wire. This class only registers the types and routes them.</p>
 */
public final class TorcherinoNetwork {

    /** Protocol version string: bump it whenever a payload layout changes. */
    private static final String PROTOCOL_VERSION = "1";

    private TorcherinoNetwork() {
    }

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION)
                .optional();

        registrar.playToServer(TorcherinoPayloads.ModifierKey.TYPE, TorcherinoPayloads.ModifierKey.CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> TorcherinoServerHandlers.handleModifierKey((ServerPlayer) context.player(), payload.pressed())));

        registrar.playToServer(TorcherinoPayloads.SetValues.TYPE, TorcherinoPayloads.SetValues.CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> TorcherinoServerHandlers.handleSetValues((ServerPlayer) context.player(), payload)));

        registrar.playToServer(TorcherinoPayloads.OwnerSettings.TYPE, TorcherinoPayloads.OwnerSettings.CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> TorcherinoServerHandlers.handleOwnerSettings((ServerPlayer) context.player(), payload)));

        registrar.playToClient(TorcherinoPayloads.OpenScreen.TYPE, TorcherinoPayloads.OpenScreen.CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> TorcherinoNeoForgeClient.handleOpenScreen(payload)));
    }

    /** Server side: send one packet to one player. */
    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    /** Client side: send one packet to the server. */
    public static void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }

    /** Kept for diagnostics: the mod id doubles as the channel namespace. */
    public static String channelName() {
        return Constants.MOD_ID;
    }
}
