/*
 * 本文件：加速内核的统计输出（默认关闭）。
 * 说明：累计"扫描位置数 / 实际补刻次数 / 跳过的区块段数"等计数，按配置的间隔打印一行日志，
 *      方便服主自己判断"是倍率问题还是整机问题"。间隔为 0 时全部计数被跳过，热路径零负担。
 */
package com.sci.torcherino.api;

import com.sci.torcherino.Constants;
import com.sci.torcherino.TorcherinoConfig;

import net.minecraft.server.level.ServerLevel;

/**
 * Periodic diagnostics of the acceleration loop.
 *
 * <p>The numbers answer the question a server admin actually has: how much work does the
 * mod ask the server to do, and is that work bounded? They are printed every
 * {@code general.statsLogIntervalSeconds} seconds, and the whole thing is skipped while the
 * interval is {@code 0}, which is the default.</p>
 */
public final class AccelerationStats {

    private static int lastTick = Integer.MIN_VALUE;
    private static long windowStartNanos;
    private static int torcherinosLastTick;
    private static int torcherinosThisTick;
    private static long positions;
    private static long randomTicks;
    private static long blockEntityTicks;
    private static long sectionsSkipped;

    private AccelerationStats() {
    }

    /** @return {@code true} while the statistics logger is switched on. */
    public static boolean enabled() {
        return TorcherinoConfig.statsLogIntervalSeconds > 0;
    }

    /**
     * Called once per Torcherino that is about to scan. The first call of a new server tick
     * closes the previous window, so the report is produced at a tick boundary.
     */
    public static void torcherino(ServerLevel level) {
        if (!enabled()) {
            return;
        }
        final int tickCount = level.getServer().getTickCount();
        if (tickCount != lastTick) {
            lastTick = tickCount;
            torcherinosLastTick = torcherinosThisTick;
            torcherinosThisTick = 0;
            final long now = System.nanoTime();
            final long interval = TorcherinoConfig.statsLogIntervalSeconds * 1_000_000_000L;
            if (windowStartNanos == 0L) {
                windowStartNanos = now;
            } else if (now - windowStartNanos >= interval) {
                report(now - windowStartNanos);
                windowStartNanos = now;
            }
        }
        torcherinosThisTick++;
    }

    /** One more position of the scan window was visited. */
    public static void position() {
        if (enabled()) {
            positions++;
        }
    }

    /** Extra random ticks that were really handed out. */
    public static void randomTick(int calls) {
        if (enabled() && calls > 0) {
            randomTicks += calls;
        }
    }

    /** Extra block entity ticks that were really handed out. */
    public static void blockEntityTick(int calls) {
        if (enabled() && calls > 0) {
            blockEntityTicks += calls;
        }
    }

    /** A chunk section was proven empty without walking it. */
    public static void sectionSkipped() {
        if (enabled()) {
            sectionsSkipped++;
        }
    }

    private static void report(long elapsedNanos) {
        Constants.LOG.info(
                "Torcherino stats over {} ms: torcherinos/tick={}, positions={}, random ticks={}, block entity ticks={}, "
                        + "sections skipped={}, adaptive divisor={}, global budget left={}",
                elapsedNanos / 1_000_000L,
                torcherinosLastTick,
                positions,
                randomTicks,
                blockEntityTicks,
                sectionsSkipped,
                AdaptiveBrake.currentDivisor(),
                GlobalBudget.remaining());
        positions = 0L;
        randomTicks = 0L;
        blockEntityTicks = 0L;
        sectionsSkipped = 0L;
    }
}
