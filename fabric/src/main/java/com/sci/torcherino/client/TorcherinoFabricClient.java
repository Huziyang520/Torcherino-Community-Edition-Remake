/*
 * 本文件：Fabric 侧客户端入口。
 * 说明：把 11 个方块登记进 cutout 渲染层（否则透明像素会变黑），并接收服务端的「打开编辑界面」包。
 *      改装键就是原版潜行（服务端直接读 isShiftKeyDown），因此这里不注册任何按键。
 */
package com.sci.torcherino.client;

import com.sci.torcherino.blocks.ModBlocks;
import com.sci.torcherino.client.screen.TorcherinoScreen;
import com.sci.torcherino.network.TorcherinoFabricNetworking;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

/**
 * Client entry point: render layers and the editor screen.
 */
public class TorcherinoFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Custom blocks default to the solid render layer, which paints the fully
        // transparent pixels of the torch textures black instead of discarding them.
        // Vanilla registers its own torch blocks as cutout, so every block of this
        // mod is put on the same layer here.
        for (Block block : ModBlocks.blocksForRendering()) {
            BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout());
        }

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
