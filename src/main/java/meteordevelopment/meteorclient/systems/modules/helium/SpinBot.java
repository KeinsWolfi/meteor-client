/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.helium;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;

public class SpinBot extends Module {

    public final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final SettingGroup sgBooleans = settings.createGroup("Booleans");

    public final Setting<Integer> dpty = sgGeneral.add(new IntSetting.Builder()
            .name("degrees-per-tick")
            .description("How many degrees per tick to rotate.")
            .sliderMin(1)
            .sliderMax(360)
            .defaultValue((360/20))
            .build()
    );

    public final Setting<Integer> dptp = sgGeneral.add(new IntSetting.Builder()
        .name("degrees-per-tick")
        .description("How many degrees per tick to rotate.")
        .sliderMin(1)
        .sliderMax(90)
        .defaultValue((90/20))
        .build()
    );

    public final Setting<Boolean> spinYaw = sgBooleans.add(new BoolSetting.Builder()
        .name("Spin yaw")
        .description("Spin with yaw.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> spinPitch = sgBooleans.add(new BoolSetting.Builder()
        .name("Spin pitch")
        .description("Spin with pitch.")
        .defaultValue(true)
        .build()
    );

    public SpinBot() {
        super(Categories.Helium, "SpinBot", "Makes you spin around.");
    }

    @EventHandler
    public void onTick(TickEvent.Pre event){
        if(spinYaw.get()){
            double yaw = Rotations.serverYaw;

            yaw += dpty.get();
            yaw %= 360;

            Rotations.rotate(yaw, Rotations.serverPitch, 0);
        }
        if(spinPitch.get()){
            double pitch = Rotations.serverPitch;

            pitch += dptp.get();

            Rotations.rotate(Rotations.serverYaw, pitch, 0);
        }
        if(spinYaw.get() && spinPitch.get()){
            double yaw = Rotations.serverYaw;
            double pitch = Rotations.serverPitch;

            yaw += dpty.get();
            yaw %= 360;

            pitch += dptp.get();

            Rotations.rotate(yaw, pitch, 0);
        }
    }
}
