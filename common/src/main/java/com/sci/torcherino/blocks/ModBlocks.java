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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * All blocks and block items of the mod.
 *
 * <p>7.5 registered seven blocks, each backed by a single {@code BlockTorch}. Modern
 * Minecraft separates the floor and wall torch, so the four Torcherino tiers become
 * eight blocks ({@code blockX} plus {@code wall_blockX}) while the three Lanterino
 * variants stay as they were. The original registry names are kept for the floor
 * variants; the wall variants are new and have no item of their own - the torch item
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

    private ModBlocks() {
    }

    public static void register(IRegistrationHelper helper) {
        CREATIVE_ITEMS.clear();

        // ---- floor torches: original registry names ------------------------
        TORCHERINO = helper.registerBlock("blocktorcherino",
                () -> new BlockTorcherino(torchProperties()));
        COMPRESSED_TORCHERINO = helper.registerBlock("blockcompressedtorcherino",
                () -> new BlockCompressedTorcherino(torchProperties()));
        DOUBLE_COMPRESSED_TORCHERINO = helper.registerBlock("blockdoublecompressedtorcherino",
                () -> new BlockDoubleCompressedTorcherino(torchProperties()));
        TRIPLE_COMPRESSED_TORCHERINO = helper.registerBlock("blocktriplecompressedtorcherino",
                () -> new BlockTripleCompressedTorcherino(torchProperties()));

        // ---- wall torches: new names, no items -----------------------------
        WALL_TORCHERINO = helper.registerBlock("wall_blocktorcherino",
                () -> new BlockWallTorcherino(torchProperties()));
        WALL_COMPRESSED_TORCHERINO = helper.registerBlock("wall_blockcompressedtorcherino",
                () -> new BlockWallCompressedTorcherino(torchProperties()));
        WALL_DOUBLE_COMPRESSED_TORCHERINO = helper.registerBlock("wall_blockdoublecompressedtorcherino",
                () -> new BlockWallDoubleCompressedTorcherino(torchProperties()));
        WALL_TRIPLE_COMPRESSED_TORCHERINO = helper.registerBlock("wall_blocktriplecompressedtorcherino",
                () -> new BlockWallTripleCompressedTorcherino(torchProperties()));

        // ---- Lanterino: original registry names ----------------------------
        LANTERINO = helper.registerBlock("blocklanterino",
                () -> new BlockLanterino(lanterinoProperties()));
        COMPRESSED_LANTERINO = helper.registerBlock("blockcompressedlanterino",
                () -> new BlockCompressedLanterino(lanterinoProperties()));
        DOUBLE_COMPRESSED_LANTERINO = helper.registerBlock("blockdoublecompressedlanterino",
                () -> new BlockDoubleCompressedLanterino(lanterinoProperties()));

        // ---- items: 1.12.2 registered one ItemBlock per block --------------
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
     * 7.5 called {@code setLightLevel(0.9375F)}, which renders as light 15, and inherited
     * the vanilla torch's wood sound, zero hardness and "no collision" behaviour.
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
     * 7.5 used {@code BlockPumpkin} with hardness 1.0, wood sound and light level
     * {@code 1.0F}. The map colour follows the modern pumpkin.
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
