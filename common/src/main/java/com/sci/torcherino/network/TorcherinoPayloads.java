/*
 * 本文件：两个加载器共用的网络包定义（1.20.5 起的 payload 体系）。
 * 说明：四个包 —— 改装键状态（C2S）、编辑界面数值回传（C2S）、所有者设置（C2S）、打开编辑界面（S2C）。
 *      包体只依赖原版类型，因此放在 common；各加载器只负责注册类型与分发，字段顺序必须与这里一致。
 */
package com.sci.torcherino.network;

import com.sci.torcherino.Constants;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * The four packets of the mod, as {@link CustomPacketPayload} records.
 *
 * <p>Written by hand instead of with {@code StreamCodec.composite} so the wire format is
 * explicit and stays identical on both loaders: every integer is a var int, the block
 * position uses the vanilla encoding, and the strings are UTF.</p>
 */
public final class TorcherinoPayloads {

    private TorcherinoPayloads() {
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, path);
    }

    /** Client to server: is the modifier key (sneak) held down right now? */
    public record ModifierKey(boolean pressed) implements CustomPacketPayload {

        public static final Type<ModifierKey> TYPE = new Type<>(id("modifier_key"));

        public static final StreamCodec<RegistryFriendlyByteBuf, ModifierKey> CODEC = StreamCodec.of(
                (buf, payload) -> buf.writeBoolean(payload.pressed()),
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
                (buf, payload) -> {
                    buf.writeBlockPos(payload.pos());
                    buf.writeVarInt(payload.xRange());
                    buf.writeVarInt(payload.zRange());
                    buf.writeVarInt(payload.yRange());
                    buf.writeVarInt(payload.speed());
                    buf.writeVarInt(payload.redstoneMode());
                },
                buf -> new SetValues(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt(),
                        buf.readVarInt(), buf.readVarInt(), buf.readVarInt()));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Client to server, owner only: flip "others may edit" or drop the claim entirely. */
    public record OwnerSettings(BlockPos pos, boolean othersCanEdit, boolean unclaim)
            implements CustomPacketPayload {

        public static final Type<OwnerSettings> TYPE = new Type<>(id("owner_settings"));

        public static final StreamCodec<RegistryFriendlyByteBuf, OwnerSettings> CODEC = StreamCodec.of(
                (buf, payload) -> {
                    buf.writeBlockPos(payload.pos());
                    buf.writeBoolean(payload.othersCanEdit());
                    buf.writeBoolean(payload.unclaim());
                },
                buf -> new OwnerSettings(buf.readBlockPos(), buf.readBoolean(), buf.readBoolean()));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * Server to client: open the editor. Carries everything the screen shows, so the client
     * never has to query the block entity again - the window title (translation key), the
     * tier factor and the owner information all come along.
     */
    public record OpenScreen(BlockPos pos, String titleKey, int xRange, int zRange, int yRange,
                             int speed, int redstoneMode, int tierMultiplier, String ownerDisplay,
                             boolean othersCanEdit, boolean viewerIsOwner, boolean ownerEnabled)
            implements CustomPacketPayload {

        public static final Type<OpenScreen> TYPE = new Type<>(id("open_screen"));

        public static final StreamCodec<RegistryFriendlyByteBuf, OpenScreen> CODEC = StreamCodec.of(
                (buf, payload) -> {
                    buf.writeBlockPos(payload.pos());
                    buf.writeUtf(payload.titleKey());
                    buf.writeVarInt(payload.xRange());
                    buf.writeVarInt(payload.zRange());
                    buf.writeVarInt(payload.yRange());
                    buf.writeVarInt(payload.speed());
                    buf.writeVarInt(payload.redstoneMode());
                    buf.writeVarInt(payload.tierMultiplier());
                    buf.writeUtf(payload.ownerDisplay());
                    buf.writeBoolean(payload.othersCanEdit());
                    buf.writeBoolean(payload.viewerIsOwner());
                    buf.writeBoolean(payload.ownerEnabled());
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
