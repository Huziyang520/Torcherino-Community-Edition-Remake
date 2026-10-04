package com.sci.torcherino.api;

import com.sci.torcherino.Constants;
import com.sci.torcherino.TorcherinoConfig;

import net.minecraft.server.level.ServerLevel;

/**
 * Adaptive brake: keeps the server tick bounded by scaling the acceleration work down while
 * the server is already too slow.
 *
 * <p>The tick duration is measured from inside the acceleration core itself, so no server
 * internals or loader specific APIs are involved: every Torcherino is ticked exactly once per
 * server tick, and the gap between two such ticks is the duration of the tick in between.
 * The measurement is taken once per tick, by whichever Torcherino ticks first, and every
 * Torcherino of that tick then uses the same divisor.</p>
 *
 * <p><b>Disabled by default.</b> While {@code general.adaptiveThrottle} is off,
 * {@link #divisor(ServerLevel)} returns {@code 1} and nothing is scaled, so the configured
 * speed is played back exactly as written.</p>
 */
public final class AdaptiveBrake {

    /** Lowest threshold a player may configure, so the brake cannot react to noise. */
    private static final int MIN_THRESHOLD_MS = 10;
    /**
     * Gaps longer than this are not ticks: the world was paused, the chunk was unloaded or
     * the process was frozen. Reacting to them would brake once for something that never
     * happened.
     */
    private static final long PLAUSIBLE_TICK_NANOS = 1_000_000_000L;
    /** How often the brake may report itself, so a changing divisor cannot flood the log. */
    private static final long LOG_COOLDOWN_NANOS = 5_000_000_000L;

    private static int lastTickCount = Integer.MIN_VALUE;
    private static long lastTickStart;
    private static int divisor = 1;
    private static long lastLogNanos;

    private AdaptiveBrake() {
    }

    /**
     * A tick slower than this counts as unhealthy. 50 ms is the "TPS 20" warning line; the
     * value is configurable because a heavily modded server may only be healthy above it.
     */
    private static long healthyTickNanos() {
        final int configured = TorcherinoConfig.adaptiveThrottleThresholdMs;
        return Math.max(MIN_THRESHOLD_MS, configured) * 1_000_000L;
    }

    /** Upper bound of the divisor, so a stuck server cannot scale the acceleration to zero. */
    private static int maxDivisor() {
        return Math.max(1, TorcherinoConfig.adaptiveThrottleMaxDivisor);
    }

    /**
     * @return how many times the acceleration rounds have to be divided this tick. Always
     *         {@code 1} while the brake is switched off or while the server is healthy.
     */
    public static int divisor(ServerLevel level) {
        if (!TorcherinoConfig.adaptiveThrottle) {
            return 1;
        }
        final int tickCount = level.getServer().getTickCount();
        if (tickCount == lastTickCount) {
            return divisor;
        }
        final long now = System.nanoTime();
        if (lastTickCount != Integer.MIN_VALUE) {
            final long elapsed = now - lastTickStart;
            if (elapsed >= PLAUSIBLE_TICK_NANOS) {
                divisor = 1;
            } else {
                // Rounded up: one millisecond over the line is already "one step over".
                final long healthy = healthyTickNanos();
                final long over = (elapsed + healthy - 1) / healthy;
                final int next = over <= 1 ? 1 : (int) Math.min(over, maxDivisor());
                if (next != divisor) {
                    report(next, elapsed);
                }
                divisor = next;
            }
        }
        lastTickStart = now;
        lastTickCount = tickCount;
        return divisor;
    }

    private static void report(int next, long elapsed) {
        if (next == 1 && divisor > 1) {
            Constants.LOG.info("Adaptive brake released: server tick back to normal, acceleration at full speed again");
            lastLogNanos = System.nanoTime();
            return;
        }
        if (next <= divisor || System.nanoTime() - lastLogNanos < LOG_COOLDOWN_NANOS) {
            return;
        }
        lastLogNanos = System.nanoTime();
        Constants.LOG.info("Adaptive brake engaged: server tick took {} ms, Torcherino acceleration scaled to 1/{}",
                elapsed / 1_000_000L, next);
    }

    /** @return the divisor currently in effect, for diagnostics. */
    public static int currentDivisor() {
        return divisor;
    }
}
