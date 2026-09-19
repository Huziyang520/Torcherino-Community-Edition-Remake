/*
 * 本文件：加速火把的方块实体 —— 整个模组的加速核心。
 * 说明：保存范围 / 速度 / 红石状态；每服务端 tick 遍历以自身为中心、半径等于当前范围档位的立方体，对随机刻方块补 randomTick、对方块实体补 ticker 调用。
 */
package com.sci.torcherino.blocks.tiles;

import com.sci.torcherino.TorcherinoRegistry;
import com.sci.torcherino.blocks.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Acceleration core of the Torcherino family.
 *
 * <p>Every server tick the block entity walks the cube of radius {@code mode}
 * centred on itself. Randomly ticking blocks receive {@code speed(speed)} extra
 * {@code randomTick} calls and block entities receive {@code speed(speed)} extra
 * ticker invocations.</p>
 *
 * <p>Fields and NBT keys keep their classic names so existing worlds keep their
 * saved speed/mode/redstone state.</p>
 */
public class TileTorcherino extends BlockEntity {

    /**
     * Area mode labels (English fallback strings); the index doubles as the radius used
     * by the acceleration loop. The player facing text comes from the language files.
     */
    private static final String[] MODES = new String[]{
            "Stopped",
            "Area: 1x1x1",
            "Area: 2x2x2",
            "Area: 3x3x3",
            "Area: 4x4x4",
            "Area: 5x5x5",
            "Area: 6x6x6",
            "Area: 7x7x7",
            "Area: 8x8x8",
            "Area: 9x9x9",
            "Area: 10x10x10",
            "Area: 11x11x11",
            "Area: 12x12x12",
            "Area: 13x13x13",
            "Area: 14x14x14",
            "Area: 15x5x15"
    };

    private static final int SPEEDS = 8;

    private boolean poweredByRedstone;
    private byte speed;
    private byte mode;
    private byte cachedMode = -1;
    private final RandomSource rand = RandomSource.create();

    private int xMin;
    private int yMin;
    private int zMin;
    private int xMax;
    private int yMax;
    private int zMax;

    public TileTorcherino(BlockPos pos, BlockState state) {
        this(ModBlockEntities.TORCHERINO.get(), pos, state);
    }

