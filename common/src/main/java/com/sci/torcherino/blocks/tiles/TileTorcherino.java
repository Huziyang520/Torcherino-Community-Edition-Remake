package com.sci.torcherino.blocks.tiles;

import com.sci.torcherino.Constants;
import com.sci.torcherino.TorcherinoConfig;
import com.sci.torcherino.TorcherinoRegistry;
import com.sci.torcherino.api.AccelerationCache;
import com.sci.torcherino.api.AccelerationStats;
import com.sci.torcherino.api.AdaptiveBrake;
import com.sci.torcherino.api.ChunkView;
import com.sci.torcherino.api.GlobalBudget;
import com.sci.torcherino.blocks.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

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
 *
 * <p>Since 1.4.1 the walk itself is section based. The cuboid is covered by at most 3x3
 * chunk columns; each column is resolved once, then every 16x16x16 section is either
 * skipped (all air, or a palette probe proves it holds nothing worth ticking) or ticked by
 * reading its palette directly. Previously every position of the cuboid paid a world level
 * {@code getBlockState}, and the inner loop paid one more on every accelerated round.</p>
 *
 * <p>The reusable parts of that work live in {@code com.sci.torcherino.api}: the per state
 * attribute cache ({@link AccelerationCache}), the loaded only chunk access
 * ({@link ChunkView}) and the optional adaptive brake ({@link AdaptiveBrake}).</p>
 */
public class TileTorcherino extends BlockEntity {

    /** Upper bounds of the three ranges, shared by every tier. */
    public static final int MAX_XZ_RANGE = 15;
    public static final int MAX_Y_RANGE = 15;
    /** Upper bound of the speed level, i.e. how many gears the classic interaction offers. */
    public static final int MAX_SPEED = 8;
    /**
     * Resolution of the stored speed: it is kept in hundredths of a level, so
     * {@code speedScaled / SPEED_SCALE} is the level the classic interaction counts in.
     * The free multiplier mode allows every hundredth, the classic mode steps by
     * {@code SPEED_SCALE} (one gear).
     */
    public static final int SPEED_SCALE = 100;
    /** Upper bound of the stored speed; 800 is level 8, ten times the classic maximum. */
    public static final int MAX_SPEED_SCALED = MAX_SPEED * SPEED_SCALE;

    /** Redstone modes, in the same order as the upstream interface. */
    public static final int REDSTONE_NORMAL = 0;
    public static final int REDSTONE_INVERTED = 1;
    public static final int REDSTONE_IGNORED = 2;
    public static final int REDSTONE_OFF = 3;
    public static final int REDSTONE_MODES = 4;

    /**
     * A section is only walked when its palette may hold a block that can actually be
     * accelerated. The test is deliberately the same one the per-position code applies:
     * air, fluids, blacklisted blocks and everything that neither ticks randomly nor owns
     * a block entity can never produce work. {@code maybeHas} is allowed to answer
     * {@code true} for a section that matches nothing, so this is a pure filter - it only
     * removes work that would have been discarded position by position anyway.
     */
    private static final Predicate<BlockState> ACCELERATABLE = AccelerationCache::isAcceleratable;

    private int xRange;
    private int yRange;
    private int zRange;
    /** Speed in hundredths of a level, see {@link #SPEED_SCALE}. */
    private int speedScaled;
    private int redstoneMode;

    /**
     * Carry of the fractional part of the multiplier. A multiplier such as 3.5 cannot run
     * "three and a half" ticks, so the half is spread over the following ticks; over time
     * the work done matches the configured multiplier exactly.
     */
    private double speedCarry;

    /** Resolved from the redstone mode plus the last known signal. */
    private boolean active = true;

    /** Last known neighbour signal, fed in by the block's redstone refresh. */
    private boolean poweredByRedstone;

    private boolean pendingRedstoneRefresh;
    private int scannerMultiplier;

    private final RandomSource rand = RandomSource.create();

