/*
 * 本文件：Fabric 侧的配置界面登记（preLaunch 入口）。
 * 说明：Mod Menu 在自己的客户端入口里一次性收集各模组的界面工厂，晚于普通客户端入口，
 *      因此这里提前登记到 Neo Configuration Screen Port，模组列表才会出现「配置」按钮。
 */
package com.sci.torcherino.client;

import com.sci.torcherino.Constants;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

/**
 * Registers the configuration screen early enough for Mod Menu to pick it up.
 *
 * <p>Mod Menu reads the factories other mods provide during its own client entrypoint, which
 * runs before a regular client entrypoint of this mod would. Registering here - before any
 * entrypoint runs - keeps the config button on the mod list entry.</p>
 */
public final class TorcherinoConfigScreenRegistrar implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            register();
        }
    }

    /** Kept in its own method so the client only screen port class is never touched on a server. */
    private static void register() {
        com.huziyang.neoconfigurationscreenport.fabric.ConfigScreenRegistration.register(Constants.MOD_ID);
    }
}
