/*
 * 本文件：Fabric 侧客户端入口。
 * 说明：把 11 个方块登记进 cutout 渲染层（否则透明像素会变黑）、注册「改装键（左 Shift）」并上报状态、
 *      并接收服务端的「打开编辑界面」包。
 */
package com.sci.torcherino.client;

import com.sci.torcherino.blocks.ModBlocks;
import com.sci.torcherino.client.screen.TorcherinoScreen;
import com.sci.torcherino.network.TorcherinoFabricNetworking;
import com.sci.torcherino.platform.Services;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import org.lwjgl.glfw.GLFW;

/**
 * Client entry point: render layers, the modifier key and the editor screen.
 *
 * <p>The modifier key defaults to Left Shift. Its translation key keeps the classic
 * (misspelled) spelling so existing key bindings keep working.</p>
 */
public class TorcherinoFabricClient implements ClientModInitializer {

    public static final KeyMapping USAGE_KEY = new KeyMapping(
            "key.torcherino.useage_key",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_SHIFT,
            "key.categories.gameplay");

    /** Re-send interval of the modifier key hint, in client ticks. */
    private static final int KEEP_ALIVE_TICKS = 20;

    private boolean lastState;
    private int keepAliveTicks = KEEP_ALIVE_TICKS;

    @Override
    public void onInitializeClient() {
        // Custom blocks default to the solid render layer, which paints the fully
        // transparent pixels of the torch textures black instead of discarding them.
        // Vanilla registers its own torch blocks as cutout, so every block of this
        // mod is put on the same layer here.
        for (Block block : ModBlocks.blocksForRendering()) {
            BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout());
        }

        KeyBindingHelper.registerKeyBinding(USAGE_KEY);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.isPaused() || client.player == null) {
                return;
            }
            final boolean down = USAGE_KEY.isDown();
            // The server only uses this as a hint when a block is right clicked, so it is
            // re-sent on every change and once per second on top of that. A single lost
            // packet would otherwise leave the server with a stale modifier state.
            if (down != this.lastState || --this.keepAliveTicks <= 0) {
                this.keepAliveTicks = KEEP_ALIVE_TICKS;
                Services.PLATFORM.sendModifierKeyToServer(down);
                this.lastState = down;
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(TorcherinoFabricNetworking.OPEN_SCREEN,
                (client, handler, buf, responseSender) -> {
                    final BlockPos pos = buf.readBlockPos();
                    final String titleKey = buf.readUtf();
                    final int xRange = buf.readInt();
                    final int zRange = buf.readInt();
                    final int yRange = buf.readInt();
                    final int speed = buf.readInt();
                    final int redstoneMode = buf.readInt();
                    final int tierMultiplier = buf.readInt();
                    client.execute(() -> client.setScreen(new TorcherinoScreen(pos, titleKey, xRange, zRange,
                            yRange, speed, redstoneMode, tierMultiplier)));
                });
    }
}
