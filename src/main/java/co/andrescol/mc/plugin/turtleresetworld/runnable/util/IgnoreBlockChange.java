package co.andrescol.mc.plugin.turtleresetworld.runnable.util;

import org.bukkit.Material;

import java.util.Objects;

/**
 * This class represent a filter. The {@link #equals(Object)} method
 * return true if the objects have the same blocks. For example: if in the
 * old world there is aa AIR block and in the clone world there is GRASS
 */
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
        return (newMaterial == that.newMaterial && actualMaterial == that.actualMaterial) ||
                (newMaterial == that.actualMaterial && actualMaterial == that.newMaterial);
    }

    @Override
    public int hashCode() {
        if(newMaterial.ordinal() > actualMaterial.ordinal()) {
            return String.format("%d %d", newMaterial.ordinal(), actualMaterial.ordinal()).hashCode();
        } else {
            return String.format("%d %d", actualMaterial.ordinal(), newMaterial.ordinal()).hashCode();
        }

    }
}
