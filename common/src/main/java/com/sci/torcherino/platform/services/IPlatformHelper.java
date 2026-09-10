package com.sci.torcherino.platform.services;

import java.nio.file.Path;

/**
 * Loader agnostic access to the few platform specific primitives the common code
 * needs. Implementations live in the loader specific projects and are located
 * through {@link java.util.ServiceLoader}.
 */
public interface IPlatformHelper {

    /**
     * @return the human readable name of the loader currently running the mod.
     */
    String getPlatformName();

    /**
     * @param modId the mod id to look up.
     * @return true when a mod with that id is currently loaded.
     */
    boolean isModLoaded(String modId);

    /**
     * @return true when running inside a development environment.
     */
    boolean isDevelopmentEnvironment();

    /**
     * @return the directory that holds the game configuration files.
     */
    Path getConfigDir();

    /**
     * Sends the client side "modifier key" state to the server.
     *
     * @param pressed true when the modifier key is currently held down.
     */
    void sendModifierKeyToServer(boolean pressed);

    default String getEnvironmentName() {
        return this.isDevelopmentEnvironment() ? "development" : "production";
    }
}
