/*
 * 本文件：NeoForge 侧客户端接线（只在客户端加载）。
 * 说明：注册 NeoForge 自带的配置界面、进服时打印配方开关提示、以及接收「打开编辑界面」包。
 *      渲染层不再需要单独登记 —— 26.x 的方块渲染类型由方块模型 JSON 决定。
 */
package com.sci.torcherino.client;

import com.sci.torcherino.TorcherinoConfig;
import com.sci.torcherino.client.screen.TorcherinoScreen;
import com.sci.torcherino.network.TorcherinoPayloads;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client entry point of the NeoForge module.
 *
 * <p>Only touched from inside a {@code FMLEnvironment.dist.isClient()} guard in the mod
 * constructor, so a dedicated server never loads any of the classes referenced here.</p>
 */
public final class TorcherinoNeoForgeClient {

    private TorcherinoNeoForgeClient() {
    }

    public static void init(ModContainer container) {
        // NeoForge's own configuration screen, shown by the config button of the mod list.
        // It replaces Cloth Config on this loader and needs no extra dependency.
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (IConfigScreenFactory) (modContainer, parent) -> new ConfigurationScreen(modContainer, parent));

        // Chat notice about the recipe switches, printed once per world join.
        NeoForge.EVENT_BUS.addListener(TorcherinoNeoForgeClient::onLoggingIn);
    }

    private static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        if (TorcherinoConfig.joinNotice && event.getPlayer() != null) {
            event.getPlayer().sendSystemMessage(
                    Component.translatable("message.torcherino.join_notice"));
        }
    }

    /** Handles the server's "open the editor" packet. Runs on the client thread. */
    public static void handleOpenScreen(TorcherinoPayloads.OpenScreen payload) {
        // setScreenAndShow is the only call that reliably shows the editor from the packet
        // handler on this version (a plain setScreen here did nothing). Do not "clean it up"
        // without an in-game test: it is the state the user signed off on.
        Minecraft.getInstance().setScreenAndShow(new TorcherinoScreen(
                payload.pos(), payload.titleKey(), payload.xRange(), payload.zRange(), payload.yRange(),
                payload.speed(), payload.redstoneMode(), payload.tierMultiplier(), payload.ownerDisplay(),
                payload.othersCanEdit(), payload.viewerIsOwner(), payload.ownerEnabled()));
    }
}
