/*
 * 本文件：方块「是否值得加速」的共享属性缓存（加速内核的公用 API）。
 * 说明：把「能不能被加速 / 是否随机刻方块 / 是否带方块实体」的判定结果按 BlockState 缓存下来，
 *      扫描热路径上每格只需一次查表，替代原来的 air 判定 + 流体 instanceof + 黑名单 HashSet 查询 + 两次状态查询。
 *      BlockState 是不可变单例，因此结果天然稳定；唯一会变的输入是黑名单，
 *      由 TorcherinoRegistry 在重建默认黑名单时调用 invalidate() 主动失效。
 */
package com.sci.torcherino.api;

import com.sci.torcherino.TorcherinoRegistry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared per state lookup of the acceleration attributes.
 *
 * <p>The acceleration core asks the very same questions about the very same block states
 * millions of times per tick, and the answers cannot change: a {@code BlockState} is an
 * immutable singleton, and the only mutable input - the blacklist - invalidates this cache
 * when it is rebuilt. One map lookup therefore replaces four separate tests per visited
 * position, and the section level palette probe can use the identical rule, so both levels
 * of the scan agree by construction.</p>
 *
 * <p>A {@code ConcurrentHashMap} is used because a configuration reload rebuilds the
 * blacklist on another thread while the server thread may be scanning.</p>
 */
public final class AccelerationCache {

    /** The state may be handed extra random ticks. */
    public static final int RANDOM_TICK = 1;
    /** The state owns a block entity that may be handed extra ticks. */
    public static final int BLOCK_ENTITY = 2;
    /** The state is worth visiting at all: not air, not a fluid, not blacklisted, and one of the two above. */
    public static final int ACCELERATABLE = 4;

    /** {@code 0} means "nothing to do here"; the boxed small integers are shared by the JDK. */
    private static final Map<BlockState, Integer> FLAGS = new ConcurrentHashMap<>(4096);

    private AccelerationCache() {
    }

    /**
     * @param state an immutable block state.
     * @return the OR of {@link #RANDOM_TICK}, {@link #BLOCK_ENTITY} and {@link #ACCELERATABLE},
     *         or {@code 0} when the state can never produce work.
     */
    public static int flags(BlockState state) {
        final Integer cached = FLAGS.get(state);
        if (cached != null) {
            return cached;
        }
        final int computed = compute(state);
        FLAGS.put(state, computed);
        return computed;
    }

    /** @return {@code true} when a section holding this state has to be walked. */
    public static boolean isAcceleratable(BlockState state) {
        return (flags(state) & ACCELERATABLE) != 0;
    }

    /** Drops every cached answer. Called whenever the blacklist is rebuilt. */
    public static void invalidate() {
        FLAGS.clear();
    }

    private static int compute(BlockState state) {
        if (state.isAir()) {
            return 0;
        }
        final boolean randomTick = state.isRandomlyTicking();
        final boolean blockEntity = state.hasBlockEntity();
        if (!randomTick && !blockEntity) {
            return 0;
        }
        final Block block = state.getBlock();
        if (block instanceof LiquidBlock || TorcherinoRegistry.isBlockBlacklisted(block)) {
            return 0;
        }
        int flags = ACCELERATABLE;
        if (randomTick) {
            flags |= RANDOM_TICK;
        }
        if (blockEntity) {
            flags |= BLOCK_ENTITY;
        }
        return flags;
    }
}
