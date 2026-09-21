/*
 * 本文件：Forge 侧客户端事件处理器（只在 Dist.CLIENT 下被引用）。
 * 说明：注册「改装键（左 Shift）」、把 11 个方块登记进 cutout 渲染层（否则透明像素会变黑）、每 tick 轮询按键并把状态变化发给服务端。
 */
package com.sci.torcherino.client;

import com.sci.torcherino.blocks.ModBlocks;
import com.sci.torcherino.client.screen.TorcherinoConfigScreen;
import com.sci.torcherino.client.screen.TorcherinoScreen;
import com.sci.torcherino.network.TorcherinoNetwork;

import net.minecraft.client.gui.screens.Screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Client only Forge handlers. This class must only be touched from inside a
 * {@code Dist.CLIENT} guard, since it references client only types.
 */
public final class TorcherinoForgeClientEvents {

    public static final KeyMapping USAGE_KEY = new KeyMapping(
            "key.torcherino.useage_key",
            InputConstants.Type.KEYSYM,
            // Right shift, not left: the left one is vanilla's sneak key, and sharing it made
            // the key list show a conflict (and made sneak look broken). Sneaking still works
            // as the modifier in the classic interaction, so nothing is lost.
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "key.categories.gameplay");

    private static boolean lastState;

    private TorcherinoForgeClientEvents() {
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(USAGE_KEY);
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isPaused() || minecraft.player == null) {
            return;
        }
        final boolean down = USAGE_KEY.isDown();
        if (down != lastState) {
            TorcherinoNetwork.sendToServer(down);
            lastState = down;
        }
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
    public static void openScreen(BlockPos pos, String titleKey, int xRange, int zRange, int yRange, int speed,
                                  int redstoneMode, int tierMultiplier) {
        Minecraft.getInstance().setScreen(
                new TorcherinoScreen(pos, titleKey, xRange, zRange, yRange, speed, redstoneMode, tierMultiplier));
    }

    /**
     * Entry point of the configuration screen, handed to Forge so that the mod list (and
     * "Configured") can open it.
     */
    public static Screen createConfigScreen(Screen parent) {
        return new TorcherinoConfigScreen(parent);
    }
}
