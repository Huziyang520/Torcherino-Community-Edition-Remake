/*
 * 本文件：Forge 侧网络通道。
 * 说明：四个包 —— 改装键状态（客户端→服务端）、编辑界面数值回传（客户端→服务端）、
 *      所有者设置（客户端→服务端：切换"他人可否修改" / 取消认领）、打开编辑界面（服务端→客户端）。
 *      数值回传与经典快捷操作都会在服务端复核所有者权限，改过的客户端也绕不过去。
 */
package com.sci.torcherino.network;

import com.sci.torcherino.Constants;
import com.sci.torcherino.TorcherinoInteraction;
import com.sci.torcherino.TorcherinoKeyStates;
import com.sci.torcherino.blocks.tiles.TileTorcherino;
import com.sci.torcherino.client.TorcherinoForgeClientEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

/**
 * Forge network channel. Holds the modifier key hint, the value update coming back from
 * the editor, the owner settings and the "open the editor" request going the other way.
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
        CHANNEL.messageBuilder(SetValuesMessage.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SetValuesMessage::encode)
                .decoder(SetValuesMessage::decode)
                .consumerMainThread(SetValuesMessage::handle)
                .add();
        CHANNEL.messageBuilder(OpenScreenMessage.class, 2, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenScreenMessage::encode)
                .decoder(OpenScreenMessage::decode)
                .consumerMainThread(OpenScreenMessage::handle)
                .add();
        CHANNEL.messageBuilder(OwnerSettingsMessage.class, 3, NetworkDirection.PLAY_TO_SERVER)
                .encoder(OwnerSettingsMessage::encode)
                .decoder(OwnerSettingsMessage::decode)
                .consumerMainThread(OwnerSettingsMessage::handle)
                .add();
    }

    public static void sendToServer(boolean pressed) {
        CHANNEL.sendToServer(new ModifierKeyMessage(pressed));
    }

    /** Server side: asks one client to open the editor. */
    public static void openScreen(ServerPlayer player, TileTorcherino torcherino) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenScreenMessage(
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
                torcherino.isOwner(player)));
    }

    /** Client side: sends the values edited in the screen back to the server. */
    public static void sendValues(BlockPos pos, int xRange, int zRange, int yRange, int speed, int redstoneMode) {
        CHANNEL.sendToServer(new SetValuesMessage(pos, xRange, zRange, yRange, speed, redstoneMode));
    }

    /** Client side: owner only, either flips the "others may edit" flag or drops the claim. */
    public static void sendOwnerSettings(BlockPos pos, boolean othersCanEdit, boolean unclaim) {
        CHANNEL.sendToServer(new OwnerSettingsMessage(pos, othersCanEdit, unclaim));
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

    /** Editor values coming back from the client. */
    public record SetValuesMessage(BlockPos pos, int xRange, int zRange, int yRange, int speed, int redstoneMode) {

        public SetValuesMessage(FriendlyByteBuf buf) {
            this(buf.readBlockPos(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeBlockPos(this.pos);
            buf.writeInt(this.xRange);
            buf.writeInt(this.zRange);
            buf.writeInt(this.yRange);
            buf.writeInt(this.speed);
            buf.writeInt(this.redstoneMode);
        }

        public static SetValuesMessage decode(FriendlyByteBuf buf) {
            return new SetValuesMessage(buf);
        }

        public static void handle(SetValuesMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
            final NetworkEvent.Context context = contextSupplier.get();
            context.setPacketHandled(true);
            final ServerPlayer sender = context.getSender();
            if (sender == null) {
                return;
            }
            context.enqueueWork(() -> {
                // Range checks happen again inside the block entity, so a modified client
                // cannot widen the area past its tier limits. The owner check happens here as
                // well: a claimed Torcherino with "others may not edit" ignores the packet.
                final BlockEntity blockEntity = sender.level().getBlockEntity(message.pos());
                if (blockEntity instanceof TileTorcherino torcherino && torcherino.mayEdit(sender)) {
                    torcherino.setValues(message.xRange(), message.zRange(), message.yRange(),
                            message.speed(), message.redstoneMode());
                }
            });
        }
    }

    /** Owner settings coming back from the client. */
    public record OwnerSettingsMessage(BlockPos pos, boolean othersCanEdit, boolean unclaim) {

        public OwnerSettingsMessage(FriendlyByteBuf buf) {
            this(buf.readBlockPos(), buf.readBoolean(), buf.readBoolean());
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeBlockPos(this.pos);
            buf.writeBoolean(this.othersCanEdit);
            buf.writeBoolean(this.unclaim);
        }

        public static OwnerSettingsMessage decode(FriendlyByteBuf buf) {
            return new OwnerSettingsMessage(buf);
        }

        public static void handle(OwnerSettingsMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
            final NetworkEvent.Context context = contextSupplier.get();
            context.setPacketHandled(true);
            final ServerPlayer sender = context.getSender();
            if (sender == null) {
                return;
            }
            context.enqueueWork(() -> {
                final BlockEntity blockEntity = sender.level().getBlockEntity(message.pos());
                if (!(blockEntity instanceof TileTorcherino torcherino) || !torcherino.isOwner(sender)) {
                    // Only the owner may change who is allowed to edit, or drop the claim.
                    sender.displayClientMessage(Component.translatable("message.torcherino.locked"), true);
                    return;
                }
                if (message.unclaim()) {
                    torcherino.clearOwner();
                } else {
                    torcherino.setOthersCanEdit(message.othersCanEdit());
                }
            });
        }
    }

    /**
     * Server side request to open the editor on a client.
     *
     * <p>Carries the translation key of the block so the window title follows the block
     * type (a compressed jack o'lantern is not titled "Torcherino"), plus the tier factor
     * so the editor can print the same percentage the action bar uses, and the owner
     * information the editor shows.</p>
     */
    public record OpenScreenMessage(BlockPos pos, String titleKey, int xRange, int zRange, int yRange, int speed,
                                    int redstoneMode, int tierMultiplier, String ownerDisplay,
                                    boolean othersCanEdit, boolean viewerIsOwner) {

        public OpenScreenMessage(FriendlyByteBuf buf) {
            this(buf.readBlockPos(), buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(),
                    buf.readInt(), buf.readInt(), buf.readUtf(), buf.readBoolean(), buf.readBoolean());
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeBlockPos(this.pos);
            buf.writeUtf(this.titleKey);
            buf.writeInt(this.xRange);
            buf.writeInt(this.zRange);
            buf.writeInt(this.yRange);
            buf.writeInt(this.speed);
            buf.writeInt(this.redstoneMode);
            buf.writeInt(this.tierMultiplier);
            buf.writeUtf(this.ownerDisplay);
            buf.writeBoolean(this.othersCanEdit);
            buf.writeBoolean(this.viewerIsOwner);
        }

        public static OpenScreenMessage decode(FriendlyByteBuf buf) {
            return new OpenScreenMessage(buf);
        }

        public static void handle(OpenScreenMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
            final NetworkEvent.Context context = contextSupplier.get();
            context.setPacketHandled(true);
            if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                context.enqueueWork(() -> TorcherinoForgeClientEvents.openScreen(message.pos(), message.titleKey(),
                        message.xRange(), message.zRange(), message.yRange(), message.speed(),
                        message.redstoneMode(), message.tierMultiplier(), message.ownerDisplay(),
                        message.othersCanEdit(), message.viewerIsOwner()));
            }
        }
    }
}
