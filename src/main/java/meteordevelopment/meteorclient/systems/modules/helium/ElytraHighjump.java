/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.helium;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

public class ElytraHighjump extends Module {

    private boolean takeOff = false;

    public SettingGroup sgGeneral = settings.getDefaultGroup();

    public SettingGroup sgAutoTakeoff = settings.createGroup("Auto Takeoff");

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

    public Setting<Boolean> autoTakeoff = sgAutoTakeoff.add(new BoolSetting.Builder()
        .name("Auto Takeoff")
        .description("Automatically takes off when you jump.")
        .defaultValue(true)
        .build()
    );

    public Setting<TakeoffMode> takeoffMode = sgAutoTakeoff.add(new EnumSetting.Builder<TakeoffMode>()
        .name("Takeoff Mode")
        .description("The mode of takeoff.")
        .defaultValue(TakeoffMode.Vertical)
        .visible(() -> autoTakeoff.get())
        .build()
    );

    public ElytraHighjump() {
        super(Categories.Helium, "Elytra Highjump", "Allows you to jump higher while using an elytra.");
    }

    @EventHandler
    public void onTick(TickEvent.Pre event){
        if (mc.player == null) return;
        if (mc.player.isGliding()) {
            toggle();
            return;
        }
        if(takeOff){
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            mc.player.startGliding();
            takeOff = false;
            if(autoTakeoff.get()){
                FindItemResult item;
                switch (takeoffMode.get()){
                    case Vertical:
                        float oldPitch = mc.player.getPitch();
                        mc.player.setPitch(-90);
                        item = InvUtils.findInHotbar(Items.FIREWORK_ROCKET);
                        InvUtils.swap(item.slot(), false);
                        mc.interactionManager.interactItem(mc.player, mc.player.getActiveHand());
                        break;
                    case viewingDirection:
                        item = InvUtils.findInHotbar(Items.FIREWORK_ROCKET);
                        InvUtils.swap(item.slot(), false);
                        mc.interactionManager.interactItem(mc.player, mc.player.getActiveHand());
                        break;
                }
            }
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

    public enum TakeoffMode {
        Vertical,
        viewingDirection
    }
}
