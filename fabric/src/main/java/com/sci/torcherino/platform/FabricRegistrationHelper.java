package com.sci.torcherino.platform;

import com.sci.torcherino.Constants;
import com.sci.torcherino.platform.services.BlockEntityFactory;
import com.sci.torcherino.platform.services.IRegistrationHelper;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Fabric registers directly and immediately. Everything is registered from inside
 * {@code ModInitializer#onInitialize}, which still runs before the registries freeze.
 */
public class FabricRegistrationHelper implements IRegistrationHelper {

    private static ResourceLocation id(String name) {
        return new ResourceLocation(Constants.MOD_ID, name);
    }

    @Override
    public void registerBlock(String name, Block block) {
        Registry.register(BuiltInRegistries.BLOCK, id(name), block);
    }

    @Override
    public void registerItem(String name, Item item) {
        Registry.register(BuiltInRegistries.ITEM, id(name), item);
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> registerBlockEntityType(
            String name, BlockEntityFactory<T> factory, Block... validBlocks) {
        final BlockEntityType<T> type = FabricBlockEntityTypeBuilder
                .create(factory::create, validBlocks)
                .build();
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id(name), type);
        return type;
    }
}
