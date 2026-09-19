/*
 * 本文件：服务端保存的「改装键（左 Shift）」按下状态表。
 * 说明：客户端按键状态变化时发包，服务端记在这里，右键火把时用来判断是切范围还是切速度。
 */
package com.sci.torcherino;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server side record of the client's modifier key state.
 *
 * <p>Entries are keyed by player UUID and removed explicitly when a player disconnects,
 * so a long running server does not accumulate stale entries.</p>
 */
public final class TorcherinoKeyStates {

    private static final Map<UUID, Boolean> STATES = new HashMap<>();

    private TorcherinoKeyStates() {
    }

    public static void set(Player player, boolean pressed) {
        STATES.put(player.getUUID(), pressed);
    }

    /**
     * @return the last known state, or {@code null} when the client never reported one.
     */
    public static Boolean get(Player player) {
        return STATES.get(player.getUUID());
    }

    public static void clear(Player player) {
        STATES.remove(player.getUUID());
    }
}
