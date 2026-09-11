package com.sci.torcherino.blocks;

import com.sci.torcherino.blocks.tiles.TileCompressedTorcherino;
import com.sci.torcherino.blocks.tiles.TileDoubleCompressedTorcherino;
import com.sci.torcherino.blocks.tiles.TileTorcherino;
import com.sci.torcherino.blocks.tiles.TileTripleCompressedTorcherino;
import com.sci.torcherino.platform.services.IRegistrationHelper;
import com.sci.torcherino.platform.services.RegistryEntry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Block entity types.
 *
 * <p>Registry names are copied verbatim from 7.5 ({@code torcherino_tile} and friends).
 * Every type lists both the floor and the wall block that share it, plus the matching
 * Lanterino variant when one exists - 7.5 achieved the same result through
 * {@code Block#createTileEntity}.</p>
 *
 * <p>Everything is lazy for the same reason as {@link ModBlocks}, and because the valid
 * blocks can only be resolved once the block registry has been filled. The loader builds
 * the type itself, since vanilla's builder takes a package private supplier interface.</p>
 */
public final class ModBlockEntities {

    public static RegistryEntry<BlockEntityType<TileTorcherino>> TORCHERINO;
    public static RegistryEntry<BlockEntityType<TileCompressedTorcherino>> COMPRESSED_TORCHERINO;
    public static RegistryEntry<BlockEntityType<TileDoubleCompressedTorcherino>> DOUBLE_COMPRESSED_TORCHERINO;
    public static RegistryEntry<BlockEntityType<TileTripleCompressedTorcherino>> TRIPLE_COMPRESSED_TORCHERINO;

    private ModBlockEntities() {
    }

    public static void register(IRegistrationHelper helper) {
        TORCHERINO = helper.registerBlockEntityType("torcherino_tile", TileTorcherino::new,
                () -> validBlocks(ModBlocks.TORCHERINO, ModBlocks.WALL_TORCHERINO, ModBlocks.LANTERINO));

        COMPRESSED_TORCHERINO = helper.registerBlockEntityType("compressed_torcherino_tile",
                TileCompressedTorcherino::new,
                () -> validBlocks(ModBlocks.COMPRESSED_TORCHERINO, ModBlocks.WALL_COMPRESSED_TORCHERINO,
                        ModBlocks.COMPRESSED_LANTERINO));

        DOUBLE_COMPRESSED_TORCHERINO = helper.registerBlockEntityType("double_compressed_torcherino_tile",
                TileDoubleCompressedTorcherino::new,
                () -> validBlocks(ModBlocks.DOUBLE_COMPRESSED_TORCHERINO, ModBlocks.WALL_DOUBLE_COMPRESSED_TORCHERINO,
                        ModBlocks.DOUBLE_COMPRESSED_LANTERINO));

        TRIPLE_COMPRESSED_TORCHERINO = helper.registerBlockEntityType("triple_compressed_torcherino_tile",
                TileTripleCompressedTorcherino::new,
                () -> validBlocks(ModBlocks.TRIPLE_COMPRESSED_TORCHERINO, ModBlocks.WALL_TRIPLE_COMPRESSED_TORCHERINO));
    }

    @SafeVarargs
    private static Block[] validBlocks(RegistryEntry<? extends Block>... entries) {
        final Block[] blocks = new Block[entries.length];
        for (int i = 0; i < entries.length; i++) {
            blocks[i] = entries[i].get();
        }
        return blocks;
    }
}
