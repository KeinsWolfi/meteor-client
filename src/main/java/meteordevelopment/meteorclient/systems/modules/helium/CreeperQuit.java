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
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class CreeperQuit extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> disconnectThreshold = sgGeneral.add(new IntSetting.Builder()
            .name("disconnect-threshold")
            .description("The ticks a creeper needs to be fused before disconnecting.")
            .defaultValue(10)
            .min(1)
            .sliderMin(1)
            .sliderMax(15)
            .build()
    );

    private final Setting<Boolean> autoToggle = sgGeneral.add(new BoolSetting.Builder()
            .name("auto-toggle")
            .description("Automatically toggles off after a disconnect.")
            .defaultValue(false)
            .build()
    );

    int timer = 0;

    public CreeperQuit() {
        super(Categories.Helium, "CreeperQuit", "Creeper quit.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event){
        List<Entity> creepers = new ArrayList<>();
        for(Entity entity : mc.world.getEntities()){
            if(entity.getType().equals(EntityType.CREEPER)){
                creepers.add(entity);
            }
        }
        for (Entity creeper : creepers){
            CreeperEntity creeperEntity = (CreeperEntity) creeper;
            if(creeperEntity.getFuseSpeed() >= 1){
                timer++;
            }
        }
        if(timer > disconnectThreshold.get()){
            timer = 0;
            if(autoToggle.get()) toggle();
            mc.getNetworkHandler().onDisconnect(new DisconnectS2CPacket(Text.literal("Creeper detected.")));
        }
        if(creepers.isEmpty()){
            timer = 0;
        }
    }
}
