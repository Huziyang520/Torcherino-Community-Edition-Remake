package com.sci.torcherino;

import com.sci.torcherino.blocks.ModBlocks;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
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
        // Right click on a Torcherino toggles mode / speed, exactly like the 1.12.2
        // PlayerInteractEvent.RightClickBlock handler. The result is sided so client and
        // server agree and neither block nor item usage is processed afterwards.
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) ->
                TorcherinoInteraction.useBlock(level, hitResult.getBlockPos(), player, hand)
                        ? InteractionResult.sidedSuccess(level.isClientSide())
                        : InteractionResult.PASS);

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                TorcherinoKeyStates.clear(handler.getPlayer()));

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(entries -> ModBlocks.creativeItems().forEach(entries::accept));
    }
}
