/*
 * 本文件：两端 loader 共用的网络负载定义（只依赖原版类）。
 * 说明：四个负载 —— 改装键状态（C2S）、编辑界面数值回传（C2S）、所有者设置（C2S）、打开编辑界面（S2C）。
 *      Fabric 与 NeoForge 各自注册这些负载类型，编解码只有这一份，避免两端字段顺序不一致。
 */
package com.sci.torcherino.network;

import com.sci.torcherino.Constants;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * All custom payloads of the mod.
 *
 * <p>They live in the common project because the format is loader independent: both loaders
 * register the same types and reuse the same stream codecs, so the two sides can never drift
 * apart field by field.</p>
 */
public final class TorcherinoPayloads {

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, path);
    }

    private TorcherinoPayloads() {
    }

    /** Client to server: is the modifier key (sneak) held down? */
    public record ModifierKey(boolean pressed) implements CustomPacketPayload {

        public static final Type<ModifierKey> TYPE = new Type<>(id("modifier_key"));

        public static final StreamCodec<RegistryFriendlyByteBuf, ModifierKey> CODEC = StreamCodec.of(
                (buf, value) -> buf.writeBoolean(value.pressed()),
                buf -> new ModifierKey(buf.readBoolean()));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Client to server: the values edited in the graphical editor. */
    public record SetValues(BlockPos pos, int xRange, int zRange, int yRange, int speed, int redstoneMode)
            implements CustomPacketPayload {

        public static final Type<SetValues> TYPE = new Type<>(id("set_values"));

        public static final StreamCodec<RegistryFriendlyByteBuf, SetValues> CODEC = StreamCodec.of(
                (buf, value) -> {
                    buf.writeBlockPos(value.pos());
                    buf.writeVarInt(value.xRange());
                    buf.writeVarInt(value.zRange());
                    buf.writeVarInt(value.yRange());
                    buf.writeVarInt(value.speed());
                    buf.writeVarInt(value.redstoneMode());
                },
                buf -> new SetValues(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt(),
                        buf.readVarInt(), buf.readVarInt(), buf.readVarInt()));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Client to server: owner only, either flips "others may edit" or drops the claim. */
    public record OwnerSettings(BlockPos pos, boolean othersCanEdit, boolean unclaim) implements CustomPacketPayload {

        public static final Type<OwnerSettings> TYPE = new Type<>(id("owner_settings"));

        public static final StreamCodec<RegistryFriendlyByteBuf, OwnerSettings> CODEC = StreamCodec.of(
                (buf, value) -> {
                    buf.writeBlockPos(value.pos());
                    buf.writeBoolean(value.othersCanEdit());
                    buf.writeBoolean(value.unclaim());
                },
                buf -> new OwnerSettings(buf.readBlockPos(), buf.readBoolean(), buf.readBoolean()));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Server to client: open the editor, with everything the window needs to render. */
    public record OpenScreen(BlockPos pos, String titleKey, int xRange, int zRange, int yRange, int speed,
                             int redstoneMode, int tierMultiplier, String ownerDisplay,
                             boolean othersCanEdit, boolean viewerIsOwner, boolean ownerEnabled)
            implements CustomPacketPayload {

        public static final Type<OpenScreen> TYPE = new Type<>(id("open_screen"));

        public static final StreamCodec<RegistryFriendlyByteBuf, OpenScreen> CODEC = StreamCodec.of(
                (buf, value) -> {
                    buf.writeBlockPos(value.pos());
                    buf.writeUtf(value.titleKey());
                    buf.writeVarInt(value.xRange());
                    buf.writeVarInt(value.zRange());
                    buf.writeVarInt(value.yRange());
                    buf.writeVarInt(value.speed());
                    buf.writeVarInt(value.redstoneMode());
                    buf.writeVarInt(value.tierMultiplier());
                    buf.writeUtf(value.ownerDisplay());
                    buf.writeBoolean(value.othersCanEdit());
                    buf.writeBoolean(value.viewerIsOwner());
                    buf.writeBoolean(value.ownerEnabled());
                },
                buf -> new OpenScreen(buf.readBlockPos(), buf.readUtf(), buf.readVarInt(), buf.readVarInt(),
                        buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readUtf(),
                        buf.readBoolean(), buf.readBoolean(), buf.readBoolean()));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
