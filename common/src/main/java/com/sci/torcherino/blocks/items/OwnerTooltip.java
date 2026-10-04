/*
 * 本文件：加速火把物品的提示行工具。
 * 说明：方块被破坏时，掉落物会带上 BlockEntityTag（见掉落表的 copy_nbt），其中包含所有者信息；
 *      这里把它读出来，在物品栏的提示里补一行「所有者：xxx」，与界面上的写法保持一致。
 *      名字优先用放置时记下的 OwnerName；没有就地在场时用 UUID 找玩家名，最后退化为短 UUID。
 */
package com.sci.torcherino.blocks.items;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

final class OwnerTooltip {

    /** The very key the editor uses for its owner line, so both always read the same. */
    private static final String OWNER_KEY = "gui.torcherino.owner";
    /** Key the block entity writes its data under inside the item. */
    private static final String BLOCK_ENTITY_KEY = "BlockEntityTag";

    private OwnerTooltip() {
    }

    /**
     * Appends the owner line of the Torcherino this item was broken from, if it carries one.
     *
     * @param level the level the tooltip is built in; used to resolve a name from the UUID
     *              while that player is around. May be {@code null}.
     */
    static void appendOwner(ItemStack stack, Level level, List<Component> tooltip) {
        final CompoundTag data = stack.getTagElement(BLOCK_ENTITY_KEY);
        if (data == null) {
            return;
        }
        String name = data.contains("OwnerName") ? data.getString("OwnerName") : "";
        if (name.isEmpty() && data.contains("Owner")) {
            name = resolveName(data.getString("Owner"), level);
        }
        if (name.isEmpty()) {
            return;
        }
        tooltip.add(Component.translatable(OWNER_KEY, name).withStyle(ChatFormatting.GRAY));
    }

    /** Name of the recorded owner: the present player, or a short form of the UUID. */
    private static String resolveName(String ownerId, Level level) {
        if (ownerId.isEmpty()) {
            return ownerId;
        }
        final UUID uuid;
        try {
            uuid = UUID.fromString(ownerId);
        } catch (IllegalArgumentException e) {
            // Not a UUID: show what is stored instead of hiding it.
            return ownerId;
        }
        if (level != null) {
            final Player player = level.getPlayerByUUID(uuid);
            if (player != null) {
                return player.getName().getString();
            }
        }
        // Offline and unknown: a short form keeps the line readable without guessing a name.
        return ownerId.length() <= 8 ? ownerId : ownerId.substring(0, 8);
    }
}