    /**
     * Resolved tickers per block state. Resolving one walks the block class and allocates a
     * lambda, and the answer for a given state never changes, so the high tiers (where the
     * same position is ticked hundreds of times per game tick) stop paying for it.
     */
    private final Map<BlockState, Optional<BlockEntityTicker<BlockEntity>>> tickerCache = new HashMap<>();

    /**
     * UUID of the player who placed this Torcherino, as text, or {@code null} when unknown
     * (placed before this field existed, or placed by something that is not a player). Only
     * read by the optional owner gate.
     */
    private String owner;

    /**
     * Name of the owner as it was when the block was placed, or {@code null}. Written next to
     * the UUID so a dropped item can print a readable owner even while that player is offline;
     * the gate itself never reads it.
     */
    private String ownerName;

    /**
     * Whether players other than the owner may change the settings of this Torcherino.
     * {@code true} is the default, i.e. the behaviour before the owner feature existed:
     * everyone may edit. Only the owner may flip it.
     */
    private boolean othersCanEdit = true;

    /**
     * Saturation bookkeeping of the visited positions, keyed by packed position. Only filled
     * while {@code general.skipSaturatedPositions} is on, which is why the map starts empty
     * and stays that way on every default configuration.
     */
    private final Map<Long, Idle> idlePositions = new HashMap<>();

    /**
     * Positions with fewer planned rounds than this never take part in the saturation check:
     * below it the 27 state reads of the signature would cost more than the calls they save.
     */
    private static final int SATURATION_MIN_ROUNDS = 4;
    /** Consecutive ticks without any change before a position is throttled. */
    private static final int SATURATION_CONFIRM_TICKS = 2;
    /** Safety valve: the bookkeeping can never outgrow the scan window, but be explicit. */
    private static final int SATURATION_MAX_ENTRIES = 1 << 17;

    /** Saturation bookkeeping of a single position. */
    private static final class Idle {
        /** Signature of the surrounding block states, as of the previous tick. */
        private long signature;
        /** How many consecutive ticks that signature stayed the same. */
        private int unchangedTicks;
    }

    // ---- pre-computed scan window ------------------------------------------------------

    /**
     * Inclusive block bounds of the scan, clamped to the build height: outside of it the
     * world answers {@code VOID_AIR}, which the loop would skip as air anyway. The section
     * indices below are relative to the level's lowest section, and are what the walk
     * actually iterates.
     */
    private boolean scanValid;
    private int scanMinX;
    private int scanMaxX;
    private int scanMinY;
    private int scanMaxY;
    private int scanMinZ;
    private int scanMaxZ;
    private int scanMinSectionIndex;
    private int scanMaxSectionIndex;

    /** Cache keys of the scan window, so it is rebuilt only when a range or the origin moved. */
    private int cachedXRange = -1;
    private int cachedYRange = -1;
    private int cachedZRange = -1;
    private int cachedOriginX = Integer.MIN_VALUE;
    private int cachedOriginY;
    private int cachedOriginZ;
    private int cachedMinSection = Integer.MIN_VALUE;
    private int cachedSectionsCount = -1;

    /**
     * Remaining acceleration calls of the current tick. {@link Integer#MAX_VALUE} means the
     * configured cap is off, which is the default; a positive configured value bounds the
     * cost of one Torcherino at extreme tiers, speeds or areas.
     */
    private int workBudget = Integer.MAX_VALUE;

    /**
     * Ceiling of the rounds a single position may use while a budget is active. Without it
     * the first position of the scan would eat the whole budget and the rest of the area
     * would be skipped; sharing it over {@link #BUDGET_SHARE} positions keeps a capped
     * Torcherino useful instead of turning it into "one block, accelerated extremely".
     */
    private int roundsPerPosition = Integer.MAX_VALUE;

    /** How many positions a capped Torcherino tries to reach with its budget. */
    private static final int BUDGET_SHARE = 64;

