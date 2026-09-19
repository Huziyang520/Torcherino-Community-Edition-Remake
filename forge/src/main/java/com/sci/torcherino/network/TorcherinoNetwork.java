/*
 * 本文件：Forge 侧网络通道。
 * 说明：只注册一个「客户端 → 服务端」的改装键状态包，内部以 record 承载载荷。
 */
package com.sci.torcherino.network;

import com.sci.torcherino.Constants;
import com.sci.torcherino.TorcherinoKeyStates;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

/**
 * Forge network channel. Only the server bound modifier key message exists.
 */
public final class TorcherinoNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Constants.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private TorcherinoNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(ModifierKeyMessage.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ModifierKeyMessage::encode)
                .decoder(ModifierKeyMessage::decode)
                .consumerMainThread(ModifierKeyMessage::handle)
                .add();
    }

    public static void sendToServer(boolean pressed) {
        CHANNEL.sendToServer(new ModifierKeyMessage(pressed));
    }

    /** Payload carrying the client modifier key state. */
    public record ModifierKeyMessage(boolean pressed) {

        public ModifierKeyMessage(FriendlyByteBuf buf) {
            this(buf.readBoolean());
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeBoolean(this.pressed);
        }

        public static ModifierKeyMessage decode(FriendlyByteBuf buf) {
            return new ModifierKeyMessage(buf);
        }

        public static void handle(ModifierKeyMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
            final NetworkEvent.Context context = contextSupplier.get();
            context.setPacketHandled(true);
            final ServerPlayer sender = context.getSender();
            if (sender != null) {
                TorcherinoKeyStates.set(sender, message.pressed());
            }
        }
    }
}
