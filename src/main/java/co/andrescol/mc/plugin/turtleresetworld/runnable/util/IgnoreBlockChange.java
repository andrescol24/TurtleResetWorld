package co.andrescol.mc.plugin.turtleresetworld.runnable.util;

import org.bukkit.Material;

import java.util.Objects;

public class IgnoreBlockChange {

    private final Material newMaterial;
    private final Material actualMaterial;

    public IgnoreBlockChange(Material newMaterial, Material actualMaterial) {
        this.newMaterial = newMaterial;
        this.actualMaterial = actualMaterial;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IgnoreBlockChange that = (IgnoreBlockChange) o;
        return newMaterial == that.newMaterial && actualMaterial == that.actualMaterial;
    }

    @Override
    public int hashCode() {
        return Objects.hash(newMaterial, actualMaterial);
    }
}
