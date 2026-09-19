/*
 * 本文件：全部方块与方块物品的注册。
 * 说明：共 11 个方块（四档火把各有落地 / 贴墙两种形态，另有三个南瓜灯），注册名规则见类注释；所有实例都延迟到加载器的注册窗口内创建。
 */
package com.sci.torcherino.blocks;

import com.sci.torcherino.blocks.blocks.BlockCompressedLanterino;
import com.sci.torcherino.blocks.blocks.BlockCompressedTorcherino;
import com.sci.torcherino.blocks.blocks.BlockDoubleCompressedLanterino;
import com.sci.torcherino.blocks.blocks.BlockDoubleCompressedTorcherino;
import com.sci.torcherino.blocks.blocks.BlockLanterino;
import com.sci.torcherino.blocks.blocks.BlockTorcherino;
import com.sci.torcherino.blocks.blocks.BlockTripleCompressedTorcherino;
import com.sci.torcherino.blocks.blocks.BlockWallCompressedTorcherino;
import com.sci.torcherino.blocks.blocks.BlockWallDoubleCompressedTorcherino;
import com.sci.torcherino.blocks.blocks.BlockWallTorcherino;
import com.sci.torcherino.blocks.blocks.BlockWallTripleCompressedTorcherino;
import com.sci.torcherino.platform.services.IRegistrationHelper;
import com.sci.torcherino.platform.services.RegistryEntry;

import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * All blocks and block items of the mod.
 *
 * <p>Minecraft separates floor and wall torches, so the four Torcherino tiers are eight
 * blocks ({@code blockX} plus {@code wall_blockX}) while the three Lanterino variants are
 * ordinary carved pumpkins. The wall variants have no item of their own - the torch item
 * places both through {@link StandingAndWallBlockItem}.</p>
 *
 * <p><b>Every instance is created lazily</b>, inside the supplier handed to the loader.
 * {@code Block}'s constructor writes to {@code BuiltInRegistries.BLOCK} through
 * {@code createIntrusiveHolder}, so instantiating a block here - while the mod
 * constructor runs and the registry is frozen - throws
 * {@code IllegalStateException: Registry is already frozen}.</p>
 */
public final class ModBlocks {

    public static RegistryEntry<BlockTorcherino> TORCHERINO;
    public static RegistryEntry<BlockWallTorcherino> WALL_TORCHERINO;
    public static RegistryEntry<BlockCompressedTorcherino> COMPRESSED_TORCHERINO;
    public static RegistryEntry<BlockWallCompressedTorcherino> WALL_COMPRESSED_TORCHERINO;
    public static RegistryEntry<BlockDoubleCompressedTorcherino> DOUBLE_COMPRESSED_TORCHERINO;
    public static RegistryEntry<BlockWallDoubleCompressedTorcherino> WALL_DOUBLE_COMPRESSED_TORCHERINO;
    public static RegistryEntry<BlockTripleCompressedTorcherino> TRIPLE_COMPRESSED_TORCHERINO;
    public static RegistryEntry<BlockWallTripleCompressedTorcherino> WALL_TRIPLE_COMPRESSED_TORCHERINO;
    public static RegistryEntry<BlockLanterino> LANTERINO;
    public static RegistryEntry<BlockCompressedLanterino> COMPRESSED_LANTERINO;
    public static RegistryEntry<BlockDoubleCompressedLanterino> DOUBLE_COMPRESSED_LANTERINO;

    private static final List<RegistryEntry<Item>> CREATIVE_ITEMS = new ArrayList<>();

    /**
     * Every registered block, in registration order. Kept separately from the fields
     * above so the client side render layer setup of each loader can iterate them
     * without knowing the individual names.
     */
    private static final List<RegistryEntry<? extends Block>> BLOCKS = new ArrayList<>();

    private ModBlocks() {
    }

    /**
     * Registers a block and remembers its handle for the render layer pass.
     */
    private static <T extends Block> RegistryEntry<T> registerBlock(IRegistrationHelper helper, String name, Supplier<T> supplier) {
        final RegistryEntry<T> entry = helper.registerBlock(name, supplier);
        BLOCKS.add(entry);
        return entry;
    }

