/*
 * 本文件：平台能力接口（加载器无关）。
 * 说明：声明 common 需要的那点平台差异 —— 加载器名、模组是否加载、是否开发环境、配置目录、
 *      改装键状态发包，以及可视化编辑界面的开启/回传。
 */
package com.sci.torcherino.platform.services;

import com.sci.torcherino.blocks.tiles.TileTorcherino;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;


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
     * Sends the client side "modifier key" state to the server.
     *
     * @param pressed true when the modifier key is currently held down.
     */
    void sendModifierKeyToServer(boolean pressed);

    /**
     * Asks the given player's client to open the Torcherino editor. Server side only.
     *
     * @param player      the player that right clicked the block.
     * @param torcherino  the block entity whose values the editor should show.
     */
    void openTorcherinoScreen(ServerPlayer player, TileTorcherino torcherino);

    /**
     * Sends the values edited in the graphical editor to the server. Client side only.
     */
    void sendTorcherinoValues(BlockPos pos, int xRange, int zRange, int yRange, int speed, int redstoneMode);

    /**
     * Sends an owner setting change to the server. Client side only, and only the owner of the
     * Torcherino is allowed to send it - the server checks that again.
     *
     * @param unclaim when {@code true} the claim is dropped entirely and {@code othersCanEdit}
     *                is ignored.
     */
    void sendTorcherinoOwnerSettings(BlockPos pos, boolean othersCanEdit, boolean unclaim);

    default String getEnvironmentName() {
        return this.isDevelopmentEnvironment() ? "development" : "production";
    }
}
