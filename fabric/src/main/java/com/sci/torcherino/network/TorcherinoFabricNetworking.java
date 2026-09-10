package com.sci.torcherino.network;

import com.sci.torcherino.Constants;
import com.sci.torcherino.TorcherinoKeyStates;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Fabric counterpart of the 7.5 {@code SimpleNetworkWrapper} channel that carried the
 * client modifier key state. Only the server bound direction exists, matching the
 * original {@code registerMessage(..., Side.SERVER)}.
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
