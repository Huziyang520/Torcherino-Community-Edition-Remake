/*
 * 本文件：Jade（Waila 后继）联动 —— 准星指向加速火把时显示倍率 / 状态 / 所有者（可选）。
 * 说明：只在客户端加载，且只在 Jade 已安装时才会被它加载；模组不内置也不依赖 Jade。
 *      三个显示项各自是一个 Jade 插件配置项（默认全开），可在 config/jade/ 的插件配置里关闭。
 *      数据全部来自客户端已同步的方块实体（倍率 / 红石模式 / 所有者都会随 NBT 同步），因此不需要服务端数据包。
 */
package com.sci.torcherino.compat.jade;

import com.sci.torcherino.Constants;
import com.sci.torcherino.blocks.blocks.BlockLanterino;
import com.sci.torcherino.blocks.blocks.BlockTorcherino;
import com.sci.torcherino.blocks.blocks.BlockWallTorcherino;
import com.sci.torcherino.blocks.tiles.TileTorcherino;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.config.IPluginConfig;

import java.util.UUID;

/**
 * Jade component provider for every block of the Torcherino family.
 *
 * <p>Three lines, each behind its own Jade plugin config switch (all on by default): the
 * multiplier, the running state together with a note when redstone is involved, and the
 * owner. Nothing here needs a server round trip - the client already owns the synchronised
 * block entity data that the editor uses.</p>
 */
public final class TorcherinoJadeProvider implements IBlockComponentProvider {

    public static final TorcherinoJadeProvider INSTANCE = new TorcherinoJadeProvider();

    /** Provider id, also the name shown in Jade's own provider list. */
    private static final ResourceLocation UID = new ResourceLocation(Constants.MOD_ID, "acceleration");
    /** Config keys of the three lines. */
    private static final ResourceLocation SHOW_SPEED = new ResourceLocation(Constants.MOD_ID, "speed");
    private static final ResourceLocation SHOW_STATE = new ResourceLocation(Constants.MOD_ID, "state");
    private static final ResourceLocation SHOW_OWNER = new ResourceLocation(Constants.MOD_ID, "owner");

    private TorcherinoJadeProvider() {
    }

    /**
     * Wires the provider and its three switches into Jade. Called from the loader specific
     * plugin classes, which are the only entry points Jade knows about.
     */
    public static void register(IWailaClientRegistration registration) {
        // Defaults are on: the tooltip only shows what the player asked Jade to show anyway,
        // and each line can be switched off in Jade's plugin configuration.
        registration.addConfig(SHOW_SPEED, true);
        registration.addConfig(SHOW_STATE, true);
        registration.addConfig(SHOW_OWNER, true);
        // All eleven blocks of the mod extend one of these three classes.
        registration.registerBlockComponent(INSTANCE, BlockTorcherino.class);
        registration.registerBlockComponent(INSTANCE, BlockWallTorcherino.class);
        registration.registerBlockComponent(INSTANCE, BlockLanterino.class);
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof TileTorcherino torcherino)) {
            return;
        }
        if (config.get(SHOW_SPEED)) {
            tooltip.add(Component.translatable("gui.torcherino.speed", torcherino.getSpeedPercent() + "%"));
        }
        if (config.get(SHOW_STATE)) {
            Component state = Component.translatable("gui.torcherino.jade.state",
                    Component.translatable(torcherino.isActive()
                            ? "gui.torcherino.config.on"
                            : "gui.torcherino.config.off"));
            if (torcherino.getRedstoneMode() != TileTorcherino.REDSTONE_IGNORED) {
                // Any other mode means the redstone signal decides, which the player should see.
                state = state.copy().append(Component.translatable("gui.torcherino.jade.redstoneNote"));
            }
            tooltip.add(state);
        }
        if (config.get(SHOW_OWNER)) {
            tooltip.add(ownerLabel(torcherino));
        }
    }

    /**
     * "Owner: name" line. The name comes from the client's player list when the owner is known
     * there (online, or seen in this session); otherwise the UUID is shortened, because an
     * offline name is not available on the client.
     */
    private static Component ownerLabel(TileTorcherino torcherino) {
        final String owner = torcherino.getOwnerId();
        if (owner == null || owner.isEmpty()) {
            return Component.translatable("gui.torcherino.owner",
                    Component.translatable("gui.torcherino.owner.none"));
        }
        try {
            final UUID uuid = UUID.fromString(owner);
            final Minecraft client = Minecraft.getInstance();
            if (client.getConnection() != null) {
                final PlayerInfo info = client.getConnection().getPlayerInfo(uuid);
                if (info != null) {
                    return Component.translatable("gui.torcherino.owner", info.getProfile().getName());
                }
            }
        } catch (IllegalArgumentException e) {
            // Unreadable owner id: show what is stored instead of hiding it.
            return Component.translatable("gui.torcherino.owner", owner);
        }
        return Component.translatable("gui.torcherino.owner", owner.substring(0, Math.min(8, owner.length())));
    }
}