    /**
     * Result of {@link AdaptiveBrake} for the current tick: {@code 1} unless the server is
     * already slower than the healthy line and the brake switch is on.
     */
    private int brakeDivisor = 1;

    /**
     * Rotates the chunk column a capped scan starts at, so the budget is spread over the
     * whole area instead of always being spent on the lowest column. Only ever advanced
     * while the budget is finite, so an uncapped scan keeps its natural order.
     */
    private int columnRotation;

    /**
     * The same idea one level down: the section a capped column starts at. It keeps a
     * counter of its own because a column and a section list have different lengths -
     * sharing one counter stepped the section by the column modulus and scrambled the
     * rotation, so a capped scan could keep grinding the same slice of the area.
     */
    private int sectionRotation;

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

    /** Percentage the editor and the action bar show for the current speed. */
    public int getSpeedPercent() {
        return this.speedScaled * this.getTierMultiplier();
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
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (this.pendingRedstoneRefresh) {
            this.pendingRedstoneRefresh = false;
            this.updateActive();
        }
        if (!this.active || this.speedScaled == 0 || (this.xRange == 0 && this.yRange == 0 && this.zRange == 0)) {
            return;
        }
        if (TorcherinoConfig.ownerOnlyWhenOnline && !this.ownerIsOnline(serverLevel)) {
            // Owner gate: an abandoned Torcherino stops costing the server anything. Off by
            // default, and a Torcherino without a recorded owner never takes this path.
            return;
        }
        this.rebuildScanIfNeeded(serverLevel);
        if (!this.scanValid || this.scanMinY > this.scanMaxY) {
            return;
        }
        // Hoisted out of the scan: the multiplier cannot change while a tick runs.
        final double exact = this.speedScaled * this.getTierMultiplier() / (double) SPEED_SCALE;
        int whole = (int) Math.floor(exact);
        this.speedCarry += exact - whole;
        if (this.speedCarry >= 1.0D) {
            this.speedCarry -= 1.0D;
            whole++;
        }
        this.scannerMultiplier = whole;
        // The switch decides whether the cap is used at all; 0 from the getter means unlimited.
        final int configuredBudget = TorcherinoConfig.effectiveMaxAcceleratedTicksPerTick();
        if (configuredBudget > 0) {
            this.workBudget = configuredBudget;
            this.roundsPerPosition = Math.max(1, configuredBudget / BUDGET_SHARE);
        } else {
            this.workBudget = Integer.MAX_VALUE;
            this.roundsPerPosition = Integer.MAX_VALUE;
        }
        this.brakeDivisor = AdaptiveBrake.divisor(serverLevel);
        // Diagnostics and the shared budget both key off the server tick number, so they are
        // told about this Torcherino before it starts scanning.
        AccelerationStats.torcherino(serverLevel);
        GlobalBudget.enterTick(serverLevel);
        this.scanArea(serverLevel);
    }

    /**
     * Rounds one position may use this tick: the smallest of the configured multiplier, the
     * remaining per Torcherino budget, the per position share and the fair share of the
     * server wide budget, then divided by the adaptive brake. With the ship defaults - no
     * budget, no global budget, brake off - this is exactly the configured multiplier, i.e.
     * the classic behaviour.
     */
    private int roundsFor() {
        int rounds = Math.min(this.scannerMultiplier, Math.min(this.workBudget, this.roundsPerPosition));
        // The shared budget is applied second: a Torcherino may never take more than its fair
        // share of the server wide allowance, whatever its own cap says.
        rounds = GlobalBudget.allowance(rounds);
        if (this.brakeDivisor > 1 && rounds > 1) {
            rounds = Math.max(1, rounds / this.brakeDivisor);
        }
        return rounds;
    }

