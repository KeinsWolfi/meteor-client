/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.helium;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

public class ElytraHighjump extends Module {

    private boolean takeOff = false;

    public SettingGroup sgGeneral = settings.getDefaultGroup();

    public Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("Mode")
        .description("Sus")
        .defaultValue(Mode.Vanilla)
        .build()
    );

    public Setting<Double> multilpier = sgGeneral.add(new DoubleSetting.Builder()
        .name("Multiplier")
        .description("Multiplier of the jump")
        .defaultValue(1.5)
        .min(1)
        .sliderMax(10)
        .visible(() -> mode.get() == Mode.Boost)
        .build()
    );

    public ElytraHighjump() {
        super(Categories.Helium, "Elytra Highjump", "Allows you to jump higher while using an elytra.");
    }

    @EventHandler
    public void onTick(TickEvent.Pre event){
        if (mc.player == null) return;
        if (mc.player.isFallFlying()) {
            toggle();
            return;
        }
        if(takeOff){
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            mc.player.startFallFlying();
            takeOff = false;
            toggle();
            return;
        }
        takeOff = true;
        if(mode.get().equals(Mode.Vanilla)){
            mc.player.jump();
        } else if (mode.get().equals(Mode.Boost)){
            mc.player.setVelocity(mc.player.getVelocity().x, (0.42F * multilpier.get()), mc.player.getVelocity().z);
        }

    }

    public enum Mode {
        Vanilla,
        Boost
    }
}
