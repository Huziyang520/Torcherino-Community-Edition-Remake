/*
 * 本文件：注册条目的惰性句柄接口。
 * 说明：common 不持有裸实例、也不持有加载器类型，只留「id + 按需 get()」；注册完成前 get() 返回 null，调用方必须自行判空。
 */
package com.sci.torcherino.platform.services;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * A handle to something that has been handed to a loader for registration.
 *
 * <p>Common code must never keep a raw instance around, because on Forge the instance
 * may not exist yet when the handle is created. It also must never keep a loader
 * specific type such as {@code RegistryObject}. This interface is the narrow common
 * denominator: an id plus a lazy accessor.</p>
 *
 * <p>Calling {@link #get()} before the loader has registered the entry yields
 * {@code null} on Forge and the instance on Fabric, so callers must only use it after
 * registration has completed (setup phase, event callbacks, game runtime).</p>
 */
public interface RegistryEntry<T> extends Supplier<T> {

    ResourceLocation getId();

    /** {@code true} when the entry has been registered and {@link #get()} is usable. */
    default boolean isPresent() {
        return get() != null;
    }
}
