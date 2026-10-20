package com.sci.torcherino.blocks.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.List;
import java.util.UUID;

/**
 * Reads the owner of the Torcherino an item was broken from.
 *
 * <p>The loot table copies the block entity data onto the drop as the
 * {@code minecraft:block_entity_data} component. The owner line is built from the stored
 * name, or from a short form of the UUID when no name was recorded. 26.x tooltips no longer
 * expose the level, so an offline UUID cannot be resolved to a player name here.</p>
 */
final class OwnerTooltip {

    /** The very key the editor uses for its owner line, so both always read the same. */
    private static final String OWNER_KEY = "gui.torcherino.owner";

    private OwnerTooltip() {
    }

    static void appendOwner(ItemStack stack, java.util.function.Consumer<Component> tooltip) {
        final TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null) {
            return;
        }
        final var tag = data.copyTagWithoutId();
        String name = tag.getString("OwnerName").orElse("");
        if (name.isEmpty()) {
            final String owner = tag.getString("Owner").orElse("");
            name = shorten(owner);
        }
        if (name.isEmpty()) {
            return;
        }
        tooltip.accept(Component.translatable(OWNER_KEY, name).withStyle(ChatFormatting.GRAY));
    }

    /** Short form of a stored UUID, so the line stays readable without guessing a name. */
    private static String shorten(String ownerId) {
        if (ownerId.isEmpty()) {
            return ownerId;
        }
        try {
            UUID.fromString(ownerId);
        } catch (IllegalArgumentException e) {
            // Not a UUID: show what is stored instead of hiding it.
            return ownerId;
        }
        return ownerId.length() <= 8 ? ownerId : ownerId.substring(0, 8);
    }
}
