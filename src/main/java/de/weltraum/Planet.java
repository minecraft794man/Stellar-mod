package de.weltraum;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Alle Ziele der Rakete. Die Dimensionen (orbit, moon, mars) stehen in data/weltraum/dimension. */
public enum Planet {
    EARTH("earth", Level.OVERWORLD, 1.0, false, "Erde", new Vec3(0.5, 100, 0.5)),
    ORBIT("orbit", key("orbit"), 0.10, true, "Erdorbit", new Vec3(0.5, 101, 0.5)),
    MOON("moon", key("moon"), 0.17, true, "Mond", new Vec3(0.5, 2, 0.5)),
    MARS("mars", key("mars"), 0.38, true, "Mars", new Vec3(0.5, 2, 0.5));

    private final String id;
    private final ResourceKey<Level> dimension;
    private final double gravity;
    private final boolean airless;
    private final String displayName;
    private final Vec3 spawn;

    Planet(String id, ResourceKey<Level> dimension, double gravity, boolean airless, String displayName, Vec3 spawn) {
        this.id = id;
        this.dimension = dimension;
        this.gravity = gravity;
        this.airless = airless;
        this.displayName = displayName;
        this.spawn = spawn;
    }

    private static ResourceKey<Level> key(String name) {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(WeltraumMod.MODID, name));
    }

    public String id() { return id; }
    public ResourceKey<Level> dimension() { return dimension; }
    /** Faktor auf die normale Schwerkraft (1.0 = Erde). */
    public double gravity() { return gravity; }
    public boolean isAirless() { return airless; }
    public String displayName() { return displayName; }
    public Vec3 spawn() { return spawn; }

    public Planet next() {
        Planet[] all = values();
        return all[(ordinal() + 1) % all.length];
    }

    public static Planet byDimension(ResourceKey<Level> dim) {
        for (Planet p : values()) {
            if (p.dimension.equals(dim)) return p;
        }
        return null;
    }

    public static Planet byId(String id) {
        for (Planet p : values()) {
            if (p.id.equals(id)) return p;
        }
        return null;
    }
}
