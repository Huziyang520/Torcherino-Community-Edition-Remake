/*
 * 本文件：NeoForge 侧事件处理器。
 * 说明：右键方块打开界面并取消原版交互、玩家登出时清理按键状态、把物品塞进原版功能方块创造栏。
 */
package com.sci.torcherino;

import com.sci.torcherino.blocks.ModBlocks;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * NeoForge event handlers: right click, logout cleanup and creative tab contents.
 */
public final class TorcherinoNeoForgeEvents {

    private TorcherinoNeoForgeEvents() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (TorcherinoInteraction.useBlock(event.getLevel(), event.getPos(), event.getEntity(), event.getHand())) {
            event.setUseBlock(net.minecraft.util.TriState.FALSE);
            event.setUseItem(net.minecraft.util.TriState.FALSE);
            event.setCanceled(true);
        }
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        TorcherinoKeyStates.clear(event.getEntity());
    }

    public static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            ModBlocks.creativeItems().forEach(event::accept);
        }
    }
}