    private void rebuildScanIfNeeded(ServerLevel level) {
        final BlockPos origin = this.worldPosition;
        final int minSection = level.getMinSection();
        final int sectionsCount = level.getSectionsCount();
        if (this.scanValid
                && this.cachedXRange == this.xRange && this.cachedYRange == this.yRange
                && this.cachedZRange == this.zRange
                && this.cachedOriginX == origin.getX() && this.cachedOriginY == origin.getY()
                && this.cachedOriginZ == origin.getZ()
                && this.cachedMinSection == minSection && this.cachedSectionsCount == sectionsCount) {
            return;
        }
        this.cachedXRange = this.xRange;
        this.cachedYRange = this.yRange;
        this.cachedZRange = this.zRange;
        this.cachedOriginX = origin.getX();
        this.cachedOriginY = origin.getY();
        this.cachedOriginZ = origin.getZ();
        this.cachedMinSection = minSection;
        this.cachedSectionsCount = sectionsCount;

        this.scanMinX = origin.getX() - this.xRange;
        this.scanMaxX = origin.getX() + this.xRange;
        this.scanMinZ = origin.getZ() - this.zRange;
        this.scanMaxZ = origin.getZ() + this.zRange;
        this.scanMinY = Math.max(level.getMinBuildHeight(), origin.getY() - this.yRange);
        this.scanMaxY = Math.min(level.getMaxBuildHeight() - 1, origin.getY() + this.yRange);
        this.scanValid = true;
        // A rebuilt window means different positions: saturation bookkeeping of the old area
        // says nothing about the new one.
        this.idlePositions.clear();
        if (this.scanMinY > this.scanMaxY) {
            return;
        }
        this.scanMinSectionIndex = (this.scanMinY >> 4) - minSection;
        this.scanMaxSectionIndex = (this.scanMaxY >> 4) - minSection;
    }

    /**
     * Walks the scan window one chunk column at a time. Resolving a column is a single
     * chunk lookup for up to 16x16x16 positions, instead of one per position.
     */
    private void scanArea(ServerLevel level) {
        final int minChunkX = this.scanMinX >> 4;
        final int maxChunkX = this.scanMaxX >> 4;
        final int minChunkZ = this.scanMinZ >> 4;
        final int maxChunkZ = this.scanMaxZ >> 4;
        final int columnsX = maxChunkX - minChunkX + 1;
        final int columns = columnsX * (maxChunkZ - minChunkZ + 1);
        // A capped scan also starts at a rotating chunk column, so the budget is spread over
        // the whole area instead of always being spent on the lowest column.
        final int first = this.workBudget == Integer.MAX_VALUE
                ? 0
                : Math.floorMod(this.columnRotation++, columns);
        for (int step = 0; step < columns; step++) {
            int flat = first + step;
            if (flat >= columns) {
                flat -= columns;
            }
            final int chunkX = minChunkX + flat % columnsX;
            final int chunkZ = minChunkZ + flat / columnsX;
            final int minX = Math.max(this.scanMinX, chunkX << 4);
            final int maxX = Math.min(this.scanMaxX, (chunkX << 4) + 15);
            final int minZ = Math.max(this.scanMinZ, chunkZ << 4);
            final int maxZ = Math.min(this.scanMaxZ, (chunkZ << 4) + 15);
            // Only chunks that are already loaded: forcing a load here would generate
            // terrain and read the disk in the middle of a tick.
            final LevelChunk chunk = ChunkView.loadedChunk(level, chunkX, chunkZ);
            if (chunk == null) {
                continue;
            }
            this.scanColumn(level, chunk, minX, maxX, minZ, maxZ);
            if (this.workBudget <= 0) {
                return;
            }
        }
    }

