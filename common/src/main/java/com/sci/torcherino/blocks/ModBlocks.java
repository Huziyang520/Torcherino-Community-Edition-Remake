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

import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * All blocks and block items of the mod.
 *
 * <p>Registry names are copied verbatim from Torcherino 7.5. The four torch variants
 * additionally ship a wall counterpart named {@code wall_<name>} because modern
 * Minecraft models floor and wall torches as separate blocks.</p>
 *
 * <p>Instances are constructed here, inside the loader registration window, and then
 * handed to the loader through {@link IRegistrationHelper}. Nothing is instantiated
 * during class loading, which modern registries forbid.</p>
 */
public final class ModBlocks {

    public static BlockTorcherino TORCHERINO;
    public static BlockWallTorcherino WALL_TORCHERINO;

    public static BlockCompressedTorcherino COMPRESSED_TORCHERINO;
    public static BlockWallCompressedTorcherino WALL_COMPRESSED_TORCHERINO;

    public static BlockDoubleCompressedTorcherino DOUBLE_COMPRESSED_TORCHERINO;
    public static BlockWallDoubleCompressedTorcherino WALL_DOUBLE_COMPRESSED_TORCHERINO;

    public static BlockTripleCompressedTorcherino TRIPLE_COMPRESSED_TORCHERINO;
    public static BlockWallTripleCompressedTorcherino WALL_TRIPLE_COMPRESSED_TORCHERINO;

    public static BlockLanterino LANTERINO;
    public static BlockCompressedLanterino COMPRESSED_LANTERINO;
    public static BlockDoubleCompressedLanterino DOUBLE_COMPRESSED_LANTERINO;

    private static final List<Item> CREATIVE_ITEMS = new ArrayList<>();

    private ModBlocks() {
    }

    public static void register(IRegistrationHelper helper) {
        // ---- tier 0: regular Torcherino ----
        TORCHERINO = new BlockTorcherino(torchProperties());
        WALL_TORCHERINO = new BlockWallTorcherino(torchProperties().dropsLike(TORCHERINO));
        helper.registerBlock("blocktorcherino", TORCHERINO);
        helper.registerBlock("wall_blocktorcherino", WALL_TORCHERINO);
        registerTorchItem(helper, "blocktorcherino", TORCHERINO, WALL_TORCHERINO);

        // ---- tier 1: compressed ----
        COMPRESSED_TORCHERINO = new BlockCompressedTorcherino(torchProperties());
        WALL_COMPRESSED_TORCHERINO = new BlockWallCompressedTorcherino(torchProperties().dropsLike(COMPRESSED_TORCHERINO));
        helper.registerBlock("blockcompressedtorcherino", COMPRESSED_TORCHERINO);
        helper.registerBlock("wall_blockcompressedtorcherino", WALL_COMPRESSED_TORCHERINO);
        registerTorchItem(helper, "blockcompressedtorcherino", COMPRESSED_TORCHERINO, WALL_COMPRESSED_TORCHERINO);

        // ---- tier 2: double compressed ----
        DOUBLE_COMPRESSED_TORCHERINO = new BlockDoubleCompressedTorcherino(torchProperties());
        WALL_DOUBLE_COMPRESSED_TORCHERINO = new BlockWallDoubleCompressedTorcherino(torchProperties().dropsLike(DOUBLE_COMPRESSED_TORCHERINO));
        helper.registerBlock("blockdoublecompressedtorcherino", DOUBLE_COMPRESSED_TORCHERINO);
        helper.registerBlock("wall_blockdoublecompressedtorcherino", WALL_DOUBLE_COMPRESSED_TORCHERINO);
        registerTorchItem(helper, "blockdoublecompressedtorcherino", DOUBLE_COMPRESSED_TORCHERINO, WALL_DOUBLE_COMPRESSED_TORCHERINO);

        // ---- tier 3: triple compressed ----
        TRIPLE_COMPRESSED_TORCHERINO = new BlockTripleCompressedTorcherino(torchProperties());
        WALL_TRIPLE_COMPRESSED_TORCHERINO = new BlockWallTripleCompressedTorcherino(torchProperties().dropsLike(TRIPLE_COMPRESSED_TORCHERINO));
        helper.registerBlock("blocktriplecompressedtorcherino", TRIPLE_COMPRESSED_TORCHERINO);
        helper.registerBlock("wall_blocktriplecompressedtorcherino", WALL_TRIPLE_COMPRESSED_TORCHERINO);
        registerTorchItem(helper, "blocktriplecompressedtorcherino", TRIPLE_COMPRESSED_TORCHERINO, WALL_TRIPLE_COMPRESSED_TORCHERINO);

        // ---- Lanterino family ----
        LANTERINO = new BlockLanterino(lanterinoProperties());
        helper.registerBlock("blocklanterino", LANTERINO);
        registerBlockItem(helper, "blocklanterino", LANTERINO);

        COMPRESSED_LANTERINO = new BlockCompressedLanterino(lanterinoProperties());
        helper.registerBlock("blockcompressedlanterino", COMPRESSED_LANTERINO);
        registerBlockItem(helper, "blockcompressedlanterino", COMPRESSED_LANTERINO);

        DOUBLE_COMPRESSED_LANTERINO = new BlockDoubleCompressedLanterino(lanterinoProperties());
        helper.registerBlock("blockdoublecompressedlanterino", DOUBLE_COMPRESSED_LANTERINO);
        registerBlockItem(helper, "blockdoublecompressedlanterino", DOUBLE_COMPRESSED_LANTERINO);
    }

    private static void registerTorchItem(IRegistrationHelper helper, String name,
                                          BlockTorcherino standing, BlockWallTorcherino wall) {
        final Item item = new StandingAndWallBlockItem(standing, wall, new Item.Properties(), Direction.DOWN);
        helper.registerItem(name, item);
        CREATIVE_ITEMS.add(item);
    }

    private static void registerBlockItem(IRegistrationHelper helper, String name, net.minecraft.world.level.block.Block block) {
        final Item item = new BlockItem(block, new Item.Properties());
        helper.registerItem(name, item);
        CREATIVE_ITEMS.add(item);
    }

    /** @return every item of the mod, in the original display order. */
    public static List<Item> creativeItems() {
        return Collections.unmodifiableList(CREATIVE_ITEMS);
    }

    /**
     * Torch properties. 7.5 called {@code setLightLevel(0.9375F)} which evaluates to
     * light level 15, one step brighter than a vanilla torch.
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
     * Lanterino properties. 7.5 set hardness 1.0, wood sounds and a full light level.
     */
    private static BlockBehaviour.Properties lanterinoProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_ORANGE)
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .lightLevel(state -> 15)
                .isValidSpawn((state, level, pos, entityType) -> true)
                .pushReaction(PushReaction.DESTROY);
    }
}
