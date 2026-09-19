/*
 * 本文件：4 个方块实体类型的注册。
 * 说明：注册名形如 torcherino_tile；每个类型列出共用的落地 / 贴墙 / 南瓜灯方块，类型本身由加载器构造。
 */
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
 * <p>Every type lists both the floor and the wall block that share it, plus the matching
 * Lanterino variant when one exists.</p>
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
