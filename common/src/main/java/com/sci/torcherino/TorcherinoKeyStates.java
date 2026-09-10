package com.sci.torcherino;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server side record of the client's modifier key state.
 *
 * <p>7.5 kept a {@code HashMap<EntityPlayer, Boolean>} which leaked entries when a
 * player disconnected. The behaviour is identical here, only keyed by UUID and with
 * an explicit removal hook.</p>
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