    protected TileTorcherino(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Speed multiplier applied on top of the configured speed level.
     *
     * @param base the raw speed level (0-8).
     * @return the number of extra ticks to perform per accelerated block.
     */
    protected int speed(int base) {
        return base;
    }

    /**
     * Server side ticker installed through {@code EntityBlock#getTicker}.
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (blockEntity instanceof TileTorcherino torcherino) {
            torcherino.tick(level, pos);
        }
    }

    public void tick(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }
        if (this.poweredByRedstone || this.mode == 0 || this.speed == 0) {
            return;
        }
        this.updateCachedModeIfNeeded(pos);
        this.tickNeighbors(level);
    }

    private void updateCachedModeIfNeeded(BlockPos pos) {
        if (this.cachedMode != this.mode) {
            this.xMin = pos.getX() - this.mode;
            this.yMin = pos.getY() - this.mode;
            this.zMin = pos.getZ() - this.mode;
            this.xMax = pos.getX() + this.mode;
            this.yMax = pos.getY() + this.mode;
            this.zMax = pos.getZ() + this.mode;
            this.cachedMode = this.mode;
        }
    }

    private void tickNeighbors(Level level) {
        for (int x = this.xMin; x <= this.xMax; x++) {
            for (int y = this.yMin; y <= this.yMax; y++) {
                for (int z = this.zMin; z <= this.zMax; z++) {
                    this.tickBlock(level, new BlockPos(x, y, z));
                }
            }
        }
    }

    private void tickBlock(Level level, BlockPos pos) {
        final BlockState blockState = level.getBlockState(pos);
        final Block block = blockState.getBlock();
        if (block instanceof LiquidBlock || TorcherinoRegistry.isBlockBlacklisted(block)) {
            return;
        }

        final int multiplier = this.speed(this.speed);

        if (block.isRandomlyTicking(blockState)) {
            for (int i = 0; i < multiplier && level.getBlockState(pos) == blockState; i++) {
                block.randomTick(blockState, (ServerLevel) level, pos, this.rand);
            }
        }

        if (blockState.hasBlockEntity()) {
            final BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null || blockEntity.isRemoved()) {
                return;
            }
            if (TorcherinoRegistry.isTileBlacklisted(blockEntity.getClass())) {
                return;
            }
            final BlockEntityTicker<BlockEntity> ticker = resolveTicker(block, level, blockState, blockEntity);
            if (ticker == null) {
                return;
            }
            for (int i = 0; i < multiplier && !blockEntity.isRemoved(); i++) {
                ticker.tick(level, pos, blockState, blockEntity);
            }
        }
    }

    /**
     * Resolves the ticker the game itself would run for this block entity.
     */
    @SuppressWarnings("unchecked")
    private static BlockEntityTicker<BlockEntity> resolveTicker(Block block, Level level, BlockState state, BlockEntity blockEntity) {
        if (!(block instanceof net.minecraft.world.level.block.EntityBlock entityBlock)) {
            return null;
        }
        return (BlockEntityTicker<BlockEntity>) entityBlock.getTicker(
                level, state, (BlockEntityType<BlockEntity>) blockEntity.getType());
    }

    public void setPoweredByRedstone(boolean poweredByRedstone) {
        this.poweredByRedstone = poweredByRedstone;
    }

    public boolean isPoweredByRedstone() {
        return this.poweredByRedstone;
    }

    /**
     * Advances either the speed level (modifier held) or the area mode.
     */
    public void changeMode(boolean modifier) {
        if (modifier) {
            if (this.speed < SPEEDS) {
                this.speed++;
            } else {
                this.speed = 0;
            }
        } else if (this.mode < MODES.length - 1) {
            this.mode++;
        } else {
            this.mode = 0;
        }
    }

    /**
     * Action bar text shown after a mode change.
     *
     * <p>It is a translatable component: the client resolves the keys against
     * {@code assets/torcherino/lang/*.json}, so the text follows the player's language.
     * The percent sign is part of the argument, not of the format string, to keep the
     * pattern free of literal {@code %}.</p>
     */
    public Component getDescription() {
        return Component.translatable("message.torcherino.status",
                this.getModeDescription(),
                this.speed(this.speed) * 100 + "%");
    }

    /**
     * Translatable form of {@link #getMode()}.
     *
     * <p>The last entry keeps the legacy {@code "Area: 15x5x15"} wording (see the
     * {@link #MODES} note) instead of the real {@code 15x15x15} radius.</p>
     */
    public Component getModeDescription() {
        if (this.mode == 0) {
            return Component.translatable("message.torcherino.mode.stopped");
        }
        if (this.mode == MODES.length - 1) {
            return Component.translatable("message.torcherino.mode.area", 15, 5, 15);
        }
        final int radius = this.mode;
        return Component.translatable("message.torcherino.mode.area", radius, radius, radius);
    }

    public String getMode() {
        return MODES[this.mode];
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        // Refresh the redstone state for freshly created block entities: Level may call
        // setLevel before the block's onPlace hook.
        if (!level.isClientSide() && !this.poweredByRedstone) {
            this.poweredByRedstone = level.hasNeighborSignal(this.worldPosition);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByte("Speed", this.speed);
        tag.putByte("Mode", this.mode);
        tag.putBoolean("PoweredByRedstone", this.poweredByRedstone);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.speed = tag.getByte("Speed");
        this.mode = tag.getByte("Mode");
        this.poweredByRedstone = tag.getBoolean("PoweredByRedstone");
    }

    @Override
    public CompoundTag getUpdateTag() {
        final CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
