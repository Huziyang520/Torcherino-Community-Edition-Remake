package com.sci.torcherino.api;

import com.sci.torcherino.TorcherinoConfig;

import net.minecraft.server.level.ServerLevel;

/**
 * Shared per server tick acceleration budget.
 *
 * <p>Every Torcherino keeps its own budget as well; this one bounds the cost of the whole
 * mod at once, which is what a server admin needs when a single player builds a wall of
 * triple compressed Torcherinos. The total is divided over the Torcherinos that took part
 * in the previous tick, so one of them cannot eat the whole allowance and leave the rest
 * of the world starved.</p>
 *
 * <p><b>Disabled by default.</b> While {@code general.maxAcceleratedTicksPerTickGlobal} is
 * {@code 0}, {@link #allowance(int)} returns its argument unchanged and {@link #spend(int)}
 * does nothing, so the configured speed is played back exactly as written.</p>
 */
public final class GlobalBudget {

    /** Tick the current window belongs to, so every Torcherino sees the same allowance. */
    private static int lastTick = Integer.MIN_VALUE;
    /** Remaining calls of the current tick; {@code Integer.MAX_VALUE} while uncapped. */
    private static int remaining = Integer.MAX_VALUE;
    /** Torcherinos that scanned during the previous tick: the divisor of the fair share. */
    private static int activeLastTick = 1;
    private static int activeThisTick;

    private GlobalBudget() {
    }

    /**
     * Called by every Torcherino that is about to scan. The first call of a tick opens a new
     * window; the rest only count themselves, which is what makes the share fair.
     */
    public static void enterTick(ServerLevel level) {
        // The switch decides whether the cap is used at all; 0 from the getter means unlimited.
        final int configured = TorcherinoConfig.effectiveMaxAcceleratedTicksGlobal();
        final int tickCount = level.getServer().getTickCount();
        if (tickCount != lastTick) {
            lastTick = tickCount;
            activeLastTick = Math.max(1, activeThisTick);
            activeThisTick = 0;
            remaining = configured > 0 ? configured : Integer.MAX_VALUE;
        }
        activeThisTick++;
    }

    /**
     * @param wanted rounds this Torcherino would like to run for one position.
     * @return how many of them the global budget allows right now; {@code wanted} while the
     *         feature is off, {@code 0} when the budget for this tick is used up.
     */
    public static int allowance(int wanted) {
        if (remaining == Integer.MAX_VALUE) {
            return wanted;
        }
        if (remaining <= 0) {
            return 0;
        }
        final int share = Math.max(1, remaining / activeLastTick);
        return Math.min(wanted, Math.min(share, remaining));
    }

    /** Charges calls that really ran. */
    public static void spend(int amount) {
        if (remaining != Integer.MAX_VALUE && amount > 0) {
            remaining = Math.max(0, remaining - amount);
        }
    }

    /** @return {@code true} while a finite budget is configured. */
    public static boolean capped() {
        return remaining != Integer.MAX_VALUE;
    }

    /** @return {@code true} when a finite budget is configured and already used up. */
    public static boolean exhausted() {
        return remaining <= 0;
    }

    /**
     * @return the calls left in this tick, or {@code -1} while the budget is unlimited.
     *         Reported by the statistics logger.
     */
    public static int remaining() {
        return remaining == Integer.MAX_VALUE ? -1 : remaining;
    }
}
