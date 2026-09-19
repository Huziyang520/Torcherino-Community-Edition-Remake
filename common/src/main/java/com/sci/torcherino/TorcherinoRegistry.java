/*
 * 本文件：运行时的加速黑名单。
 * 说明：把配置 / 代码里的字符串解析成方块或方块实体类并登记；加速核心每 tick 用它跳过不该被加速的对象。
 */
package com.sci.torcherino;

import com.sci.torcherino.blocks.ModBlocks;
import com.sci.torcherino.blocks.tiles.TileCompressedTorcherino;
import com.sci.torcherino.blocks.tiles.TileDoubleCompressedTorcherino;
import com.sci.torcherino.blocks.tiles.TileTorcherino;
import com.sci.torcherino.blocks.tiles.TileTripleCompressedTorcherino;
import com.sci.torcherino.platform.Services;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * Runtime blacklist used by the acceleration core.
 *
 * <p>String semantics: an entry containing a
 * colon is treated as {@code modid:unlocalized} and resolved against the block
 * registry, while an entry without a colon is treated as a fully qualified
 * BlockEntity class name.</p>
 */
public final class TorcherinoRegistry {

    private static final Set<Block> BLACKLISTED_BLOCKS = new HashSet<>();
    private static final Set<Class<? extends BlockEntity>> BLACKLISTED_TILES = new HashSet<>();

    private TorcherinoRegistry() {
    }

    /**
     * Resolves a configuration string and blacklists whatever it points at.
     *
     * @param string either {@code modid:unlocalized} or a BlockEntity class name.
     */
    public static void blacklistString(String string) {
        if (string == null || string.isBlank()) {
            return;
        }
        final String trimmed = string.trim();
        if (trimmed.indexOf(':') == -1) {
            try {
                final Class<?> clazz = Class.forName(trimmed);
                if (!BlockEntity.class.isAssignableFrom(clazz)) {
                    Constants.LOG.info("Class not a BlockEntity: {}", trimmed);
                    return;
                }
                @SuppressWarnings("unchecked")
                final Class<? extends BlockEntity> tileClass = (Class<? extends BlockEntity>) clazz;
                blacklistTile(tileClass);
            } catch (ClassNotFoundException e) {
                Constants.LOG.info("Class not found: {}, ignoring", trimmed);
            }
            return;
        }

        final String[] parts = trimmed.split(":", 2);
        final ResourceLocation id = ResourceLocation.tryParse(parts[0] + ":" + parts[1]);
        if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
            Constants.LOG.info("Could not find block: {}, ignoring", trimmed);
            return;
        }
        final Block block = BuiltInRegistries.BLOCK.get(id);
        Constants.LOG.info("Blacklisting block: {}", id);
        blacklistBlock(block);
    }

    public static void blacklistBlock(Block block) {
        // Registry entries resolve lazily, so an entry that is not registered yet yields
        // null. Skipping it is harmless: nothing can match null in the acceleration loop.
        if (block != null) {
            BLACKLISTED_BLOCKS.add(block);
        }
    }

    public static void blacklistTile(Class<? extends BlockEntity> tile) {
        BLACKLISTED_TILES.add(tile);
    }

    public static boolean isBlockBlacklisted(Block block) {
        return BLACKLISTED_BLOCKS.contains(block);
    }

    public static boolean isTileBlacklisted(Class<? extends BlockEntity> tile) {
        return BLACKLISTED_TILES.contains(tile);
    }

    /**
     * Rebuilds the default blacklist. Called once during common initialisation; safe
     * to call again after a configuration reload.
     */
    public static void registerDefaults() {
        BLACKLISTED_BLOCKS.clear();
        BLACKLISTED_TILES.clear();

        // Air and fluids are never worth accelerating; each fluid is a single block here.
        blacklistBlock(Blocks.AIR);
        blacklistBlock(Blocks.WATER);
        blacklistBlock(Blocks.LAVA);

        // Own blocks, so Torcherinos never accelerate each other.
        blacklistBlock(ModBlocks.TORCHERINO.get());
        blacklistBlock(ModBlocks.WALL_TORCHERINO.get());
        blacklistBlock(ModBlocks.COMPRESSED_TORCHERINO.get());
        blacklistBlock(ModBlocks.WALL_COMPRESSED_TORCHERINO.get());
        blacklistBlock(ModBlocks.DOUBLE_COMPRESSED_TORCHERINO.get());
        blacklistBlock(ModBlocks.WALL_DOUBLE_COMPRESSED_TORCHERINO.get());
        blacklistBlock(ModBlocks.TRIPLE_COMPRESSED_TORCHERINO.get());
        blacklistBlock(ModBlocks.WALL_TRIPLE_COMPRESSED_TORCHERINO.get());
        blacklistBlock(ModBlocks.LANTERINO.get());
        blacklistBlock(ModBlocks.COMPRESSED_LANTERINO.get());
        blacklistBlock(ModBlocks.DOUBLE_COMPRESSED_LANTERINO.get());

        blacklistTile(TileTorcherino.class);
        blacklistTile(TileCompressedTorcherino.class);
        blacklistTile(TileDoubleCompressedTorcherino.class);
        blacklistTile(TileTripleCompressedTorcherino.class);

        if (Services.PLATFORM.isModLoaded("projecte")) {
            blacklistString("projecte:dm_pedestal");
        }

        for (String block : TorcherinoConfig.blacklistedBlocks) {
            blacklistString(block);
        }
        for (String tile : TorcherinoConfig.blacklistedTiles) {
            blacklistString(tile);
        }
    }
}
