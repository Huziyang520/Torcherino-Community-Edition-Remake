/*
 * 本文件：Fabric 侧网络通道。
 * 说明：只有「客户端 → 服务端」一个方向的改装键状态包。
 */
package com.sci.torcherino.network;

import com.sci.torcherino.Constants;
import com.sci.torcherino.TorcherinoKeyStates;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Fabric network channel carrying the client modifier key state. Only the server bound
 * direction exists.
 */
public final class TorcherinoFabricNetworking {

    public static final ResourceLocation MODIFIER_KEY = new ResourceLocation(Constants.MOD_ID, "modifier_key");

    private TorcherinoFabricNetworking() {
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(MODIFIER_KEY, (server, player, handler, buf, responseSender) -> {
            final boolean pressed = buf.readBoolean();
            server.execute(() -> TorcherinoKeyStates.set(player, pressed));
        });
    }

    /** Client only. */
    public static void sendModifierKey(boolean pressed) {
        final FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(pressed);
        ClientPlayNetworking.send(MODIFIER_KEY, buf);
    }
}
