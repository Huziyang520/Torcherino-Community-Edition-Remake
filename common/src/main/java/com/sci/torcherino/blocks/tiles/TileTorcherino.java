/*
 * 本文件：加速火把的方块实体 —— 整个模组的加速核心。
 * 说明：保存三轴范围（X/Z/Y 各自 ±N）、速度与红石模式；每服务端 tick 遍历以自身为中心的立方区域，
 *      对随机刻方块补 randomTick、对方块实体补 ticker 调用。旧存档的 Speed / Mode / PoweredByRedstone 仍可读取。
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
 * <p>Every server tick the block entity walks the cuboid defined by its three ranges
 * centred on itself. Randomly ticking blocks receive {@code speed(speed)} extra
 * {@code randomTick} calls and block entities receive {@code speed(speed)} extra ticker
 * invocations.</p>
 *
 * <p>Since 1.1.0 the area is described by an independent X / Z / Y range and the redstone
 * behaviour by a four way mode, matching the interface of the newer upstream Torcherino.
 * Older saves that only know the classic {@code Mode} radius and the
 * {@code PoweredByRedstone} flag are migrated transparently on load.</p>
 */
public class TileTorcherino extends BlockEntity {

    /** Upper bounds of the three ranges, shared by every tier. */
    public static final int MAX_XZ_RANGE = 15;
    public static final int MAX_Y_RANGE = 15;
    /** Upper bound of the speed level. */
    public static final int MAX_SPEED = 8;

    /** Redstone modes, in the same order as the upstream interface. */
    public static final int REDSTONE_NORMAL = 0;
    public static final int REDSTONE_INVERTED = 1;
    public static final int REDSTONE_IGNORED = 2;
    public static final int REDSTONE_OFF = 3;
    public static final int REDSTONE_MODES = 4;

    private int xRange;
    private int yRange;
    private int zRange;
    private int speed;
    private int redstoneMode;

    /** Resolved from the redstone mode plus the last known signal. */
    private boolean active = true;

    /** Last known neighbour signal, fed in by the block's redstone refresh. */
    private boolean poweredByRedstone;

    private boolean pendingRedstoneRefresh;
    private int scannerMultiplier;

    private final RandomSource rand = RandomSource.create();

    /**
     * Pre-computed scan area. {@code BlockPos.betweenClosed} hands out one reused mutable
     * position, so iterating it allocates nothing per visited block. It is rebuilt only
     * when a range or the block position changes.
     */
    private Iterable<BlockPos> area = BlockPos.betweenClosed(BlockPos.ZERO, BlockPos.ZERO);
    private int cachedXRange = -1;
    private int cachedYRange = -1;
    private int cachedZRange = -1;
    private int cachedOriginX = Integer.MIN_VALUE;
    private int cachedOriginY;
    private int cachedOriginZ;

    public TileTorcherino(BlockPos pos, BlockState state) {
        this(ModBlockEntities.TORCHERINO.get(), pos, state);
    }

