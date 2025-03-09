/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.events.world;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;

public class ParticleEvent extends Cancellable {
    private static final ParticleEvent INSTANCE = new ParticleEvent();

    public ParticleEffect particle;
    public double x;
    public double y;
    public double z;

    public static ParticleEvent get(ParticleEffect particle, double x, double y, double z) {
        INSTANCE.setCancelled(false);
        INSTANCE.particle = particle;
        INSTANCE.x = x;
        INSTANCE.y = y;
        INSTANCE.z = z;
        return INSTANCE;
    }
}
