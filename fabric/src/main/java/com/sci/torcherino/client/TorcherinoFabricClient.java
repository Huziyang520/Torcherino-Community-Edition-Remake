package com.sci.torcherino.client;

import com.sci.torcherino.platform.Services;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * Client entry point: registers the modifier key and reports state changes to the
 * server, mirroring the 1.12.2 {@code ClientTickEvent} polling loop.
 *
 * <p>The original key code was {@code 42} in LWJGL2 which is Left Shift; the GLFW
 * equivalent is {@code GLFW_KEY_LEFT_SHIFT}. The (misspelled) translation key is kept
 * verbatim.</p>
 */
public class TorcherinoFabricClient implements ClientModInitializer {

    public static final KeyMapping USAGE_KEY = new KeyMapping(
            "key.torcherino.useage_key",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_SHIFT,
            "key.categories.gameplay");

    private boolean lastState;

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(USAGE_KEY);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.isPaused()) {
                return;
            }
            if (client.player == null) {
                return;
            }
            final boolean down = USAGE_KEY.isDown();
            if (down != this.lastState) {
                Services.PLATFORM.sendModifierKeyToServer(down);
                this.lastState = down;
            }
        });
    }
}