    protected TileTorcherino(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Tier multiplier applied on top of the speed level.
     *
     * @param base the raw speed level (0-8).
     * @return the number of extra ticks to perform per accelerated block.
     */
    protected int speed(int base) {
        return base;
    }

    /**
     * Tier factor of this block: 1 for a plain Torcherino, 9 / 81 / 729 for the compressed
     * ones. {@code speed(1)} is exactly that constant, independent of the current level,
     * which is what the editor needs to print a percentage.
     */
    public int getTierMultiplier() {
        return this.speed(1);
    }

    /** Percentage the editor and the action bar show for the current speed level. */
    public int getSpeedPercent() {
        return this.speed(this.speed) * 100;
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
        if (this.pendingRedstoneRefresh) {
            this.pendingRedstoneRefresh = false;
            this.updateActive();
        }
        if (!this.active || this.speed == 0 || (this.xRange == 0 && this.yRange == 0 && this.zRange == 0)) {
            return;
        }
        this.rebuildAreaIfNeeded();
        // Hoisted out of the scan: the multiplier cannot change while a tick runs.
        this.scannerMultiplier = this.speed(this.speed);
        // The iterator hands out one reused position, so this loop allocates nothing per
        // visited block; only the blocks that really tick get an immutable copy.
        for (BlockPos cursor : this.area) {
            this.tickBlock(level, cursor);
        }
    }

    private void rebuildAreaIfNeeded() {
        final BlockPos origin = this.worldPosition;
        if (this.cachedXRange == this.xRange && this.cachedYRange == this.yRange && this.cachedZRange == this.zRange
                && this.cachedOriginX == origin.getX() && this.cachedOriginY == origin.getY()
                && this.cachedOriginZ == origin.getZ()) {
            return;
        }
        this.cachedXRange = this.xRange;
        this.cachedYRange = this.yRange;
        this.cachedZRange = this.zRange;
        this.cachedOriginX = origin.getX();
        this.cachedOriginY = origin.getY();
        this.cachedOriginZ = origin.getZ();
        this.area = BlockPos.betweenClosed(
                origin.getX() - this.xRange, origin.getY() - this.yRange, origin.getZ() - this.zRange,
                origin.getX() + this.xRange, origin.getY() + this.yRange, origin.getZ() + this.zRange);
    }

    /**
     * @param pos a <b>reused</b> cursor owned by the scan. It is only read here; an
     *            immutable copy is handed to the game before anything is ticked, because
     *            the called code may keep the position it receives.
     */
    private void tickBlock(Level level, BlockPos pos) {
        final BlockState blockState = level.getBlockState(pos);
        final Block block = blockState.getBlock();
        if (blockState.isAir()) {
            // Cheaper than the blacklist lookup and true for most of the scanned cuboid.
            return;
        }
        if (block instanceof LiquidBlock || TorcherinoRegistry.isBlockBlacklisted(block)) {
            return;
        }

        final int multiplier = this.scannerMultiplier;

        if (block.isRandomlyTicking(blockState)) {
            final BlockPos target = pos.immutable();
            for (int i = 0; i < multiplier && level.getBlockState(pos) == blockState; i++) {
                block.randomTick(blockState, (ServerLevel) level, target, this.rand);
            }
        }

        if (!blockState.hasBlockEntity()) {
            return;
        }

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
        final BlockPos target = pos.immutable();
        for (int i = 0; i < multiplier && !blockEntity.isRemoved(); i++) {
            ticker.tick(level, target, blockState, blockEntity);
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


    // ------------------------------------------------------------------ values

    public int getXRange() {
        return this.xRange;
    }

    public int getYRange() {
        return this.yRange;
    }

    public int getZRange() {
        return this.zRange;
    }

    public int getSpeed() {
        return this.speed;
    }

    public int getRedstoneMode() {
        return this.redstoneMode;
    }

    /** {@code true} while the torch may accelerate, taking the redstone mode into account. */
    public boolean isActive() {
        return this.active;
    }

    /**
     * Applies a full set of editor values. Out of range values are clamped, so a client
     * can never widen the area beyond the tier limits.
     *
     * @return {@code true} when the values were applied (they always are; the return value
     *         exists so callers can log a rejection if the rules get stricter).
     */
    public boolean setValues(int xRange, int zRange, int yRange, int speed, int redstoneMode) {
        this.xRange = clamp(xRange, MAX_XZ_RANGE);
        this.zRange = clamp(zRange, MAX_XZ_RANGE);
        this.yRange = clamp(yRange, MAX_Y_RANGE);
        this.speed = clamp(speed, MAX_SPEED);
        this.redstoneMode = clamp(redstoneMode, REDSTONE_MODES - 1);
        this.updateActive();
        this.sync();
        return true;
    }

    private static int clamp(int value, int max) {
        if (value < 0) {
            return 0;
        }
        return Math.min(value, max);
    }

    /** Recomputes {@link #active} from the redstone mode and the last known signal. */
    private void updateActive() {
        this.active = switch (this.redstoneMode) {
            case REDSTONE_INVERTED -> this.poweredByRedstone;
            case REDSTONE_IGNORED -> true;
            case REDSTONE_OFF -> false;
            default -> !this.poweredByRedstone;
        };
    }

    public void setPoweredByRedstone(boolean poweredByRedstone) {
        this.poweredByRedstone = poweredByRedstone;
        this.updateActive();
    }

    public boolean isPoweredByRedstone() {
        return this.poweredByRedstone;
    }

    /**
     * Advances either the speed level (modifier held) or the cubic area, which is the
     * legacy quick interaction that is used when the graphical editor is switched off.
     * Cycling the area always restores a cube, exactly like the classic radius did.
     */
    public void changeMode(boolean modifier) {
        if (modifier) {
            this.speed = this.speed < MAX_SPEED ? this.speed + 1 : 0;
            this.sync();
            return;
        }
        final int next = this.xRange < MAX_XZ_RANGE ? this.xRange + 1 : 0;
        this.setValues(next, next, next, this.speed, this.redstoneMode);
    }

    /** Action bar text of the quick interaction. */
    public Component getDescription() {
        return Component.translatable("message.torcherino.status",
                this.getModeDescription(),
                this.speed(this.speed) * 100 + "%");
    }

    /** "Stopped" or the current area, as a translatable component. */
    public Component getModeDescription() {
        if (this.xRange == 0 && this.yRange == 0 && this.zRange == 0) {
            return Component.translatable("message.torcherino.mode.stopped");
        }
        return Component.translatable("message.torcherino.mode.area",
                this.xRange * 2 + 1, this.yRange * 2 + 1, this.zRange * 2 + 1);
    }

    /** Sends the block entity data to every tracking client. */
    private void sync() {
        this.setChanged();
        if (this.level instanceof ServerLevel serverLevel) {
            final BlockState state = this.getBlockState();
            serverLevel.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    // ------------------------------------------------------------------ persistent

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        // Refresh the redstone state for freshly created block entities (Level may call
        // setLevel before the block's onPlace hook). The probe itself is deferred to the
        // next tick: this hook also runs while a chunk is still being loaded, and the
        // world should not be queried at that point.
        if (!level.isClientSide()) {
            this.pendingRedstoneRefresh = true;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("XRange", this.xRange);
        tag.putInt("ZRange", this.zRange);
        tag.putInt("YRange", this.yRange);
        tag.putInt("Speed", this.speed);
        tag.putInt("RedstoneMode", this.redstoneMode);
        tag.putBoolean("Active", this.active);
        // Legacy keys are still written so a world stays readable by the classic radius
        // model; "Mode" mirrors the X range, which is what the old code used as radius.
        tag.putByte("Mode", (byte) this.xRange);
        tag.putBoolean("PoweredByRedstone", this.poweredByRedstone);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.poweredByRedstone = tag.getBoolean("PoweredByRedstone");

        if (tag.contains("XRange")) {
            this.xRange = clamp(tag.getInt("XRange"), MAX_XZ_RANGE);
            this.zRange = clamp(tag.getInt("ZRange"), MAX_XZ_RANGE);
            this.yRange = clamp(tag.getInt("YRange"), MAX_Y_RANGE);
            this.speed = clamp(tag.getInt("Speed"), MAX_SPEED);
            this.redstoneMode = clamp(tag.getInt("RedstoneMode"), REDSTONE_MODES - 1);
        } else {
            // Classic save: one radius for all three axes, redstone could only switch it off.
            final int radius = clamp(tag.getByte("Mode"), MAX_XZ_RANGE);
            this.xRange = radius;
            this.zRange = radius;
            this.yRange = radius;
            this.speed = clamp(tag.getByte("Speed"), MAX_SPEED);
            this.redstoneMode = REDSTONE_NORMAL;
        }
        this.updateActive();
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
