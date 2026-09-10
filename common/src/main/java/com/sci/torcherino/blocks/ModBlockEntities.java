package com.sci.torcherino.blocks;

import com.sci.torcherino.blocks.tiles.TileCompressedTorcherino;
import com.sci.torcherino.blocks.tiles.TileDoubleCompressedTorcherino;
import com.sci.torcherino.blocks.tiles.TileTorcherino;
import com.sci.torcherino.blocks.tiles.TileTripleCompressedTorcherino;
import com.sci.torcherino.platform.services.IRegistrationHelper;

import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Block entity types.
 *
 * <p>Registry names are copied verbatim from 7.5 ({@code torcherino_tile} and friends).
 * Every type lists both the floor and the wall block that share it, plus the matching
 * Lanterino variant when one exists - 7.5 achieved the same result through
 * {@code Block#createTileEntity}.</p>
 *
 * <p>The types are built by the loader because the vanilla builder takes a package
 * private supplier interface. See {@link IRegistrationHelper}.</p>
 */
public final class ModBlockEntities {

    public static BlockEntityType<TileTorcherino> TORCHERINO;
    public static BlockEntityType<TileCompressedTorcherino> COMPRESSED_TORCHERINO;
    public static BlockEntityType<TileDoubleCompressedTorcherino> DOUBLE_COMPRESSED_TORCHERINO;
    public static BlockEntityType<TileTripleCompressedTorcherino> TRIPLE_COMPRESSED_TORCHERINO;

    private ModBlockEntities() {
    }

    public static void register(IRegistrationHelper helper) {
        TORCHERINO = helper.registerBlockEntityType("torcherino_tile", TileTorcherino::new,
                ModBlocks.TORCHERINO, ModBlocks.WALL_TORCHERINO, ModBlocks.LANTERINO);

        COMPRESSED_TORCHERINO = helper.registerBlockEntityType("compressed_torcherino_tile", TileCompressedTorcherino::new,
                ModBlocks.COMPRESSED_TORCHERINO, ModBlocks.WALL_COMPRESSED_TORCHERINO, ModBlocks.COMPRESSED_LANTERINO);

        DOUBLE_COMPRESSED_TORCHERINO = helper.registerBlockEntityType("double_compressed_torcherino_tile",
                TileDoubleCompressedTorcherino::new,
                ModBlocks.DOUBLE_COMPRESSED_TORCHERINO, ModBlocks.WALL_DOUBLE_COMPRESSED_TORCHERINO,
                ModBlocks.DOUBLE_COMPRESSED_LANTERINO);

        TRIPLE_COMPRESSED_TORCHERINO = helper.registerBlockEntityType("triple_compressed_torcherino_tile",
                TileTripleCompressedTorcherino::new,
                ModBlocks.TRIPLE_COMPRESSED_TORCHERINO, ModBlocks.WALL_TRIPLE_COMPRESSED_TORCHERINO);
    }
}
