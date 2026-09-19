/*
 * 本文件：Fabric 侧客户端入口。
 * 说明：把 11 个方块登记进 cutout 渲染层（否则透明像素会变黑）、注册「改装键（左 Shift）」并在其状态变化时发包给服务端。
 */
package com.sci.torcherino.client;

import com.sci.torcherino.blocks.ModBlocks;
import com.sci.torcherino.platform.Services;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import org.lwjgl.glfw.GLFW;

/**
 * Client entry point: registers the modifier key and reports state changes to the
 * server, so the server always knows whether the modifier key is held.
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

    private boolean lastState;

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
            if (client.isPaused()) {
                return;
            }
            if (client.player == null) {
                return;
            }
            final boolean down = USAGE_KEY.isDown();
            if (down != this.lastState) {
                Services.PLATFORM.sendModifierKeyToServer(down);
                this.lastState = down;
            }
        });
    }
}
