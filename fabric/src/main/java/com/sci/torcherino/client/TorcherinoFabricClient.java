/*
 * 本文件：Fabric 侧客户端入口。
 * 说明：接收服务端的「打开编辑界面」包并在下一 tick 打开屏幕。
 *      26.x 的方块渲染类型由方块模型 JSON 决定，因此不再需要手工登记 cutout 渲染层。
 *      改装键就是原版潜行（服务端直接读 isShiftKeyDown），所以这里不注册任何按键。
 */
package com.sci.torcherino.client;

import com.sci.torcherino.TorcherinoConfig;
import com.sci.torcherino.client.screen.TorcherinoScreen;
import com.sci.torcherino.network.TorcherinoPayloads;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Client entry point: the editor screen and the join notice.
 */
public class TorcherinoFabricClient implements ClientModInitializer {

    /** Set on join, printed on the first tick where the local player exists. */
    private static boolean pendingJoinNotice;

    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> pendingJoinNotice = true);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!pendingJoinNotice || client.player == null) {
                return;
            }
            pendingJoinNotice = false;
            if (TorcherinoConfig.joinNotice) {
                client.player.sendSystemMessage(Component.translatable("message.torcherino.join_notice"));
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(TorcherinoPayloads.OpenScreen.TYPE,
                (payload, context) -> context.client().execute(() -> open(payload)));
    }

    private static void open(TorcherinoPayloads.OpenScreen payload) {
        Minecraft.getInstance().setScreen(new TorcherinoScreen(
                payload.pos(), payload.titleKey(), payload.xRange(), payload.zRange(), payload.yRange(),
                payload.speed(), payload.redstoneMode(), payload.tierMultiplier(), payload.ownerDisplay(),
                payload.othersCanEdit(), payload.viewerIsOwner(), payload.ownerEnabled()));
    }
}
