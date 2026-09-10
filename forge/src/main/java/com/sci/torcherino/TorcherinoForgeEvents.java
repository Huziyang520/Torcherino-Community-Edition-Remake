package com.sci.torcherino;

import com.sci.torcherino.blocks.ModBlocks;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;

/**
 * Forge event handlers, ported from the 1.12.2 {@code EventHandler}.
 */
public final class TorcherinoForgeEvents {

    private TorcherinoForgeEvents() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (TorcherinoInteraction.useBlock(event.getLevel(), event.getPos(), event.getEntity(), event.getHand())) {
            event.setUseBlock(Event.Result.DENY);
            event.setUseItem(Event.Result.DENY);
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
