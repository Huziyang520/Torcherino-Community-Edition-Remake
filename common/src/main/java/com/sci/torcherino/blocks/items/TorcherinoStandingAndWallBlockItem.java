/*
 * 本文件：火把系列（Torcherino）的物品形态，地火把与墙火把共用一件物品。
 * 说明：只是给 StandingAndWallBlockItem 加一行「所有者」提示 —— 方块被破坏时掉落物会带上
 *      BlockEntityTag，所有者信息就在里面，见 OwnerTooltip。
 */
package com.sci.torcherino.blocks.items;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Wall aware torch item that keeps showing the owner of the Torcherino it was broken from.
 *
 * <p>Only the tooltip is added; placing the block is untouched, and the recorded owner is
 * restored by the ordinary {@code BlockEntityTag} path of {@code BlockItem}.</p>
 */
public class TorcherinoStandingAndWallBlockItem extends StandingAndWallBlockItem {

    public TorcherinoStandingAndWallBlockItem(Block standingBlock, Block wallBlock, Properties properties,
                                              Direction attachmentDirection) {
        super(standingBlock, wallBlock, properties, attachmentDirection);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        OwnerTooltip.appendOwner(stack, level, tooltip);
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