    public static void register(IRegistrationHelper helper) {
        CREATIVE_ITEMS.clear();
        BLOCKS.clear();

        // ---- floor torches: original registry names ------------------------
        TORCHERINO = registerBlock(helper, "blocktorcherino",
                () -> new BlockTorcherino(torchProperties()));
        COMPRESSED_TORCHERINO = registerBlock(helper, "blockcompressedtorcherino",
                () -> new BlockCompressedTorcherino(torchProperties()));
        DOUBLE_COMPRESSED_TORCHERINO = registerBlock(helper, "blockdoublecompressedtorcherino",
                () -> new BlockDoubleCompressedTorcherino(torchProperties()));
        TRIPLE_COMPRESSED_TORCHERINO = registerBlock(helper, "blocktriplecompressedtorcherino",
                () -> new BlockTripleCompressedTorcherino(torchProperties()));

        // ---- wall torches: new names, no items -----------------------------
        WALL_TORCHERINO = registerBlock(helper, "wall_blocktorcherino",
                () -> new BlockWallTorcherino(torchProperties()));
        WALL_COMPRESSED_TORCHERINO = registerBlock(helper, "wall_blockcompressedtorcherino",
                () -> new BlockWallCompressedTorcherino(torchProperties()));
        WALL_DOUBLE_COMPRESSED_TORCHERINO = registerBlock(helper, "wall_blockdoublecompressedtorcherino",
                () -> new BlockWallDoubleCompressedTorcherino(torchProperties()));
        WALL_TRIPLE_COMPRESSED_TORCHERINO = registerBlock(helper, "wall_blocktriplecompressedtorcherino",
                () -> new BlockWallTripleCompressedTorcherino(torchProperties()));

        // ---- Lanterino: original registry names ----------------------------
        LANTERINO = registerBlock(helper, "blocklanterino",
                () -> new BlockLanterino(lanterinoProperties()));
        COMPRESSED_LANTERINO = registerBlock(helper, "blockcompressedlanterino",
                () -> new BlockCompressedLanterino(lanterinoProperties()));
        DOUBLE_COMPRESSED_LANTERINO = registerBlock(helper, "blockdoublecompressedlanterino",
                () -> new BlockDoubleCompressedLanterino(lanterinoProperties()));

        // ---- items: one item per block --------------------------------------
        // Torches use StandingAndWallBlockItem so that placing against a wall produces
        // the wall variant, exactly like the vanilla torch item does.
        CREATIVE_ITEMS.add(helper.registerItem("blocktorcherino", () -> new StandingAndWallBlockItem(
                TORCHERINO.get(), WALL_TORCHERINO.get(), new Item.Properties(), Direction.DOWN)));
        CREATIVE_ITEMS.add(helper.registerItem("blockcompressedtorcherino", () -> new StandingAndWallBlockItem(
                COMPRESSED_TORCHERINO.get(), WALL_COMPRESSED_TORCHERINO.get(),
                new Item.Properties(), Direction.DOWN)));
        CREATIVE_ITEMS.add(helper.registerItem("blockdoublecompressedtorcherino", () -> new StandingAndWallBlockItem(
                DOUBLE_COMPRESSED_TORCHERINO.get(), WALL_DOUBLE_COMPRESSED_TORCHERINO.get(),
                new Item.Properties(), Direction.DOWN)));
        CREATIVE_ITEMS.add(helper.registerItem("blocktriplecompressedtorcherino", () -> new StandingAndWallBlockItem(
                TRIPLE_COMPRESSED_TORCHERINO.get(), WALL_TRIPLE_COMPRESSED_TORCHERINO.get(),
                new Item.Properties(), Direction.DOWN)));

        CREATIVE_ITEMS.add(helper.registerItem("blocklanterino",
                () -> new BlockItem(LANTERINO.get(), new Item.Properties())));
        CREATIVE_ITEMS.add(helper.registerItem("blockcompressedlanterino",
                () -> new BlockItem(COMPRESSED_LANTERINO.get(), new Item.Properties())));
        CREATIVE_ITEMS.add(helper.registerItem("blockdoublecompressedlanterino",
                () -> new BlockItem(DOUBLE_COMPRESSED_LANTERINO.get(), new Item.Properties())));
    }

    /**
     * Every item belonging to this mod, resolved on demand. Only valid once registration
     * has finished, which is when creative tabs are built.
     */
    public static List<Item> creativeItems() {
        final List<Item> resolved = new ArrayList<>(CREATIVE_ITEMS.size());
        for (RegistryEntry<Item> entry : CREATIVE_ITEMS) {
            final Item item = entry.get();
            if (item != null) {
                resolved.add(item);
            }
        }
        return Collections.unmodifiableList(resolved);
    }

    /**
     * All blocks of this mod, resolved on demand. Used by the client render layer setup.
     *
     * <p>Without an explicit render layer every custom block falls back to
     * {@code RenderType.solid()}. The torch textures are 16x16 with 236 fully
     * transparent pixels, so a solid layer paints those pixels black instead of
     * discarding them, which is why the torches showed up as black slabs.</p>
     */
    public static List<Block> blocksForRendering() {
        final List<Block> resolved = new ArrayList<>(BLOCKS.size());
        for (RegistryEntry<? extends Block> entry : BLOCKS) {
            final Block block = entry.get();
            if (block != null) {
                resolved.add(block);
            }
        }
        return Collections.unmodifiableList(resolved);
    }

    /**
     * Torch-like properties: light level 15, wood sound, zero hardness, no collision.
     */
    private static BlockBehaviour.Properties torchProperties() {
        return BlockBehaviour.Properties.of()
                .noCollission()
                .instabreak()
                .lightLevel(state -> 15)
                .sound(SoundType.WOOD)
                .pushReaction(PushReaction.DESTROY);
    }

    /**
     * Pumpkin-like properties: hardness 1.0, wood sound, light level 15 and the pumpkin
     * map colour.
     */
    private static BlockBehaviour.Properties lanterinoProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_ORANGE)
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .lightLevel(state -> 15)
                .pushReaction(PushReaction.DESTROY);
    }
}
