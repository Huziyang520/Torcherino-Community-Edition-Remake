package com.sci.torcherino.client;

import com.sci.torcherino.network.TorcherinoNetwork;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Client only Forge handlers. This class must only be touched from inside a
 * {@code Dist.CLIENT} guard, since it references client only types.
 */
public final class TorcherinoForgeClientEvents {

    public static final KeyMapping USAGE_KEY = new KeyMapping(
            "key.torcherino.useage_key",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_SHIFT,
            "key.categories.gameplay");

    private static boolean lastState;

    private TorcherinoForgeClientEvents() {
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(USAGE_KEY);
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isPaused() || minecraft.player == null) {
            return;
        }
        final boolean down = USAGE_KEY.isDown();
        if (down != lastState) {
            TorcherinoNetwork.sendToServer(down);
            lastState = down;
        }
    }
}
