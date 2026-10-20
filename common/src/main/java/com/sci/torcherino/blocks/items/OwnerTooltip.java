/*
 * 本文件：加速火把物品的提示行工具。
 * 说明：方块被破坏时，掉落物会带上 block_entity_data 组件（见掉落表的 copy_components），其中包含所有者信息；
 *      这里把它读出来，在物品栏的提示里补一行「所有者：xxx」，与界面上的写法保持一致。
 *      名字优先用放置时记下的 OwnerName；没有就地在场时用 UUID 找玩家名，最后退化为短 UUID。
 */
package com.sci.torcherino.blocks.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.List;
import java.util.UUID;

final class OwnerTooltip {

    /** The very key the editor uses for its owner line, so both always read the same. */
    private static final String OWNER_KEY = "gui.torcherino.owner";
    private OwnerTooltip() {
    }

    /**
     * Appends the owner line of the Torcherino this item was broken from, if it carries one.
     *
     * <p>1.21 tooltips no longer expose the level, so the name stored next to the UUID is the
     * only readable source; when it is missing the UUID is shortened instead.</p>
     */
    static void appendOwner(ItemStack stack, List<Component> tooltip) {
        // Since 1.20.5 the block entity payload of a dropped block lives in the
        // minecraft:block_entity_data component instead of a BlockEntityTag NBT element.
        final CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null) {
            return;
        }
        final CompoundTag tag = data.copyTag();
        String name = tag.contains("OwnerName") ? tag.getString("OwnerName") : "";
        if (name.isEmpty() && tag.contains("Owner")) {
            name = shorten(tag.getString("Owner"));
        }
        if (name.isEmpty()) {
            return;
        }
        tooltip.add(Component.translatable(OWNER_KEY, name).withStyle(ChatFormatting.GRAY));
    }

    /** Short form of the recorded owner: readable without guessing a name. */
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
