/*
 * 本文件：RegistryEntry 的通用实现。
 * 说明：Fabric 直接用它；Forge 也复用它，只是把 supplier 包一层 RegistryObject 的存在性判断。
 */
package com.sci.torcherino.platform.services;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Straightforward {@link RegistryEntry} backed by an id and a lazy supplier. Used on
 * Fabric, and as the building block for the Forge wrapper around {@code RegistryObject}.
 */
public final class SimpleRegistryEntry<T> implements RegistryEntry<T> {

    private final ResourceLocation id;
    private final Supplier<T> supplier;

    public SimpleRegistryEntry(ResourceLocation id, Supplier<T> supplier) {
        this.id = Objects.requireNonNull(id, "id");
        this.supplier = Objects.requireNonNull(supplier, "supplier");
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public T get() {
        return this.supplier.get();
    }

    @Override
    public String toString() {
        return "RegistryEntry[" + this.id + "]";
    }
}
