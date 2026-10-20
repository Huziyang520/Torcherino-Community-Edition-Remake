/*
 * 本文件：NeoForge 侧客户端事件处理器（只在客户端加载）。
 * 说明：把 16 个方块登记进 cutout 渲染层（否则透明像素会变黑）、接收服务端的「打开编辑界面」负载、进服提示。
 *      改装键就是原版潜行（服务端直接读 isShiftKeyDown），因此这里不注册任何按键。
 *      配置界面由 NeoForge 自带的 ConfigurationScreen 提供，见 TorcherinoNeoForge。
 */
package com.sci.torcherino.client;

import com.sci.torcherino.TorcherinoConfig;
import com.sci.torcherino.blocks.ModBlocks;
import com.sci.torcherino.client.screen.TorcherinoScreen;
import com.sci.torcherino.network.TorcherinoPayloads.OpenScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

/**
 * Client only NeoForge handlers. This class must only be touched from inside a
 * {@code Dist.CLIENT} guard, since it references client only types.
 */
public final class TorcherinoNeoClientEvents {

    private TorcherinoNeoClientEvents() {
    }

    /**
     * Custom blocks default to {@code RenderType.solid()}, which paints the fully
     * transparent pixels of the torch textures black instead of discarding them.
     * Vanilla registers its own torch blocks as cutout, so every block of this mod
     * is put on the same layer here.
     */
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (Block block : ModBlocks.blocksForRendering()) {
                ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout());
            }
        });
    }

    /**
     * Client side half of the editor: the server asked for it, so open it. Only reachable
     * on the physical client, which is why this class is never touched on a server.
     */
    public static void openScreen(OpenScreen payload) {
        Minecraft.getInstance().setScreen(new TorcherinoScreen(payload.pos(), payload.titleKey(),
                payload.xRange(), payload.zRange(), payload.yRange(), payload.speed(), payload.redstoneMode(),
                payload.tierMultiplier(), payload.ownerDisplay(), payload.othersCanEdit(),
                payload.viewerIsOwner(), payload.ownerEnabled()));
    }

    /**
     * Prints the recipe notice in chat after joining a world, unless the player turned it
     * off in the client configuration.
     */
    public static void onLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        if (!TorcherinoConfig.joinNotice) {
            return;
        }
        event.getPlayer().displayClientMessage(Component.translatable("message.torcherino.join_notice"), false);
    }
}