    /**
     * Walks the sections of one chunk column that the window overlaps. Sections that are
     * all air, or whose palette holds nothing that could be accelerated, cost one probe.
     */
    private void scanColumn(ServerLevel level, LevelChunk chunk, int minX, int maxX, int minZ, int maxZ) {
        final int minIndex = this.scanMinSectionIndex;
        final int maxIndex = this.scanMaxSectionIndex;
        final int count = maxIndex - minIndex + 1;
        // A capped scan starts at a rotating section: the budget then trims a different
        // slice of the area every tick instead of always the same far corner.
        final int first = this.workBudget == Integer.MAX_VALUE
                ? minIndex
                : minIndex + Math.floorMod(this.sectionRotation++, count);
        for (int step = 0; step < count; step++) {
            int index = first + step;
            if (index > maxIndex) {
                index -= count;
            }
            final LevelChunkSection section = chunk.getSection(index);
            if (section.hasOnlyAir() || !section.maybeHas(ACCELERATABLE)) {
                AccelerationStats.sectionSkipped();
                continue;
            }
            final int sectionMinY = (index + this.cachedMinSection) << 4;
            final int minY = Math.max(sectionMinY, this.scanMinY);
            final int maxY = Math.min(sectionMinY + 15, this.scanMaxY);
            for (int y = minY; y <= maxY; y++) {
                final int localY = y & 15;
                for (int x = minX; x <= maxX; x++) {
                    final int localX = x & 15;
                    for (int z = minZ; z <= maxZ; z++) {
                        final BlockState state = section.getBlockState(localX, localY, z & 15);
                        // One shared lookup decides everything about this position, and the
                        // answer is handed to tickBlock so it does not have to ask again.
                        final int flags = AccelerationCache.flags(state);
                        if (flags == 0) {
                            continue;
                        }
                        this.tickBlock(level, chunk, section, x, y, z, state, flags);
                        if (this.workBudget <= 0) {
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Accelerates one position. Every state read of the inner loops comes straight from the
     * section the scan already holds, which is both cheaper than a world level lookup and
     * still exact: a palette read always sees the changes a tick just made.
     */
    private void tickBlock(ServerLevel level, LevelChunk chunk, LevelChunkSection section,
                           int x, int y, int z, BlockState blockState, int flags) {
        // Every budget is checked before any lookup: work that cannot be paid for must not be
        // prepared. Without a configured cap these are always satisfiable.
        if (this.workBudget <= 0 || GlobalBudget.exhausted()) {
            return;
        }
        final int localX = x & 15;
        final int localY = y & 15;
        final int localZ = z & 15;
        AccelerationStats.position();

        // Saturation skip (off by default): a position that has not changed - itself nor the
        // block states around it - for a couple of ticks is throttled to a single call until
        // something moves again. This is the only place that deliberately hands out fewer
        // calls than configured, which is why it needs the switch.
        final boolean throttled = TorcherinoConfig.skipSaturatedPositions
                && this.scannerMultiplier >= SATURATION_MIN_ROUNDS
                && this.refreshSaturation(section, x, y, z, localX, localY, localZ);

        // One immutable copy per accelerated position, shared by both branches below.
        BlockPos target = null;

        if ((flags & AccelerationCache.RANDOM_TICK) != 0) {
            final int rounds = throttled ? 1 : this.roundsFor();
            if (rounds > 0) {
                target = new BlockPos(x, y, z);
                final Block block = blockState.getBlock();
                int done;
                if (TorcherinoConfig.probabilisticRandomTick) {
                    // One roll replaces the whole burst, so the tick cost of this position
                    // stops depending on the multiplier. Off by default: the configured rate
                    // is only matched in expectation, not call for call.
                    done = rollsRandomTick(level, rounds) ? 1 : 0;
                    if (done == 1) {
                        block.randomTick(blockState, level, target, this.rand);
                    }
                } else {
                    done = 0;
                    while (done < rounds) {
                        block.randomTick(blockState, level, target, this.rand);
                        done++;
                        if (section.getBlockState(localX, localY, localZ) != blockState) {
                            // The block was replaced, removed or transformed: ticking the
                            // stale state any further would accelerate something that is no
                            // longer there.
                            break;
                        }
                    }
                }
                // Only the calls that really ran are charged. Charging the planned amount
                // would let a block that stopped early eat budget that another position could
                // use, which is exactly what a configured cap must not do.
                this.workBudget -= done;
                GlobalBudget.spend(done);
                AccelerationStats.randomTick(done);
            }
        }

        if ((flags & AccelerationCache.BLOCK_ENTITY) == 0 || this.workBudget <= 0 || GlobalBudget.exhausted()) {
            return;
        }
        if (target == null) {
            target = new BlockPos(x, y, z);
        }
        // LevelChunk#getBlockEntity with CHECK is this very map lookup on the common path
        // (it only consults the pending worldgen map when the map has no entry), so the
        // official accessor is kept: it also promotes a block entity that is still pending.
        final BlockEntity blockEntity = chunk.getBlockEntity(target, LevelChunk.EntityCreationType.CHECK);
        if (blockEntity == null || blockEntity.isRemoved()) {
            return;
        }
        if (TorcherinoRegistry.isTileBlacklisted(blockEntity.getClass())) {
            return;
        }
        final BlockEntityTicker<BlockEntity> ticker = resolveTicker(level, blockState, blockEntity);
        if (ticker == null) {
            return;
        }
        final int rounds = throttled ? 1 : this.roundsFor();
        int done = 0;
        while (done < rounds && !blockEntity.isRemoved()) {
            ticker.tick(level, target, blockState, blockEntity);
            done++;
        }
        // Same rule as the random tick branch: charge what actually ran.
        this.workBudget -= done;
        GlobalBudget.spend(done);
        AccelerationStats.blockEntityTick(done);
    }

    /**
     * Updates the saturation bookkeeping of one position.
     *
     * @return {@code true} when the position should be throttled to a single call this tick.
     */
    private boolean refreshSaturation(LevelChunkSection section, int x, int y, int z,
                                      int localX, int localY, int localZ) {
        final long signature = neighbourhoodSignature(section, localX, localY, localZ);
        if (signature == 0L) {
            // The position sits on the border of its section, so its neighbourhood cannot be
            // read completely: it simply keeps running at the configured speed.
            return false;
        }
        final long key = BlockPos.asLong(x, y, z);
        Idle idle = this.idlePositions.get(key);
        if (idle == null) {
            if (this.idlePositions.size() >= SATURATION_MAX_ENTRIES) {
                this.idlePositions.clear();
            }
            idle = new Idle();
            idle.signature = signature;
            this.idlePositions.put(key, idle);
            return false;
        }
        if (idle.signature == signature) {
            if (idle.unchangedTicks < SATURATION_CONFIRM_TICKS) {
                idle.unchangedTicks++;
            }
        } else {
            idle.signature = signature;
            idle.unchangedTicks = 0;
        }
        return idle.unchangedTicks >= SATURATION_CONFIRM_TICKS;
    }

    /**
     * Signature of a position together with the twenty six block states around it, all read
     * from the section the scan already holds. The states are compared by identity, which is
     * enough: a {@code BlockState} is an immutable singleton, so "same reference" means "same
     * block and same properties".
     *
     * @return the signature, or {@code 0} when the position is on the border of the section
     *         and the neighbourhood cannot be read without leaving it.
     */
    private static long neighbourhoodSignature(LevelChunkSection section, int localX, int localY, int localZ) {
        if (localX == 0 || localX == 15 || localY == 0 || localY == 15 || localZ == 0 || localZ == 15) {
            return 0L;
        }
        long hash = 1L;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    hash = hash * 31L + System.identityHashCode(
                            section.getBlockState(localX + dx, localY + dy, localZ + dz));
                }
            }
        }
        return hash;
    }

    /**
     * Decides whether a probabilistic random tick happens.
     *
     * <p>Vanilla hands a random tick to a position with the probability
     * {@code randomTickSpeed / 4096}; a Torcherino that would hand out {@code rounds} calls
     * therefore scales that probability by {@code rounds}. One roll replaces the whole burst,
     * which is what turns the cost of a tick from "positions x multiplier" into "positions".
     * The roll uses the level random, so the acceleration does not disturb the sequence the
     * Torcherino passes to {@code randomTick}.</p>
     */
    private static boolean rollsRandomTick(ServerLevel level, int rounds) {
        final int randomTickSpeed = level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        if (randomTickSpeed <= 0) {
            return false;
        }
        final long chance = (long) rounds * randomTickSpeed;
        return chance >= 4096L || level.getRandom().nextInt(4096) < chance;
    }

    /** @return {@code true} when this Torcherino may run, i.e. its owner is online. */
    private boolean ownerIsOnline(ServerLevel level) {
        if (this.owner == null || this.owner.isEmpty()) {
            // No owner recorded: the gate cannot judge, so it lets the Torcherino run.
            return true;
        }
        try {
            return level.getServer().getPlayerList().getPlayer(UUID.fromString(this.owner)) != null;
        } catch (IllegalArgumentException e) {
            Constants.LOG.warn("Torcherino at {} has an unreadable owner '{}', ignoring the owner gate",
                    this.worldPosition, this.owner);
            return true;
        }
    }

    /**
     * Records the player who placed this Torcherino. Used by the optional owner gate and, as
     * far as the name is concerned, by the item tooltip of whatever this block drops.
     *
     * @param uuid the placer, or {@code null} to drop the claim.
     * @param name the placer's display name, may be {@code null} or empty when unknown.
     */
    public void setOwner(UUID uuid, String name) {
        this.owner = uuid == null ? null : uuid.toString();
        this.ownerName = uuid == null || name == null || name.isEmpty() ? null : name;
        this.sync();
    }

    /** @return the owner as UUID text, or {@code null} while the Torcherino is unclaimed. */
    public String getOwnerId() {
        return this.owner;
    }

    /** @return the stored owner name, or {@code null} while it is unknown. */
    public String getOwnerName() {
        return this.ownerName;
    }

    /** @return {@code true} while players other than the owner may edit the settings. */
    public boolean othersCanEdit() {
        return this.othersCanEdit;
    }

    /**
     * Owner only - the enforcement lives in the packet handlers, this setter just stores and
     * synchronises the flag.
     */
    public void setOthersCanEdit(boolean value) {
        this.othersCanEdit = value;
        this.sync();
    }

    /** Drops the claim, so the Torcherino counts as unclaimed again. Owner only as well. */
    public void clearOwner() {
        this.owner = null;
        this.ownerName = null;
        this.othersCanEdit = true;
        this.sync();
    }

    /** @return {@code true} when {@code player} is the recorded owner. */
    public boolean isOwner(Player player) {
        return this.owner != null && !this.owner.isEmpty() && this.owner.equals(player.getUUID().toString());
    }

    /**
     * @return {@code true} when {@code player} may change the settings: an unclaimed
     *         Torcherino is open to everyone, a claimed one only to its owner while the
     *         "other players may edit" flag is off.
     */
    public boolean mayEdit(Player player) {
        return this.owner == null || this.owner.isEmpty() || this.othersCanEdit || this.isOwner(player);
    }

    /**
     * Resolves the ticker the game itself would run for this block entity, remembering the
     * answer per block state (the lookup is pure, so caching it changes nothing).
     */
    private BlockEntityTicker<BlockEntity> resolveTicker(Level level, BlockState state, BlockEntity blockEntity) {
        final Optional<BlockEntityTicker<BlockEntity>> cached = this.tickerCache.get(state);
        if (cached != null) {
            return cached.orElse(null);
        }
        final BlockEntityTicker<BlockEntity> resolved = computeTicker(level, state, blockEntity);
        this.tickerCache.put(state, Optional.ofNullable(resolved));
        return resolved;
    }

    private static BlockEntityTicker<BlockEntity> computeTicker(Level level, BlockState state, BlockEntity blockEntity) {
        if (!(state.getBlock() instanceof EntityBlock entityBlock)) {
            return null;
        }
        @SuppressWarnings("unchecked")
        final BlockEntityTicker<BlockEntity> ticker = (BlockEntityTicker<BlockEntity>) entityBlock.getTicker(
                level, state, (BlockEntityType<BlockEntity>) blockEntity.getType());
        return ticker;
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

    /** Speed in hundredths of a level; {@link #getSpeedPercent()} is the human readable form. */
    public int getSpeedScaled() {
        return this.speedScaled;
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
    public boolean setValues(int xRange, int zRange, int yRange, int speedScaled, int redstoneMode) {
        this.xRange = clamp(xRange, MAX_XZ_RANGE);
        this.zRange = clamp(zRange, MAX_XZ_RANGE);
        this.yRange = clamp(yRange, MAX_Y_RANGE);
        this.speedScaled = clamp(speedScaled, MAX_SPEED_SCALED);
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
            // One step is one classic gear; free multiplier values are reachable through
            // the editor only, they simply keep whatever is left of the last gear here.
            this.speedScaled = this.speedScaled + SPEED_SCALE <= MAX_SPEED_SCALED
                    ? this.speedScaled + SPEED_SCALE : 0;
            this.sync();
            return;
        }
        final int next = this.xRange < MAX_XZ_RANGE ? this.xRange + 1 : 0;
        this.setValues(next, next, next, this.speedScaled, this.redstoneMode);
    }

    /** Action bar text of the quick interaction. */
    public Component getDescription() {
        return Component.translatable("message.torcherino.status",
                this.getModeDescription(),
                this.getSpeedPercent() + "%");
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
        tag.putInt("SpeedScaled", this.speedScaled);
        // Readers that only know the classic model understand a whole level, not hundredths.
        tag.putInt("Speed", this.speedScaled / SPEED_SCALE);
        tag.putInt("RedstoneMode", this.redstoneMode);
        tag.putBoolean("Active", this.active);
        // Legacy keys are still written so a world stays readable by the classic radius
        // model; "Mode" mirrors the X range, which is what the old code used as radius.
        tag.putByte("Mode", (byte) this.xRange);
        tag.putBoolean("PoweredByRedstone", this.poweredByRedstone);
        // Only written when known, so a world from before the owner gate is unchanged.
        if (this.owner != null) {
            tag.putString("Owner", this.owner);
            if (this.ownerName != null) {
                tag.putString("OwnerName", this.ownerName);
            }
            tag.putBoolean("OthersCanEdit", this.othersCanEdit);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.poweredByRedstone = tag.getBoolean("PoweredByRedstone");
        this.owner = tag.contains("Owner") ? tag.getString("Owner") : null;
        this.ownerName = tag.contains("OwnerName") ? tag.getString("OwnerName") : null;
        // Missing key means "written before the flag existed" or "no owner": both are "others
        // may edit".
        this.othersCanEdit = !tag.contains("OthersCanEdit") || tag.getBoolean("OthersCanEdit");

        if (tag.contains("XRange")) {
            this.xRange = clamp(tag.getInt("XRange"), MAX_XZ_RANGE);
            this.zRange = clamp(tag.getInt("ZRange"), MAX_XZ_RANGE);
            this.yRange = clamp(tag.getInt("YRange"), MAX_Y_RANGE);
            this.speedScaled = clamp(tag.contains("SpeedScaled")
                    ? tag.getInt("SpeedScaled") : tag.getInt("Speed") * SPEED_SCALE, MAX_SPEED_SCALED);
            this.redstoneMode = clamp(tag.getInt("RedstoneMode"), REDSTONE_MODES - 1);
        } else {
            // Classic save: one radius for all three axes, redstone could only switch it off.
            final int radius = clamp(tag.getByte("Mode"), MAX_XZ_RANGE);
            this.xRange = radius;
            this.zRange = radius;
            this.yRange = radius;
            this.speedScaled = clamp(tag.getByte("Speed") * SPEED_SCALE, MAX_SPEED_SCALED);
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
