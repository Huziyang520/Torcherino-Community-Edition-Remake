/*
 * 本文件：Forge 侧客户端事件处理器（只在 Dist.CLIENT 下被引用）。
 * 说明：把 11 个方块登记进 cutout 渲染层（否则透明像素会变黑）、打开编辑界面、提供配置界面入口。
 *      改装键就是原版潜行（服务端直接读 isShiftKeyDown），因此这里不注册任何按键。
 */
package com.sci.torcherino.client;

import com.sci.torcherino.blocks.ModBlocks;
import com.sci.torcherino.client.screen.TorcherinoConfigScreen;
import com.sci.torcherino.client.screen.TorcherinoScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client only Forge handlers. This class must only be touched from inside a
 * {@code Dist.CLIENT} guard, since it references client only types.
 */
public final class TorcherinoForgeClientEvents {

    private TorcherinoForgeClientEvents() {
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
     * Entry point of the configuration screen, handed to Forge so that the mod list can
     * open it (Configured reads the ForgeConfigSpec instead).
     */
    public static Screen createConfigScreen(Screen parent) {
        return new TorcherinoConfigScreen(parent);
    }
}
