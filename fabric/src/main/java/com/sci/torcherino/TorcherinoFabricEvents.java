/*
 * 本文件：Fabric 侧事件接线。
 * 说明：右键方块打开界面、玩家掉线时清理按键状态、并把物品塞进原版功能方块创造栏。
 */
package com.sci.torcherino;

import com.sci.torcherino.blocks.ModBlocks;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTabs;

/**
 * Fabric event wiring: right click handling, key state cleanup on disconnect and
 * creative tab contents.
 */
public final class TorcherinoFabricEvents {

    private TorcherinoFabricEvents() {
    }

    public static void register() {
        // Right click on a Torcherino opens the editor / cycles the mode. The result is
        // sided so client and server agree and neither block nor item usage is processed.
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) ->
                TorcherinoInteraction.useBlock(level, hitResult.getBlockPos(), player, hand)
                        ? level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER
                        : InteractionResult.PASS);

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                TorcherinoKeyStates.clear(handler.getPlayer()));

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(entries -> ModBlocks.creativeItems().forEach(entries::accept));
    }
}
