package com.sci.torcherino.platform;

import com.sci.torcherino.Constants;
import com.sci.torcherino.platform.services.BlockEntityFactory;
import com.sci.torcherino.platform.services.IRegistrationHelper;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Forge registration through deferred registers. The instances themselves are already
 * built by the common code; the deferred registers simply carry them into the
 * appropriate {@code RegisterEvent}, which keeps the "no eager registry writes" rule.
 */
public class ForgeRegistrationHelper implements IRegistrationHelper {

    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.MOD_ID);

    /** Must be called from the mod constructor with the mod event bus. */
    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
    }

    @Override
    public void registerBlock(String name, Block block) {
        BLOCKS.register(name, () -> block);
    }

    @Override
    public void registerItem(String name, Item item) {
        ITEMS.register(name, () -> item);
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> registerBlockEntityType(
            String name, BlockEntityFactory<T> factory, Block... validBlocks) {
        final BlockEntityType<T> type = BlockEntityType.Builder
                .of((pos, state) -> factory.create(pos, state), validBlocks)
                .build(null);
        BLOCK_ENTITIES.register(name, () -> type);
        return type;
    }
}
