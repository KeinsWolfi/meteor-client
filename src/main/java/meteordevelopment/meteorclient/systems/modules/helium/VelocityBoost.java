/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.helium;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;

public class VelocityBoost extends Module {

    public final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Double> modifierX = sgGeneral.add(new DoubleSetting.Builder()
        .name("x-modifier")
        .description("X Velocity Modifier.")
        .defaultValue(1.5)
        .sliderMin(0.1)
        .sliderMax(10)
        .build()
    );

    public final Setting<Double> modifierY = sgGeneral.add(new DoubleSetting.Builder()
        .name("y-modifier")
        .description("Y Velocity Modifier.")
        .defaultValue(1.5)
        .sliderMin(0.1)
        .sliderMax(10)
        .build()
    );

    public final Setting<Double> modifierZ = sgGeneral.add(new DoubleSetting.Builder()
        .name("z-modifier")
        .description("Z Velocity Modifier.")
        .defaultValue(1.5)
        .sliderMin(0.1)
        .sliderMax(10)
        .build()
    );

    public VelocityBoost() {
        super(Categories.Helium, "velocity-boost", "Boosts your velocity. Great for travelling.");
    }

    @EventHandler
    public void onPacket(PacketEvent.Receive e) {
        if (e.packet instanceof EntityVelocityUpdateS2CPacket packet && packet.getEntityId() == mc.player.getId()) {
            // Convert velocity from raw int format to double
            double veloX = packet.getVelocityX();
            double veloY = packet.getVelocityY();
            double veloZ = packet.getVelocityZ();
            double totalVelo = Math.sqrt((veloX * veloX) + (veloY * veloY) + (veloZ * veloZ));

            ChatUtils.debug("Total Velocity: " + totalVelo + " (" + veloX + ", " + veloY + ", " + veloZ + ")");

            // Now the condition checks the actual velocity instead of raw values
            if (totalVelo > 2.3 && totalVelo < 2.5) {  // Corrected check
                Vec3d velocity = mc.player.getVelocity();
                mc.execute(() -> {
                    mc.player.setVelocity(velocity.x * modifierX.get(), velocity.y * modifierY.get(), velocity.z * modifierZ.get());
                });
                //mc.player.setVelocity(velocity.x * modifierX.get(), velocity.y * modifierY.get(), velocity.z * modifierZ.get());
            }
        }
    }
}
